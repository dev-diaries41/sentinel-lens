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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.fpf.sentinellens.R
import com.fpf.sentinellens.data.logs.DetectionLogEntity
import com.fpf.sentinellens.lib.toDateString

@Composable
fun DetectionLogScreen(viewModel: DetectionLogViewModel = viewModel()) {
    val items by viewModel.log.collectAsState(emptyList())
    val isClearLogsAlertVisible by viewModel.isClearLogsAlertVisible.collectAsState()

    if ( isClearLogsAlertVisible) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Clear logs") },
            text = { Text("Press 'OK' to clear all detection logs.") },
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
                        DetectionLogItemCard(data = item)
                    }
                }
            }
        }
    }
}

@Composable
fun DetectionLogItemCard(data: DetectionLogEntity) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "Log icon",
                modifier = Modifier.padding(end = 16.dp).size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Detection Log",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(text = toDateString(data.date),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.alpha(0.8f).padding(bottom = 4.dp)
                    )
                }

                Text(text = "Person detected: ${data.name ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.8f).padding(bottom = 4.dp)
                )
                Text(text = "Similarity: ${"%.2f".format(data.similarity)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.8f).padding(bottom = 4.dp)
                )
                Text(text = "Detection type: ${data.type}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.8f).padding(bottom = 4.dp)
                )
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
