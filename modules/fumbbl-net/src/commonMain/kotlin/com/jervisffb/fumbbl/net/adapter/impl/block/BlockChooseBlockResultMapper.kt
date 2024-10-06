package com.jervisffb.fumbbl.net.adapter.impl.block

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.adapter.addOptional
import com.jervisffb.fumbbl.net.model.BlockResult.BOTH_DOWN
import com.jervisffb.fumbbl.net.model.BlockResult.POW
import com.jervisffb.fumbbl.net.model.BlockResult.POW_PUSHBACK
import com.jervisffb.fumbbl.net.model.BlockResult.PUSHBACK
import com.jervisffb.fumbbl.net.model.BlockResult.SKULL
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.reports.BlockChoiceReport
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BothDown
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseReroll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult

object BlockChooseBlockResultMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            game.actingPlayer.playerAction == PlayerAction.BLOCK &&
            command.firstReport() is BlockChoiceReport
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
        val report = command.reportList.last() as BlockChoiceReport

        // From the logs we cannot detect when the user stops re-rolling things.
        // We only see that they finally choose a result. This is problematic because
        // Jervis has a "NoRerollSelected" event to make that transition.
        // But if no rerolls are available, this event is just skipped.
        // Making it optional here _should_ cover all the cases.
        newActions.addOptional(NoRerollSelected(), StandardBlockChooseReroll.ReRollSourceOrAcceptRoll)

        // There isn't an easy way to figure out exactly which die PUSHBACK
        // points to when selected (i.e. if you rolled 3 and 4). For now,
        // we just find the first matching die and hope it works.
        val selectedBlockDie = when (report.blockResult) {
            SKULL -> DBlockResult(1)
            BOTH_DOWN -> DBlockResult(2)
            PUSHBACK -> {
                if (report.blockRoll.contains(3)) {
                    DBlockResult(3)
                } else {
                    DBlockResult(4)
                }
            }
            POW_PUSHBACK -> DBlockResult(5)
            POW -> DBlockResult(6)
        }
        val action = DicePoolResultsSelected(listOf(DicePoolChoice(id = 0, diceSelected = listOf(selectedBlockDie))))
        newActions.add(action, StandardBlockChooseResult.SelectBlockResult)

        // Automatically use block
        val attacker = fumbblGame.getPlayerById(fumbblGame.actingPlayer.playerId!!.id)!!
        val defender = fumbblGame.getPlayerById(report.defenderId.id)!!
        if (report.blockResult == BOTH_DOWN) {
            if (attacker.skillArray.contains("Block")) {
                newActions.add(Confirm, BothDown.AttackerChooseToUseBlock)
            }
            if (defender.skillArray.contains("Block")) {
                newActions.add(Confirm, BothDown.AttackerChooseToUseBlock)
            }
        }
    }
}
