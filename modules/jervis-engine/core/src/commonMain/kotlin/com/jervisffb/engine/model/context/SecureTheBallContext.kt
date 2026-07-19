package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player

data class SecureTheBallContext(
    val player: Player,
    val hasMoved: Boolean = false,
    // Typed as `ProcedureContext?` to keep this common data class free of
    // ruleset-specific imports. In BB2025 games this holds a
    // `bb2025.model.context.SecureTheBallRollContext`; BB2025 code casts on use.
    val roll: ProcedureContext? = null,
    val securedTheBall: Boolean = false
) : ProcedureContext
