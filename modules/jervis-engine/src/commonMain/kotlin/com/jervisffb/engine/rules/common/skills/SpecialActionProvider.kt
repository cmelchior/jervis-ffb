package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.rules.PlayerSpecialActionType

/**
 * Interface for skills that provide a special player action.
 *
 * Note, for skills that replace blocks, the procedure being referenced is the Standalone
 * variant. Special skills that can be used as part of a Multiple Block
 * are defined in [com.jervisffb.engine.model.context.MultipleBlockContext] and will
 * be handled separately there.
 */
interface SpecialActionProvider {
    val specialAction: PlayerSpecialActionType
    var isSpecialActionUsed: Boolean
}
