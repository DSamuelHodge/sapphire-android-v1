package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "island_settings")
data class IslandSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isEnabled: Boolean = true,
    val isAutoCutoutEnabled: Boolean = true,
    val cutoutPosition: String = "CENTER", // "CENTER", "LEFT", "RIGHT"
    val xOffsetPx: Int = 0,
    val yOffsetPx: Int = 0,
    val collapsedWidthDp: Int = 180,
    val collapsedHeightDp: Int = 38,
    val cornerRadiusDp: Int = 24,
    val themePreset: String = "SAPPHIRE_NEON", // "SAPPHIRE_NEON", "MIDNIGHT_ONYX", "EMERALD_AURORA", "RUBY_EMBER", "CYBER_VIOLET"
    val customAccentColor: Long = 0xFF00D2FF,
    val visualizerBarsCount: Int = 4,
    val singleTapAction: String = "EXPAND", // "EXPAND", "OPEN_APP", "DISMISS"
    val longPressAction: String = "EXPAND", // "EXPAND", "SETTINGS"
    val autoHideDelaySeconds: Int = 5,
    val showBatteryAlerts: Boolean = true,
    val showTimerAlerts: Boolean = true,
    val showMediaAlerts: Boolean = true,
    val showNotificationAlerts: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
)
