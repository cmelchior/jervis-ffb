package com.jervisffb.engine

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.model.Team

class ActionRequest(
    val team: Team?,
    val actions: List<ActionDescriptor>
) {
    val size = actions.size
}


