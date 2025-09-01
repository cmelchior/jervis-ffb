package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.model.modifiers.StatModifier.Type
import com.jervisffb.engine.rules.common.skills.Duration

/**
 * Enumerate the possible stat modifications that can happen after
 * rolling on the Lasting Injury Table.
 *
 * @see [com.jervisffb.rules.bb2020.procedures.injury.LastingInjuryRoll]
 * @see [com.jervisffb.rules.bb2020.procedures.injury.RiskingInjuryRoll.RollForLastingInjury]
 */
enum class LastingInjuryResult(
    override val description: String,
    override val modifier: Int,
    override val type: Type,
    override val expiresAt: Duration = Duration.PERMANENT
): StatModifier {
    HEAD_INJURY("Head Injury (-1 AV)", -1, Type.AV),
    SMASHED_KNEE("Smashed Knee (-1 MA)", -1, Type.MA),
    BROKEN_ARM("Broken Arm (+1 PA)",1, Type.PA),
    NECK_INJURY("Neck Injury (+1 AG)", 1, Type.AG),
    DISLOCATED_SHOULDER("Dislocated Shoulder (-1 ST)", -1, Type.ST),
}
