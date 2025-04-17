package com.fpf.sentinellens.ui.screens.watchlist

import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpf.sentinellens.data.faces.Face
import com.fpf.sentinellens.R
import com.fpf.sentinellens.data.faces.FaceType
import com.fpf.sentinellens.ui.components.MediaStoreImage
import com.fpf.sentinellens.ui.components.SwipeableCard
import com.fpf.sentinellens.ui.screens.settings.modeOptions


@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel = viewModel(),
    onNavigate: () -> Unit,
    ) {
    val faceList by viewModel.faceList.observeAsState(emptyList())
    Box(
        modifier = Modifier.padding(16.dp)
    ){
        Column {
            if (faceList.isEmpty()) {
                EmptyPersonScreen(
                    onNavigate
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.End){
                    Button(
                        onClick = {onNavigate()}
                    ) {
                        Text("Add person")
                    }
                }
                LazyColumn {
                    items(
                        items = faceList,
                        key = { it.id }
                    ) { item ->
                        SwipeableCard(
                            data = item,
                            onDelete = {viewModel.deleteFace(item.id)},
                            item= { PersonCard(data = item) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PersonCard(data: Face) {
    val filePath = "faces/${data.id.hashCode()}.jpg"

    Card (
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)

    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            MediaStoreImage(
                path = filePath,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = modeOptions[data.type]!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when(data.type){
                        FaceType.BLACKLIST -> Color.Red
                        FaceType.WHITELIST -> Color.Green
                    }
                )
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