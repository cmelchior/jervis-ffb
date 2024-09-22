package dk.ilios.jervis.fumbbl.adapter.impl.blitz

import dk.ilios.jervis.actions.BlockDice
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.reports.BlockChoiceReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.block.BothDown
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockChooseReroll
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockChooseResult

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
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.reportList.last() as BlockChoiceReport
        // TODO Figure out how rerolls are represented
        val result = report.blockResult.toJervisResult()
        newActions.add(NoRerollSelected(), StandardBlockChooseReroll.ReRollSourceOrAcceptRoll)
        newActions.add(report.blockResult.toJervisResult(), StandardBlockChooseResult.SelectBlockResult)

        // TODO What does FUMBBL do exactly in the case of Blocking and using Block/Wrestle
        if (result.blockResult == BlockDice.BOTH_DOWN) {
            if (fumbblGame.getPlayerById(fumbblGame.actingPlayer.playerId!!.id)?.skillArray?.contains("Block") == true) {
                newActions.add(Confirm, BothDown.AttackerChooseToUseBlock)
            }
            if (fumbblGame.getPlayerById(report.defenderId.id)?.skillArray?.contains("Block") == true) {
                newActions.add(Confirm, BothDown.DefenderChooseToUseBlock)
            }
        }
    }
}
