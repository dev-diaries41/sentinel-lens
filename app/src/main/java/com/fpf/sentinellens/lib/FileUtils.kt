package com.fpf.sentinellens.lib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.fpf.smartscansdk.core.utils.getScaledDimensions
import java.io.File
import androidx.core.graphics.scale


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


suspend fun loadLocalImage(file: File, maxSize: Int = DEFAULT_IMAGE_DISPLAY_SIZE): Bitmap? {
    return try {
        if (!file.exists()) {
            Log.e("loadLocalImage", "Image file does not exist: ${file.absolutePath}")
            null
        } else {
            val original = BitmapFactory.decodeFile(file.absolutePath) ?: return null
            val (w, h) = getScaledDimensions(imgWith = original.width, imgHeight = original.height, maxSize = maxSize)
            original.scale(w, h)
        }
    } catch (e: Exception) {
        Log.e("loadLocalImage", "Error loading image: $e")
        null
    }
}


