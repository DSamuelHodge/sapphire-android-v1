package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.IslandEvent
import com.example.domain.model.IslandThemePreset

@Composable
fun CollapsedIslandPill(
    event: IslandEvent?,
    theme: IslandThemePreset,
    heightDp: Int = 38,
    minWidthDp: Int = 180,
    cutoutPosition: String = "CENTER",
    visualizerBarsCount: Int = 4,
    modifier: Modifier = Modifier
) {
    val cornerRadius = (heightDp / 2).dp

    Box(
        modifier = modifier
            .height(heightDp.dp)
            .widthIn(min = minWidthDp.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.Black)
            .border(
                width = 1.dp,
                color = theme.surfaceBorder,
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT SLOT: App Icon / Artwork / Status Icon
            LeftSlotView(event = event, theme = theme)

            // CENTER RESERVATION SPACE for Camera Cutout
            if (cutoutPosition == "CENTER") {
                Spacer(modifier = Modifier.width(40.dp))
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            // RIGHT SLOT: Live visualizer / battery / timer
            RightSlotView(event = event, theme = theme, barCount = visualizerBarsCount)
        }
    }
}

@Composable
private fun LeftSlotView(
    event: IslandEvent?,
    theme: IslandThemePreset
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(theme.surfaceColor),
        contentAlignment = Alignment.Center
    ) {
        when (event) {
            is IslandEvent.MediaPlayback -> {
                if (event.albumArtBitmap != null) {
                    Image(
                        bitmap = event.albumArtBitmap.asImageBitmap(),
                        contentDescription = "Album Artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Playing",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            is IslandEvent.MessageNotification -> {
                if (event.appIconBitmap != null) {
                    Image(
                        bitmap = event.appIconBitmap.asImageBitmap(),
                        contentDescription = event.appName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification",
                        tint = theme.primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            is IslandEvent.BatteryStatus -> {
                Icon(
                    imageVector = if (event.isCharging) Icons.Default.ElectricBolt else Icons.Default.BatteryChargingFull,
                    contentDescription = "Battery",
                    tint = if (event.isCharging) Color(0xFF10B981) else theme.primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            is IslandEvent.CountdownTimer -> {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    tint = theme.primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            is IslandEvent.NavigationTurn -> {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Navigation",
                    tint = theme.primaryColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            null -> {
                // Idle Sapphire Halo
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(theme.primaryColor)
                )
            }
        }
    }
}

@Composable
private fun RightSlotView(
    event: IslandEvent?,
    theme: IslandThemePreset,
    barCount: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        when (event) {
            is IslandEvent.MediaPlayback -> {
                LiveAudioVisualizerCanvas(
                    isPlaying = event.isPlaying,
                    primaryColor = theme.primaryColor,
                    secondaryColor = theme.secondaryColor,
                    barCount = barCount,
                    width = 24.dp,
                    height = 14.dp
                )
            }
            is IslandEvent.MessageNotification -> {
                Text(
                    text = event.appName.take(7),
                    color = theme.primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            is IslandEvent.BatteryStatus -> {
                Text(
                    text = "${event.percentage}%",
                    color = if (event.isCharging) Color(0xFF10B981) else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            is IslandEvent.CountdownTimer -> {
                val minutes = event.remainingSeconds / 60
                val seconds = event.remainingSeconds % 60
                val timeStr = String.format("%02d:%02d", minutes, seconds)
                Text(
                    text = timeStr,
                    color = theme.primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            is IslandEvent.NavigationTurn -> {
                Text(
                    text = event.distanceText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            null -> {
                Text(
                    text = "Sapphire",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
