package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.Player

class ReportPickup(player: Player, target: Int, modifiers: List<DiceModifier>, result: D6Result, success: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String =
        if (success) {
            "${player.name} picked up the ball [${result.value} + ${modifiers.fold(
                0,
            ) { acc, mod -> acc + mod.modifier }} >= $target]."
        } else {
            "${player.name} failed to pickup the ball [${result.value} + ${modifiers.fold(
                0,
            ) { acc, mod -> acc + mod.modifier }} < $target]."
        }
}
