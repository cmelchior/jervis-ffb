package com.jervisffb.fumbbl.net.api.commands

import com.jervisffb.fumbbl.net.model.GameList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("netCommandId")
sealed interface ServerCommand : NetCommand {
    override val netCommandId: String
    val commandNr: Int
}

@Serializable
@SerialName("serverGameList")
data class ServerCommandGameList(
    override val netCommandId: String,
    override val commandNr: Int,
    val gameList: GameList,
) : ServerCommand
