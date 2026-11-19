package com.jervisffb.ui.game.animations

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.ui.game.UiGameController

/**
 * Briefly highlights a selected or rolled direction before the engine advances.
 */
class DirectionSelectedAnimation(
    override val uiController: UiGameController,
    val coordinate: PitchCoordinate,
    val direction: Direction,
    // Remove the previous result's static arrow when showing a re-rolled direction.
    val previousCoordinate: PitchCoordinate? = null,
) : JervisAnimation {
    val fadeDurationMillis: Int = uiController.scaledAnimationMs(200)
    val selectedDurationMillis: Int = uiController.scaledAnimationMs(300)
    val durationMillis: Int = fadeDurationMillis + selectedDurationMillis
}
