package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.entity.IslandSettingsEntity
import com.example.domain.model.IslandEvent
import com.example.domain.model.IslandVisualState
import com.example.ui.components.DynamicIslandView
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun island_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                DynamicIslandView(
                    event = IslandEvent.MediaPlayback(
                        id = "test_1",
                        packageName = "com.spotify.music",
                        appName = "Spotify",
                        trackTitle = "Starboy",
                        artistName = "The Weeknd ft. Daft Punk",
                        isPlaying = true,
                        durationMs = 230000L,
                        currentPositionMs = 45000L
                    ),
                    visualState = IslandVisualState.EXPANDED,
                    settings = IslandSettingsEntity()
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
