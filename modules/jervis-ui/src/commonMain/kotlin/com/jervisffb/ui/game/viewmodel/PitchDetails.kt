package com.jervisffb.ui.game.viewmodel

import androidx.compose.ui.graphics.Color
import com.jervisffb.ui.game.view.JervisTheme
import io.ktor.client.plugins.logging.DEFAULT

/**
 * The background color of the log panels. Can be used to make the text more readable on certain
 * backgrounds like Blizzard (where it otherwise ends up white on white).
 */
data class PanelBackground(
    val color: Color,
    val hoverColor: Color,
) {
    companion object {
        val DEFAULT = PanelBackground(JervisTheme.black.copy(alpha = 0.15f), JervisTheme.black.copy(alpha = 0.3f))
    }
}

/**
 * List of supported backgrounds for the pitch. They are slightly configurable
 * due to the impact a background can have on the visualal apperance.
 */
enum class PitchDetails(
    val resource: String,
    val description: String,
    // Draw lines, dots and end zone markers (if false, it is assumed they are part of the image)
    val drawPitchMarkers: Boolean,
    // Suggested background colors for the log panels
    val panelBackground: PanelBackground = PanelBackground.DEFAULT
) {
    // Use a custom pitch for now as we need something that can be used for both Standard and BB7
    // It would probably look better with custom pitch, but this should be fine for now.
    HEAT(
        "jervis/pitch/default/heat.png",
        "Sweltering Heat",
        true,
    ),
    SUNNY(
        "jervis/pitch/default/sunny.png",
        "Very Sunny",
        true
    ),
    NICE(
        "jervis/pitch/default/nice.png",
        "Perfect Conditions",
        true
    ),
    RAIN(
        "jervis/pitch/default/rain.png",
        "Pouring Rain",
        true
    ),
    BLIZZARD(
        "jervis/pitch/default/blizzard.png",
        "Blizzard",
        true,
        PanelBackground(JervisTheme.black.copy(0.2f), JervisTheme.black.copy(0.4f)),
    ),
}
