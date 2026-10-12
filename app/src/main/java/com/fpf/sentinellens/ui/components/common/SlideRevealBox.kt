package com.fpf.sentinellens.ui.components.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.animation.core.animateIntAsState
import kotlin.math.max

@Composable
fun SlideRevealBox(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    offsetPx: Int = 0, // Offset in pixels
    reverse: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (!isVisible) return

    var contentHeight by remember { mutableIntStateOf(0) }

    val animatedOffsetPx by animateIntAsState(
        targetValue = offsetPx,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val direction = if (reverse) -1 else 1

    SubcomposeLayout(modifier = modifier.clipToBounds()) { constraints ->
        val placeables = subcompose("content", content).map { it.measure(constraints) }
        contentHeight = placeables.maxOfOrNull { it.height } ?: 0

        val visibleHeight = max(contentHeight - animatedOffsetPx, 0)

        layout(constraints.maxWidth, visibleHeight) {
            val animatedY = direction * animatedOffsetPx
            placeables.forEach {
                it.placeRelative(
                    x = 0,
                    y = animatedY
                )
            }
        }
    }
}
