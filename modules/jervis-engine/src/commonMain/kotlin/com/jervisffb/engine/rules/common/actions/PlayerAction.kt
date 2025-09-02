package com.jervisffb.engine.rules.common.actions

import com.jervisffb.engine.fsm.Procedure
import kotlinx.serialization.Serializable

/**
 * Wrapper representing a player action. This can either be a normal or special action.
 */
@Serializable()
data class PlayerAction(
    val name: String,
    val type: ActionType,
    // Even if this action is of a given [type]. It will be treated as this type for the purpose
    // of counting actions. This is mostly relevant for Special Actions, like Chainsaw and the like
    // that counts as Block.
    val countsAs: PlayerStandardActionType?,
    // How many times (by different players) can this action be used pr team turn.
    val availablePrTurn: Int,
    // if type == BLOCK, this decides if this action also works during the blitz
    val worksDuringBlitz: Boolean,
    val procedure: Procedure,
    val compulsory: Boolean, // If true, players must choose this action
) {
    val isSpecial = (type == PlayerStandardActionType.SPECIAL) || (countsAs == PlayerStandardActionType.SPECIAL)
}
