package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiGameSnapshot

/**
 * Numbers on the squares indicating how many move steps were used to reach
 * that step. The starting square is counted as "0".
 */
object MoveUsedStatusIndicator: FieldStatusIndicator {
    override fun decorate(
        uiSnapshot: UiGameSnapshot,
        node: ActionNode,
        state: Game,
        request: ActionRequest
    ) {
        uiSnapshot.uiIndicators.getAllMoveUsed().forEach { (coordinate, moveUsed) ->
            uiSnapshot.fieldSquares[coordinate]?.moveUsed = moveUsed
        }
    }
}
