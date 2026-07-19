package com.jervisffb.engine.rules.bb2025.procedures.actions.block

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.context.StumbleContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.PitchCoordinate

// Helper method for creating a push context before moving a player back
// This is used by all results that push back.
fun createPushContext(state: Game): PushContext {
    val blockContext = state.getContext<BlockContext>()
    val stumbleContext = state.getContextOrNull<StumbleContext>()?.also {
        check(it.attacker == blockContext.attacker) { "BlockContext and StumbleContext out of sync:\n$blockContext\n$it" }
        check(it.defender == blockContext.defender) { "BlockContext and StumbleContext out of sync:\n$blockContext\n$it" }
    }
    // TODO Are there any special skills that also knock players down?
    val isKnockedDown = stumbleContext?.isDefenderDown() ?: (blockContext.result.blockResult == BlockDice.POW)

    // Setup the context needed to resolve the full push include
    val newContext = PushContext(
        firstPusher = blockContext.attacker,
        firstPushee = blockContext.defender,
        isAttackerUsingJuggernaut = false,
        isDefenderKnockedDown = isKnockedDown,
        blockContext.isUsingMultiBlock,
        mutableListOf(
            PushContext.PushData(
                pusher = blockContext.attacker,
                pushee = blockContext.defender,
                from = blockContext.defender.location as PitchCoordinate,
                isBlitzing = blockContext.isBlitzing,
                isChainPush = false,
            )
        )
    )
    return newContext
}
