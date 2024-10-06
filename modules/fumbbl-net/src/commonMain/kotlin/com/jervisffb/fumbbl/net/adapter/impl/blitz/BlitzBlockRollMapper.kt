package com.jervisffb.fumbbl.net.adapter.impl.blitz

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.reports.BlockReport
import com.jervisffb.fumbbl.net.model.reports.BlockRollReport
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRollDice
import com.jervisffb.engine.rules.BlockType

object BlitzBlockRollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            game.actingPlayer.playerAction == PlayerAction.BLITZ &&
            command.firstReport() is BlockReport
            && command.sound == "block"
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
        val report = command.reportList.last() as BlockRollReport
        val diceRoll = report.blockRoll.map { DBlockResult(it) }
        newActions.add(BlockTypeSelected(BlockType.STANDARD), BlitzAction.SelectBlockType)
        newActions.add(DiceRollResults(diceRoll), StandardBlockRollDice.RollDice)
    }
}
