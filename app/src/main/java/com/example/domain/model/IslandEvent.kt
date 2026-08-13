package com.example.domain.model

import android.app.PendingIntent
import android.app.RemoteInput
import android.graphics.Bitmap
import android.net.Uri

sealed interface IslandEvent {
    val id: String
    val timestamp: Long
    val priority: Int // Higher value = higher display priority

    data class MessageNotification(
        override val id: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val priority: Int = 80,
        val key: String,
        val packageName: String,
        val appName: String,
        val title: String,
        val text: String,
        val subText: String = "",
        val appIconBitmap: Bitmap? = null,
        val largeIconBitmap: Bitmap? = null,
        val contactAvatarUri: Uri? = null,
        val replyAction: DirectReplyAction? = null,
        val quickActions: List<QuickActionItem> = emptyList(),
        val contentIntent: PendingIntent? = null
    ) : IslandEvent

    data class MediaPlayback(
        override val id: String,
        override val timestamp: Long = System.currentTimeMillis(),
        override val priority: Int = 60,
        val packageName: String,
        val appName: String,
        val trackTitle: String,
        val artistName: String,
        val albumArtBitmap: Bitmap? = null,
        val isPlaying: Boolean = true,
        val durationMs: Long = 0L,
        val currentPositionMs: Long = 0L,
        val canSeek: Boolean = true,
        val isFavorite: Boolean = false,
        val audioSessionId: Int = 0
    ) : IslandEvent

    data class BatteryStatus(
        override val id: String = "battery_event",
        override val timestamp: Long = System.currentTimeMillis(),
        override val priority: Int = 90,
        val percentage: Int,
        val isCharging: Boolean,
        val isFastCharging: Boolean = false,
        val powerSource: String = "AC", // "AC", "USB", "WIRELESS"
        val batteryHealth: String = "Good",
        val temperatureCelsius: Float = 25f
    ) : IslandEvent

    data class CountdownTimer(
        override val id: String = "countdown_timer",
        override val timestamp: Long = System.currentTimeMillis(),
        override val priority: Int = 70,
        val title: String = "Timer",
        val remainingSeconds: Int,
        val totalSeconds: Int,
        val isRunning: Boolean = true
    ) : IslandEvent

    data class NavigationTurn(
        override val id: String = "nav_turn",
        override val timestamp: Long = System.currentTimeMillis(),
        override val priority: Int = 85,
        val maneuverInstruction: String,
        val distanceText: String,
        val maneuverIconType: String = "STRAIGHT", // LEFT, RIGHT, UTURN, STRAIGHT, MERGE
        val destination: String = ""
    ) : IslandEvent
}

data class DirectReplyAction(
    val title: CharSequence,
    val pendingIntent: PendingIntent,
    val remoteInput: RemoteInput
)

data class QuickActionItem(
    val title: String,
    val actionIntent: PendingIntent?,
    val isDestructive: Boolean = false
)

enum class IslandVisualState {
    COLLAPSED,
    EXPANDED,
    HIDDEN
}

enum class CutoutAlignment {
    CENTER,
    LEFT,
    RIGHT
}
