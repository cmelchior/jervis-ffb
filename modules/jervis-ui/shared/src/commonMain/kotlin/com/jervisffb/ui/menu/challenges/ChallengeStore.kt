package com.jervisffb.ui.menu.challenges

import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.menu.challenges.ChallengeUserState.SolvedState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

/**
 * Per-user state for a single challenge.
 *
 * [favorite] is `true` if the user has starred the challenge.
 * [solved] represents how, or if, the user has solved the challenge.
 * [vote] the user's rating: `-1` (down), `0` (none) or `+1` (up).
 */
data class ChallengeUserState(
    val favorite: Boolean = false,
    val solved: SolvedState = SolvedState.UNSOLVED,
    val solvedDate: LocalDateTime? = null,
    val voted: Boolean = false,
)  {
    enum class SolvedState {
        UNSOLVED, // User has not solved this challenge
        SOLVED, // User has solved this challenge, but someone has done it better
        BEST_IN_CLASS, // User has solved this challenge
    }

    fun isSolved(): Boolean {
        return solved != SolvedState.UNSOLVED
    }

    fun getFormattedDate(): String {
        return when (solvedDate != null) {
            true -> displayFormat.format(solvedDate)
            false -> ""
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
    }

}

/**
 * Single source of truth for per-user challenge state (favorite / solved / rating vote).
 *
 * The state is cached in-memory as a reactive [StateFlow] so the list and detail screens stay in
 * sync automatically, and every mutation is written through to [SETTINGS_MANAGER] so it survives
 * app restarts. Challenge state is stored under dynamic per-challenge keys, so no static setting
 * keys need to be registered.
 *
 * This is a UI-module singleton in the same spirit as `SETTINGS_MANAGER` and `FILE_MANAGER`.
 */
object ChallengeStore {

    private fun favoriteKey(id: ChallengeId) = "jervis.challenge.${id.value}.favorite"
    private fun solvedKey(id: ChallengeId) = "jervis.challenge.${id.value}.solved"
    private fun voteKey(id: ChallengeId) = "jervis.challenge.${id.value}.vote"

    private val _state = MutableStateFlow<Map<ChallengeId, ChallengeUserState>>(emptyMap())
    val state: StateFlow<Map<ChallengeId, ChallengeUserState>> = _state.asStateFlow()

    /**
     * Loads persisted state for [challenges] into the in-memory cache. Idempotent: challenges that
     * are already loaded keep their current (possibly newer) in-memory value.
     */
    fun load(challenges: List<Challenge>) {
        val updated = _state.value.toMutableMap()
        challenges.forEach { challenge ->
            if (!updated.containsKey(challenge.id)) {
                updated[challenge.id] = readFromSettings(challenge)
            }
        }
        _state.value = updated
    }

    fun userState(id: ChallengeId): ChallengeUserState = _state.value[id] ?: ChallengeUserState()

    fun toggleFavorite(id: ChallengeId) {
        val newValue = !userState(id).favorite
        SETTINGS_MANAGER.put(favoriteKey(id), newValue)
        update(id) { it.copy(favorite = newValue) }
    }

    /**
     * Sets the user's rating vote. Voting in the direction that is already selected clears the vote
     * (back to `0`), so a vote can be undone by tapping it again.
     */
    fun setVote(id: ChallengeId, voted: Boolean) {
        SETTINGS_MANAGER.put(voteKey(id), voted)
        update(id) { it.copy(voted = voted) }
    }

    /**
     * Marks a challenge as solved (or not). Not yet triggered from the UI; this is the hook the
     * future "play challenge" flow will call once success detection exists.
     */
    fun setSolved(id: ChallengeId, solved: SolvedState) {
        SETTINGS_MANAGER.put(solvedKey(id), solved)
        update(id) { it.copy(solved = solved) }
    }

    private fun readFromSettings(challenge: Challenge) = ChallengeUserState(
        favorite = SETTINGS_MANAGER.getBoolean(favoriteKey(challenge.id), false),
        solved = SETTINGS_MANAGER.getString(solvedKey(challenge.id), when (challenge.solvedByDefault) {
            true -> SolvedState.UNSOLVED.name
            false -> SolvedState.BEST_IN_CLASS.name
        }).let { SolvedState.valueOf(it) },
        voted = SETTINGS_MANAGER.getBoolean(voteKey(challenge.id), false),
    )

    private fun update(id: ChallengeId, transform: (ChallengeUserState) -> ChallengeUserState) {
        val updated = _state.value.toMutableMap()
        updated[id] = transform(updated[id] ?: ChallengeUserState())
        _state.value = updated
    }
}
