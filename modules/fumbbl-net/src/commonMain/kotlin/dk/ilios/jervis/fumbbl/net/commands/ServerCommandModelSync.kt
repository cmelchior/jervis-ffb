package dk.ilios.jervis.fumbbl.net.commands

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("serverModelSync")
data class ServerCommandModelSync(
    override val netCommandId: String,
    override val commandNr: Int,
    val modelChangeList: ModelChangeList,
    val reportList: ReportList,
    val sound: String? = null,
    val gameTime: Long,
    val turnTime: Long
): ServerCommand