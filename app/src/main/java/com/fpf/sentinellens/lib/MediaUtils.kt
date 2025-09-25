package com.fpf.sentinellens.lib

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream


fun cameraImageToBitmap(image: Image, rotationDegrees: Int = 0): Bitmap {
    // Get YUV planes from the image
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    // Allocate a byte array in NV21 format and fill it
    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    // Convert NV21 byte array to a YuvImage
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)

    // Compress the YuvImage to JPEG
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
    val imageBytes = out.toByteArray()

    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    // Apply a matrix transformation if rotation is necessary
    return if (rotationDegrees != 0) {
        val matrix = Matrix().apply {
            postRotate(rotationDegrees.toFloat())
        }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

/**
 * A simple LRU Cache to hold Bitmaps to avoid decoding them multiple times.
 */
object BitmapCache {
    private val cache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(calculateMemoryCacheSize()) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    private fun calculateMemoryCacheSize(): Int {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt() // in KB
        val calculatedCacheSize = maxMemory / 8
        val maxAllowedCacheSize = 50 * 1024

        return if (calculatedCacheSize > maxAllowedCacheSize) {
            maxAllowedCacheSize
        } else {
            calculatedCacheSize
        }
    }

    fun get(key: String): Bitmap? = cache.get(key)
    fun put(key: String, bitmap: Bitmap): Bitmap? = cache.put(key, bitmap)
}

fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    return ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
}

suspend fun loadBitmapFromUri(
    context: Context, uri: Uri, targetWidth: Int? = null, targetHeight: Int? = null): Bitmap? {
    return withContext(Dispatchers.IO) {
        BitmapCache.get(uri.toString()) ?: try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                if (targetWidth != null && targetHeight != null) {
                    decoder.setTargetSize(targetWidth, targetHeight)
                }
            }
            BitmapCache.put(uri.toString(), bitmap)
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}


suspend fun loadBitmapFromLocalPath(
    context: Context,
    path: String,
): Bitmap? {
    return withContext(Dispatchers.IO) {
        BitmapCache.get(path) ?: try {
            val bitmap = loadLocalImage(context, path)
            if(bitmap != null){
                BitmapCache.put(path, bitmap)
            }
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}


suspend fun insertVideoIntoMediaStore(videoFile: File, context: Context): Uri?=withContext(
    Dispatchers.IO) {
    val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.name)
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
    }

    val resolver = context.contentResolver
    val collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    val videoUri = resolver.insert(collectionUri, values)
    if (videoUri == null) {
        Log.e("VideoInsertionError", "Failed to create new MediaStore record.")
        return@withContext null
    }

    try {
        resolver.openOutputStream(videoUri)?.use { outputStream ->
            copyFileToStream(videoFile, outputStream)
            outputStream.flush()
        }
    } catch (e: Exception) {
        Log.e("VideoInsertionError", "Error copying file to MediaStore", e)
        resolver.delete(videoUri, null, null)
        return@withContext  null
    }

    videoUri
}


private fun copyFileToStream(sourceFile: File, outputStream: OutputStream){
    FileInputStream(sourceFile).use { inputStream ->
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
    }
}