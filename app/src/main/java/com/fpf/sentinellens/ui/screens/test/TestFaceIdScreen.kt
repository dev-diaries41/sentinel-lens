package com.fpf.sentinellens.ui.screens.test

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpf.sentinellens.R
import com.fpf.sentinellens.navigation.TopBarState
import com.fpf.sentinellens.settings.AppSettings
import com.fpf.sentinellens.ui.components.ImageDisplay
import com.fpf.sentinellens.ui.components.common.LoadingIndicator
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("DefaultLocale")
@Composable
fun TestFaceIdScreen(
    viewModel: TestFaceIdViewModel = koinViewModel(),
    appSettings: StateFlow<AppSettings>,
    onTopBarChange: (TopBarState) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val hasAnyFaces by viewModel.hasAnyFaces.collectAsState()
    val selectedImage by viewModel.selectedImage.collectAsState()
    val blacklistResult by viewModel.blacklistResult.collectAsState()
    val whitelistResult by viewModel.whitelistResult.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val settings by appSettings.collectAsState()
    val scrollState = rememberScrollState()
    val selectedImageBoundedBoxes by viewModel.selectedImageBoundedBoxes.collectAsState()
    val title = stringResource(R.string.title_face_id)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.updateImageUri(it) }
        }
    )

    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), shape = MaterialTheme.shapes.medium)
                    .clip(shape = MaterialTheme.shapes.medium)
                    .clickable { imagePickerLauncher.launch("image/*") }
//
            ) {
                if (selectedImage != null) {
                    if(selectedImageBoundedBoxes != null){
                        ImageDisplay(
                            bitmap = selectedImageBoundedBoxes!!,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    }else{
                        ImageDisplay(
                            uri = selectedImage!!,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    IconButton(
                        onClick = { viewModel.reset() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                            .size(16.dp)
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove Image",
                            tint = MaterialTheme.colorScheme.inversePrimary
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Person icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize(0.8f)
                        )
                        Text(
                            text= "Upload image",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            }

            if (hasAnyFaces == false) {
                Text(
                    text=stringResource(R.string.error_watchlist_not_setup_message),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    )
            }

            if (selectedImage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    enabled = !isLoading && hasAnyFaces == true,
                    onClick = { viewModel.run() },
                ) {
                    Text("Identify face")

                    LoadingIndicator(
                        isVisible = isLoading,
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            blacklistResult?.let{ result ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Is blacklist match: ${result.isMatch}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Similarity: ${String.format("%.2f", result.similarity * 100)}%",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (result.isMatch) {
                    Text("Name: ${result.name}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            whitelistResult?.let{ result ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Is whitelist match: ${result.isMatch}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Similarity: ${String.format("%.2f", result.similarity)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (result.isMatch) {
                    Text("Name: ${result.name}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
