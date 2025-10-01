package com.fpf.sentinellens.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.fpf.sentinellens.lib.DEFAULT_IMAGE_DISPLAY_SIZE
import com.fpf.sentinellens.lib.loadBitmapFromLocalUri
import com.fpf.sentinellens.lib.loadBitmapFromUri

@Composable
fun ImageDisplay(
    modifier: Modifier = Modifier,
    uri: Uri,
    contentScale: ContentScale = ContentScale.Crop,
    maxSize: Int = DEFAULT_IMAGE_DISPLAY_SIZE
) {
    val context = LocalContext.current

    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = uri) {
        value = if(uri.scheme == "file") loadBitmapFromLocalUri(uri, maxSize) else loadBitmapFromUri(context, uri, maxSize)
    }

    val bitmap = bitmapState.value
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(Color.Gray))
    }
}