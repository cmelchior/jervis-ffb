package com.jervisffb.fumbbl.net.adapter.impl.blitz

import com.jervisffb.engine.actions.BlockDice
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
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.Stumble
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseReroll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult

object BlitzChooseBlockResultMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            game.actingPlayer.playerAction == PlayerAction.BLITZ &&
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

        // From the logs we cannot detect when the user stops rerolling things.
        // We only see that they finally choose a result. This is problematic because
        // Jervis has a "NoRerollSelected" event to make that transition.
        // But if no rerolls are available, this event is just skipped.
        // Making it optional here _should_ cover all the cases.
        newActions.addOptional(NoRerollSelected(), StandardBlockChooseReroll.ReRollSourceOrAcceptRoll)

        // There isn't an easy way to figure out exactly which die PUSHBACK
        // points to when selected (i.e. if you rolled 3 and 4). For now,
        // we just find the first matching die and hope it works.
        // TODO This is shared with BlockChooseBlockResultsMapper
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

        // TODO What does FUMBBL do exactly in the case of Blocking and using Block/Wrestle
        if (report.blockResult == com.jervisffb.fumbbl.net.model.BlockResult.PUSHBACK) {

        }

        if (report.blockResult == POW_PUSHBACK) {
            // Always use Tackle (I think there are a few cases where Fumbbl will ask for this)
            // Fix this when we encounter them
            if (fumbblGame.getPlayerById(fumbblGame.actingPlayer.playerId!!.id)?.skillArray?.contains("Tackle") == true) {
                newActions.add(Confirm, Stumble.ChooseToUseTackle)
            }
        }

        if (selectedBlockDie.blockResult == BlockDice.BOTH_DOWN) {
            if (fumbblGame.getPlayerById(fumbblGame.actingPlayer.playerId!!.id)?.skillArray?.contains("Block") == true) {
                newActions.add(Confirm, BothDown.AttackerChooseToUseBlock)
            }
            if (fumbblGame.getPlayerById(report.defenderId.id)?.skillArray?.contains("Block") == true) {
                newActions.add(Confirm, BothDown.DefenderChooseToUseBlock)
            }
        }
    }
}
