package com.fpf.sentinellens.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpf.sentinellens.R
import com.fpf.sentinellens.ui.components.CustomSlider
import com.fpf.sentinellens.ui.components.SettingsTextInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDetailScreen(
    type: String,
    viewModel: SettingsViewModel,
) {
    val appSettings by viewModel.appSettings.collectAsState()
    val scrollState = rememberScrollState()


    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            when (type) {
                "threshold" -> {
                    CustomSlider(
                        minValue = 0.5f,
                        maxValue = 0.8f,
                        initialValue = appSettings.similarityThreshold,
                        onValueChange = { value ->
                            viewModel.updateSimilarityThreshold(value)
                        },
                        description = stringResource(R.string.setting_similarity_threshold_description)
                    )
                }

                "telegram" -> {
                    SettingsTextInput(
                        label = "Channel ID",
                        value = appSettings.telegramChannelId,
                        onValueChange = { viewModel.updateTelegramChannelId(it) } ,
                        placeholder = { Text(text = "Enter Channel Id e.g @myChannel") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsTextInput(
                        label = "Bot token",
                        value = appSettings.telegramBotToken,
                        onValueChange = { viewModel.updateTelegramBotToken(it) },
                        placeholder = { Text(text = "Enter Bot Token") }
                    )

                }
                else -> {
                    Text("Unknown setting type")
                }
            }
        }
    }
}