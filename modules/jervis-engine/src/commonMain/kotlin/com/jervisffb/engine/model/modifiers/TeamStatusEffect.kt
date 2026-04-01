package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable

/**
 * Some effects are hard to put into other buckets, like a Team rolling Cheering
 * Fans and getting an Offensive Assist on the first block. In these cases, we
 * might want to mark the Tea somehow. This is done through this enum and is
 * added to players through [com.jervisffb.engine.commands.AddTeamStatusEffect].
 *
 * It is up to the individual procedures to check for effects and react to them. Effects are
 * removed again as part of checking for any other modifiers that has a [Duration].
 */
@Serializable
enum class TeamStatusEffectType(val description: String) {
    CHEERING_FANS_OFFENSIVE_ASSIST("Offensive Assist on Next Block"),
}

@Serializable
data class TeamStatusEffect(
    val type: TeamStatusEffectType,
    val duration: Duration,
) {
    companion object {
        fun cheeringFans() = TeamStatusEffect(TeamStatusEffectType.CHEERING_FANS_OFFENSIVE_ASSIST, Duration.END_OF_OWN_TEAM_TURN)
    }
}
