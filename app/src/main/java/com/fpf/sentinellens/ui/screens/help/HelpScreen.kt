package com.fpf.sentinellens.ui.screens.help


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpf.sentinellens.R


@Composable
fun HelpScreen() {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            Text(
                text = stringResource(id = R.string.help_title_watchlist),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            BulletPoint(stringResource(id = R.string.help_description_watchlist_1))
            BulletPoint(stringResource(id = R.string.help_description_watchlist_2))
            BulletPoint(stringResource(id = R.string.help_description_watchlist_3))

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.help_title_telegram),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            BulletPoint(stringResource(id = R.string.help_description_telegram_1))
            BulletPoint(stringResource(id = R.string.help_description_telegram_2))
            BulletPoint(stringResource(id = R.string.help_description_telegram_3))
        }
    }
}


@Composable
fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(bottom = 6.dp)) {
        Text(
            text = "\u2022", // Bullet character
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}