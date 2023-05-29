package dk.ilios.analyzer.fumbbl.net.commands

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("netCommandId")
sealed class ServerCommand {
    abstract val netCommandId: String
    abstract val commandNr: Int
}