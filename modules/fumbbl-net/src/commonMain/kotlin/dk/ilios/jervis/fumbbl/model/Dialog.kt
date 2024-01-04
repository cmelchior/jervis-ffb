package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

// {"dialogId":"coinChoice"}
// {"dialogId":"receiveChoice","choosingTeamId":"1158756"}
@Serializable // Extend this to cover all dialogs
data class DialogOptions(
    val dialogId: DialogId
)

