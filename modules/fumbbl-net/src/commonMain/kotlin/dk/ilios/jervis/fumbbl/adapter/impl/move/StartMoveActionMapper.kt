package dk.ilios.jervis.fumbbl.adapter.impl.move

import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.reports.PlayerActionReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.procedures.ActivatePlayer
import dk.ilios.jervis.procedures.TeamTurn

object StartMoveActionMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        val firstReport = command.firstReport()
        return (
            firstReport is PlayerActionReport &&
            firstReport.playerAction == PlayerAction.MOVE
        )
    }

    override fun mapServerCommand(
        fumbblGame: FumbblGame,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.firstReport() as PlayerActionReport
        val movingPlayer = jervisGame.getPlayerById(PlayerId(report.actingPlayerId.id))
        newActions.add(PlayerSelected(movingPlayer.id), TeamTurn.SelectPlayerOrEndTurn)
        newActions.add(
            { state, rules -> PlayerActionSelected(rules.teamActions.move.type) },
            ActivatePlayer.DeclareActionOrDeselectPlayer
        )
    }
}
