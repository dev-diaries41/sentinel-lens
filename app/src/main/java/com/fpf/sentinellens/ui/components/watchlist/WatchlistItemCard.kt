package com.fpf.sentinellens.ui.components.watchlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.fpf.sentinellens.surveillance.DetectionTypes
import com.fpf.sentinellens.surveillance.Face
import com.fpf.sentinellens.surveillance.FaceType
import com.fpf.sentinellens.surveillance.file
import com.fpf.sentinellens.ui.components.ImageDisplay
import com.fpf.sentinellens.ui.action.MenuActionConfig
import com.fpf.sentinellens.ui.components.common.DropDownMenuWrapper


@Composable
fun WatchlistItemCard(
    data: Face,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val actions: List<MenuActionConfig> = listOf(
        MenuActionConfig.Button(label ="Edit", onClick = onEdit ),
        MenuActionConfig.Button(label ="Delete", onClick = onDelete )
    )
    var showMenu by remember { mutableStateOf(false) }


    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),

    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImageDisplay(
                uri = data.file(context).toUri(),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text(
                        text = "Name: ${data.name}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box{
                        IconButton (onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "menu"
                            )
                        }
                        DropDownMenuWrapper(
                            modifier = Modifier.widthIn(min = 144.dp) ,
                            expanded = showMenu,
                            actions = actions,
                            onClose = {showMenu = false}
                        )
                    }

                }
                Text(
                    buildAnnotatedString {
                        append("Detection type: ")
                        withStyle(
                            style = SpanStyle(
                                color = when (data.type) {
                                    FaceType.BLACKLIST -> Color.Red
                                    FaceType.WHITELIST -> Color.Green
                                }
                            )
                        ) {
                            append(DetectionTypes[data.type]!!)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
