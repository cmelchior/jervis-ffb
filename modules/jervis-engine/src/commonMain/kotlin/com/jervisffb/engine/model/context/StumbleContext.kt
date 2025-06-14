package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player

data class StumbleContext(
    val attacker: Player,
    val defender: Player,
    val attackerUsesTackle: Boolean = false,
    val defenderUsesDodge: Boolean = false,
) : ProcedureContext {
    fun isDefenderDown(): Boolean {
        return !defenderUsesDodge || attackerUsesTackle
    }
}
