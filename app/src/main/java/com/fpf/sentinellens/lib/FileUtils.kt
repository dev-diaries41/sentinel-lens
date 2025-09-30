package com.fpf.sentinellens.lib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.io.File


fun getDirectoryName(context: Context, uri: Uri): String {
    val documentDir = DocumentFile.fromTreeUri(context, uri)
    return documentDir?.name.toString()
}

suspend fun saveImageLocally(bitmap: Bitmap, file: File): Boolean {
    return try {

        file.parentFile?.let { parentDir ->
            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }
        }
        file.outputStream().use { outStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        }
        true
    } catch (e: Exception) {
        Log.e("saveImageLocally", "Error saving image: $e")
        false
    }
}


suspend fun loadLocalImage(file: File): Bitmap? {
    return try {
        if (!file.exists()) {
            Log.e("loadLocalImage", "Image file does not exist: ${file.absolutePath}")
            null
        } else {
            BitmapFactory.decodeFile(file.absolutePath)
        }
    } catch (e: Exception) {
        Log.e("loadLocalImage", "Error loading image: $e")
        null
    }
}


