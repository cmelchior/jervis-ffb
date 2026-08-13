package com.jervisffb.ui.game

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.locations.PitchCoordinate

/**
 * Provides persistent, visual focus derived from the current game state but
 * not directly related to the game state.
 *
 * Examples adding focus tutorials, guides, or challenges.
 */
fun interface UiFocusProvider {
    fun getFocus(state: Game): UiFocus
}

/**
 * Data wrapper representing all currently selected focus areas.
 */
data class UiFocus(
    val players: Map<PlayerId, UiFocusStyle> = emptyMap(),
    val squares: Map<PitchCoordinate, UiFocusStyle> = emptyMap(),
) {
    companion object {
        val NONE = UiFocus()
    }
}

/**
 * List different focus styles.
 * Unclear if this is really needed, but it sets us up for future tutorials or
 * other guides.
 */
enum class UiFocusStyle {
    CHALLENGE_TARGET,
}
