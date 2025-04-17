package com.fpf.sentinellens.ui.screens.test

import androidx.compose.ui.graphics.Color
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpf.sentinellens.R
import com.fpf.sentinellens.ui.components.ImageUploader
import com.fpf.sentinellens.ui.screens.settings.SettingsViewModel

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestFaceIdScreen(viewModel: TestFaceIdViewModel = viewModel(), settingsViewModel: SettingsViewModel) {
    val hasAnyFaces by viewModel.hasAnyFaces.observeAsState()
    val selectedImage by viewModel.selectedImage.observeAsState()
    val blacklistResult by viewModel.blacklistSimilarity.observeAsState()
    val whitelistResult by viewModel.whitelistSimilarity.observeAsState()
    val error by viewModel.error.observeAsState()
    val isLoading by viewModel.loading.observeAsState()
    val settings by settingsViewModel.appSettings.collectAsState()
    val scrollState = rememberScrollState()

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

            ImageUploader(
                imageUri = selectedImage,
                onImageSelected = { images ->
                    viewModel.updateImageUri(images)
                    if (selectedImage == null) {
                        viewModel.clearInferenceResult()
                    }
                }
            )

            if (hasAnyFaces == false) {
                Text(
                    text=stringResource(R.string.error_watchlist_not_setup_message),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    )
            }

            AnimatedVisibility (
                visible = isLoading == true,
                enter = fadeIn(animationSpec = tween(durationMillis = 500)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(durationMillis = 500)) + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.White,
                        strokeWidth = 4.dp
                    )
                }
            }

            // Add check in enabled
            if (selectedImage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    enabled = isLoading == false && hasAnyFaces == true,
                    onClick = {
                        viewModel.inference()
                    },
                ) {
                    Text("Identify face")
                }
            }

            if (blacklistResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val (similarity, name) = blacklistResult!!
                val isMatch = similarity >= settings.similarityThreshold
                Text(
                    "Is blacklist match: $isMatch",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Similarity: ${String.format("%.2f", similarity)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (isMatch) {
                    Text(
                        "Name: $name",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (whitelistResult != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val (similarity, name) = whitelistResult!!
                val isMatch = similarity >= settings.similarityThreshold
                Text(
                    "Is whitelist match: $isMatch",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Similarity: ${String.format("%.2f", similarity)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (isMatch) {
                    Text(
                        "Name: $name",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }


            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text=error!!,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
