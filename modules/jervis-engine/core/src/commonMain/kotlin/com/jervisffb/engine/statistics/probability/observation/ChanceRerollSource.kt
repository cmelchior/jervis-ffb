package com.jervisffb.engine.statistics.probability.observation

import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable

/** A self-contained snapshot of a reroll source at the time of a roll. */
@Serializable
data class ChanceRerollSource(
    val id: RerollSourceId,
    val owner: TeamId,
    val kind: ChanceRerollSourceKind,
    val description: String,
    val resetAt: Duration,
    val skillId: SkillId? = null,
    val tests: List<ChanceRerollTest> = emptyList(),
)
