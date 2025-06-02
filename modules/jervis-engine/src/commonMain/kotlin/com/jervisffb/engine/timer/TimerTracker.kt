package com.jervisffb.engine.timer

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.utils.jervisLogger
import com.jervisffb.utils.singleThreadDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.random.Random
import kotlin.time.Duration

/**
 * Class responsible for tracking game timers as well as calculating actions
 * once it happens.
 */
@OptIn(ExperimentalAtomicApi::class)
class TimerTracker(private val settings: TimerSettings, random: Random,) {

    companion object {
        val LOG = jervisLogger()
    }

    data class TurnId(val half: Int, val turn: String) {
        constructor(game: Game) : this(game.halfNo, "${game.homeTeam.turnMarker}-${game.awayTeam.turnMarker}")
    }

    // Need to
    class TeamTimerData(private val settings: TimerSettings) {
        /**
         * Only used if [TimerSettings.gameLimit] has a value
         */
        var gameTimeUsed: Duration = Duration.Companion.ZERO
        /**
         * Only used if [TimerSettings.gameBuffer] is non-zero.
         */
        val totalBuffer = settings.gameBuffer
        var bufferUsed: Duration = Duration.Companion.ZERO

        // How to track the exact phase?
        val setup: MutableMap<TurnId, Duration> = mutableMapOf()
        var turn: MutableMap<TurnId, Duration> = mutableMapOf()
        // For "out-of-turns" events we use the size of the `history`
        // as an ID. This should work since `Undo` will remove entries
        // from this. We cannot use the `GameActionId` itself, since that
        // keeps incrementing, also for Undo actions.
        var outOfTurn: MutableMap<Int, Duration> = mutableMapOf()

        fun calculateTimeLeftForSetupAction(gameTimeUsed: Duration, setupTimeUsed: Duration): Duration {
            if (outOfGameTime(gameTimeUsed)) return Duration.Companion.ZERO
            val gameLimit = settings.gameLimit
            val useBuffer = settings.setupUseBuffer
            val freeTime = settings.setupFreeTime
            val actionTime = settings.setupActionTime
            return calculateTimeLeftForAction(useBuffer, gameLimit, gameTimeUsed, freeTime, actionTime, setupTimeUsed)
        }

        fun calculateTimeLeftForTurnAction(gameTimeUsed: Duration, turnTimeUsed: Duration): Duration {
            if (outOfGameTime(gameTimeUsed)) return Duration.Companion.ZERO
            val gameLimit = settings.gameLimit
            val useBuffer = settings.turnUseBuffer
            val freeTime = settings.turnFreeTime
            val actionTime = settings.turnActionTime
            return calculateTimeLeftForAction(useBuffer, gameLimit, gameTimeUsed, freeTime, actionTime, turnTimeUsed)
        }

        fun saveSetupTimeUsed(id: TurnId, timeUsed: Duration) {
            gameTimeUsed += timeUsed
            bufferUsed += (timeUsed - settings.setupFreeTime)
            setup[id] = setup[id]!! + timeUsed
        }

        fun saveTurnTimeUsed(id: TurnId, timeUsed: Duration) {
            gameTimeUsed += timeUsed
            bufferUsed += (timeUsed - settings.turnFreeTime)
            turn[id] = turn[id]!! + timeUsed
        }

        fun saveOutOfTurnTimeUsed(id: Int, timeUsed: Duration) {
            gameTimeUsed += timeUsed
            bufferUsed += (timeUsed - settings.outOfTurnResponseFreeTime)
            outOfTurn[id] = outOfTurn[id]!! + timeUsed
        }

        fun calculateTimeLeftOutOfTurnAction(gameTimeUsed: Duration, outOfTurnTimeUsed: Duration): Duration {
            if (outOfGameTime(gameTimeUsed)) return Duration.Companion.ZERO
            val gameLimit = settings.gameLimit
            val useBuffer = settings.outOfTurnResponseUseBuffer
            val freeTime = settings.outOfTurnResponseFreeTime
            val actionTime = settings.outOfTurnResponseActionTime
            return calculateTimeLeftForAction(useBuffer, gameLimit, gameTimeUsed, freeTime, actionTime, outOfTurnTimeUsed)
        }

