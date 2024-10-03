package dk.ilios.jervis.fumbbl.adapter.impl.blitz

import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.change.GameSetDefenderId
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.procedures.actions.blitz.BlitzAction
import dk.ilios.jervis.rules.Rules

object SelectBlitzDefenderMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            (game.actingPlayer.playerAction == PlayerAction.BLITZ || game.actingPlayer.playerAction == PlayerAction.BLITZ_MOVE) &&
            command.reportList.isEmpty() &&
            command.firstChangeId() == ModelChangeId.GAME_SET_DEFENDER_ID &&
            (command.modelChangeList.first() as GameSetDefenderId).key != null
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
        val defenderId = (command.modelChangeList.first() as GameSetDefenderId).key!!
        newActions.add(
            action = { _: Game, _: Rules -> PlayerSelected(PlayerId(defenderId)) },
            expectedNode = BlitzAction.MoveOrBlockOrEndAction
        )
    }
}
