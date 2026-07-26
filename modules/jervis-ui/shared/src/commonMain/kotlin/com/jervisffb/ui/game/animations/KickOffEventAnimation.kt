package com.jervisffb.ui.game.animations

import com.jervisffb.ui.game.UiGameController
import org.jetbrains.compose.resources.DrawableResource

class KickOffEventAnimation(override val uiController: UiGameController, val image: DrawableResource): JervisAnimation {
    val duration = uiController.scaledAnimationMs(500)
}
