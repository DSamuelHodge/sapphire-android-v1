package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.IslandThemePreset
import com.example.ui.components.NotchCalibrationViewfinder
import com.example.ui.viewmodel.IslandViewModel

@Composable
fun CalibrationScreen(
    viewModel: IslandViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settingsState.collectAsState()
    val theme = IslandThemePreset.fromString(settings.themePreset)

    var xOffset by remember(settings.xOffsetPx) { mutableFloatStateOf(settings.xOffsetPx.toFloat()) }
    var yOffset by remember(settings.yOffsetPx) { mutableFloatStateOf(settings.yOffsetPx.toFloat()) }
    var widthDp by remember(settings.collapsedWidthDp) { mutableFloatStateOf(settings.collapsedWidthDp.toFloat()) }
    var heightDp by remember(settings.collapsedHeightDp) { mutableFloatStateOf(settings.collapsedHeightDp.toFloat()) }
    var radiusDp by remember(settings.cornerRadiusDp) { mutableFloatStateOf(settings.cornerRadiusDp.toFloat()) }
    var cutoutPos by remember(settings.cutoutPosition) { mutableStateOf(settings.cutoutPosition) }
    var autoCutout by remember(settings.isAutoCutoutEnabled) { mutableStateOf(settings.isAutoCutoutEnabled) }

    fun commitCalibration() {
        viewModel.updateCalibration(
            xOffset = xOffset.toInt(),
            yOffset = yOffset.toInt(),
            widthDp = widthDp.toInt(),
            heightDp = heightDp.toInt(),
            cornerRadiusDp = radiusDp.toInt(),
            cutoutPosition = cutoutPos,
            isAutoCutout = autoCutout
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER
        item {
            Spacer(modifier = Modifier.height(52.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Notch Calibration Studio",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Align the dynamic pill directly over your camera",
                        color = Color(0xFFA1A1AA),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // REAL-TIME NOTCH CALIBRATION VIEWFINDER
        item {
            NotchCalibrationViewfinder(
                xOffset = xOffset.toInt(),
                yOffset = yOffset.toInt(),
                widthDp = widthDp.toInt(),
                heightDp = heightDp.toInt(),
                cornerRadiusDp = radiusDp.toInt(),
                cutoutPosition = cutoutPos,
                theme = theme
            )
        }

        // CUTOUT POSITION SELECTOR
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "CAMERA CUTOUT LOCATION",
                        color = Color(0xFF71717A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PositionButton(
                            title = "Left Hole",
                            selected = cutoutPos == "LEFT",
                            themeColor = theme.primaryColor,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                cutoutPos = "LEFT"
                                xOffset = 0f
                                commitCalibration()
                            }
                        )
                        PositionButton(
                            title = "Center Notch",
                            selected = cutoutPos == "CENTER",
                            themeColor = theme.primaryColor,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                cutoutPos = "CENTER"
                                xOffset = 0f
                                commitCalibration()
                            }
                        )
                        PositionButton(
                            title = "Right Hole",
                            selected = cutoutPos == "RIGHT",
                            themeColor = theme.primaryColor,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                cutoutPos = "RIGHT"
                                xOffset = 0f
                                commitCalibration()
                            }
                        )
                    }
                }
            }
        }

        // FINE-TUNING SLIDERS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "DIMENSIONS & OFFSET TUNING",
                        color = Color(0xFF71717A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // Y Offset
                    SliderControl(
                        label = "Vertical Y Offset",
                        valueText = "${yOffset.toInt()} px",
                        value = yOffset,
                        valueRange = 0f..120f,
                        themeColor = theme.primaryColor,
                        onValueChange = {
                            yOffset = it
                            commitCalibration()
                        }
                    )

                    // X Offset
                    SliderControl(
                        label = "Horizontal X Offset",
                        valueText = "${xOffset.toInt()} px",
                        value = xOffset,
                        valueRange = -100f..100f,
                        themeColor = theme.primaryColor,
                        onValueChange = {
                            xOffset = it
                            commitCalibration()
                        }
                    )

                    // Collapsed Width
                    SliderControl(
                        label = "Collapsed Pill Width",
                        valueText = "${widthDp.toInt()} dp",
                        value = widthDp,
                        valueRange = 120f..300f,
                        themeColor = theme.primaryColor,
                        onValueChange = {
                            widthDp = it
                            commitCalibration()
                        }
                    )

                    // Collapsed Height
                    SliderControl(
                        label = "Collapsed Pill Height",
                        valueText = "${heightDp.toInt()} dp",
                        value = heightDp,
                        valueRange = 30f..54f,
                        themeColor = theme.primaryColor,
                        onValueChange = {
                            heightDp = it
                            commitCalibration()
                        }
                    )

                    // Corner Radius
                    SliderControl(
                        label = "Corner Radius",
                        valueText = "${radiusDp.toInt()} dp",
                        value = radiusDp,
                        valueRange = 10f..32f,
                        themeColor = theme.primaryColor,
                        onValueChange = {
                            radiusDp = it
                            commitCalibration()
                        }
                    )
                }
            }
        }

        // RESET & TEST ACTIONS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        xOffset = 0f
                        yOffset = 0f
                        widthDp = 180f
                        heightDp = 38f
                        radiusDp = 24f
                        cutoutPos = "CENTER"
                        commitCalibration()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Reset Defaults")
                }

                Button(
                    onClick = {
                        viewModel.triggerTestMessage("Calibration Test", "Notch alignment test popup")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primaryColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = "Test", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Test Notch")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PositionButton(
    title: String,
    selected: Boolean,
    themeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) themeColor else Color(0xFF1C1C1E))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (selected) Color.Black else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SliderControl(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    themeColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueText,
                color = themeColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = themeColor,
                inactiveTrackColor = Color(0xFF2C2C2E)
            )
        )
    }
}
