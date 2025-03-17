package com.jervisffb.ui.game

import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.rules.bb2020.tables.CasualtyResult
import com.jervisffb.fumbbl.net.model.BloodSpot
import org.jetbrains.compose.resources.DrawableResource

data class MoveUsed(val coordinate: FieldCoordinate, val value: Int)
data class BloodSpot(val coordinate: FieldCoordinate, val injury: CasualtyResult, val icon: DrawableResource)

/**
 * Tracking persistent UI decorations, i.e., things that are consequences of
 * model changes, but shouldn't be tracked there. Some examples being
 * blood spots after injuries and showing where a player moved during their move.
 */
class UiGameDecorations {

    // State used to track UI decorators for things that are not tracked
    // in the rules engine layer.
    val bloodspots: MutableList<BloodSpot> = mutableListOf()
    private val undostack: MutableMap<GameActionId, () -> Unit> = mutableMapOf()
    private val blodspots: MutableMap<FieldCoordinate, BloodSpot> = mutableMapOf()

    private var usedMoveToStandUp: Int? = null
    private val movesUsed: MutableList<MoveUsed> = mutableListOf()

    // Track when standing up, so we can adjust showing "Move Used" decorator
    fun addMoveUsedToStandUp(move: Int) {
        usedMoveToStandUp = move
    }

    fun addMoveUsed(coordinate: Location) {
        if (coordinate !is FieldCoordinate) TODO("Missing support for $coordinate")
        this.movesUsed.add(MoveUsed(coordinate, movesUsed.size + (usedMoveToStandUp ?: 0)))
    }

    fun getMoveUsedOrNull(coordinate: FieldCoordinate): Int? {
        for (i in movesUsed.indices.reversed()) {
            if (movesUsed[i].coordinate == coordinate) {
                return movesUsed[i].value
            }
        }
        return null
    }

    fun removeLastMoveUsed() {
        movesUsed.removeLastOrNull()
    }

    fun resetMovesUsed() {
        usedMoveToStandUp = null
        movesUsed.clear()
    }

    fun registerUndo(deltaId: GameActionId, action: () -> Unit) {
        undostack[deltaId] = action
    }
    fun undo(deltaId: GameActionId? = null) {
        undostack[deltaId]?.invoke()
    }
}

