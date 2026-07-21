package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class DivingCatchContext(
    val ball: Ball,
    // Which players have been selected for being able to use Diving Catch
    val selectedPlayers: PersistentList<Player> = persistentListOf(),
    // When resolving Diving Catch for players in order, this is the current player being resolved.
    val currentPlayer: Player? = null
): ProcedureContext
