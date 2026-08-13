package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AppFilterDao
import com.example.data.local.dao.IslandSettingsDao
import com.example.data.local.dao.NotificationHistoryDao
import com.example.data.local.entity.AppFilterEntity
import com.example.data.local.entity.IslandSettingsEntity
import com.example.data.local.entity.NotificationHistoryEntity

@Database(
    entities = [
        IslandSettingsEntity::class,
        AppFilterEntity::class,
        NotificationHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun islandSettingsDao(): IslandSettingsDao
    abstract fun appFilterDao(): AppFilterDao
    abstract fun notificationHistoryDao(): NotificationHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sapphire_dynamic_island.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        db.execSQL(
                            "INSERT OR IGNORE INTO island_settings (id, isEnabled, isAutoCutoutEnabled, cutoutPosition, xOffsetPx, yOffsetPx, collapsedWidthDp, collapsedHeightDp, cornerRadiusDp, themePreset, customAccentColor, visualizerBarsCount, singleTapAction, longPressAction, autoHideDelaySeconds, showBatteryAlerts, showTimerAlerts, showMediaAlerts, showNotificationAlerts, hapticFeedbackEnabled) VALUES (1, 1, 1, 'CENTER', 0, 0, 180, 38, 24, 'SAPPHIRE_NEON', 4278244095, 4, 'EXPAND', 'EXPAND', 5, 1, 1, 1, 1, 1)"
                        )
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
