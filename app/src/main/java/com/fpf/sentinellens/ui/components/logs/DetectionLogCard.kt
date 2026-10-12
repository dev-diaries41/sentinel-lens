package com.fpf.sentinellens.ui.components.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.fpf.sentinellens.surveillance.DetectionLog
import com.fpf.sentinellens.utils.toDateString

@Composable
fun DetectionLogCard(data: DetectionLog) {
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