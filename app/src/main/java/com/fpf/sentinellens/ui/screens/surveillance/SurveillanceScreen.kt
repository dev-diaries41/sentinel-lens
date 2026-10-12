package com.fpf.sentinellens.ui.screens.surveillance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fpf.sentinellens.R
import com.fpf.sentinellens.navigation.TopBarState
import com.fpf.sentinellens.surveillance.DetectionTypes
import com.fpf.sentinellens.ui.components.pickers.OptionPicker
import com.fpf.sentinellens.ui.permissions.RequestPermissions
import com.fpf.sentinellens.ui.theme.Green500
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SurveillanceScreen(
    viewModel: SurveillanceViewModel = koinViewModel(),
    onTopBarChange: (TopBarState) -> Unit,
    onViewLogs: () -> Unit,
    onTestSurveillance: () -> Unit
) {
    val hasAnyFaces by viewModel.hasAnyFaces.collectAsState()
    val isSurveillanceActive by viewModel.isSurveillanceActive.collectAsState()
    val detectionMode by viewModel.detectionMode.collectAsState()
    val scrollState = rememberScrollState()
    val title = stringResource(R.string.title_surveillance)

    var hasCameraPermissions by remember{mutableStateOf(false)}
    var isSelectingMode by remember { mutableStateOf(false) }

    RequestPermissions { _, cameraGranted ->
        hasCameraPermissions = cameraGranted
    }

    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                title = title,
                actions = {
                    IconButton(onClick = onViewLogs) {
                        Icon(
                            Icons.Filled.Description,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = onTestSurveillance) {
                        Icon(
                            Icons.Filled.Science,
                            contentDescription = null
                        )
                    }
                }
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ){
                OutlinedButton (
                    onClick = { isSelectingMode = true },
                    modifier = Modifier.widthIn(max = 140.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DetectionTypes[detectionMode]!!,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface

                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint =MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (isSurveillanceActive) {
                    Row(
                        modifier = Modifier
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
            }
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
                if(!hasCameraPermissions){
                    Text(
                        text = stringResource(R.string.surveillance_missing_camera_permission),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical=8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (hasAnyFaces == false) {
                    Text(
                        text = stringResource(R.string.error_watchlist_not_setup_message),
                        modifier = Modifier.padding(vertical=8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    text = stringResource(R.string.surveillance_notice_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

        RecordButton(
            isRecording = isSurveillanceActive,
            enabled = hasCameraPermissions && hasAnyFaces == true,
            onClick = {
                if (isSurveillanceActive) viewModel.stopSurveillance()
                else viewModel.startSurveillance()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }

    OptionPicker(
        isVisible = isSelectingMode,
        title = stringResource(id = R.string.setting_surveillance_mode),
        selectedOption = detectionMode,
        options = DetectionTypes.entries.map{ it.value to it.key},
        onClose = { isSelectingMode = false },
        onSelect = {
            viewModel.updateDetectionMode(it)
            isSelectingMode = false
        }
    )
}



@Composable
fun RecordButton(
    isRecording: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alpha = if (enabled) 1f else 0.5f

    Surface(
        modifier = modifier
            .padding(16.dp)
            .size(64.dp)
            // fade entire button
            .alpha(alpha),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(4.dp, MaterialTheme.colorScheme.onBackground),
        onClick = onClick,
        enabled = enabled
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