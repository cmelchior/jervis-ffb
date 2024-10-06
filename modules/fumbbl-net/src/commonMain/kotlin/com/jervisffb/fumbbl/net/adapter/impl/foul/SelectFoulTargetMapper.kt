package com.jervisffb.fumbbl.net.adapter.impl.foul

import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.model.reports.FoulReport
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FumbblFoulAction

object SelectFoulTargetMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return command.firstReport() is FoulReport
    }

    override fun mapServerCommand(
        fumbblGame: FumbblGame,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        // There is a bug in FUMBBL, so you do not have to select the Foul target
        // when star
        val report = command.firstReport() as FoulReport
        newActions.add(PlayerSelected(report.defenderId.toJervisId()), FumbblFoulAction.MoveOrFoulOrEndAction)
    }
}
