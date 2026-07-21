package com.jervisffb.engine.bb2025.context

import com.jervisffb.engine.bb2025.procedures.rerolls.TeamMascotReroll
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption

data class MascotContext(
    val team: Team,
    val reroll: TeamMascotReroll,
    val roll: D6DieRoll? = null,
    // Whether the mascot roll was successful,
    val isSuccessful: Boolean = false,
    // If Mascot failed, this tracks any alternative reroll selected
    val alternativeRerollSelected: DiceRerollOption? = null,
    // Track whether Mascot or another re-roll was use successfully, allowing
    // the caller procedure to continue with their reroll.
    val isRerollAllowed: Boolean = false,
): ProcedureContext
