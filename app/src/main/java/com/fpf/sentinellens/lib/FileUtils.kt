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

fun saveImageLocally(context: Context, bitmap: Bitmap, filePath: String): Boolean {
    return try {
        val outFile = File(context.filesDir, filePath)

        outFile.parentFile?.let { parentDir ->
            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }
        }
        outFile.outputStream().use { outStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        }
        true
    } catch (e: Exception) {
        Log.e("saveImageLocally", "Error saving image: $e")
        false
    }
}


fun loadLocalImage(context: Context, filePath: String): Bitmap? {
    return try {
        val file = File(context.filesDir, filePath)

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

fun deleteLocalFile(context: Context, fileName: String): Boolean {
    val file = File(context.filesDir, fileName)
    return if (file.exists()) {
        file.delete()
    } else {
        false
    }
}

