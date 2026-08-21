package com.jervisffb.ui.game.animations

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.ui.game.UiGameController

/**
 * Fades a remotely selected pushback direction from its normal arrow to its
 * selected arrow, then keeps the selected arrow visible until the engine can
 * advance.
 */
class DirectionSelectedAnimation(
    override val uiController: UiGameController,
    val coordinate: PitchCoordinate,
    val direction: Direction,
) : JervisAnimation {
    val fadeDurationMillis: Int = uiController.scaledAnimationMs(200)
    val selectedDurationMillis: Int = uiController.scaledAnimationMs(300)
    val durationMillis: Int = fadeDurationMillis + selectedDurationMillis
}
