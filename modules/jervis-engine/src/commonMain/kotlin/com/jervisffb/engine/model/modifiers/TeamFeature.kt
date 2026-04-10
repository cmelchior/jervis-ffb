package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable

/**
 * Some rule effects are hard to put into other buckets, like a Team rolling
 * Cheering Fans and getting an Offensive Assist on the first block. In these
 * cases, we want to mark the Team somehow. This is done through this enum and
 * is added to players through [com.jervisffb.engine.commands.AddTeamFeature].
 *
 * It is up to the individual procedures to check for effects and react to them. Effects are
 * removed again as part of checking for any other modifiers that has a [Duration].
 */
@Serializable
enum class TeamFeatureType(val description: String) {
    CHEERING_FANS_OFFENSIVE_ASSIST("Offensive Assist on Next Block"),
}

@Serializable
data class TeamFeature(
    val type: TeamFeatureType,
    val duration: Duration,
) {
    companion object {
        fun cheeringFans() = TeamFeature(TeamFeatureType.CHEERING_FANS_OFFENSIVE_ASSIST, Duration.END_OF_OWN_TEAM_TURN)
    }
}
