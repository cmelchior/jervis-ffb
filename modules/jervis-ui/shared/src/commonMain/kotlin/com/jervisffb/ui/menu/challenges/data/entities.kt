package com.jervisffb.ui.menu.challenges.data

import com.jervisffb.engine.challenge.Challenge
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import com.jervisffb.ui.utils.toFixed
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

/**
 * This file contains data classes modeling the challenge system.
 * This is the first attempt at figuring out what is needed to move this to
 * a database backend.
 *
 * Some requirements so far:
 * - Challenges should be versioned.
 *   a)s Changing title/description is free.
 *   b) Changing rules/setup should mark all previous scores as "old" and
 *      the score should not be listed on the scoreboard. The old score should
 *      be visible on the lists/details page but have its own icon. Either
 *      a broken trophy or maybe silver. It should be clear the score is old.
 *
 * - Each challenge should be able to declare its own scoring method, while
 *   still making it cheap for the server to generate a high-score list.
 *
 * - Clients should not be able to just send their score to a server. The server
 *   must also run the game in order to verify the score.
 *   a) A malicious client will always be able to cheat with "completion time",
 *      so the time to complete a challenge should never be used for scoring.
 */

// A single entry on a challenge's scoreboard.
data class ScoreboardEntry(
    val coach: Coach,
    val score: ChallengeScore<*>,
) {

    fun getFormattedScore(): String {
        return when (score) {
            is ChallengeScore.CompletionOnly -> {
                val tz = TimeZone.currentSystemDefault()
                scoringTimeFormatter.format(score.date.toLocalDateTime(tz))
            }
            is ChallengeScore.ProbabilityScore -> {
                score.result.formattedSuccessProbability()
            }
        }
    }

    companion object {
        private val scoringTimeFormatter = LocalDateTime.Format {
            date(LocalDate.Formats.ISO)
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
        }
    }
}

/**
 * Per-user state for a single challenge.
 *
 * [favorite] is `true` if the user has starred the challenge.
 * [solved] represents how, or if, the user has solved the challenge.
 */
data class ChallengeUserState(
    val favorite: Boolean = false,
    val solved: SolvedState = SolvedState.UNSOLVED,
    val score: ChallengeScore<*>? = null,
    val voted: Boolean = false,
)  {
    enum class SolvedState {
        UNSOLVED, // User has not solved this challenge
        SOLVED, // User has solved this challenge, but someone has done it better
        BEST_IN_CLASS, // User has solved this challenge
    }

    fun isSolved(): Boolean {
        return solved != SolvedState.UNSOLVED && score != null
    }

    fun getFormattedDate(): String {
        return when (score != null) {
            true -> displayFormat.format(score.date.toLocalDateTime(TimeZone.currentSystemDefault()))
            false -> ""
        }
    }

    fun getFormattedScore(): String {
        return when (score) {
            is ChallengeScore.CompletionOnly -> {
                val tz = TimeZone.currentSystemDefault()
                scoringTimeFormatter.format(score.date.toLocalDateTime(tz))
            }
            is ChallengeScore.ProbabilityScore -> {
                score.result.formattedSuccessProbability()
            }
            null -> ""
        }
    }

    companion object {
        private val displayFormat = LocalDateTime.Format {
            day(Padding.NONE)
            char(' ')
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            year()
        }

        private val scoringTimeFormatter = LocalDateTime.Format {
            date(LocalDate.Formats.ISO)
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
        }
    }
}

private fun ProbabilityScoreResult.formattedSuccessProbability(): String = when (this) {
    is ProbabilityScoreResult.Scored -> "${(successProbability.value * 100.0).toFixed(2)}%"
    is ProbabilityScoreResult.Unsupported -> "Unranked"
}


private fun ProbabilityScoreResult.formattedRisk(): String = when (this) {
    is ProbabilityScoreResult.Scored -> surprisal.toFixed(3)
    is ProbabilityScoreResult.Unsupported -> "Unranked"
}

// Data required to display a challenge in a list for a single user
data class ChallengeRow(
    val data: Challenge,
    val votes: Int = 0, // Includes user vote.
    val userState: ChallengeUserState,
) {
    val score = userState.score
}

// Data required to display challenge details for a single user
data class ChallengeDetails(
    val data: Challenge,
    val userState: ChallengeUserState,
    val votes: Int, // Includes user vote.
    val scoreboard: List<ScoreboardEntry>,
) {
    val id = data.id
    val isPlayable = true // We might want to disable some challenges
}
