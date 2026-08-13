package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.IslandThemePreset

@Composable
fun NotchCalibrationViewfinder(
    xOffset: Int,
    yOffset: Int,
    widthDp: Int,
    heightDp: Int,
    cornerRadiusDp: Int,
    cutoutPosition: String,
    theme: IslandThemePreset,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0D0D0D))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Draw status bar guide line
            drawLine(
                color = Color(0x223B82F6),
                start = Offset(0f, 60.dp.toPx()),
                end = Offset(canvasW, 60.dp.toPx()),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Draw center grid crosshair
            drawLine(
                color = Color(0x1AFFFFFF),
                start = Offset(canvasW / 2f, 0f),
                end = Offset(canvasW / 2f, canvasH),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            // Calculate anchor base position
            val baseX = when (cutoutPosition) {
                "LEFT" -> 40.dp.toPx()
                "RIGHT" -> canvasW - 40.dp.toPx()
                else -> canvasW / 2f
            }
            val baseY = 24.dp.toPx()

            // Draw simulated hardware camera punch-hole
            drawCircle(
                color = Color(0xFF1C1C1E),
                radius = 12.dp.toPx(),
                center = Offset(baseX, baseY)
            )
            drawCircle(
                color = theme.primaryColor.copy(alpha = 0.5f),
                radius = 13.dp.toPx(),
                center = Offset(baseX, baseY),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Calculate Island Box bounds with user offsets
            val islandW = widthDp.dp.toPx()
            val islandH = heightDp.dp.toPx()
            val islandCorner = cornerRadiusDp.dp.toPx()

            val islandLeft = baseX - (islandW / 2f) + xOffset.dp.toPx()
            val islandTop = baseY - (islandH / 2f) + yOffset.dp.toPx()

            // Draw glowing Island pill boundary
            drawRoundRect(
                color = theme.surfaceColor,
                topLeft = Offset(islandLeft, islandTop),
                size = Size(islandW, islandH),
                cornerRadius = CornerRadius(islandCorner, islandCorner)
            )
            drawRoundRect(
                color = theme.primaryColor,
                topLeft = Offset(islandLeft, islandTop),
                size = Size(islandW, islandH),
                cornerRadius = CornerRadius(islandCorner, islandCorner),
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner center dot
            drawCircle(
                color = theme.primaryColor,
                radius = 3.dp.toPx(),
                center = Offset(islandLeft + islandW / 2f, islandTop + islandH / 2f)
            )
        }

        // Overlay status text info
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)
        ) {
            Text(
                text = "OFFSET: X=$xOffset px, Y=$yOffset px  |  SIZE: ${widthDp}x${heightDp} dp",
                color = Color(0xFFA1A1AA),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
