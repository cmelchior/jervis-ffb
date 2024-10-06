package com.jervisffb.fumbbl.net.model

import kotlinx.serialization.Serializable

// {"dialogId":"coinChoice"}
// {"dialogId":"receiveChoice","choosingTeamId":"1158756"}
@Serializable // Extend this to cover all dialogs
data class DialogOptions(
    val dialogId: com.jervisffb.fumbbl.net.model.DialogId,
)
