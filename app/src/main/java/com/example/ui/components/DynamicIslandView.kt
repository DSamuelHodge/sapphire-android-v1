package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.IslandSettingsEntity
import com.example.domain.model.IslandEvent
import com.example.domain.model.IslandThemePreset
import com.example.domain.model.IslandVisualState
import com.example.manager.IslandStateManager
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynamicIslandView(
    event: IslandEvent?,
    visualState: IslandVisualState,
    settings: IslandSettingsEntity,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val theme = IslandThemePreset.fromString(settings.themePreset)

    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateDpAsState(
        targetValue = offsetX.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "swipe_offset"
    )

    val draggableState = rememberDraggableState { delta ->
        offsetX += delta
        if (offsetX > 150f || offsetX < -150f) {
            onDismiss()
            offsetX = 0f
        }
    }

    Box(
        modifier = modifier
            .offset { IntOffset(animatedOffsetX.value.roundToInt(), 0) }
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStopped = { offsetX = 0f }
            )
            .testTag("dynamic_island_root"),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedContent(
            targetState = visualState,
            transitionSpec = {
                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
            },
            label = "island_state_transition"
        ) { targetState ->
            when (targetState) {
                IslandVisualState.EXPANDED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        when (event) {
                            is IslandEvent.MediaPlayback -> {
                                ExpandedMediaCard(
                                    event = event,
                                    theme = theme
                                )
                            }
                            is IslandEvent.MessageNotification -> {
                                ExpandedNotificationCard(
                                    event = event,
                                    theme = theme,
                                    onDismiss = onDismiss
                                )
                            }
                            is IslandEvent.BatteryStatus -> {
                                ExpandedBatteryCard(
                                    event = event,
                                    theme = theme
                                )
                            }
                            is IslandEvent.CountdownTimer -> {
                                ExpandedTimerCard(
                                    event = event,
                                    theme = theme
                                )
                            }
                            is IslandEvent.NavigationTurn -> {
                                ExpandedNotificationCard(
                                    event = IslandEvent.MessageNotification(
                                        id = event.id,
                                        key = event.id,
                                        packageName = "com.google.android.apps.maps",
                                        appName = "Navigation",
                                        title = event.maneuverInstruction,
                                        text = "${event.distanceText} • ${event.destination}"
                                    ),
                                    theme = theme,
                                    onDismiss = onDismiss
                                )
                            }
                            null -> {
                                // Default Empty expanded
                                CollapsedIslandPill(
                                    event = null,
                                    theme = theme,
                                    heightDp = settings.collapsedHeightDp,
                                    minWidthDp = settings.collapsedWidthDp,
                                    cutoutPosition = settings.cutoutPosition
                                )
                            }
                        }
                    }
                }
                IslandVisualState.COLLAPSED, IslandVisualState.HIDDEN -> {
                    Box(
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    if (settings.hapticFeedbackEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    if (settings.singleTapAction == "EXPAND" || event != null) {
                                        IslandStateManager.setVisualState(IslandVisualState.EXPANDED)
                                    } else {
                                        IslandStateManager.toggleExpandCollapse()
                                    }
                                },
                                onLongClick = {
                                    if (settings.hapticFeedbackEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    IslandStateManager.setVisualState(IslandVisualState.EXPANDED)
                                }
                            )
                            .testTag("island_collapsed_pill")
                    ) {
                        CollapsedIslandPill(
                            event = event,
                            theme = theme,
                            heightDp = settings.collapsedHeightDp,
                            minWidthDp = settings.collapsedWidthDp,
                            cutoutPosition = settings.cutoutPosition,
                            visualizerBarsCount = settings.visualizerBarsCount
                        )
                    }
                }
            }
        }
    }
}
