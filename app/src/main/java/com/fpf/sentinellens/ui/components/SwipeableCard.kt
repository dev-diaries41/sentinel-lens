package com.fpf.sentinellens.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun <T> SwipeableCard(
    data: T,
    onDelete: () -> Unit,
    item: @Composable (T) -> Unit // Generic type instead of Any
) {
    var cardWidth by remember { mutableFloatStateOf(0f) }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val threshold = cardWidth * 0.4f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                cardWidth = coordinates.size.width.toFloat()
            }
            .clipToBounds()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        if (dragAmount > 0) return@detectHorizontalDragGestures
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (offsetX.value < -threshold) {
                                offsetX.animateTo(
                                    targetValue = -cardWidth,
                                    animationSpec = tween(durationMillis = 300)
                                )
                                onDelete()
                            } else {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 300)
                                )
                            }
                        }
                    }
                )
            }
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp)  // ← external margin
                .clipToBounds()
                .pointerInput(Unit) {}
        ) {
            // 1) red background
            Box(Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .fillMaxHeight(0.98f)
                        .align(Alignment.CenterEnd)
                        .background(if (offsetX.value < 0f) Color.Red else Color.Transparent)
                )
            }

            Card(
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .fillMaxWidth()
            ) {
                item(data)
            }
        }
    }
}
