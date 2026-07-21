package com.jervisffb.engine.common.reports

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportCatch(player: Player, target: Int, modifiers: List<DiceModifier>, result: D6Result, success: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String =
        if (success) {
            "${player.name} caught the ball [${result.value} + ${modifiers.fold(
                0,
            ) { acc, mod -> acc + mod.modifier }} >= $target]."
        } else {
            "${player.name} failed to catch the ball [${result.value} + ${modifiers.fold(
                0,
            ) { acc, mod -> acc + mod.modifier }} < $target]."
        }
}
