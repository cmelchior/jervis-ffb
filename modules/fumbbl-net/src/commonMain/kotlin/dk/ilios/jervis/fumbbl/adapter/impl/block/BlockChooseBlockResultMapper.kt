package dk.ilios.jervis.fumbbl.adapter.impl.block

import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DicePoolChoice
import dk.ilios.jervis.actions.DicePoolResultsSelected
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.adapter.addOptional
import dk.ilios.jervis.fumbbl.model.BlockResult.BOTH_DOWN
import dk.ilios.jervis.fumbbl.model.BlockResult.POW
import dk.ilios.jervis.fumbbl.model.BlockResult.POW_PUSHBACK
import dk.ilios.jervis.fumbbl.model.BlockResult.PUSHBACK
import dk.ilios.jervis.fumbbl.model.BlockResult.SKULL
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.reports.BlockChoiceReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.block.BothDown
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockChooseReroll
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockChooseResult

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
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
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
