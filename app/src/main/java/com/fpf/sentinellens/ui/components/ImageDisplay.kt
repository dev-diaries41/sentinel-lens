package com.fpf.sentinellens.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade

@Composable
fun ImageDisplay(
    uri: Uri,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    size:Int? = 512,
    onSizeChanged: ((Int, Int) -> Unit)? = null,
    onError: ((error:  AsyncImagePainter.State.Error)-> Unit)? = null
    ) {
    val context = LocalContext.current

    val request = remember(uri, size) {
        val builder = ImageRequest.Builder(context)
            .allowHardware(true)
            .crossfade(true)
            .data(uri)

        if (size != null) {
            builder.size(size)
        }

        builder.build()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center) {

        AsyncImage(
            model = request,
            contentDescription = "Displayed image",
            contentScale = contentScale,
            modifier = Modifier.matchParentSize(),
            onError = { onError?.invoke(it) },
            onSuccess = {
                onSizeChanged?.invoke(
                    it.result.image.width,
                    it.result.image.height
                )
            }
        )
    }
}
