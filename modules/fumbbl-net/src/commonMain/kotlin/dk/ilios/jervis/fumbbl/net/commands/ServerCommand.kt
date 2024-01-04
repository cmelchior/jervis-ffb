package dk.ilios.jervis.fumbbl.net.commands

import dk.ilios.jervis.fumbbl.model.GameList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("netCommandId")
sealed interface ServerCommand: NetCommand {
    override val netCommandId: String
    val commandNr: Int
}

@Serializable
@SerialName("serverGameList")
data class ServerCommandGameList(
    override val netCommandId: String,
    override val commandNr: Int,
    val gameList: GameList,
): ServerCommand

