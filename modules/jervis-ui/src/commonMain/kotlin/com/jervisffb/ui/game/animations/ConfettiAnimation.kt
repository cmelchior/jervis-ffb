package com.jervisffb.ui.game.animations

import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.ui.game.viewmodel.FieldViewModel
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
    val shotCoords: List<FieldCoordinate> = buildList {
        when (homeTeamScored) {
            false -> {
                add(FieldCoordinate(0, rules.fieldHeight - 1))
                add(FieldCoordinate(0, rules.fieldHeight - 2))
                add(FieldCoordinate(1, rules.fieldHeight - 1))
            }
            true -> {
                add(FieldCoordinate(rules.fieldWidth - 1, rules.fieldHeight - 1))
                add(FieldCoordinate(rules.fieldWidth - 1, rules.fieldHeight - 2))
                add(FieldCoordinate(rules.fieldWidth - 2, rules.fieldHeight - 1))
            }
        }
    }

    fun getShotCoords(index: Int, vm: FieldViewModel): Pair<Float, Float> {
        val coordinate = shotCoords[index % shotCoords.size]
        val sq = vm.squareOffsets[coordinate]
        if (coordinate.x < rules.fieldWidth / 2) {
            // Bottom-left corner when on the Home side
            val cx = sq?.let { it.positionInRoot.x - vm.fieldCoordinates.positionInRoot.x } ?: 0f
            val cy = sq?.let { it.positionInRoot.y + vm.sharedFieldData.size.squareSize.height - vm.fieldCoordinates.positionInRoot.y } ?: 0f
            return cx to cy
        } else {
            // Bottom-right corner when on the Away side
            val cx = sq?.let { it.positionInRoot.x + vm.sharedFieldData.size.squareSize.width - vm.fieldCoordinates.positionInRoot.x } ?: 0f
            val cy = sq?.let { it.positionInRoot.y + vm.sharedFieldData.size.squareSize.height - vm.fieldCoordinates.positionInRoot.y } ?: 0f
            return cx to cy
        }
    }
}
