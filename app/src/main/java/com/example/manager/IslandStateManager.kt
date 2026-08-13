package com.example.manager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.RemoteInput
import com.example.domain.model.DirectReplyAction
import com.example.domain.model.IslandEvent
import com.example.domain.model.IslandVisualState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object IslandStateManager {
    private const val TAG = "IslandStateManager"
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _currentEvent = MutableStateFlow<IslandEvent?>(null)
    val currentEvent: StateFlow<IslandEvent?> = _currentEvent.asStateFlow()

    private val _visualState = MutableStateFlow(IslandVisualState.COLLAPSED)
    val visualState: StateFlow<IslandVisualState> = _visualState.asStateFlow()

    private val _isOverlayRunning = MutableStateFlow(false)
    val isOverlayRunning: StateFlow<Boolean> = _isOverlayRunning.asStateFlow()

    private val _isNotificationServiceConnected = MutableStateFlow(false)
    val isNotificationServiceConnected: StateFlow<Boolean> = _isNotificationServiceConnected.asStateFlow()

    private val _isAccessibilityConnected = MutableStateFlow(false)
    val isAccessibilityConnected: StateFlow<Boolean> = _isAccessibilityConnected.asStateFlow()

    // Active events collection
    private var activeMediaEvent: IslandEvent.MediaPlayback? = null
    private var activeTimerEvent: IslandEvent.CountdownTimer? = null
    private var activeBatteryEvent: IslandEvent.BatteryStatus? = null
    private var activeNotificationEvent: IslandEvent.MessageNotification? = null

    private var autoDismissJob: Job? = null
    private var timerTickJob: Job? = null

    var autoHideSeconds: Int = 6

    fun setOverlayRunning(running: Boolean) {
        _isOverlayRunning.value = running
    }

    fun setNotificationServiceConnected(connected: Boolean) {
        _isNotificationServiceConnected.value = connected
    }

    fun setAccessibilityConnected(connected: Boolean) {
        _isAccessibilityConnected.value = connected
    }

    fun setVisualState(state: IslandVisualState) {
        _visualState.value = state
        if (state == IslandVisualState.EXPANDED) {
            // Cancel auto-dismiss while user is inspecting expanded view
            autoDismissJob?.cancel()
        }
    }

    fun toggleExpandCollapse() {
        _visualState.value = if (_visualState.value == IslandVisualState.EXPANDED) {
            IslandVisualState.COLLAPSED
        } else {
            IslandVisualState.EXPANDED
        }
    }

    fun postNotificationEvent(event: IslandEvent.MessageNotification) {
        activeNotificationEvent = event
        reevaluateCurrentEvent()
        scheduleAutoDismiss(autoHideSeconds)
    }

    fun removeNotificationEvent(key: String) {
        if (activeNotificationEvent?.key == key || activeNotificationEvent?.id == key) {
            activeNotificationEvent = null
            reevaluateCurrentEvent()
        }
    }

    fun postMediaEvent(event: IslandEvent.MediaPlayback) {
        activeMediaEvent = event
        reevaluateCurrentEvent()
    }

    fun removeMediaEvent() {
        activeMediaEvent = null
        reevaluateCurrentEvent()
    }

    fun updateMediaPlayState(isPlaying: Boolean) {
        activeMediaEvent?.let { current ->
            activeMediaEvent = current.copy(isPlaying = isPlaying)
            reevaluateCurrentEvent()
        }
    }

    fun updateMediaProgress(positionMs: Long) {
        activeMediaEvent?.let { current ->
            activeMediaEvent = current.copy(currentPositionMs = positionMs)
            if (_currentEvent.value is IslandEvent.MediaPlayback) {
                _currentEvent.value = activeMediaEvent
            }
        }
    }

    fun postBatteryEvent(event: IslandEvent.BatteryStatus) {
        activeBatteryEvent = event
        reevaluateCurrentEvent()
        scheduleAutoDismiss(5)
    }

    fun startCountdownTimer(seconds: Int, title: String = "Timer") {
        timerTickJob?.cancel()
        activeTimerEvent = IslandEvent.CountdownTimer(
            title = title,
            remainingSeconds = seconds,
            totalSeconds = seconds,
            isRunning = true
        )
        reevaluateCurrentEvent()

        timerTickJob = scope.launch {
            var rem = seconds
            while (rem > 0 && activeTimerEvent?.isRunning == true) {
                delay(1000)
                rem--
                activeTimerEvent = activeTimerEvent?.copy(remainingSeconds = rem)
                if (_currentEvent.value is IslandEvent.CountdownTimer) {
                    _currentEvent.value = activeTimerEvent
                }
            }
            if (rem <= 0) {
                delay(2000)
                activeTimerEvent = null
                reevaluateCurrentEvent()
            }
        }
    }

    fun stopCountdownTimer() {
        timerTickJob?.cancel()
        activeTimerEvent = null
        reevaluateCurrentEvent()
    }

    fun postNavigationEvent(event: IslandEvent.NavigationTurn) {
        _currentEvent.value = event
        _visualState.value = IslandVisualState.COLLAPSED
        scheduleAutoDismiss(8)
    }

    fun dismissCurrentEvent() {
        autoDismissJob?.cancel()
        val current = _currentEvent.value
        when (current) {
            is IslandEvent.MessageNotification -> activeNotificationEvent = null
            is IslandEvent.BatteryStatus -> activeBatteryEvent = null
            is IslandEvent.CountdownTimer -> stopCountdownTimer()
            is IslandEvent.MediaPlayback -> activeMediaEvent = null
            else -> {}
        }
        reevaluateCurrentEvent()
    }

    private fun reevaluateCurrentEvent() {
        // Evaluate event priorities:
        // 1. New Message Notification (Highest active momentary priority)
        // 2. Dynamic Countdown Timer
        // 3. Media Playback (if active)
        // 4. Battery alert (momentary)
        val selected: IslandEvent? = when {
            activeNotificationEvent != null -> activeNotificationEvent
            activeTimerEvent != null -> activeTimerEvent
            activeBatteryEvent != null -> activeBatteryEvent
            activeMediaEvent != null -> activeMediaEvent
            else -> null
        }
        _currentEvent.value = selected
        if (selected != null && _visualState.value == IslandVisualState.HIDDEN) {
            _visualState.value = IslandVisualState.COLLAPSED
        }
    }

    private fun scheduleAutoDismiss(seconds: Int) {
        autoDismissJob?.cancel()
        if (seconds <= 0) return
        autoDismissJob = scope.launch {
            delay(seconds * 1000L)
            // If current is message or battery, collapse or dismiss
            if (_visualState.value == IslandVisualState.EXPANDED) {
                _visualState.value = IslandVisualState.COLLAPSED
                delay(2000)
            }
            if (_currentEvent.value is IslandEvent.MessageNotification || _currentEvent.value is IslandEvent.BatteryStatus) {
                dismissCurrentEvent()
            }
        }
    }

    // Quick Reply Action Execution
    fun sendDirectReply(context: Context, replyAction: DirectReplyAction, replyText: String) {
        try {
            val intent = Intent()
            val bundle = Bundle()
            bundle.putCharSequence(replyAction.remoteInput.resultKey, replyText)
            
            val remoteInputs = arrayOf(
                androidx.core.app.RemoteInput.Builder(replyAction.remoteInput.resultKey)
                    .setLabel(replyAction.remoteInput.label)
                    .build()
            )
            androidx.core.app.RemoteInput.addResultsToIntent(remoteInputs, intent, bundle)
            replyAction.pendingIntent.send(context, 0, intent)
            Log.d(TAG, "Sent direct reply: $replyText")
            
            // Auto dismiss after sending reply
            dismissCurrentEvent()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending direct reply", e)
        }
    }
}
