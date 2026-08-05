package com.jervisffb.engine.challenge

import com.jervisffb.engine.model.Player

/**
 * Which player (or players) a [ChallengeGoal] is aimed at.
 */
sealed interface GoalTarget {
    val description: String

    /** One specific player, e.g. "Block <playerName>" */
    data class SpecificPlayer(val player: Player) : GoalTarget {
        override val description: String = player.name
    }

    /** Any [count] players, e.g. "push 2 players off the pitch". */
    data class AnyPlayers(val count: Int, val sameTeam: Boolean = false) : GoalTarget {
        init {
            require(count >= 1) { "A goal has to target at least one player: $count" }
        }
        override val description: String
            get() {
                return if (sameTeam) {
                    when (count) {
                        1 -> "any player"
                        else -> "any $count players"
                    }
                } else {
                    when (count) {
                        1 -> "any opponent"
                        else -> "any $count opponents"
                    }
                }
            }
    }
}



