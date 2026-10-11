package com.fpf.sentinellens.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.fpf.smartscansdk.core.media.getScaledDimensions
import java.io.File
import androidx.core.graphics.scale


 fun saveImageLocally(bitmap: Bitmap, file: File) {
     file.parentFile?.let { parentDir ->
         if (!parentDir.exists()) {
             parentDir.mkdirs()
         }
     }
     file.outputStream().use { outStream ->
         bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
     }
}


suspend fun loadLocalImage(file: File, maxSize: Int = DEFAULT_IMAGE_DISPLAY_SIZE): Bitmap? {
    return try {
        if (!file.exists()) {
            Log.e("loadLocalImage", "Image file does not exist: ${file.absolutePath}")
            null
        } else {
            val original = BitmapFactory.decodeFile(file.absolutePath) ?: return null
            val (w, h) = getScaledDimensions(width = original.width, height = original.height, maxSize = maxSize)
            original.scale(w, h)
        }
    } catch (e: Exception) {
        Log.e("loadLocalImage", "Error loading image: $e")
        null
    }
}