        private fun outOfGameTime(gameTimeUsed: Duration): Boolean {
            return settings.gameLimit != null && settings.gameLimit <= gameTimeUsed
        }

        // Helper method, making sure that all action time calculations flow through the same flow.
        private fun calculateTimeLeftForAction(
            useBuffer: Boolean,
            gameLimit: Duration?,
            gameTimeUsed: Duration,
            freeTime: Duration,
            actionTime: Duration?,
            actionTimeUsed: Duration
        ): Duration {
            if (useBuffer) {
                val totalTimeLeft = gameLimit?.let { it - gameTimeUsed } ?: Duration.INFINITE
                val totalBufferLeft = totalBuffer - bufferUsed
                val time = (freeTime + totalBufferLeft)
                    // Restrict to action time, taking into account any time already used
                    .coerceAtMost(actionTime?.let { it - actionTimeUsed } ?: Duration.INFINITE)
                    // Restrict to the global time limit
                    .coerceAtMost(totalTimeLeft)
                return time
            } else {
                val totalTimeLeft = gameLimit?.let { it - gameTimeUsed } ?: Duration.INFINITE
                return totalTimeLeft.coerceAtMost(actionTime?.let { it - actionTimeUsed } ?: Duration.INFINITE)
            }
        }
    }

    val gameStart: Instant = Clock.System.now()
    private val homeTeamData = TeamTimerData(settings)
    private val awayTeamData = TeamTimerData(settings)
    private val timerScope = CoroutineScope(CoroutineName("TimerTrackerScope") + singleThreadDispatcher("TimerTracker"))
    private var timerJob: Job? = null
    private val outOfTime = AtomicBoolean(false)
    private var nextActionBy: Instant? = null
    private var updateTimerHandler: ((Instant) -> Unit)? = null

    /**
     * Checks if the action owner for the current state of the game is out of time.
     */
    fun isOutOfTime(controller: GameEngineController): Boolean {
        return false
        // TODO Why do we need the controller state here?
        //  Was it because Undo might mess it up?
    }

    /**
     * Returns when the next action should be generated by.

     * This can also be a point in time before the current wall-clock. If that is the
     * case, note there is no guarantee that [isOutOfTime] returns `true`, although
     * it will generally be the case.
     */
    fun getDeadlineForNextAction(): Instant? {
        return nextActionBy
    }

    /**
     * Starts a new action request cycle. Timers will also start when calling this action.
     *
     * @param controller
     * @param onOutOfTime called if a timeout is triggered. This is a hint to the UI to create
     * an [com.jervisffb.engine.actions.OutOfTime] action. It cannot be created here, because it
     * would interrupt the UI event flow.
     *
     * @return `true` if the user is still allowed to select their own action. `false` if the
     * control is out of the coaches hand. Either because the opponent selected out of time or
     * OutOfTimeBehavior.AUTOMATIC_TIMEOUT is the chosen
     */
    fun startNextAction(
        controller: GameEngineController,
        onOutOfTime: suspend (GameActionId) -> Unit
    ): Boolean {
        if (!settings.timersEnabled) return true

        val state = controller.state
        val rules = controller.rules
        val actions = controller.getAvailableActions()
        val (start, timeLeft, updateCallback) = calculateTimeLeftForAction(controller, actions)

        if (rules.isInSetupPhase(state)) {

        }



        // If the last event was `OutOfTime`, it means we are in the process of exiting the current "phase".








        return if (timeLeft <= Duration.ZERO) {
            nextActionBy = start + timeLeft
            outOfTime.store(true)
            false
        } else {
            updateTimerHandler = updateCallback
            nextActionBy = start + timeLeft
            timerJob = timerScope.launch {
                delay(timeLeft)
                outOfTime.store(true)
                onOutOfTime(actions.nextActionId)
            }
            true
        }
    }

