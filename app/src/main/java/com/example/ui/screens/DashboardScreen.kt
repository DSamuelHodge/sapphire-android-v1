package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.IslandThemePreset
import com.example.ui.components.DynamicIslandView
import com.example.ui.viewmodel.IslandViewModel

@Composable
fun DashboardScreen(
    viewModel: IslandViewModel,
    onNavigateToCalibration: () -> Unit,
    onNavigateToCustomization: () -> Unit,
    onNavigateToAppFilters: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settingsState.collectAsState()
    val event by viewModel.currentEvent.collectAsState()
    val visualState by viewModel.visualState.collectAsState()
    val isOverlayRunning by viewModel.isOverlayRunning.collectAsState()
    val isNotifConnected by viewModel.isNotificationConnected.collectAsState()
    val isAccessibilityConnected by viewModel.isAccessibilityConnected.collectAsState()

    val hasOverlayPerm = viewModel.hasOverlayPermission(context)
    val hasNotifAccess = viewModel.hasNotificationAccess(context)
    val theme = IslandThemePreset.fromString(settings.themePreset)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER & LIVE ISLAND PREVIEW SANDBOX
        item {
            Spacer(modifier = Modifier.height(52.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Sapphire Dynamic Island",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live interactive notch preview and overlay manager",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }
        }

        // INTERACTIVE LIVE SANDBOX PREVIEW
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(32.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE NOTCH STAGE",
                            color = Color(0xFF71717A),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.primaryColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (event != null) "ACTIVE: ${event!!::class.simpleName}" else "STANDBY",
                                color = theme.primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // THE LIVE DYNAMIC ISLAND COMPOSABLE
                    DynamicIslandView(
                        event = event,
                        visualState = visualState,
                        settings = settings,
                        onDismiss = { viewModel.dismissCurrentIsland() }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Tap or long-press the island above to test micro-interactions",
                        color = Color(0xFF71717A),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // MASTER OVERLAY TOGGLE
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (settings.isEnabled) theme.primaryColor else Color(0xFF1C1C1E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Overlay Master",
                                tint = if (settings.isEnabled) Color.Black else Color(0xFFA1A1AA),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dynamic Overlay Service",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (settings.isEnabled) {
                                    if (hasOverlayPerm) "Active over camera cutout" else "Permission required"
                                } else "Overlay is disabled",
                                color = if (settings.isEnabled && hasOverlayPerm) Color(0xFF10B981) else Color(0xFFA1A1AA),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = settings.isEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.toggleMasterIsland(enabled, context)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = theme.primaryColor
                        ),
                        modifier = Modifier.testTag("master_overlay_switch")
                    )
                }
            }
        }

        // SYSTEM SERVICE HEALTH STATUS
        item {
            Text(
                text = "SYSTEM STATUS",
                color = Color(0xFF71717A),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(
                    title = "Overlay",
                    isOk = hasOverlayPerm,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPermissions
                )
                StatusBadge(
                    title = "Listener",
                    isOk = isNotifConnected || hasNotifAccess,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPermissions
                )
                StatusBadge(
                    title = "Accessibility",
                    isOk = isAccessibilityConnected,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPermissions
                )
            }
        }

        // TEST EVENT TRIGGER SUITE
        item {
            Text(
                text = "TRIGGER TEST EVENTS",
                color = Color(0xFF71717A),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTriggerButton(
                        icon = Icons.Default.MusicNote,
                        title = "Music Player",
                        subtitle = "Starboy • The Weeknd",
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.triggerTestMedia() }
                    )
                    QuickTriggerButton(
                        icon = Icons.Default.Notifications,
                        title = "Message Alert",
                        subtitle = "WhatsApp with Reply",
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.triggerTestMessage() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTriggerButton(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Battery Charge",
                        subtitle = "Fast Charge 65W",
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.triggerTestBattery() }
                    )
                    QuickTriggerButton(
                        icon = Icons.Default.Timer,
                        title = "Timer 60s",
                        subtitle = "Dynamic Countdown",
                        color = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.triggerTestTimer(60) }
                    )
                }

                QuickTriggerButton(
                    icon = Icons.Default.Navigation,
                    title = "Navigation Live Activity",
                    subtitle = "Turn-by-turn routing instruction",
                    color = Color(0xFF6366F1),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.triggerTestNavigation() }
                )
            }
        }

        // QUICK NAVIGATION TILES
        item {
            Text(
                text = "CONFIG & TOOLS",
                color = Color(0xFF71717A),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NavTile(
                    icon = Icons.Default.Tune,
                    title = "Notch Calibration Studio",
                    subtitle = "Adjust X/Y offsets, width, height and cutout alignment",
                    onClick = onNavigateToCalibration
                )
                NavTile(
                    icon = Icons.Default.Palette,
                    title = "Themes & Gestures",
                    subtitle = "Neon palettes, visualizer bars, and interaction rules",
                    onClick = onNavigateToCustomization
                )
                NavTile(
                    icon = Icons.Default.SettingsSuggest,
                    title = "Selective App Filters",
                    subtitle = "Choose which apps can post to the dynamic island",
                    onClick = onNavigateToAppFilters
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusBadge(
    title: String,
    isOk: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(
                1.dp,
                if (isOk) Color(0x3310B981) else Color(0x33EF4444),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = if (isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (isOk) "Active" else "Action needed",
                tint = if (isOk) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QuickTriggerButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFA1A1AA),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun NavTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1C1E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFA1A1AA),
                    fontSize = 12.sp
                )
            }
        }
    }
}
