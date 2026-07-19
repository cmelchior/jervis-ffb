package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.model.modifiers.StatModifier.Type
import com.jervisffb.engine.rules.common.skills.Duration

enum class KickoffStatModifier(
    override val description: String,
    override val modifier: Int,
    override val type: StatModifier.Type,
    override val expiresAt: Duration,
): StatModifier {
    DODGY_SNACK_MA("Dodgy Snack (-1 MV)", -1, Type.MA, expiresAt = Duration.END_OF_DRIVE),
    DODGY_SNACK_AV("Dodgy Snack (-1 AV)", -1, Type.AV, expiresAt = Duration.END_OF_DRIVE),
}
