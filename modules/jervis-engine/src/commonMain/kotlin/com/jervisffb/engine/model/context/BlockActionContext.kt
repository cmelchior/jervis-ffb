package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.actions.BlockType

/**
 * Context for a "Block Action". This context only tracks the top-level state relevant to a block action.
 * All state related to the type of block is tracked in the relevant contexts.
 */
data class BlockActionContext(
    val attacker: Player,
    val defender: Player,
    val blockType: BlockType? = null,
    val hasBlocked: Boolean = false,
): ProcedureContext
