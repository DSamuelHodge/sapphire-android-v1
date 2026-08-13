package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LiveAudioVisualizerCanvas(
    isPlaying: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    width: Dp = 22.dp,
    height: Dp = 16.dp
) {
    val transition = rememberInfiniteTransition(label = "visualizer_anim")

    val bar1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isPlaying) 0.95f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )

    val bar2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = if (isPlaying) 0.35f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(460, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )

    val bar3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isPlaying) 1.0f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val bar4 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = if (isPlaying) 0.45f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(510, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    val barHeights = listOf(bar1, bar2, bar3, bar4)

    Canvas(
        modifier = modifier
            .width(width)
            .height(height)
    ) {
        val totalWidth = size.width
        val totalHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.8f)).coerceAtLeast(2f)
        val spacing = (totalWidth - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)

        val gradient = Brush.verticalGradient(
            colors = listOf(primaryColor, secondaryColor)
        )

        for (i in 0 until barCount) {
            val fraction = barHeights.getOrElse(i % barHeights.size) { 0.5f }
            val currentBarHeight = (totalHeight * fraction).coerceIn(3f, totalHeight)
            val left = i * (barWidth + spacing)
            val top = (totalHeight - currentBarHeight) / 2f

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(left, top),
                size = Size(barWidth, currentBarHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
