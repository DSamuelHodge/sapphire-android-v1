package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.IslandEvent
import com.example.domain.model.IslandThemePreset
import com.example.domain.model.IslandVisualState
import com.example.manager.IslandStateManager

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandedNotificationCard(
    event: IslandEvent.MessageNotification,
    theme: IslandThemePreset,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    val context = LocalContext.current
    var isReplying by remember(event.id) { mutableStateOf(false) }
    var replyText by remember(event.id) { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Black)
            .border(
                width = 1.dp,
                color = theme.surfaceBorder,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // HEADER LINE: Source App Name + Timestamp ("Now") + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.appName.uppercase(),
                        color = Color(0xFF8E8E93),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• Now",
                        color = Color(0xFF636366),
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = {
                        IslandStateManager.setVisualState(IslandVisualState.COLLAPSED)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Collapse",
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BODY SECTION: Left Avatar + Title + Message snippet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            event.contentIntent?.send()
                            IslandStateManager.dismissCurrentEvent()
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                verticalAlignment = Alignment.Top
            ) {
                // Left Graphic: 40dp x 40dp
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(theme.surfaceColor)
                        .border(1.dp, theme.glowColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (event.largeIconBitmap != null) {
                        Image(
                            bitmap = event.largeIconBitmap.asImageBitmap(),
                            contentDescription = event.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (event.appIconBitmap != null) {
                        Image(
                            bitmap = event.appIconBitmap.asImageBitmap(),
                            contentDescription = event.appName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title.ifBlank { event.appName },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.text.ifBlank { event.subText },
                        color = Color(0xFFC7C7CC),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // INLINE QUICK REPLY INPUT BOX
            if (isReplying) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = {
                            Text(
                                text = "Reply to ${event.title.take(15)}...",
                                color = Color(0xFF8E8E93),
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = theme.primaryColor,
                            unfocusedBorderColor = Color(0xFF3A3A3C),
                            focusedContainerColor = Color(0xFF1C1C1E),
                            unfocusedContainerColor = Color(0xFF1C1C1E)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (replyText.isNotBlank()) {
                                event.replyAction?.let {
                                    IslandStateManager.sendDirectReply(context, it, replyText)
                                } ?: run {
                                    // Simulated dismissal if no remote input
                                    IslandStateManager.dismissCurrentEvent()
                                }
                            }
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_reply_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                event.replyAction?.let {
                                    IslandStateManager.sendDirectReply(context, it, replyText)
                                } ?: run {
                                    IslandStateManager.dismissCurrentEvent()
                                }
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(theme.primaryColor)
                            .testTag("send_reply_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Reply",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // ACTION FOOTER BUTTONS
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Reply Trigger Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFF2C2C2E))
                            .clickable { isReplying = true }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                            .testTag("reply_pill_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = "Reply",
                            tint = theme.primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reply",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Direct Notification Actions
                    event.quickActions.forEach { action ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(Color(0xFF2C2C2E))
                                .clickable {
                                    try {
                                        action.actionIntent?.send()
                                        IslandStateManager.dismissCurrentEvent()
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("action_pill_${action.title}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = action.title,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Dismiss Action
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .background(Color(0xFF1F1F21))
                            .clickable { onDismiss() }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dismiss",
                            color = Color(0xFF8E8E93),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
