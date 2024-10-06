package com.jervisffb.fumbbl.net.api.commands

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("netCommandId")
sealed interface NetCommand {
    val netCommandId: String
}
