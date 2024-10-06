package com.jervisffb.fumbbl.net.adapter.impl.setup

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.ext.d6
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.model.reports.KickoffResultReport
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOffEvent

object KickoffRollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
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
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        // TODO PitchInvasion is handled separately, what about other results?
        val report: KickoffResultReport = command.reportList.reports.first() as KickoffResultReport
        val roll = report.kickoffRoll
        newActions.add(
            DiceRollResults(roll.first().d6, roll.last().d6),
            TheKickOffEvent.RollForKickOffEvent,
        )
    }
}
