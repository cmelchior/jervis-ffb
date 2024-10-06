package com.jervisffb.fumbbl.net.api.commands

import com.jervisffb.fumbbl.net.api.ClientMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("serverJoin")
data class ServerCommandJoin(
    override val netCommandId: String,
    override val commandNr: Int,
    val coach: String,
    val clientMode: ClientMode,
    val spectators: Int,
    val playerNames: List<String>,
) : com.jervisffb.fumbbl.net.api.commands.ServerCommand
