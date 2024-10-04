package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.model.Player

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
