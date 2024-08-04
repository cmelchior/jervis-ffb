package dk.ilios.jervis.fumbbl.net.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("serverVersion")
data class ServerCommandVersion(
    override val netCommandId: String,
    override val commandNr: Int,
    val serverVersion: String,
    val clientVersion: String,
    val clientPropertyNames: List<String>,
    val clientPropertyValues: List<String>,
    val testing: Boolean,
) : ServerCommand
