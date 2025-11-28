package com.jervisffb.engine.model.context

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.model.Player
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

data class ChargeContext(
    val roll: D3Result,
    val playersToSelect: Int,
    val selectedPlayers: Set<Player> = emptySet(),
    val activatedPlayers: PersistentSet<Player> = persistentSetOf(),
): ProcedureContext

