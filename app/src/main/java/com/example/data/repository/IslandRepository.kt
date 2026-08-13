package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AppFilterEntity
import com.example.data.local.entity.IslandSettingsEntity
import com.example.data.local.entity.NotificationHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class IslandRepository(private val database: AppDatabase) {

    private val settingsDao = database.islandSettingsDao()
    private val appFilterDao = database.appFilterDao()
    private val historyDao = database.notificationHistoryDao()

    val settingsFlow: Flow<IslandSettingsEntity> = settingsDao.getSettingsFlow()
        .map { it ?: IslandSettingsEntity() }

    suspend fun getSettings(): IslandSettingsEntity {
        return withContext(Dispatchers.IO) {
            settingsDao.getSettings() ?: IslandSettingsEntity().also {
                settingsDao.insertOrUpdate(it)
            }
        }
    }

    suspend fun updateSettings(settings: IslandSettingsEntity) {
        withContext(Dispatchers.IO) {
            settingsDao.insertOrUpdate(settings)
        }
    }

    suspend fun updateCalibration(
        xOffset: Int,
        yOffset: Int,
        widthDp: Int,
        heightDp: Int,
        radiusDp: Int,
        cutoutPos: String,
        autoCutout: Boolean
    ) {
        withContext(Dispatchers.IO) {
            val current = getSettings()
            val updated = current.copy(
                xOffsetPx = xOffset,
                yOffsetPx = yOffset,
                collapsedWidthDp = widthDp,
                collapsedHeightDp = heightDp,
                cornerRadiusDp = radiusDp,
                cutoutPosition = cutoutPos,
                isAutoCutoutEnabled = autoCutout
            )
            settingsDao.insertOrUpdate(updated)
        }
    }

    // App Filters
    val allAppFilters: Flow<List<AppFilterEntity>> = appFilterDao.getAllFiltersFlow()

    suspend fun isAppAllowed(packageName: String): Boolean {
        return withContext(Dispatchers.IO) {
            val filter = appFilterDao.getFilterForPackage(packageName)
            filter?.isEnabled ?: true // Default allowed if not explicitly blocked
        }
    }

    suspend fun setAppFilter(packageName: String, appName: String, isEnabled: Boolean) {
        withContext(Dispatchers.IO) {
            appFilterDao.insertOrUpdate(
                AppFilterEntity(
                    packageName = packageName,
                    appName = appName,
                    isEnabled = isEnabled
                )
            )
        }
    }

    suspend fun saveAppFilters(filters: List<AppFilterEntity>) {
        withContext(Dispatchers.IO) {
            appFilterDao.insertAll(filters)
        }
    }

    // Notification History
    val historyFlow: Flow<List<NotificationHistoryEntity>> = historyDao.getRecentHistoryFlow()

    suspend fun recordNotification(
        packageName: String,
        appName: String,
        title: String,
        text: String,
        subText: String = "",
        eventType: String = "MESSAGE",
        hadQuickReply: Boolean = false
    ) {
        withContext(Dispatchers.IO) {
            historyDao.insert(
                NotificationHistoryEntity(
                    packageName = packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    subText = subText,
                    timestamp = System.currentTimeMillis(),
                    eventType = eventType,
                    hadQuickReply = hadQuickReply
                )
            )
        }
    }

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            historyDao.clearAll()
        }
    }

    suspend fun deleteHistoryItem(id: Long) {
        withContext(Dispatchers.IO) {
            historyDao.deleteById(id)
        }
    }
}
