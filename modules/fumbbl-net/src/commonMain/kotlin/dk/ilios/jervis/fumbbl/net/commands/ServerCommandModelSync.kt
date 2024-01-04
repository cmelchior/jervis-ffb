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
    // val sound: Any?,  // If this could be some specific type, please replace `Any?` with that type
    // These are not set when reporting Game Winnings
    val gameTime: Long,
    val turnTime: Long
): ServerCommand