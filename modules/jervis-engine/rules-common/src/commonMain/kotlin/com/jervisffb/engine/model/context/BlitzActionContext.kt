package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.actions.BlockType

data class BlitzActionContext(
    val attacker: Player,
    val defender: Player? = null,
    val blockType: BlockType? = null,
    val hasMoved: Boolean = false,
    val hasBlocked: Boolean = false,
    val didFollowUp: Boolean = false,
) : ProcedureContext
