package com.jervisffb.engine.rules.common.planner

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.challenge.Challenge
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.engine.rules.policy.GameRulePolicy

/**
 * This interface represents extra move restrictions on top of those normally
 * allowed by the rules. This is normally configured by a [Challenge].
 *
 * Implementations may only remove options supplied by the base rules. Adding
 * new move types or targets requires a [Procedure] that knows how to
 * resolve them.
 */
interface MovePolicy : GameRulePolicy {

    /** High-level filter for movement types exposed by the base rules. */
    fun allowsMoveType(
        context: MovePolicyContext,
        type: MoveType,
    ): Boolean = true

    /**
     * Filter move candidates produced by the [ActionPlanner] or [PathFinder].
     */
    fun allowsMove(
        context: MovePolicyContext,
        candidate: MoveCandidate,
    ): Boolean = true
}
