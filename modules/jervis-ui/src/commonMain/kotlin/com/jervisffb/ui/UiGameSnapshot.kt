package com.jervisffb.ui

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.ui.dialogs.UserInputDialog
import com.jervisffb.ui.model.UiFieldSquare

/**
 * Class representing a snapshot of the current UI State as it should be shown for this "frame". This only
 * includes the model rules state, and shouldn't include ephemeral state. Things like hover state
 * should be covered by individual view models.
 *
 * Note, the snapshot is not stable as it references mutable classes. It is only stable
 * for the duration of a single game loop.
 */
class UiGameSnapshot(
    val game: Game,
    var actionsRequest: ActionRequest,
    val fieldSquares: MutableMap<FieldCoordinate, UiFieldSquare>,
) {
    fun clearHoverData() {
        // Clear the hover data, only update squares that actually changed
        fieldSquares.entries.forEach {
            if (it.value.futureMoveValue != null) {
                fieldSquares[it.key] = it.value.copy(
                    futureMoveValue = null,
                    hoverAction = null,
                )
            }
        }
    }

    // Attach actions to players found in the dogout
    val dogoutActions: MutableMap<PlayerId, () -> Unit> = mutableMapOf()

    // If set, it means we are in the middle of a move action that allows the player
    // to move multiple squares.
    var pathFinder: PathFinder.AllPathsResult? = null

    // If set, a dialog should be shown as a first priority
    var dialogInput: UserInputDialog? = null
    val unknownActions: MutableList<GameAction> = mutableListOf()

    init {
        fieldSquares[FieldCoordinate.UNKNOWN] = UiFieldSquare(FieldSquare(-1, -1))
    }
}
