package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.actions.PlayerAction

data class ActivatePlayerContext(
    // The player being activated
    val player: Player,
    // As part of activating the player, some negative effect was cleared (most likely lost tacklezones came back)
    val clearedNegativeEffects: Boolean = false,
    // As part of activating the player, some negatrait was rolled for
    val rolledForNegaTrait: Boolean = false,
    // If some effect caused the activation to "end immediately". This does not include turn overs.
    // Example: Unchannelled Fury
    val activationEndsImmediately: Boolean = false,
    // Which action does the player want to perform?
    val declaredAction: PlayerAction? = null,
    // The target of the action, if any is required.
    val target: Player? = null,
    // `true` if the action should count as being used, regardless of how the activation ended.
    // Failed nega-traits, should also set this as needed. Once set to to true, it should not be unset.
    val markActionAsUsed: Boolean = false,
): ProcedureContext {

    /**
     * Returns a copy of this context, where the action is marked as used.
     * This takes into account
     */
    fun copyWithMarkedAction(used: Boolean): ActivatePlayerContext {
        return this.copy(markActionAsUsed = used || this.markActionAsUsed)
    }
}
