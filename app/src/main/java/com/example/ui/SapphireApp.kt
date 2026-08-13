package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.entity.IslandSettingsEntity
import com.example.domain.model.IslandThemePreset
import com.example.domain.model.IslandVisualState
import com.example.ui.components.DynamicIslandView
import com.example.ui.screens.AppFilterScreen
import com.example.ui.screens.CalibrationScreen
import com.example.ui.screens.CustomizationScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NotificationHistoryScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.viewmodel.IslandViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    data object Calibration : Screen("calibration", "Calibrate", Icons.Default.Tune)
    data object Customization : Screen("customization", "Themes", Icons.Default.Palette)
    data object AppFilters : Screen("filters", "Filters", Icons.Default.Apps)
    data object History : Screen("history", "History", Icons.Default.History)
    data object Permissions : Screen("permissions", "Access", Icons.Default.Security)
}

val navScreens = listOf(
    Screen.Dashboard,
    Screen.Calibration,
    Screen.Customization,
    Screen.AppFilters,
    Screen.History,
    Screen.Permissions
)

@Composable
fun SapphireApp(
    viewModel: IslandViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val settings by viewModel.settingsState.collectAsState()
    val currentSettings = settings ?: IslandSettingsEntity()
    val theme = IslandThemePreset.fromString(currentSettings.themePreset)
    val event by viewModel.currentEvent.collectAsState()
    val visualState by viewModel.visualState.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = Color(0xFF0D0D0D),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF000000),
                contentColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = Color(0x1AFFFFFF),
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                )
            ) {
                navScreens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = theme.primaryColor,
                            indicatorColor = theme.primaryColor.copy(alpha = 0.2f),
                            unselectedIconColor = Color(0xFF71717A),
                            unselectedTextColor = Color(0xFF71717A)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToCalibration = { navController.navigate(Screen.Calibration.route) },
                        onNavigateToCustomization = { navController.navigate(Screen.Customization.route) },
                        onNavigateToAppFilters = { navController.navigate(Screen.AppFilters.route) },
                        onNavigateToHistory = { navController.navigate(Screen.History.route) },
                        onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) }
                    )
                }
                composable(Screen.Calibration.route) {
                    CalibrationScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Customization.route) {
                    CustomizationScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.AppFilters.route) {
                    AppFilterScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.History.route) {
                    NotificationHistoryScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Permissions.route) {
                    PermissionsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // TOP-LEVEL FLOATING DYNAMIC NOTCH ISLAND
            if (currentSettings.isEnabled && visualState != IslandVisualState.HIDDEN) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.TopCenter)
                        .padding(
                            top = (currentSettings.yOffsetPx / 2f).dp.coerceAtLeast(6.dp),
                            start = if (currentSettings.cutoutPosition == "LEFT") 16.dp else 12.dp,
                            end = if (currentSettings.cutoutPosition == "RIGHT") 16.dp else 12.dp
                        ),
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
                        onDismiss = { viewModel.dismissCurrentIsland() }
                    )
                }
            }
        }
    }
}
