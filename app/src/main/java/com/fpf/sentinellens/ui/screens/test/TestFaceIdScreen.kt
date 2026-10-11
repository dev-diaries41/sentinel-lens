package com.fpf.sentinellens.ui.screens.test

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpf.sentinellens.R
import com.fpf.sentinellens.navigation.TopBarState
import com.fpf.sentinellens.settings.AppSettings
import com.fpf.sentinellens.ui.components.ImageUploader
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
    val hasAnyFaces by viewModel.hasAnyFaces.collectAsState()
    val selectedImage by viewModel.selectedImage.collectAsState()
    val blacklistResult by viewModel.blacklistSimilarity.collectAsState()
    val whitelistResult by viewModel.whitelistSimilarity.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val settings by appSettings.collectAsState()
    val scrollState = rememberScrollState()
    val withBoxes by viewModel.withBoxes.collectAsState()
    val title = stringResource(R.string.title_face_id)

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

            if (selectedImage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    enabled = !isLoading && hasAnyFaces == true,
                    onClick = {
                        viewModel.inference()
                    },
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
