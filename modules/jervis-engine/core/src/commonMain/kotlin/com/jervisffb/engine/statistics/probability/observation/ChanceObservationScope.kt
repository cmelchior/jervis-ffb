package com.jervisffb.engine.statistics.probability.observation

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import kotlinx.serialization.Serializable
import kotlin.invoke
import kotlin.text.Typography.half

/**
 * Identifies the game boundaries containing a random event.
 * TODO Do we strictly need this?
 */
@Serializable
data class ChanceObservationScope(
    val half: Int,
    val drive: Int,
    val team: TeamId,
    val turn: Int,
    val player: PlayerId? = null,
) {
    companion object {
        fun fromState(state: Game, team: Team): ChanceObservationScope {
            return ChanceObservationScope(
                half = state.halfNo,
                drive = state.driveNo,
                team = team.id,
                turn = team.turnMarker,
            )
        }
    }
}

