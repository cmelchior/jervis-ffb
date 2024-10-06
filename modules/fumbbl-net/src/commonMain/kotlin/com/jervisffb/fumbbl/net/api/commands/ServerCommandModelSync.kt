package com.jervisffb.fumbbl.net.api.commands

import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.reports.Report
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

    fun lastChangeId(): ModelChangeId? {
        return modelChangeList.lastOrNull()?.id
    }

    fun firstReport(): Report? {
        return reportList.reports.firstOrNull()
    }
}
