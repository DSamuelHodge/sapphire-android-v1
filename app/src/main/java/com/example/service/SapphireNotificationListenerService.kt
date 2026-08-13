package com.example.service

import android.app.Notification
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.local.AppDatabase
import com.example.data.repository.IslandRepository
import com.example.domain.model.DirectReplyAction
import com.example.domain.model.IslandEvent
import com.example.domain.model.QuickActionItem
import com.example.manager.IslandStateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SapphireNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var repository: IslandRepository

    companion object {
        private const val TAG = "SapphireNotifService"
    }

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(applicationContext)
        repository = IslandRepository(db)
        Log.d(TAG, "Notification Listener created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        IslandStateManager.setNotificationServiceConnected(true)
        Log.d(TAG, "Notification Listener connected")
        checkActiveMediaAndNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        IslandStateManager.setNotificationServiceConnected(false)
        Log.d(TAG, "Notification Listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName
        if (pkg == packageName) return // Ignore self-notifications

        serviceScope.launch {
            val isAllowed = repository.isAppAllowed(pkg)
            if (!isAllowed) return@launch

            val notification = sbn.notification ?: return@launch
            val extras = notification.extras ?: return@launch

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: extras.getCharSequence(NotificationCompat.EXTRA_TITLE)?.toString()
                ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                ?: extras.getCharSequence(NotificationCompat.EXTRA_TEXT)?.toString()
                ?: ""
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

            // Skip empty notifications unless media
            val mediaSessionToken = extras.get(Notification.EXTRA_MEDIA_SESSION) as? MediaSession.Token

            if (mediaSessionToken != null || notification.category == Notification.CATEGORY_TRANSPORT) {
                handleMediaNotification(sbn, mediaSessionToken)
            } else if (title.isNotBlank() || text.isNotBlank()) {
                handleMessageNotification(sbn, title, text, subText)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return
        IslandStateManager.removeNotificationEvent(sbn.key)
    }

    private fun handleMessageNotification(
        sbn: StatusBarNotification,
        title: String,
        text: String,
        subText: String
    ) {
        val notification = sbn.notification
        val pm = packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        val appIconBitmap = try {
            val iconDrawable = pm.getApplicationIcon(sbn.packageName)
            (iconDrawable as? BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            null
        }

        // Look for Direct Reply Action & Quick Actions
        var directReplyAction: DirectReplyAction? = null
        val quickActions = mutableListOf<QuickActionItem>()

        notification.actions?.forEach { action ->
            action.remoteInputs?.forEach { remoteInput ->
                if (remoteInput.allowFreeFormInput && directReplyAction == null) {
                    directReplyAction = DirectReplyAction(
                        title = action.title ?: "Reply",
                        pendingIntent = action.actionIntent,
                        remoteInput = remoteInput
                    )
                }
            }
            if (action.actionIntent != null && action.title != null) {
                quickActions.add(
                    QuickActionItem(
                        title = action.title.toString(),
                        actionIntent = action.actionIntent
                    )
                )
            }
        }

        val event = IslandEvent.MessageNotification(
            id = sbn.key,
            key = sbn.key,
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            appIconBitmap = appIconBitmap,
            replyAction = directReplyAction,
            quickActions = quickActions.take(3),
            contentIntent = notification.contentIntent
        )

        IslandStateManager.postNotificationEvent(event)

        // Persist into database log
        serviceScope.launch {
            repository.recordNotification(
                packageName = sbn.packageName,
                appName = appName,
                title = title,
                text = text,
                subText = subText,
                eventType = "MESSAGE",
                hadQuickReply = directReplyAction != null
            )
        }
    }

    private fun handleMediaNotification(
        sbn: StatusBarNotification,
        token: MediaSession.Token?
    ) {
        val pm = packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        var trackTitle = sbn.notification.extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "Now Playing"
        var artistName = sbn.notification.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: appName
        var isPlaying = true
        var duration = 180000L
        var position = 0L
        var albumArt: Bitmap? = null

        if (token != null) {
            try {
                val controller = MediaController(this, token)
                val metadata = controller.metadata
                if (metadata != null) {
                    trackTitle = metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: trackTitle
                    artistName = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: artistName
                    duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).takeIf { it > 0 } ?: 180000L
                    albumArt = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                        ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
                }
                val state = controller.playbackState
                if (state != null) {
                    isPlaying = state.state == PlaybackState.STATE_PLAYING
                    position = state.position
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying MediaController", e)
            }
        }

        val event = IslandEvent.MediaPlayback(
            id = "media_${sbn.packageName}",
            packageName = sbn.packageName,
            appName = appName,
            trackTitle = trackTitle,
            artistName = artistName,
            albumArtBitmap = albumArt,
            isPlaying = isPlaying,
            durationMs = duration,
            currentPositionMs = position,
            canSeek = true
        )

        IslandStateManager.postMediaEvent(event)
    }

    private fun checkActiveMediaAndNotifications() {
        try {
            val activeNotifications = activeNotifications ?: return
            for (sbn in activeNotifications) {
                if (sbn.packageName == packageName) continue
                val token = sbn.notification?.extras?.get(Notification.EXTRA_MEDIA_SESSION) as? MediaSession.Token
                if (token != null || sbn.notification?.category == Notification.CATEGORY_TRANSPORT) {
                    handleMediaNotification(sbn, token)
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking active notifications", e)
        }
    }
}
