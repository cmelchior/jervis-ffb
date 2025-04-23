package com.jervisffb.fumbbl.net.api.commands

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("netCommandId")
sealed interface NetCommand {
    val netCommandId: String
}