    /**
     * Returns true if the current action owner is out of time.
     * This should only be called between [startNextAction] and
     * [stopTimer]
     */
    fun isOutOfTime(): Boolean {
        if (!settings.timersEnabled) return false
        return outOfTime.load()
    }

    /**
     * Stop the timer for the current action-request cycle and record
     * the time used.
     *
     * This will also cancel the generation of timer-related actions. If the timer
     * action manages to trigger just after the user sends their action, the timer
     * action will be ignored.
     */
    fun stopTimer() {
        if (!settings.timersEnabled) return
        updateTimerHandler?.invoke(Clock.System.now())
        timerJob?.cancel()
        timerJob = null
        nextActionBy = null
        outOfTime.store(false)
    }

    /**
     * Returns the appropriate out-of-time action based on the current state and timer-settings.
     * If called when no time-out actually occurred an [IllegalStateException] is thrown.
     */
    fun getOutOfTimeAction(state: Game, actions: ActionRequest) : GameAction {
        if (!settings.timersEnabled) error("Timer disabled")

        // If during setup, we want to end the setup as quickly as possible (hopefully
        // with a legal format)
        if (state.rules.isInSetupPhase(state)) {
            return OutOfTimeActionHelper.calculateOutOfTimeSetupActions(state, actions)
        }

        // There are probably other scenarios that require specific handling, they should go here
        // ...

        // Next, we attempt to cancel whatever situation we are in; this means prioritizing
        // "end" or "cancel actions.
        // Doing 5n checks is probably not the most optimal, but something for the future, and
        // most actions (outside setup and passing has relatively few options).
        val exitAction = OutOfTimeActionHelper.calculateExitAction(state, actions)
        if (exitAction != null) return exitAction

        // Finally, if we haven't been able to find a good action, just select one
        // at random (We probably want to make this an error at some point).
        LOG.d { "Calculate random out-of-time action" }
        return createRandomAction(state, actions)
    }

    private fun calculateTimeLeftForAction(
        controller: GameEngineController,
        actions: ActionRequest
    ): Triple<Instant, Duration, (Instant) -> Unit> {
        val start = Clock.System.now() // Move this elsewhere?
        val actionOwner = actions.team
        val timerData = when (actionOwner.isHomeTeam()) {
            true -> homeTeamData
            false -> awayTeamData
        }
        val game = controller.state
        val usedGameTime = timerData.gameTimeUsed

        // Figure out which of the 3 states we are tracking timers for and handle them.
        return if (controller.rules.isInSetupPhase(controller.state)) {
            val id = TurnId(game)
            val usedSetupTime = timerData.setup.getOrPut(id) { Duration.ZERO }
            val timeLeft = timerData.calculateTimeLeftForSetupAction(usedGameTime, usedSetupTime)
            // LOG.i { "Timeleft (setup) (${controller.nextActionIndex()}): ${timeLeft.inWholeSeconds}"}
            Triple(
                start,
                timeLeft,
                { end: Instant -> timerData.saveSetupTimeUsed(id, end - start) }
            )
        } else if (controller.state.activeTeam == actionOwner) {
            val id = TurnId(game)
            val usedTurnTime = timerData.turn.getOrPut(id) { Duration.ZERO }
            val timeLeft = timerData.calculateTimeLeftForTurnAction(usedGameTime, usedTurnTime)
            // LOG.i { "Timeleft (turn) (${controller.nextActionIndex()}): ${timeLeft.inWholeSeconds}"}
            Triple(
                start,
                timeLeft,
                { end: Instant -> timerData.saveTurnTimeUsed(id, end - start) }
            )
        } else {
            val id = controller.history.size
            val usedOutOfTurnTime = timerData.outOfTurn.getOrPut(id) { Duration.ZERO }
            val timeLeft = timerData.calculateTimeLeftOutOfTurnAction(usedGameTime, usedOutOfTurnTime)
            // LOG.i { "Timeleft (out-of-turn) (${controller.nextActionIndex()}): ${timeLeft.inWholeSeconds}"}
            Triple(
                start,
                timeLeft,
                { end: Instant -> timerData.saveOutOfTurnTimeUsed(id, end - start) }
            )
        }
    }
}
