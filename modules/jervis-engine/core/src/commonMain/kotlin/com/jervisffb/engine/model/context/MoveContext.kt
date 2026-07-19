package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Context data for a player moving. This includes standing up, moving
 * a single square, jumping, leaping, or using the Pogo, but no other special
 * actions.
 *
 * Used by the `MoveAction` procedure (in the `rules-common` module).
 */
data class MoveContext(
    val player: Player,
    val moveType: MoveType,
    val useVeryLongLegs: Boolean = false,
    // `true` if the player uses the +1 Leap Modifier. If the modifier is +0, this should be `false`
    val useLeapModifier: Boolean = false,
    // `true` if the player was considered "moving" (this includes Standing Up). If `false`, it
    // means the move was aborted for whatever reason. E.g. if no target square is selected or
    // Sprint isn't used.
    val hasMoved: Boolean = false,
    // When Rush fails, the player does not gain +1 movement but is still moved. This means that we cannot
    // update `Player.movesLeft` as we would otherwise after a move step. This property tracks this specific case
    // to avoid crashing the engine in `StandardMoveStep.ResolveMove`.
    val rushFailed: Boolean = false,
    // Tracks whether the player failed to Dodge an is about to fall over.
    val dodgeFailed: Boolean = false,
    val target: PitchCoordinate? = null,
    val startingSquare: PitchCoordinate = when (val location = player.location) {
        Dogout -> INVALID_GAME_STATE("Player in the dogout cannot move")
        is PitchCoordinate -> location
        is GiantLocation -> TODO("Convert startingSquare to location and adjust procedures")
    },
): ProcedureContext
