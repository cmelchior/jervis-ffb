//package com.jervisffb.fumbbl.net.adapter.impl.block
//
//import com.jervisffb.actions.DBlockResult
//import com.jervisffb.actions.DiceRollResults
//import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
//import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
//import com.jervisffb.fumbbl.net.adapter.add
//import com.jervisffb.fumbbl.net.model.PlayerAction
//import com.jervisffb.fumbbl.net.model.reports.BlockRollReport
//import com.jervisffb.fumbbl.net.model.reports.ReRollReport
//import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
//import com.jervisffb.fumbbl.net.utils.FumbblGame
//import com.jervisffb.model.Game
//import com.jervisffb.rules.bb2020.procedures.actions.block.standard.StandardBlockRerollDice
//
//object BlockReRollMapper: CommandActionMapper {
//    override fun isApplicable(
//        game: FumbblGame,
//        command: ServerCommandModelSync,
//        processedCommands: MutableList<ServerCommandModelSync>
//    ): Boolean {
//        return (
//            game.actingPlayer.playerAction == PlayerAction.BLOCK &&
//            command.reportList.lastOrNull() is BlockRollReport &&
//            command.reportList.firstOrNull() is ReRollReport &&
//            command.sound == "block"
//        )
//    }
//
//    override fun mapServerCommand(
//        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
//        jervisGame: Game,
//        command: ServerCommandModelSync,
//        processedCommands: MutableList<ServerCommandModelSync>,
//        jervisCommands: List<JervisActionHolder>,
//        newActions: MutableList<JervisActionHolder>
//    ) {
//        val rollReport = command.reportList.last() as BlockRollReport
//        val diceRoll = rollReport.blockRoll.map { DBlockResult(it) }
//        newActions.add(DiceRollResults(diceRoll), StandardBlockRerollDice.ReRollDie)
//    }
//}
