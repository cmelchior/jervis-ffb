package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.move.ScoringATouchDownContext

class ReportGoal(state: Game, context: ScoringATouchDownContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.player.name} scored a touchdown")
    }
}
