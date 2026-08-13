package com.example.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.manager.IslandStateManager

class SapphireAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "SapphireAccessibility"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        IslandStateManager.setAccessibilityConnected(true)
        Log.d(TAG, "Sapphire Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Accessibility events can be used for contextual gesture detection
    }

    override fun onInterrupt() {
        IslandStateManager.setAccessibilityConnected(false)
        Log.d(TAG, "Sapphire Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        IslandStateManager.setAccessibilityConnected(false)
    }
}
