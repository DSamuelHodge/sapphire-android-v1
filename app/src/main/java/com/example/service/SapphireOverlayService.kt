package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.repository.IslandRepository
import com.example.domain.model.IslandVisualState
import com.example.manager.IslandStateManager
import com.example.ui.components.FloatingDynamicIslandOverlay
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SapphireOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var windowManager: WindowManager? = null
    private var overlayComposeView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private lateinit var repository: IslandRepository

    companion object {
        private const val TAG = "SapphireOverlayService"
        private const val NOTIFICATION_CHANNEL_ID = "sapphire_island_channel"
        private const val NOTIFICATION_ID = 9001

        fun start(context: Context) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, SapphireOverlayService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SapphireOverlayService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        val db = AppDatabase.getInstance(applicationContext)
        repository = IslandRepository(db)

        startForegroundNotification()
        createFloatingOverlay()
        observeState()
        IslandStateManager.setOverlayRunning(true)
    }

    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Sapphire Dynamic Island",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the dynamic notch overlay active on your device"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Sapphire Island Active")
            .setContentText("Dynamic notification overlay is active")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createFloatingOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Cannot draw overlays: Permission not granted")
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 12
        }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@SapphireOverlayService)
            setViewTreeSavedStateRegistryOwner(this@SapphireOverlayService)
            setContent {
                MyApplicationTheme {
                    FloatingDynamicIslandOverlay(
                        repository = repository,
                        onDismiss = {
                            IslandStateManager.dismissCurrentEvent()
                        }
                    )
                }
            }
        }

        overlayComposeView = composeView

        try {
            windowManager?.addView(composeView, layoutParams)
            Log.d(TAG, "Floating Overlay added to WindowManager")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay view", e)
        }
    }

    private fun observeState() {
        serviceScope.launch {
            repository.settingsFlow.collectLatest { settings ->
                if (settings != null && layoutParams != null && overlayComposeView != null) {
                    layoutParams?.let { params ->
                        params.x = settings.xOffsetPx
                        params.y = settings.yOffsetPx.coerceAtLeast(0)
                        val grav = when (settings.cutoutPosition) {
                            "LEFT" -> Gravity.TOP or Gravity.START
                            "RIGHT" -> Gravity.TOP or Gravity.END
                            else -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
                        }
                        params.gravity = grav
                        try {
                            windowManager?.updateViewLayout(overlayComposeView, params)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error updating window layout", e)
                        }
                    }
                }
            }
        }

        serviceScope.launch {
            IslandStateManager.visualState.collectLatest { state ->
                layoutParams?.let { params ->
                    // Adjust focusable flag if user is typing in quick reply
                    if (state == IslandVisualState.EXPANDED) {
                        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    } else {
                        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    }
                    try {
                        if (overlayComposeView != null) {
                            windowManager?.updateViewLayout(overlayComposeView, params)
                        }
                    } catch (e: Exception) {
                        // ignore transient update race
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        IslandStateManager.setOverlayRunning(false)

        try {
            if (overlayComposeView != null && windowManager != null) {
                windowManager?.removeView(overlayComposeView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing overlay view", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
