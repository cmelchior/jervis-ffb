package com.jervisffb.fumbbl.net.api.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("serverPong")
data class ServerCommandPong(
    override val netCommandId: String,
    val timestamp: Long,
) : NetCommand
