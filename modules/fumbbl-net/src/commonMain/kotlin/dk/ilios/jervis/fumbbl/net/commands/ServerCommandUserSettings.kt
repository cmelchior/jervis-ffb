package dk.ilios.jervis.fumbbl.net.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("serverUserSettings")
data class ServerCommandUserSettings(
    override val netCommandId: String,
    override val commandNr: Int,
    val userSettingNames: List<String>,
    val userSettingValues: List<String?>,
) : ServerCommand
