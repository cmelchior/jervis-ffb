package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.skills.Duration

// Consider: Do we really need this enum?
enum class PrayerStatModifier(
    override val description: String,
    override val modifier: Int,
    override val type: StatModifier.Type,
    override val expiresAt: Duration
): StatModifier {
    IRON_MAN("Iron Man", 1, StatModifier.Type.AV, Duration.END_OF_GAME),
    GREASY_CLEATS("Greasy Cleats", -1, StatModifier.Type.MA, Duration.END_OF_DRIVE),
}
