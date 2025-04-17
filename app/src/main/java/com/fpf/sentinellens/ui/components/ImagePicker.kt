package com.fpf.sentinellens.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.fpf.sentinellens.lib.loadBitmapFromLocalPath
import com.fpf.sentinellens.lib.loadBitmapFromUri

@Composable
fun MediaStoreImage(
    modifier: Modifier = Modifier,
    uri: Uri? = null,
    path: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    targetWidth: Int? = null,
    targetHeight: Int? = null
) {
    require((uri != null) xor (path != null)) { "Exactly one of uri or path must be provided" }

    val context = LocalContext.current
    val cacheKey = path ?: uri.toString()

    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = cacheKey) {
        value = when {
            path != null -> loadBitmapFromLocalPath(context, path)
            uri != null  -> loadBitmapFromUri(context, uri, targetWidth, targetHeight)
            else -> null // should be unreachable due to require() above
        }
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

@Composable
fun ImagePicker(
    imageUris: List<String>,
    onImageUrisChanged: (List<String>) -> Unit,
    deleteImage: (String) -> Unit,
    description: String? = null
) {
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        val newUris = uris.mapNotNull { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                uri.toString()
            } catch (e: SecurityException) {
                null
            }
        }.filterNot { imageUris.contains(it) }

        if (newUris.isNotEmpty()) {
            onImageUrisChanged(imageUris + newUris)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.8f).padding(bottom = 16.dp),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(imageUris.isEmpty()){
                Text(
                    text = "No images selected.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.5f),
                )

            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        imagePickerLauncher.launch(arrayOf("image/*"))
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add image")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Images")
                }
            }

        }

        if (imageUris.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(Color.Transparent)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp)  // fixed height required to bound the grid's maximum height.
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(imageUris) { uri ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f),
                                shape = MaterialTheme.shapes.small,
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box {
                                    MediaStoreImage(
                                        uri = uri.toUri(),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        modifier = Modifier
                                            .offset(x=8.dp, y = (-8).dp)
                                            .align(Alignment.TopEnd),

                                        onClick = {
                                            onImageUrisChanged(imageUris - uri)
                                            deleteImage(uri)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.RemoveCircle,
                                            contentDescription = "Remove image",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
