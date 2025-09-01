package com.jervisffb.fumbbl.net.adapter.impl.block

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRollDice
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.reports.BlockReport
import com.jervisffb.fumbbl.net.model.reports.BlockRollReport
import com.jervisffb.fumbbl.net.model.reports.ReRollReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object BlockRollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            game.actingPlayer.playerAction == PlayerAction.BLOCK &&
                command.firstReport() is BlockReport &&
                command.reportList.last() is BlockRollReport &&
                command.reportList.first() !is ReRollReport &&
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
        val diceRoll = rollReport.blockRoll.map { DBlockResult(it) }
        newActions.add(BlockTypeSelected(BlockType.STANDARD), BlockAction.SelectBlockType)
        newActions.add(DiceRollResults(diceRoll), StandardBlockRollDice.RollDice)
    }
}
