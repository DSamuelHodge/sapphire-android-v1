package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AppFilterEntity
import com.example.data.local.entity.IslandSettingsEntity
import com.example.data.local.entity.NotificationHistoryEntity
import com.example.data.repository.IslandRepository
import com.example.domain.model.IslandEvent
import com.example.domain.model.IslandVisualState
import com.example.manager.IslandStateManager
import com.example.service.SapphireOverlayService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IslandViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IslandRepository
    val settingsState: StateFlow<IslandSettingsEntity>
    val appFiltersState: StateFlow<List<AppFilterEntity>>
    val historyState: StateFlow<List<NotificationHistoryEntity>>

    val currentEvent = IslandStateManager.currentEvent
    val visualState = IslandStateManager.visualState
    val isOverlayRunning = IslandStateManager.isOverlayRunning
    val isNotificationConnected = IslandStateManager.isNotificationServiceConnected
    val isAccessibilityConnected = IslandStateManager.isAccessibilityConnected

    private val _installedApps = MutableStateFlow<List<AppInfoItem>>(emptyList())
    val installedApps: StateFlow<List<AppInfoItem>> = _installedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = IslandRepository(db)

        settingsState = repository.settingsFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = IslandSettingsEntity()
            )

        appFiltersState = repository.allAppFilters
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        historyState = repository.historyFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        viewModelScope.launch(Dispatchers.IO) {
            repository.getSettings()
        }

        loadInstalledApps()
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun hasNotificationAccess(context: Context): Boolean {
        val packageNames = NotificationManagerCompat.getEnabledListenerPackages(context)
        return packageNames.contains(context.packageName)
    }

    fun toggleMasterIsland(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(isEnabled = enabled))
            if (enabled && hasOverlayPermission(context)) {
                SapphireOverlayService.start(context)
            } else if (!enabled) {
                SapphireOverlayService.stop(context)
            }
        }
    }

    fun updateCalibration(
        xOffset: Int,
        yOffset: Int,
        widthDp: Int,
        heightDp: Int,
        cornerRadiusDp: Int,
        cutoutPosition: String,
        isAutoCutout: Boolean
    ) {
        viewModelScope.launch {
            repository.updateCalibration(
                xOffset = xOffset,
                yOffset = yOffset,
                widthDp = widthDp,
                heightDp = heightDp,
                radiusDp = cornerRadiusDp,
                cutoutPos = cutoutPosition,
                autoCutout = isAutoCutout
            )
        }
    }

    fun updateThemePreset(preset: String) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(themePreset = preset))
        }
    }

    fun updateVisualizerBars(count: Int) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(current.copy(visualizerBarsCount = count.coerceIn(3, 8)))
        }
    }

    fun updateGestures(singleTap: String, longPress: String, autoHideSec: Int, haptic: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(
                current.copy(
                    singleTapAction = singleTap,
                    longPressAction = longPress,
                    autoHideDelaySeconds = autoHideSec,
                    hapticFeedbackEnabled = haptic
                )
            )
            IslandStateManager.autoHideSeconds = autoHideSec
        }
    }

    fun updateEventToggles(battery: Boolean, timer: Boolean, media: Boolean, notifications: Boolean) {
        viewModelScope.launch {
            val current = repository.getSettings()
            repository.updateSettings(
                current.copy(
                    showBatteryAlerts = battery,
                    showTimerAlerts = timer,
                    showMediaAlerts = media,
                    showNotificationAlerts = notifications
                )
            )
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleAppFilter(packageName: String, appName: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setAppFilter(packageName, appName, isEnabled)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    // Interactive Live Test Triggers
    fun triggerTestMessage(sender: String = "Sarah Jenkins", message: String = "Hey, are we still meeting for coffee at 3pm?") {
        val event = IslandEvent.MessageNotification(
            id = "test_msg_${System.currentTimeMillis()}",
            key = "key_test_msg",
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            title = sender,
            text = message
        )
        IslandStateManager.postNotificationEvent(event)
        viewModelScope.launch {
            repository.recordNotification(
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                title = sender,
                text = message,
                eventType = "MESSAGE",
                hadQuickReply = true
            )
        }
    }

    fun triggerTestMedia(title: String = "Starboy", artist: String = "The Weeknd ft. Daft Punk") {
        val event = IslandEvent.MediaPlayback(
            id = "test_media_${System.currentTimeMillis()}",
            packageName = "com.spotify.music",
            appName = "Spotify",
            trackTitle = title,
            artistName = artist,
            isPlaying = true,
            durationMs = 230000L,
            currentPositionMs = 45000L
        )
        IslandStateManager.postMediaEvent(event)
    }

    fun triggerTestBattery(isCharging: Boolean = true, percentage: Int = 88) {
        val event = IslandEvent.BatteryStatus(
            percentage = percentage,
            isCharging = isCharging,
            isFastCharging = true,
            powerSource = "AC SuperCharge 65W",
            batteryHealth = "Excellent",
            temperatureCelsius = 28.4f
        )
        IslandStateManager.postBatteryEvent(event)
    }

    fun triggerTestTimer(seconds: Int = 60) {
        IslandStateManager.startCountdownTimer(seconds, "Focus Session")
    }

    fun triggerTestNavigation() {
        val event = IslandEvent.NavigationTurn(
            maneuverInstruction = "In 300 feet, turn right onto Sunset Blvd",
            distanceText = "300 ft",
            destination = "Downtown Arts District"
        )
        IslandStateManager.postNavigationEvent(event)
    }

    fun dismissCurrentIsland() {
        IslandStateManager.dismissCurrentEvent()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val apps = packages
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName.contains("google") }
                .map { appInfo ->
                    AppInfoItem(
                        packageName = appInfo.packageName,
                        appName = pm.getApplicationLabel(appInfo).toString()
                    )
                }
                .sortedBy { it.appName.lowercase() }
            _installedApps.value = apps
        }
    }
}

data class AppInfoItem(
    val packageName: String,
    val appName: String
)
