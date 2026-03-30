package com.jervisffb.ui.game.animations

import com.jervisffb.engine.model.Team

class FanFactorResultAnimation(
    val homeFairWeatherRoll: Int,
    val awayFairWeatherRoll: Int,
    val homeTeam: Team,
    val awayTeam: Team,
) : JervisAnimation {

    val teamFadeInDurationMillis: Int = 400
    val valueTranslateDurationMillis: Int = 400
    val valueFadeDurationMillis: Int = 200
    val fadeOutDelayMills: Int = 1_800

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
