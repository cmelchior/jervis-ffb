package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

data class RecoverKnockedOutPlayersContext(
    val playersHandled: PersistentSet<Player> = persistentSetOf(),
    val selectedPlayer: Player? = null,
    val recoverRoll: D6DieRoll? = null,
    val modifiers: List<DiceModifier> = emptyList(),
    val isSuccess: Boolean = false,
): ProcedureContext {
    fun reset(playerHandled: Player): RecoverKnockedOutPlayersContext {
        return this.copy(
            playersHandled = playersHandled.add(playerHandled),
            selectedPlayer = null,
            recoverRoll = null,
            modifiers = emptyList(),
            isSuccess = false,
        )
    }
}
