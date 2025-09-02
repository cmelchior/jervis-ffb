package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.skills.Duration

/**
 * List all possible stat modification, across all rules variants, that can
 * happen after rolling on the Lasting Injury Table.
 *
 * @see [com.jervisffb.engine.rules.bb2020.procedures.tables.injury.LastingInjuryRoll]
 * @see [com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryRoll.RollForLastingInjury]
 */
enum class LastingInjuryResult(
    override val description: String,
    override val modifier: Int,
    override val type: StatModifier.Type,
    override val expiresAt: Duration = Duration.PERMANENT
): StatModifier {
    HEAD_INJURY("Head Injury (-1 AV)", -1, StatModifier.Type.AV),
    SMASHED_KNEE("Smashed Knee (-1 MA)", -1, StatModifier.Type.MA),
    BROKEN_ARM("Broken Arm (+1 PA)",1, StatModifier.Type.PA),
    NECK_INJURY("Neck Injury (+1 AG)", 1, StatModifier.Type.AG),
    DISLOCATED_SHOULDER("Dislocated Shoulder (-1 ST)", -1, StatModifier.Type.ST),
}
