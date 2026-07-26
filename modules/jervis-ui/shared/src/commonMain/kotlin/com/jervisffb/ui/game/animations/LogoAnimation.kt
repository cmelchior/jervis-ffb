package com.jervisffb.ui.game.animations

import com.jervisffb.ui.game.UiGameController

class LogoAnimation(override val uiController: UiGameController): JervisAnimation {
    val durationMillis = uiController.scaledAnimationMs(500)
}
