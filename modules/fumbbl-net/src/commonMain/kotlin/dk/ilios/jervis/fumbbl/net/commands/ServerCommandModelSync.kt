package dk.ilios.jervis.fumbbl.net.commands

import dk.ilios.jervis.fumbbl.model.ModelChangeId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("serverModelSync")
data class ServerCommandModelSync(
    override val netCommandId: String,
    override val commandNr: Int,
    val modelChangeList: ModelChangeList = ModelChangeList(emptyList()),
    val reportList: ReportList = ReportList(emptyList()),
    val sound: String? = null,
    val gameTime: Long = 0,
    val turnTime: Long = 0,
) : ServerCommand {

    fun firstChangeId(): ModelChangeId? {
        return modelChangeList.firstOrNull()?.id
    }
}
