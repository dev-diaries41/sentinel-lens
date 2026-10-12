package com.fpf.sentinellens.ui.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.fpf.sentinellens.R
import com.fpf.sentinellens.navigation.TopBarState
import com.fpf.sentinellens.ui.components.logs.DetectionLogCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetectionLogScreen(
    viewModel: DetectionLogViewModel = koinViewModel(),
    onTopBarChange: (TopBarState) -> Unit,
    onBack: () -> Unit,
) {
    val items by viewModel.log.collectAsState(emptyList())
    val isClearLogsAlertVisible by viewModel.isClearLogsAlertVisible.collectAsState()
    val title = stringResource(R.string.title_log)

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
    if ( isClearLogsAlertVisible) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Clear logs") },
            text = { Text(stringResource(R.string.clear_logs_alert_description)) },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.toggleAlert()
                }) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.toggleAlert()
                    viewModel.clearLogs()
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (items.isEmpty()) {
        EmptyLogScreen()
    } else {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Button (
                        modifier = Modifier.padding(bottom = 8.dp),
                        onClick = {viewModel.toggleAlert()}
                    ) {
                        Text(text = "Clear logs")
                    }
                }
                LazyColumn{
                    items(
                        items = items,
                        key = { it.id }
                    ) { item ->
                        DetectionLogCard(data = item)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLogScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Log icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp)
            )
            Text(
                text = stringResource(R.string.no_logs_history),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.no_logs_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(0.8f).padding(vertical = 8.dp)
            )
        }
    }
}
