package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.IslandEvent
import com.example.domain.model.IslandThemePreset
import com.example.manager.IslandStateManager

@Composable
fun ExpandedMediaCard(
    event: IslandEvent.MediaPlayback,
    theme: IslandThemePreset,
    modifier: Modifier = Modifier,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onTogglePlayPause: (Boolean) -> Unit = {}
) {
    var isLiked by remember(event.id) { mutableStateOf(event.isFavorite) }
    var currentSliderPos by remember(event.currentPositionMs) {
        mutableFloatStateOf(
            if (event.durationMs > 0) event.currentPositionMs.toFloat() / event.durationMs else 0.35f
        )
    }

    val elapsedSeconds = ((currentSliderPos * event.durationMs) / 1000).toInt()
    val totalSeconds = (event.durationMs / 1000).toInt()
    val remainingSeconds = (totalSeconds - elapsedSeconds).coerceAtLeast(0)

    val elapsedFormatted = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60)
    val remainingFormatted = String.format("-%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Black)
            .border(
                width = 1.dp,
                color = theme.surfaceBorder,
                shape = RoundedCornerShape(32.dp)
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // HEADER ROW: Artwork + Track Info + Live Audio Visualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art (48dp x 48dp, rounded 8dp)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.surfaceColor)
                        .border(1.dp, theme.glowColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (event.albumArtBitmap != null) {
                        Image(
                            bitmap = event.albumArtBitmap.asImageBitmap(),
                            contentDescription = "Album Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Track Info Block
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.trackTitle,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.artistName,
                        color = Color(0xFFA1A1A6),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Live Audio Visualizer (Top Right corner)
                LiveAudioVisualizerCanvas(
                    isPlaying = event.isPlaying,
                    primaryColor = theme.primaryColor,
                    secondaryColor = theme.secondaryColor,
                    barCount = 5,
                    width = 30.dp,
                    height = 20.dp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SEEKBAR CONTAINER
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = currentSliderPos,
                    onValueChange = {
                        currentSliderPos = it
                        val newPos = (it * event.durationMs).toLong()
                        IslandStateManager.updateMediaProgress(newPos)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = theme.primaryColor,
                        inactiveTrackColor = Color(0xFF2C2C2E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .testTag("media_seekbar")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = elapsedFormatted,
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = remainingFormatted,
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PLAYBACK CONTROLS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Like / Favorite
                IconButton(
                    onClick = { isLiked = !isLiked },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("media_like_button")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isLiked) Color(0xFFFF3366) else Color(0xFF8E8E93),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Previous Track
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("media_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Play / Pause Toggle
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            val newPlayState = !event.isPlaying
                            IslandStateManager.updateMediaPlayState(newPlayState)
                            onTogglePlayPause(newPlayState)
                        }
                        .testTag("media_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (event.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (event.isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Next Track
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("media_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Close / Dismiss to Collapsed
                IconButton(
                    onClick = {
                        IslandStateManager.setVisualState(com.example.domain.model.IslandVisualState.COLLAPSED)
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("media_collapse_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3A3A3C))
                    )
                }
            }
        }
    }
}
