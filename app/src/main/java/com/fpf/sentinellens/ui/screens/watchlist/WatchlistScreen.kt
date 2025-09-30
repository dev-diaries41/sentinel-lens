package com.fpf.sentinellens.ui.screens.watchlist

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpf.sentinellens.R
import com.fpf.sentinellens.ui.components.SwipeableCard


@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel = viewModel(),
    onNavigate: () -> Unit,
) {
    val faceList by viewModel.faceList.observeAsState(emptyList())
    val listState = rememberLazyListState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate() },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = shapes.extraLarge
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add person"
                )
            }
        }
    ) { padding ->
        Column (
            modifier = Modifier.padding(16.dp)
        ) {
            if (faceList.isEmpty()) {
                EmptyPersonScreen(onNavigate)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(
                        bottom = padding.calculateBottomPadding() + 80.dp
                    ),
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = faceList,
                        key = { it.id }
                    ) { item ->
                        SwipeableCard(
                            data = item,
                            onDelete = { viewModel.deleteFace(item.id) },
                            item = { WatchlistItemCard(data = item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPersonScreen(
    onNavigate: () -> Unit,

    ) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Person icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(100.dp)
            )
            Text(
                text = stringResource(R.string.no_entries),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(R.string.no_entries_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(0.8f)
                    .padding(vertical = 8.dp)
            )
            Button(
                onClick = {onNavigate()}
            ) {
                Text("Add person")
            }
        }
    }
}