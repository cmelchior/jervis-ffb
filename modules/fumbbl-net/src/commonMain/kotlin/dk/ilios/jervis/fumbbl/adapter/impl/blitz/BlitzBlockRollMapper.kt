package dk.ilios.jervis.fumbbl.adapter.impl.blitz

import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.reports.BlockReport
import dk.ilios.jervis.fumbbl.model.reports.BlockRollReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.actions.blitz.BlitzAction
import dk.ilios.jervis.procedures.actions.blitz.BlitzContext
import dk.ilios.jervis.procedures.actions.block.BlockRoll

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
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.reportList.last() as BlockRollReport
        val diceRoll = report.blockRoll.map { DBlockResult(it) }
        // TODO Double check how Fumbbl map block dice values
        newActions.add({ state, rules ->
            val context = jervisGame.getContext<BlitzContext>()
            PlayerSelected(context.defender!!.id)
       }, BlitzAction.MoveOrBlockOrEndAction)
        newActions.add(DiceResults(diceRoll), BlockRoll.RollDice)
    }
}
