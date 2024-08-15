package dk.ilios.jervis.fumbbl.adapter.impl

import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.reports.ScatterBallReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.Bounce
import dk.ilios.jervis.rules.tables.RandomDirectionTemplate

object BounceBallMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return command.reportList.firstOrNull() is ScatterBallReport && command.sound == "bounce"
    }

    override fun mapServerCommand(
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val direction = (command.reportList.first() as ScatterBallReport).directionArray.first().transformToJervisDirection()
        val roll = RandomDirectionTemplate.getRollForDirection(direction)
        newActions.add(DiceResults(roll), Bounce.RollDirection)
    }
}
