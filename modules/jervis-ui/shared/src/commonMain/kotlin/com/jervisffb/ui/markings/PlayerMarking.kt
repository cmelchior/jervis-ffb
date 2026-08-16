package com.jervisffb.ui.markings

import com.jervisffb.engine.rules.common.skills.SkillType
import kotlinx.serialization.Serializable

@Serializable
data class PlayerMarking(
    val text: String,
    val type: PlayerMarkingType = PlayerMarkingType.GAINED,
    val team: PlayerMarkingTeam = PlayerMarkingTeam.BOTH,
    val skills: List<SkillType> = emptyList(),
    val enabled: Boolean = true,
)
