package dk.ilios.jervis.fumbbl.adapter.impl.block

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
import dk.ilios.jervis.procedures.actions.block.BlockAction
import dk.ilios.jervis.procedures.actions.block.BlockRoll

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
            command.sound == "block"
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
        val blockReport = command.firstReport() as BlockReport
        val rollReport = command.reportList.last() as BlockRollReport
        val diceRoll = rollReport.blockRoll.map { DBlockResult(it) }
        // TODO Double check how Fumbbl map block dice values
        newActions.add({ state, rules ->
            PlayerSelected(blockReport.defenderId.toJervisId())
       }, BlockAction.SelectDefenderOrEndAction)
        newActions.add(DiceResults(diceRoll), BlockRoll.RollDice)
    }
}
