package com.fpf.sentinellens.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
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
fun insertVideoIntoMediaStore(videoFile: File, context: Context): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, videoFile.name)
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
    }

    val resolver = context.contentResolver
    val collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    val videoUri = resolver.insert(collectionUri, values) ?: error("Error creating new MediaStore record.")

    resolver.openOutputStream(videoUri)?.use { outputStream ->
        copyFileToStream(videoFile, outputStream)
        outputStream.flush()
    }
    return videoUri
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