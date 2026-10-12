package com.fpf.sentinellens.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.fpf.sentinellens.R
import com.fpf.sentinellens.navigation.Routes
import com.fpf.sentinellens.navigation.SettingsRoutes
import com.fpf.sentinellens.navigation.TopBarState
import com.fpf.sentinellens.settings.alertFrequencyOptions
import com.fpf.sentinellens.settings.cameraOptions
import com.fpf.sentinellens.settings.frameIntervalOptions
import com.fpf.sentinellens.settings.getLabel
import com.fpf.sentinellens.settings.maxDurationOptions
import com.fpf.sentinellens.ui.components.pickers.OptionPicker
import com.fpf.sentinellens.ui.components.settings.SettingSection
import com.fpf.smartscan.ui.action.SettingActionConfig
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onTopBarChange: (TopBarState) -> Unit,
    onNavigate: (String) -> Unit
) {
    val appSettings by viewModel.appSettings.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current // Access the current context
    val sourceCodeUrl = stringResource(R.string.source_code_url)
    val versionName: String? = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName
    } catch (e: Exception) {
        null
    }
    val title = stringResource(R.string.title_help)

    // Actions
    var isSelectingCameraType by remember { mutableStateOf(false) }
    var isSelectingFrameInterval by remember { mutableStateOf(false) }
    var isSelectingMaxDuration by remember { mutableStateOf(false) }
    var isSelectingFrequency by remember { mutableStateOf(false) }

    val surveillanceSettingActions: List<SettingActionConfig> = listOf(
        SettingActionConfig.Button(
            label = stringResource(id = R.string.setting_similarity_threshold),
            onClick = { onNavigate(Routes.settingsDetail(SettingsRoutes.THRESHOLD)) },
            description = stringResource(R.string.setting_similarity_threshold_description)
        ),
    )

    val videoSettingActions: List<SettingActionConfig> = listOf(
        SettingActionConfig.Button(
            label = stringResource(id = R.string.setting_camera_type),
            onClick = { isSelectingCameraType = true},
            description = getLabel(appSettings.cameraType, cameraOptions)

        ),
        SettingActionConfig.Button(
            label = stringResource(id = R.string.setting_frame_interval),
            onClick = { isSelectingFrameInterval = true},
            description = getLabel(appSettings.frameInterval, frameIntervalOptions)
        ),
        SettingActionConfig.Button(
            label = stringResource(id = R.string.setting_max_duration),
            onClick = { isSelectingMaxDuration = true},
            description = getLabel(appSettings.maxDuration, maxDurationOptions)
        ),
    )

    val alertSettingActions: List<SettingActionConfig> = listOf(
        SettingActionConfig.Button(
            label = stringResource(id = R.string.setting_alert_frequency),
            onClick = { isSelectingFrequency = true},
            description = getLabel(appSettings.alertFrequency, alertFrequencyOptions)
        ),
        SettingActionConfig.Button(
            label = stringResource(id = R.string.setting_telegram_config),
            onClick = { onNavigate(Routes.settingsDetail(SettingsRoutes.TELEGRAM)) },
            description = stringResource(id = R.string.setting_telegram_config_description)
        ),
    )

    val otherSettingActions: List<SettingActionConfig> = listOf(
        SettingActionConfig.Button(
            label = stringResource(id = R.string.title_donate),
            onClick = { onNavigate(Routes.DONATE) }
        ),
        SettingActionConfig.Button(
            label = stringResource(id = R.string.title_help),
            onClick = { onNavigate(Routes.HELP) }
        ),
        SettingActionConfig.Button(
            label = stringResource(id = R.string.setting_source_code),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, sourceCodeUrl.toUri())
                context.startActivity(intent)
            },
        ),
    )

    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                title = title,
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingSection(
                    stringResource(id = R.string.facial_recognition_settings),
                    settingActionConfigs = surveillanceSettingActions
                )
                SettingSection(
                    stringResource(id = R.string.video_settings),
                    settingActionConfigs = videoSettingActions
                )
                SettingSection(
                    stringResource(id = R.string.alert_settings),
                    settingActionConfigs = alertSettingActions
                )
                SettingSection(
                    stringResource(id = R.string.other_settings),
                    settingActionConfigs = otherSettingActions
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
    OptionPicker(
        isVisible = isSelectingCameraType,
        title = stringResource(id = R.string.setting_camera_type),
        selectedOption = appSettings.cameraType,
        options = cameraOptions,
        onClose = { isSelectingCameraType = false },
        onSelect = {
            viewModel.updateCameraType(it)
            isSelectingCameraType = false
        }
    )
    OptionPicker(
        isVisible = isSelectingMaxDuration,
        title = stringResource(id = R.string.setting_max_duration),
        selectedOption = appSettings.maxDuration,
        options = maxDurationOptions,
        onClose = { isSelectingMaxDuration = false },
        onSelect = {
            viewModel.updateMaxDuration(it)
            isSelectingMaxDuration = false
        }
    )

    OptionPicker(
        isVisible = isSelectingFrequency,
        title = stringResource(id = R.string.setting_alert_frequency),
        selectedOption = appSettings.alertFrequency,
        options = alertFrequencyOptions,
        onClose = { isSelectingFrequency = false },
        onSelect = {
            viewModel.updateAlertFrequency(it)
            isSelectingFrequency = false
        }
    )

    OptionPicker(
        isVisible = isSelectingFrameInterval,
        title = stringResource(id = R.string.setting_frame_interval),
        selectedOption = appSettings.frameInterval,
        options = frameIntervalOptions,
        onClose = { isSelectingFrameInterval = false },
        onSelect = {
            viewModel.updateFrameInterval(it)
            isSelectingFrameInterval = false
        }
    )
}
