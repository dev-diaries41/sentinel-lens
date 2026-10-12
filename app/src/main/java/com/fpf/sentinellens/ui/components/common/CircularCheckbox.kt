package com.fpf.sentinellens.ui.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

@Composable
fun CircularCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 20.dp,
    strokeWidth: Dp = 1.dp,
    checkedColor: Color = MaterialTheme.colorScheme.primary,
    uncheckedBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
    checkmarkColor: Color = MaterialTheme.colorScheme.onPrimary,
    contentDescription: String? = null
) {
    val bgColor by animateColorAsState(targetValue = if (checked) checkedColor else MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.5f))
    val borderColor by animateColorAsState(targetValue = if (checked) checkedColor else uncheckedBorderColor)
    val iconAlpha by animateFloatAsState(targetValue = if (checked) 1f else 0f)
    val iconScale by animateFloatAsState(targetValue = if (checked) 1f else 0.9f)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor)
            .border(BorderStroke(strokeWidth, borderColor), shape = CircleShape)
            .clickable(
                enabled = enabled && onCheckedChange != null,
                onClick = { onCheckedChange?.invoke(!checked) },
                role = Role.Checkbox,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .semantics {
                if (contentDescription != null) this.contentDescription = contentDescription
            }
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = checkmarkColor.copy(alpha = iconAlpha),
            modifier = Modifier
                .size((size.value * 0.6f).dp)
                .scale(iconScale)
                .align(Alignment.Center)
        )
    }
}
