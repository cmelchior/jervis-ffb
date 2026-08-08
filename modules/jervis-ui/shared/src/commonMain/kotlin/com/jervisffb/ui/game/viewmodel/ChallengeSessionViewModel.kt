package com.jervisffb.ui.game.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.challenge.Challenge
import com.jervisffb.engine.challenge.ChallengeOutcome
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.engine.challenge.ChallengeTracker
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.menu.GameScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.emptyList
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Tracks one attempt at a [Challenge] while the game is being played.
 *
 * Everything here is derived from the game state as it arrives, so there is no
 * progress of our own to unwind. Undoing an action simply re-evaluates the
 * state the coach went back to.
 */
class ChallengeSessionViewModel(
    private val screenModel: GameScreenModel,
    val challenge: Challenge,
) {

    // The repository should already have been initialized here, so we can
    // just use it.
    private val repository = screenModel.menuViewModel.challengesRepository
    private var startedAt: TimeSource.Monotonic.ValueTimeMark? = null

    // The tracker needs the position the attempt began from, which is whatever
    // the first snapshot shows. Building it eagerly here would race the engine
    // still setting the game up.
    private var tracker: ChallengeTracker? = null

    val score: ChallengeScore<*>?
        get() = tracker?.score

    val undosPerformed: Int
        get() = tracker?.undosPerformed ?: 0

    val outcome: StateFlow<ChallengeOutcome>
        field = MutableStateFlow(ChallengeOutcome.IN_PROGRESS)

    // Are set once the attempt finishes and do not change afterwards.
    private var finishedAfter: Duration? = null
    private var finishedActionCount: Int? = null

    val elapsed: Duration
        get() = finishedAfter ?: Duration.ZERO

    val actionCount: Int
        get() = finishedActionCount ?: 0

    /**
     * Called for every UI snapshot. Cheap enough to run on each one, and doing
     * so is what makes the outcome show up the moment the goal is reached.
     */
    fun onSnapshot(state: Game, delta: GameDelta) {
        val initialSnapshot = (tracker == null)
        val activeTracker = tracker ?: run {
            ChallengeTracker(this@ChallengeSessionViewModel.challenge).also {
                val gameStats = screenModel.uiState.gameController.statistics ?: error("Missing game statistics")
                it.initialize(state, gameStats)
                startedAt = TimeSource.Monotonic.markNow()
            }
        }
        tracker = activeTracker
        if (!initialSnapshot && finishedAfter == null) {
            val current = activeTracker.evaluate(state, delta)
            if (outcome.value != current) {
                outcome.value = current
                if (current.isFinished) {
                    val end = TimeSource.Monotonic.markNow()
                    finishedAfter = end - startedAt!!
                    finishedActionCount = tracker?.gameActions?.size ?: 0
                    // Nothing more should reach the engine: the pitch stays
                    // clickable behind the result dialog.
                    screenModel.uiState.freezeActions()
                    // Only a solved attempt is recorded - `ChallengeTracker`
                    // scores a COMPLETED outcome and nothing else, so doing this
                    // for a failure threw.
                    if (current == ChallengeOutcome.COMPLETED) {
                        screenModel.screenModelScope.launch {
                            setSolved()
                        }
                    }
                }
            }
        }
    }

    suspend fun setSolved() {
        val score = tracker?.score ?: error("Challenge ${challenge.id} is not completed")
        val actions = tracker?.gameActions ?: emptyList()
        repository.solve(
            challenge.id,
            score,
            finishedAfter!!,
            actions
        )
    }
}
