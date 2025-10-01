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
import androidx.core.net.toFile
import com.fpf.smartscansdk.core.utils.getScaledDimensions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
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

fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    return ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
}


const val DEFAULT_IMAGE_DISPLAY_SIZE = 1024

/**
 * A simple LRU Cache to hold Bitmaps to avoid decoding them multiple times.
 */
object BitmapCache {
    private val cache: LruCache<Uri, Bitmap> = object : LruCache<Uri, Bitmap>(calculateMemoryCacheSize()) {
        override fun sizeOf(key: Uri, value: Bitmap): Int {
            return value.byteCount / 1024 // in KB
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

    fun get(uri: Uri): Bitmap? = cache.get(uri)
    fun put(uri: Uri, bitmap: Bitmap): Bitmap? = cache.put(uri, bitmap)
}

suspend fun loadBitmapFromUri(context: Context, uri: Uri, maxSize: Int = DEFAULT_IMAGE_DISPLAY_SIZE): Bitmap? = withContext(Dispatchers.IO) {
    BitmapCache.get(uri) ?: try {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val (w, h) = getScaledDimensions(imgWith  = info.size.width, imgHeight = info.size.height, maxSize)
            decoder.setTargetSize(w, h)
        }
        BitmapCache.put(uri, bitmap)
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun loadBitmapFromLocalUri(uri: Uri, maxSize: Int = DEFAULT_IMAGE_DISPLAY_SIZE): Bitmap? {
    return withContext(Dispatchers.IO) {
        BitmapCache.get(uri) ?: try {
            val bitmap = loadLocalImage(uri.toFile(), maxSize)
            if(bitmap != null){
                BitmapCache.put(uri, bitmap)
            }
            bitmap
        } catch (e: Exception) {
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