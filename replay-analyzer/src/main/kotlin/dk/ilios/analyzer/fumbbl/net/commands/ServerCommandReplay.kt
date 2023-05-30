package dk.ilios.analyzer.fumbbl.net.commands

import dk.ilios.analyzer.fumbbl.model.ReportId
import dk.ilios.analyzer.fumbbl.model.change.ModelChange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import java.time.LocalDateTime

@Serializable
@SerialName("serverReplay")
data class ServerCommandReplay(
    override val netCommandId: String,
    override val commandNr: Int,
    val totalNrOfCommands: Int,
    val commandArray: List<Command>,
    val lastCommand: Boolean
): ServerCommand()

@Serializable
data class Command(
    val netCommandId: String,
    val commandNr: Int,
    val modelChangeList: ModelChangeList,
    val reportList: ReportList,
    // val sound: Any?,  // If this could be some specific type, please replace `Any?` with that type
    val gameTime: Int,
    val turnTime: Int
)

@Serializable
data class ModelChangeList(
    val modelChangeArray: List<ModelChange>
)

@Serializable
data class ReportList(
    val reports: List<Report>
)

@Serializable
data class Report(
    val reportId: ReportId,
    val teamId: String? = null,
    val dedicatedFans: Int? = null,
    val dedicatedFansRoll: Int? = null,
    val dedicatedFansResult: Int? = null,
    val weather: String? = null,
    val weatherRoll: List<Int> = listOf()
)