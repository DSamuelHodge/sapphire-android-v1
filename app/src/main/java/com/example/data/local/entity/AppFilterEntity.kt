package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_filters")
data class AppFilterEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val isPriority: Boolean = false,
    val customColor: Long? = null
)
