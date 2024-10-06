package com.jervisffb.fumbbl.net.model.change

import kotlinx.serialization.Serializable

@Serializable
data class TargetSelectionState(
    val selectedPlayerId: String? = null,
    val targetSelectionStatus: Status = Status.SELECTED,
    val targetSelectionStatusIsCommitted: Boolean,
    val playerStateOld: Int?,
    val usedSkills: List<String>,
) {
    enum class Status {
        STARTED,
        CANCELED,
        SELECTED,
        SKIPPED,
        FAILED,
    }
}
