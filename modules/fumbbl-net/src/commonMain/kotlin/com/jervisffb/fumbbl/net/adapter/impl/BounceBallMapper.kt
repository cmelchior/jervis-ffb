package com.jervisffb.fumbbl.net.adapter.impl

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.rules.bb2020.tables.RandomDirectionTemplate
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.reports.ScatterBallReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object BounceBallMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return command.reportList.firstOrNull() is ScatterBallReport && command.sound == "bounce"
    }

    override fun mapServerCommand(
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.firstReport() as ScatterBallReport
        val direction = report.directionArray.first().transformToJervisDirection()
        val roll = RandomDirectionTemplate.getRollForDirection(direction)
        newActions.add(DiceRollResults(roll), Bounce.RollDirection)
    }
}
