package com.fpf.sentinellens.lib

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream


fun insertVideoIntoMediaStore(videoFile: File, context: Context): Uri? {
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
        return null
    }

    try {
        resolver.openOutputStream(videoUri)?.use { outputStream ->
            copyFileToStream(videoFile, outputStream)
            outputStream.flush()
        }
    } catch (e: Exception) {
        Log.e("VideoInsertionError", "Error copying file to MediaStore", e)
        resolver.delete(videoUri, null, null)
        return null
    }

    return videoUri
}


private fun copyFileToStream(sourceFile: File, outputStream: OutputStream) {
    FileInputStream(sourceFile).use { inputStream ->
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
    }
}