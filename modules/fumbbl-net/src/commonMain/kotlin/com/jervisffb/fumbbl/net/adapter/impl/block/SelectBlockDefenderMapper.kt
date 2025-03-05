package com.jervisffb.fumbbl.net.adapter.impl.block

import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.change.GameSetDefenderId
import com.jervisffb.fumbbl.net.utils.FumbblGame

object SelectBlockDefenderMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            game.actingPlayer.playerAction == PlayerAction.BLOCK &&
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
            expectedNode = BlockAction.SelectDefenderOrEndAction
        )
    }
}
