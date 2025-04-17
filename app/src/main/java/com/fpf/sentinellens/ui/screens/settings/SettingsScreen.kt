package com.fpf.sentinellens.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpf.sentinellens.ui.components.SettingsCard
import androidx.core.net.toUri
import com.fpf.sentinellens.ui.components.SettingsSelect
import com.fpf.sentinellens.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val appSettings by viewModel.appSettings.collectAsState(AppSettings())
    val scrollState = rememberScrollState()
    val context = LocalContext.current // Access the current context
    val sourceCodeUrl = stringResource(R.string.source_code_url)
    val versionName: String? = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName
    } catch (e: Exception) {
        null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = stringResource(id = R.string.facial_recognition_settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                SettingsSelect(
                    label = stringResource(id = R.string.setting_surveillance_mode),
                    selectedOption = modeOptions[appSettings.mode]!!,
                    options = modeOptions.values.toList(),
                    onOptionSelected = { option ->
                        val selected = modeOptions.entries
                            .find { it.value == option }
                            ?.key ?: modeOptions.keys.first()
                        viewModel.updateMode(selected)
                    },
                )

                SettingsCard(
                    text = stringResource(id = R.string.setting_similarity_threshold),
                    onClick = { onNavigate("settingsDetail/threshold") }
                )

                Text(
                    text = stringResource(id = R.string.video_settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                SettingsSelect(
                    label = stringResource(id = R.string.setting_camera_type),
                    selectedOption = cameraOptions[appSettings.cameraType]!!,
                    options = cameraOptions.values.toList(),
                    onOptionSelected = { option ->
                        val selected = cameraOptions.entries
                            .find { it.value == option }
                            ?.key ?: cameraOptions.keys.first()
                        viewModel.updateCameraType(selected)
                    },
//                    description = stringResource(id = R.string.setting_frame_interval_description)
                )

                SettingsSelect(
                    label = stringResource(id = R.string.setting_frame_interval),
                    selectedOption = frameIntervalOptions[appSettings.frameInterval]!!,
                    options = frameIntervalOptions.values.toList(),
                    onOptionSelected = { option ->
                        val selected = frameIntervalOptions.entries
                            .find { it.value == option }
                            ?.key ?: frameIntervalOptions.keys.first()
                        viewModel.updateFrameInterval(selected)
                    },
                    description = stringResource(id = R.string.setting_frame_interval_description)
                )

                SettingsSelect(
                    label = stringResource(id = R.string.setting_max_duration),
                    selectedOption = maxDurationOptions[appSettings.maxDuration]!!,
                    options = maxDurationOptions.values.toList(),
                    onOptionSelected = { option ->
                        val selected = maxDurationOptions.entries
                            .find { it.value == option }
                            ?.key
                        viewModel.updateMaxDuration(selected)
                    },
                    description = stringResource(id = R.string.setting_max_duration_description)
                )

                Text(
                    text = stringResource(id = R.string.alert_settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                SettingsSelect(
                    label = stringResource(id = R.string.setting_alert_frequency),
                    selectedOption = alertFrequencyOptions[appSettings.alertFrequency]!!,
                    options = alertFrequencyOptions.values.toList(),
                    onOptionSelected = { option ->
                        val selected = alertFrequencyOptions.entries
                            .find { it.value == option }
                            ?.key ?: alertFrequencyOptions.keys.first()
                        viewModel.updateAlertFrequency(selected)
                    },
                    description = stringResource(id = R.string.setting_alert_frequency_description)
                )

                SettingsCard(
                    text = stringResource(id = R.string.setting_telegram_config),
                    onClick = { onNavigate("settingsDetail/telegram") },
                    description = stringResource(id = R.string.setting_telegram_config_description)
                )

                Text(
                    text = stringResource(id = R.string.other_settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                SettingsCard(
                    text = stringResource(id = R.string.title_donate),
                    onClick = { onNavigate("donate") }
                )
                SettingsCard(
                    text = stringResource(id = R.string.setting_source_code),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, sourceCodeUrl.toUri())
                        context.startActivity(intent)
                    },
                )
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon),
                    contentDescription = "Logo",
                    modifier = Modifier.size(132.dp)
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                versionName?.let {
                    Text(
                        text = "Version $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = stringResource(R.string.copyright),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
