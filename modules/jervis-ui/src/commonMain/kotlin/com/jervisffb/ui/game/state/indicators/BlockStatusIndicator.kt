package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.MultipleBlockContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.ui.game.UiSnapshotAccumulator

/**
 * Show a small "block" indicator on the player is they are currently in a block
 * sequence.
 */
object BlockStatusIndicator: FieldStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        val blockContext = state.getContextOrNull<BlockContext>()
        val multipleBlockContext = state.getContextOrNull<MultipleBlockContext>()
        val players = mutableListOf<Player>()
        if (multipleBlockContext != null) {
            multipleBlockContext.defender1?.let { players.add(it) }
            multipleBlockContext.defender2?.let { players.add(it) }
        } else if (blockContext != null) {
            players.add(blockContext.defender)
        }

        players.forEach { player ->
            when (val loc = player.location) {
                DogOut -> { /* Do not show indicators in the Dogout */ }
                is GiantLocation -> TODO("Giant locations not supported yet")
                is FieldCoordinate -> {
                    acc.updateSquare(loc) {
                        it.copy(isBlocked = false)
                    }
                }
            }
        }
    }
}
