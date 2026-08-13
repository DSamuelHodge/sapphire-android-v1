package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history")
data class NotificationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val subText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String = "MESSAGE", // MESSAGE, MEDIA, BATTERY, TIMER, SYSTEM
    val hadQuickReply: Boolean = false
)
