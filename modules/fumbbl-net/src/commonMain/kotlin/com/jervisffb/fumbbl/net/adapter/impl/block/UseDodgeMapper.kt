package com.jervisffb.fumbbl.net.adapter.impl.blitz

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.Stumble
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.reports.SkillUseReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

/**
 * Select to use Dodge when being blocked with Stumbl
 */
object UseDodgeMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        val report = command.firstReport()
        return if (report is SkillUseReport) {
            (report.skill == "Dodge" && report.used && report.skillUse == "avoidFalling")
        } else {
            return false
        }
    }

    override fun mapServerCommand(
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.firstReport() as SkillUseReport
        val dodgeUsed = report.used
        newActions.add(if (dodgeUsed) Confirm else Cancel, Stumble.ChooseToUseDodge)
    }
}
