package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.common.procedures.RecoverKnockedOutPlayersContext
import com.jervisffb.engine.common.procedures.RecoverPlayerRoll
import com.jervisffb.ui.game.UiSnapshotAccumulator

/**
 * Highlight the Knocked Out player that we are rolling to recover.
 *
 * We are doing it this way, because it is bit tricky to get the Action Wheel
 * over a player in the dogouts and using the "pointer" on the Action Wheel
 * makes it a bit messy.
 *
 * So until a better solution comes up, we instead highlight the player in the
 * dogout and then roll the dice in the center of the Teams half. Similar to
 * other generic team rolls (like Brilliant Coaching).
 */
object RecoverPlayerStatusIndicator: PitchStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        if (node == RecoverPlayerRoll.RollDie) {
            val player = state.getContext<RecoverKnockedOutPlayersContext>().selectedPlayer!!
            acc.updatePlayer(player.id) {
                it.copy(isHighlighted = true)
            }
        }
    }
}
