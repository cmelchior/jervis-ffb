package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.utils.INVALID_GAME_STATE

class ReportGameResult(
    private val state: Game,
    val extraTime: Boolean,
    val suddenDeath: Boolean
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (extraTime && suddenDeath) {
            val logEntry = when {
                state.homeScore > state.awayScore -> reportSuddenDeathWin(state.homeTeam, state.homeScore, state.awayTeam, state.awayScore)
                state.homeScore < state.awayScore -> reportSuddenDeathWin(state.awayTeam, state.awayScore, state.homeTeam, state.homeScore)
                else -> INVALID_GAME_STATE("Unsupported state")
            }
            append(logEntry)
        } else if (extraTime && !suddenDeath) {
            // Report status after overtime
            val logEntry = when {
                state.homeScore > state.awayScore -> reportExtraTimeWin(state.homeTeam, state.homeScore, state.awayTeam, state.awayScore)
                state.homeScore < state.awayScore -> reportExtraTimeWin(state.awayTeam, state.awayScore, state.homeTeam, state.homeScore)
                else -> "${state.homeTeam.name} draws ${state.awayScore} : ${state.homeScore} against ${state.awayTeam.name} (${state.homeGoals} - ${state.awayGoals} at normal time)"
            }
            append(logEntry)
        } else {
            // Report the status of a normal game with no extra time
            when {
                state.homeScore > state.awayScore -> {
                    append(reportWin(state.homeTeam, state.homeScore, state.awayTeam, state.awayScore))
                }
                state.homeScore < state.awayScore -> {
                    append(reportWin(state.awayTeam, state.awayScore, state.homeTeam, state.homeScore))
                }
                (state.homeScore == state.awayScore) -> {
                    append("${state.homeTeam.name} draws ${state.awayScore} - ${state.homeScore} against ${state.awayTeam.name}")
                }
                else -> INVALID_GAME_STATE("Unsupported state")
            }
        }
    }

    private fun reportSuddenDeathWin(winner: Team, winnerScore: Int, looser: Team, looserScore: Int): String {
        return "${winner.name} wins $winnerScore - $looserScore over ${looser.name} after Sudden Death"
    }

    private fun reportExtraTimeWin(winner: Team, winnerScore: Int, looser: Team, looserScore: Int): String {
        return "${winner.name} wins $winnerScore - $looserScore over ${looser.name} after Extra Time"
    }

    private fun reportWin(winner: Team, winnerScore: Int, looser: Team, looserScore: Int): String {
        return "${winner.name} wins $winnerScore - $looserScore over ${looser.name}"
    }

}

