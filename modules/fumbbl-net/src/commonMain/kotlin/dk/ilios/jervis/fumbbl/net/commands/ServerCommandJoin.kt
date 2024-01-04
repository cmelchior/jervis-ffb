package dk.ilios.jervis.fumbbl.net.commands

import dk.ilios.jervis.fumbbl.net.ClientMode
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
    val playerNames: List<String>
): ServerCommand
