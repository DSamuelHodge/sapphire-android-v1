package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.IslandSettingsEntity
import com.example.data.repository.IslandRepository
import com.example.domain.model.IslandVisualState
import com.example.manager.IslandStateManager

@Composable
fun FloatingDynamicIslandOverlay(
    repository: IslandRepository,
    onDismiss: () -> Unit
) {
    val settings by repository.settingsFlow.collectAsState(initial = IslandSettingsEntity())
    val event by IslandStateManager.currentEvent.collectAsState()
    val visualState by IslandStateManager.visualState.collectAsState()

    val currentSettings = settings ?: IslandSettingsEntity()

    if (currentSettings.isEnabled && visualState != IslandVisualState.HIDDEN) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 4.dp),
            contentAlignment = when (currentSettings.cutoutPosition) {
                "LEFT" -> Alignment.TopStart
                "RIGHT" -> Alignment.TopEnd
                else -> Alignment.TopCenter
            }
        ) {
            DynamicIslandView(
                event = event,
                visualState = visualState,
                settings = currentSettings,
                onDismiss = onDismiss
            )
        }
    }
}
