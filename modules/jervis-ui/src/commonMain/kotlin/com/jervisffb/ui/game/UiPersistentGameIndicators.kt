package com.jervisffb.ui.game

import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.tables.CasualtyResult
import com.jervisffb.fumbbl.net.model.BloodSpot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.DrawableResource

data class MoveUsed(val coordinate: PitchCoordinate, val value: Int)
data class BloodSpot(val coordinate: PitchCoordinate, val injury: CasualtyResult, val icon: DrawableResource)

/**
 * Tracking persistent UI decorations, i.e., things that are consequences of
 * model changes, but arent't tracked by the rules engine. Some examples being
 * blood spots after injuries and showing where a player moved during their move.
 */
class UiPersistentGameIndicators {

    // State used to track UI decorators for things that are not tracked
    // in the rules engine layer.
    val bloodspots: MutableList<BloodSpot> = mutableListOf()
    private val undostack: MutableMap<GameActionId, () -> Unit> = mutableMapOf()
    private val blodspots: MutableMap<PitchCoordinate, BloodSpot> = mutableMapOf()

    private var usedMoveToStandUp: Int? = null
    private var extraMoveOffset: Int = 0
    val movesUsed: MutableList<MoveUsed> = mutableListOf() // TODO Probably shouldn't be public

    // If set, it means that this player intends to use the Fumblerooskie on the next move
    val fumblerooskiEnabled: StateFlow<Player?>
        field = MutableStateFlow(null)

    // Track when standing up, so we can adjust showing "Move Used" decorator
    fun addMoveUsedToStandUp(move: Int) {
        usedMoveToStandUp = move
    }

    fun addMoveUsed(coordinate: Location, extraMoveCost: Int = 0) {
        if (coordinate !is PitchCoordinate) TODO("Missing support for $coordinate")
        this.movesUsed.add(MoveUsed(coordinate, movesUsed.size + extraMoveOffset + (usedMoveToStandUp ?: 0)))
        extraMoveOffset += extraMoveCost
    }

    fun getAllMoveUsed(): Map<PitchCoordinate, Int> {
        return movesUsed.associate { it.coordinate to it.value }
    }

    fun getMoveUsedOrNull(coordinate: PitchCoordinate): Int? {
        for (i in movesUsed.indices.reversed()) {
            if (movesUsed[i].coordinate == coordinate) {
                return movesUsed[i].value
            }
        }
        return null
    }

    fun removeLastMoveUsed(extraMoveCost: Int = 0) {
        movesUsed.removeLastOrNull()
        extraMoveOffset -= extraMoveCost
    }

    fun resetMovesUsed() {
        usedMoveToStandUp = null
        extraMoveOffset = 0
        movesUsed.clear()
    }

    fun registerUndo(deltaId: GameActionId, action: () -> Unit) {
        undostack[deltaId] = action
    }
    fun undo(deltaId: GameActionId? = null) {
        undostack[deltaId]?.invoke()
    }

    fun useFumblerooskiOnNextMove(player: Player?) {
        fumblerooskiEnabled.value = player
    }
}

