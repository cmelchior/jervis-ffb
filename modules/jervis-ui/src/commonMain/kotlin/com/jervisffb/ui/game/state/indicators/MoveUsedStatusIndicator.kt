package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiSnapshotAccumulator

/**
 * Numbers on the squares indicating how many move steps were used to reach
 * that step. The starting square is counted as "0".
 */
object MoveUsedStatusIndicator: FieldStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        acc.getAllMoveUsed().forEach { (coordinate, moveUsed) ->
            acc.updateSquare(coordinate) {
                it.copy(moveUsed = moveUsed)
            }
        }
    }
}
