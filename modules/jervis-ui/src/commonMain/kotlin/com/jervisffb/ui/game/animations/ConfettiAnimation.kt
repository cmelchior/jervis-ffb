package com.jervisffb.ui.game.animations

import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.ui.game.viewmodel.PitchViewModel
import kotlin.time.Duration.Companion.milliseconds

class ConfettiAnimation(
    val rules: Rules,
    val homeTeamScored: Boolean,
) : JervisAnimation {

    val particleCount = 80
    val shotCount = 3
    val shotDelay = 500.milliseconds // Delay between each shot
    val burstDuration = 1750.milliseconds
    val duration = burstDuration + shotDelay * (shotCount - 1) // Total duration for all shots to finish
    val colors = listOf(
        0xFFE53935L,
        0xFFFDD835L,
        0xFF43A047L,
        0xFF1E88E5L,
        0xFF00ACC1L,
        0xFFAB47BCL
    )

    // List of available coordinates. We will cycle through them if shotCount exceeds the list size.
    val shotCoords: List<PitchCoordinate> = buildList {
        when (homeTeamScored) {
            false -> {
                add(PitchCoordinate(0, rules.pitchHeight - 1))
                add(PitchCoordinate(0, rules.pitchHeight - 2))
                add(PitchCoordinate(1, rules.pitchHeight - 1))
            }
            true -> {
                add(PitchCoordinate(rules.pitchWidth - 1, rules.pitchHeight - 1))
                add(PitchCoordinate(rules.pitchWidth - 1, rules.pitchHeight - 2))
                add(PitchCoordinate(rules.pitchWidth - 2, rules.pitchHeight - 1))
            }
        }
    }

    fun getShotCoords(index: Int, vm: PitchViewModel): Pair<Float, Float> {
        val coordinate = shotCoords[index % shotCoords.size]
        val sq = vm.squareOffsets[coordinate]
        if (coordinate.x < rules.pitchWidth / 2) {
            // Bottom-left corner when on the Home side
            val cx = sq?.let { it.positionInRoot.x - vm.pitchCoordinates.positionInRoot.x } ?: 0f
            val cy = sq?.let { it.positionInRoot.y + vm.sharedPitchData.size.squareSize.height - vm.pitchCoordinates.positionInRoot.y } ?: 0f
            return cx to cy
        } else {
            // Bottom-right corner when on the Away side
            val cx = sq?.let { it.positionInRoot.x + vm.sharedPitchData.size.squareSize.width - vm.pitchCoordinates.positionInRoot.x } ?: 0f
            val cy = sq?.let { it.positionInRoot.y + vm.sharedPitchData.size.squareSize.height - vm.pitchCoordinates.positionInRoot.y } ?: 0f
            return cx to cy
        }
    }
}
