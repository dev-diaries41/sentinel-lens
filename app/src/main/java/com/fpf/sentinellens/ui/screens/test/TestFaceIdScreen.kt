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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpf.sentinellens.R
import com.fpf.sentinellens.ui.components.ImageUploader
import com.fpf.sentinellens.ui.screens.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@SuppressLint("DefaultLocale")
@Composable
fun TestFaceIdScreen(viewModel: TestFaceIdViewModel = koinViewModel(), settingsViewModel: SettingsViewModel) {
    val hasAnyFaces by viewModel.hasAnyFaces.collectAsState()
    val selectedImage by viewModel.selectedImage.collectAsState()
    val blacklistResult by viewModel.blacklistSimilarity.collectAsState()
    val whitelistResult by viewModel.whitelistSimilarity.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val settings by settingsViewModel.appSettings.collectAsState()
    val scrollState = rememberScrollState()
    val withBoxes by viewModel.withBoxes.collectAsState()

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
                withBoxes = withBoxes,
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
                visible = isLoading,
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
                    enabled = !isLoading && hasAnyFaces == true,
                    onClick = {
                        viewModel.inference()
                    },
                ) {
                    Text("Identify face")
                }
            }

            blacklistResult?.let{ result ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Is blacklist match: ${result.isMatch}",
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
