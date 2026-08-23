package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.skills.Duration

data class IronManStatModifier(override val expiresAt: Duration): StatModifier {
    override val description: String = "Iron Man"
    override val modifier: Int = 1
    override val type: StatModifier.Type = StatModifier.Type.AV
}

data class GreasyCleatsStatModifier(override val expiresAt: Duration): StatModifier {
    override val description: String = "Greasy Cleats"
    override val modifier: Int = -1
    override val type: StatModifier.Type = StatModifier.Type.MA
}



