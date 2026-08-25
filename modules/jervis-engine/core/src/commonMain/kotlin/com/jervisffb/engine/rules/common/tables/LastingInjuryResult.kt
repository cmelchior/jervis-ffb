package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.Duration

/**
 * List all possible stat modification, across all rules variants, that can
 * happen after rolling on the Lasting Injury Table.
 *
 * The lasting injury roll is selected through [Rules.riskingInjuryRoll].
 *
 * TODO Split into BB2020 and BB2025 variants.
 */
enum class LastingInjuryResult(
    override val description: String,
    override val modifier: Int,
    override val type: StatModifier.Type,
    override val expiresAt: Duration = Duration.PERMANENT
): StatModifier {
    // BB2025
    HEAD_INJURY("Head Injury (-1 AV)", -1, StatModifier.Type.AV),
    SMASHED_KNEE("Smashed Knee (-1 MA)", -1, StatModifier.Type.MA),
    BROKEN_ARM("Broken Arm (+1 PA)",1, StatModifier.Type.PA),
    DISLOCATED_HIP("Dislocated Hip (+1 AG)", 1, StatModifier.Type.AG),
    BROKEN_SHOULDER("Broken Shoulder (-1 ST)", -1, StatModifier.Type.ST),

    // BB2020 uses slightly different naming for the last two injuries.
    NECK_INJURY("Neck Injury (+1 AG)", 1, StatModifier.Type.AG),
    DISLOCATED_SHOULDER("Dislocated Shoulder (-1 ST)", -1, StatModifier.Type.ST),
}
