package com.fpf.sentinellens.ui.screens.surveillance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpf.sentinellens.R
import com.fpf.sentinellens.ui.permissions.RequestPermissions
import com.fpf.sentinellens.ui.theme.Green500

@Composable
fun SurveillanceScreen(
    viewModel: SurveillanceViewModel = viewModel(),
) {
    val hasAnyFaces by viewModel.hasAnyFaces.observeAsState()
    val hasPermissions by viewModel.hasPermissions.observeAsState(false)
    val isSurveillanceActive by viewModel.isSurveillanceActive.observeAsState()
    val scrollState = rememberScrollState()

    RequestPermissions { notificationGranted, storageGranted, cameraGranted ->
        viewModel.checkPermissions(notificationGranted, storageGranted, cameraGranted)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isSurveillanceActive == true) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        color = Green500,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 64.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .cornerOnlyBorder(
                            cornerRadius = 16.dp,
                            strokeWidth = 4.dp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Person Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(180.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.surveillance_notice_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                if (hasAnyFaces == false) {
                    Text(
                        text = stringResource(R.string.error_watchlist_not_setup_message),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

            }
        }

        RecordButton(
            isRecording = isSurveillanceActive == true,
            enabled = hasPermissions && hasAnyFaces == true,
            onClick = {
                if (isSurveillanceActive == true) viewModel.stopSurveillance()
                else viewModel.startSurveillance()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}



@Composable
fun RecordButton(
    isRecording: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface (
        modifier = modifier.padding(16.dp).size(64.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(4.dp, MaterialTheme.colorScheme.onBackground),
        onClick = onClick,
        enabled = enabled,
    ) {

        val innerShape = if (isRecording) RoundedCornerShape(8.dp) else CircleShape

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(innerShape)
                .background(Color.Red)
        )
    }
}


fun Modifier.cornerOnlyBorder(
    cornerRadius: Dp,
    strokeWidth: Dp,
    color: Color
) = this.then(
    Modifier.drawBehind {
        val strokePx = strokeWidth.toPx()
        val r = cornerRadius.toPx()
        val path = Path().apply {
            // top‑left
            addArc(Rect(0f, 0f, r * 2, r * 2), 180f, 90f)
            // top‑right
            addArc(Rect(size.width - 2 * r, 0f, size.width, r * 2), -90f, 90f)
            // bottom‑right
            addArc(
                Rect(size.width - 2 * r, size.height - 2 * r, size.width, size.height),
                0f,
                90f
            )
            // bottom‑left
            addArc(Rect(0f, size.height - 2 * r, r * 2, size.height), 90f, 90f)
        }
        drawPath(path, color = color, style = Stroke(width = strokePx))
    }
)