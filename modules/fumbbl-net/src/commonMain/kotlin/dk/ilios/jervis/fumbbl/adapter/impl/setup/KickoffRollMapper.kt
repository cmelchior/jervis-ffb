package dk.ilios.jervis.fumbbl.adapter.impl.setup

import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.reports.KickoffResultReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.TheKickOffEvent

object KickoffRollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return (
            command .firstChangeId() == null &&
            command.reportList.reports.firstOrNull() is KickoffResultReport
        )
    }

    override fun mapServerCommand(
        fumbblGame: FumbblGame,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        // TODO PitchInvasion is handled separately, what about other results?
        val report: KickoffResultReport = command.reportList.reports.first() as KickoffResultReport
        val roll = report.kickoffRoll
        newActions.add(
            DiceResults(roll.first().d6, roll.last().d6),
            TheKickOffEvent.RollForKickOffEvent,
        )
    }
}
