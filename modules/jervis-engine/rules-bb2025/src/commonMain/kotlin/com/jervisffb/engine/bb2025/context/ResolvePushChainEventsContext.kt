package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.locations.Location
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

data class ResolvePushChainEventsContext(
    val currentPushChainIndex: Int = -1,
    val currentPushStep: PushContext.PushData? = null,
    val resolvingAttacker: Player? = null,
    val attackerResolved: Boolean = false,
    val visitedSquares: PersistentSet<Location> = persistentSetOf(),
): ProcedureContext {
    fun getCurrentPlayer(): Player {
        return currentPushStep?.pushee ?: resolvingAttacker ?: error("No player found: $this")
    }
}
