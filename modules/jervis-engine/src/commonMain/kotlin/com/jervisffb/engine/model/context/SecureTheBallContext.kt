package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player

data class SecureTheBallContext(
    val player: Player,
    val hasMoved: Boolean = false,
    val roll: SecureTheBallRollContext? = null,
    val securedTheBall: Boolean = false
) : ProcedureContext
