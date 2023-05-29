package dk.ilios.analyzer.fumbbl.net.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("serverReplay")
data class ServerCommandReplay(
    override val netCommandId: String,
    override val commandNr: Int,
    val totalNrOfCommands: Int,
): ServerCommand()
