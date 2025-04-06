package com.jervisffb.fumbbl.net.adapter.impl.block

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseReroll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRerollDice
import com.jervisffb.engine.rules.bb2020.skills.DiceRerollOption
import com.jervisffb.engine.rules.bb2020.skills.RegularTeamReroll
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.reports.BlockRollReport
import com.jervisffb.fumbbl.net.model.reports.ReRollReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object BlockChooseRerollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            game.actingPlayer.playerAction == PlayerAction.BLOCK &&
                command.reportList.lastOrNull() is BlockRollReport &&
                command.reportList.firstOrNull() is ReRollReport &&
                (command.firstReport() as ReRollReport).reRollSource == "Team ReRoll" &&
                command.sound == "block"
        )
    }

    override fun mapServerCommand(
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val rollReport = command.reportList.last() as BlockRollReport
        val rerollReport = command.reportList.first() as ReRollReport
//        if (rerollReport.reRollSource != "Team ReRoll") {
//            throw IllegalStateException("Unexpected re-roll source: " + rerollReport.reRollSource)
//        }
        newActions.add(
            action = { state: Game, rules: Rules ->
                StandardBlockChooseReroll.ReRollSourceOrAcceptRoll.getAvailableActions(state, rules)
                    .first { it is SelectRerollOption }
                    .let { (it as SelectRerollOption).options.first { option -> option.getRerollSource(state) is RegularTeamReroll } }
                    .let { option ->
                        RerollOptionSelected(
                            DiceRerollOption(
                                option.rerollId,
                                option.dice
                            )
                        )
                    }
            },
            expectedNode = StandardBlockChooseReroll.ReRollSourceOrAcceptRoll
        )
        val diceRoll = rollReport.blockRoll.map { DBlockResult(it) }
//        newActions.add(BlockTypeSelected(BlockType.STANDARD), BlockAction.SelectBlockType)
        newActions.add(DiceRollResults(diceRoll), StandardBlockRerollDice.ReRollDie)
    }
}
