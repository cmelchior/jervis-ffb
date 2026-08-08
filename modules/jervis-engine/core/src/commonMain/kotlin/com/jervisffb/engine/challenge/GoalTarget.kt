package com.jervisffb.engine.challenge

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId

/**
 * Which player (or players) a [ChallengeGoal] is aimed at.
 *
 * Note: Use [PlayerId] and [TeamId] as these classes are created using team
 * templates, which are not the same instances used by the challenge.
 */
sealed interface GoalTarget {
    val description: String

    /** One specific player, e.g. "Block <playerName>" */
    data class SpecificPlayer(val id: PlayerId, val name: String) : GoalTarget {
        constructor(player: Player) : this(player.id, player.name)
        override val description: String = name
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



