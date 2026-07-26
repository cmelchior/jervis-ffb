package com.jervisffb.ui.game.animations

import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiGameController

class FanFactorResultAnimation(
    override val uiController: UiGameController,
    val homeFairWeatherRoll: Int,
    val awayFairWeatherRoll: Int,
    val homeTeam: Team,
    val awayTeam: Team,
) : JervisAnimation {

    val teamFadeInDurationMillis: Int = uiController.scaledAnimationMs(400)
    val valueTranslateDurationMillis: Int = uiController.scaledAnimationMs(400)
    val valueFadeDurationMillis: Int = uiController.scaledAnimationMs(200)
    val fadeOutDelayMills: Int = uiController.scaledAnimationMs(1800)

    val totalHomeFans: String
    val totalAwayFans: String

    init {
        val homeFans = (homeFairWeatherRoll + homeTeam.dedicatedFans) * 1_000
        val awayFans = (awayFairWeatherRoll + awayTeam.dedicatedFans)  * 1_000
        totalHomeFans = "${format(homeFans)} Fans"
        totalAwayFans = "${format(awayFans)} Fans"
    }

    private fun format(value: Int, separator: String = "."): String {
        return value.toString()
            .reversed()
            .chunked(3)
            .joinToString(separator)
            .reversed()
    }
}
