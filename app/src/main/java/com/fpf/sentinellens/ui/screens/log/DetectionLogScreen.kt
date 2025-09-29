package com.fpf.sentinellens.ui.screens.log

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.fpf.sentinellens.R
import com.fpf.sentinellens.data.logs.DetectionLogEntity
import com.fpf.sentinellens.lib.toDateString

@Composable
fun DetectionLogScreen(viewModel: DetectionLogViewModel = viewModel()) {
    val items by viewModel.log.collectAsState(emptyList())

    if (items.isEmpty()) {
        EmptyLogScreen()
    } else {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            Column {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 0.dp)
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
                Text(text = "Detection Log", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Date: ${toDateString(data.date)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = "Person detected: ${data.name?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Detection type: ${data.type}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.8f)
                )
                Text(
                    text = "Similarity: ${data.similarity}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.8f)
                )
            }
        }
    }

}

@Composable
fun EmptyLogScreen() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "History icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(96.dp)
                    .rotate(rotation) // Apply the rotation animation here.
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
                modifier = Modifier
                    .alpha(0.8f)
                    .padding(vertical = 8.dp)
            )
        }
    }
}
