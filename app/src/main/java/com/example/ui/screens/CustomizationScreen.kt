package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.IslandThemePreset
import com.example.ui.components.LiveAudioVisualizerCanvas
import com.example.ui.viewmodel.IslandViewModel

@Composable
fun CustomizationScreen(
    viewModel: IslandViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settingsState.collectAsState()
    val currentTheme = IslandThemePreset.fromString(settings.themePreset)

    var autoHideSec by remember(settings.autoHideDelaySeconds) {
        mutableFloatStateOf(settings.autoHideDelaySeconds.toFloat())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER
        item {
            Spacer(modifier = Modifier.height(52.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Themes & Interactions",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize colors, visualizer, and touch gestures",
                        color = Color(0xFFA1A1AA),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // THEME PRESETS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SELECT COLOR THEME",
                        color = Color(0xFF71717A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    IslandThemePreset.entries.forEach { preset ->
                        val isSelected = preset.name == settings.themePreset
                        ThemeOptionRow(
                            preset = preset,
                            isSelected = isSelected,
                            onClick = {
                                viewModel.updateThemePreset(preset.name)
                            }
                        )
                    }
                }
            }
        }

        // AUDIO VISUALIZER SETTINGS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Visualizer",
                                tint = currentTheme.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Music Visualizer Bars",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LiveAudioVisualizerCanvas(
                            isPlaying = true,
                            primaryColor = currentTheme.primaryColor,
                            secondaryColor = currentTheme.secondaryColor,
                            barCount = settings.visualizerBarsCount,
                            width = 32.dp,
                            height = 18.dp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(3, 4, 5, 6, 8).forEach { count ->
                            val isSel = count == settings.visualizerBarsCount
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) currentTheme.primaryColor else Color(0xFF1C1C1E))
                                    .clickable { viewModel.updateVisualizerBars(count) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$count",
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // GESTURES & AUTO HIDE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "TOUCH & DURATION",
                        color = Color(0xFF71717A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // Auto hide slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Auto-Dismiss Timeout",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${autoHideSec.toInt()} seconds",
                                color = currentTheme.primaryColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = autoHideSec,
                            onValueChange = {
                                autoHideSec = it
                                viewModel.updateGestures(
                                    singleTap = settings.singleTapAction,
                                    longPress = settings.longPressAction,
                                    autoHideSec = it.toInt(),
                                    haptic = settings.hapticFeedbackEnabled
                                )
                            },
                            valueRange = 3f..15f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = currentTheme.primaryColor
                            )
                        )
                    }

                    // Haptic Feedback switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Haptics",
                                tint = Color(0xFFA1A1AA),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Haptic Tactile Feedback",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        Switch(
                            checked = settings.hapticFeedbackEnabled,
                            onCheckedChange = {
                                viewModel.updateGestures(
                                    singleTap = settings.singleTapAction,
                                    longPress = settings.longPressAction,
                                    autoHideSec = settings.autoHideDelaySeconds,
                                    haptic = it
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = currentTheme.primaryColor
                            )
                        )
                    }
                }
            }
        }

        // EVENT TRIGGERS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "DYNAMIC EVENT CHANNELS",
                        color = Color(0xFF71717A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    EventToggleRow(
                        icon = Icons.Default.MusicNote,
                        title = "Music & Media Players",
                        checked = settings.showMediaAlerts,
                        color = Color(0xFF3B82F6),
                        onCheckedChange = {
                            viewModel.updateEventToggles(
                                battery = settings.showBatteryAlerts,
                                timer = settings.showTimerAlerts,
                                media = it,
                                notifications = settings.showNotificationAlerts
                            )
                        }
                    )

                    EventToggleRow(
                        icon = Icons.Default.Notifications,
                        title = "Incoming Messages & Apps",
                        checked = settings.showNotificationAlerts,
                        color = Color(0xFF10B981),
                        onCheckedChange = {
                            viewModel.updateEventToggles(
                                battery = settings.showBatteryAlerts,
                                timer = settings.showTimerAlerts,
                                media = settings.showMediaAlerts,
                                notifications = it
                            )
                        }
                    )

                    EventToggleRow(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Battery & Fast Charge Alerts",
                        checked = settings.showBatteryAlerts,
                        color = Color(0xFFF59E0B),
                        onCheckedChange = {
                            viewModel.updateEventToggles(
                                battery = it,
                                timer = settings.showTimerAlerts,
                                media = settings.showMediaAlerts,
                                notifications = settings.showNotificationAlerts
                            )
                        }
                    )

                    EventToggleRow(
                        icon = Icons.Default.Timer,
                        title = "Live Countdown Timers",
                        checked = settings.showTimerAlerts,
                        color = Color(0xFFA855F7),
                        onCheckedChange = {
                            viewModel.updateEventToggles(
                                battery = settings.showBatteryAlerts,
                                timer = it,
                                media = settings.showMediaAlerts,
                                notifications = settings.showNotificationAlerts
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThemeOptionRow(
    preset: IslandThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFF1C1C1E) else Color(0xFF141414))
            .border(
                1.dp,
                if (isSelected) preset.primaryColor else Color(0x1AFFFFFF),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Color Swatch
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(preset.primaryColor, preset.secondaryColor)
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = preset.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = preset.description,
                    color = Color(0xFFA1A1AA),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(preset.primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun EventToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    color: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = color
            )
        )
    }
}
