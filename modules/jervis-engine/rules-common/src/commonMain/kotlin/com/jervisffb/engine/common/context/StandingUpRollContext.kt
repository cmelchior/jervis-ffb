package com.jervisffb.engine.common.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class StandingUpRollContext(
    val player: Player,
    val modifiers: PersistentList<DiceModifier> = persistentListOf(),
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false
): ProcedureContext
