package com.jervisffb.engine.rules.common.planner

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.policy.GameRulePolicy
/**
 * A single candidate movement step before [GameRulePolicy] are applied.
 */
data class MoveCandidate(
    val player: PlayerId,
    val type: MoveType,
    val from: PitchCoordinate,
    val target: TargetSquare,
)
