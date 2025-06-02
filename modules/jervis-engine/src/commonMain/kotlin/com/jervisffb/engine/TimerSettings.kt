package com.jervisffb.engine

import com.jervisffb.engine.OutOfTimeBehaviour.SHOW_WARNING
import com.jervisffb.engine.TimerPreset.CUSTOM
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Enum describing pre-defined types of Blood Bowl clocks.
 *
 * Any change to an existing clock should change its type
 * to [CUSTOM].
 */
enum class TimerPreset {
    HARD_LIMIT,
    CHESS_CLOCK,
    BB_CLOCK,
    CUSTOM,
}

/**
 * Enum describing what happens when a Coach reaches the "Max time" limit
 * for a given action (but hasn't reached the Game Limit yet)
 *
 * Developer's Commentary:
 * Currently, the game clock keeps ticking when Out-of-Time is hit. This is
 * because we want to also accurately track the total game time. This means
 * it is possible to reach "Out of Time" with a setting of [SHOW_WARNING], and
 * then "Game Limit" if no action is taken.
 */
enum class OutOfTimeBehaviour {
    // Nothing happens, the game just continues. TODO This should probably be rolled into SHOW_WARNING?
    NONE,
    // A warning should be shown to the coach who needs to take the action, but
    // otherwise nothing happens.
    SHOW_WARNING,
    // A button should pop up for the opposing coach, which allows them to time
    // the coach out. This is the current FUMBBL behavior.
    OPPONENT_CALL_TIMEOUT,
    // An automatic time-out is called by the server as soon as the max limit
    // is hit.
    AUTOMATIC_TIMEOUT
}

/**
 * Enum describing what happens when a coach reaches the time allocated to them
 * for the entire game.
 */
enum class GameLimitReachedBehaviour {
    // A warning should be shown to the coach who has run out of time, but
    // otherwise nothing happens.
    NONE,
    // Knocked-down players will stand up (if they can, without rolling dice)
    // Otherwise the turn end immediately, and no other actions are allowed.
    ROLL_OVER_STAND_UP,
    // Stunned players will roll over, but otherwise the turn ends immediately
    // without the coach being allowed any actions.
    AUTOMATIC_END_TURN,
    // The coach who ran out of time will
    FORFEIT_GAME,
}

/**
 * This class describes which timer rules apply to a specific game (if any).
 *
 * TODO: This is not true. I don't think the GameEngineController is the correct place since
 *  in P2P Games we need to override user actions. This needs to happen either on the server
 *  in the GameActionHandler or inside the `LocalActionProvider` (which is what drives HotSeat games)
 * Tracking used time is done inside the [GameEngineController], but reacting to time-outs is done
 * by upper layers. For the UI, this is done through the appropriate `UiActionProvider`, and the server does it
 * through `GameActionHandlers`
 */
@Serializable
data class TimerSettings(
    val timersEnabled: Boolean = false,
    val preset: TimerPreset = TimerPreset.BB_CLOCK,

    // Overall game limit. This counts all time used across all actions
    val gameLimit: Duration? = null,
    // Overall game buffer. Only used for timers with "Use Buffer" enabled.
    // It is used to cover the difference between "free time" and "action time"
    //If no buffer is available, only "free time" is available.
    val gameBuffer: Duration = Duration.ZERO,
    val extraOvertimeLimit: Duration = Duration.ZERO, // Will be added to total limit if overtime is reached
    val extraOvertimeBuffer: Duration = Duration.ZERO, // Will be added to total buffer if overtime is reached

    val outOfTimeBehaviour: OutOfTimeBehaviour = OutOfTimeBehaviour.NONE,
    val gameLimitReached: GameLimitReachedBehaviour = GameLimitReachedBehaviour.NONE,

    val setupUseBuffer: Boolean = false,
    val setupActionTime: Duration? = null,
    val setupFreeTime: Duration = Duration.ZERO,

    val turnUseBuffer: Boolean = false,
    val turnActionTime: Duration? = null,
    val turnFreeTime: Duration = Duration.ZERO,

    val outOfTurnResponseUseBuffer: Boolean = false,
    val outOfTurnResponseActionTime: Duration? = null,
    val outOfTurnResponseFreeTime: Duration = Duration.ZERO,
) {

    /**
     * Returns this instance as a [Builder], making it easier to update.
     */
    fun toBuilder(): Builder {
        return Builder(this)
    }

    /**
     * Class making it easier to incrementally update timer settings, like through the UI.
     */
    class Builder(timerSettings: TimerSettings) {
        var timersEnabled: Boolean = timerSettings.timersEnabled
        var preset: TimerPreset = timerSettings.preset
        var gameLimit: Duration? = timerSettings.gameLimit
        var gameBuffer: Duration = timerSettings.gameBuffer
        var extraOvertimeLimit: Duration = timerSettings.extraOvertimeLimit
        var extraOvertimeBuffer: Duration = timerSettings.extraOvertimeBuffer
        var outOfTimeBehaviour: OutOfTimeBehaviour = timerSettings.outOfTimeBehaviour
        var gameLimitReached: GameLimitReachedBehaviour = timerSettings.gameLimitReached
        var setupUseBuffer: Boolean = timerSettings.setupUseBuffer
        var setupActionTime: Duration? = timerSettings.setupActionTime
        var setupFreeTime: Duration = timerSettings.setupFreeTime
        var turnUseBuffer: Boolean = timerSettings.turnUseBuffer
        var turnActionTime: Duration? = timerSettings.turnActionTime
        var turnFreeTime: Duration = timerSettings.turnFreeTime
        var outOfTurnResponseUseBuffer: Boolean = timerSettings.outOfTurnResponseUseBuffer
        var outOfTurnResponseActionTime: Duration? = timerSettings.outOfTurnResponseActionTime
        var outOfTurnResponseFreeTime: Duration = timerSettings.outOfTurnResponseFreeTime

        fun build() = TimerSettings(
            timersEnabled,
            preset,
            gameLimit,
            gameBuffer,
            extraOvertimeLimit,
            extraOvertimeBuffer,
            outOfTimeBehaviour,
            gameLimitReached,
            setupUseBuffer,
            setupActionTime,
            setupFreeTime,
            turnUseBuffer,
            turnActionTime,
            turnFreeTime,
            outOfTurnResponseUseBuffer,
            outOfTurnResponseActionTime,
            outOfTurnResponseFreeTime
        )
    }

    /**
     * Default configurations for common scenarios.
     */
    companion object {
        val HARD_LIMIT = TimerSettings(
            timersEnabled = true,
            preset = TimerPreset.HARD_LIMIT,

            gameLimit = null,
            gameBuffer = 0.minutes,
            extraOvertimeLimit = 0.minutes,
            extraOvertimeBuffer = 0.minutes,

            outOfTimeBehaviour = OutOfTimeBehaviour.AUTOMATIC_TIMEOUT,
            gameLimitReached = GameLimitReachedBehaviour.FORFEIT_GAME,

            setupUseBuffer = false,
            setupActionTime = 4.minutes,
            setupFreeTime = Duration.ZERO,

            turnUseBuffer = false,
            turnActionTime = 4.minutes,
            turnFreeTime = Duration.ZERO,

            outOfTurnResponseUseBuffer = false,
            outOfTurnResponseActionTime = 30.seconds,
            outOfTurnResponseFreeTime = Duration.ZERO,
        )
        val CHESS_CLOCK = TimerSettings(
            timersEnabled = true,
            preset = TimerPreset.CHESS_CLOCK,

            gameLimit = null,
            gameBuffer = (16*4.5+4).minutes, // Chess clock set to 4.5 minutes pr. turn + 4 minutes of initial setup
            extraOvertimeLimit = 0.minutes,
            extraOvertimeBuffer = (8*4.5+4).minutes,

            outOfTimeBehaviour = OutOfTimeBehaviour.AUTOMATIC_TIMEOUT,
            gameLimitReached = GameLimitReachedBehaviour.FORFEIT_GAME,

            setupUseBuffer = true,
            setupActionTime = null,
            setupFreeTime = Duration.ZERO,

            turnUseBuffer = true,
            turnActionTime = null,
            turnFreeTime = Duration.ZERO,

            outOfTurnResponseUseBuffer = true,
            outOfTurnResponseActionTime = null,
            outOfTurnResponseFreeTime = Duration.ZERO,

        )
        val BB_CLOCK = TimerSettings(
            timersEnabled = true,
            preset = TimerPreset.BB_CLOCK,

            gameLimit = null,
            gameBuffer = 15.minutes,
            extraOvertimeLimit = 0.minutes,
            extraOvertimeBuffer = 7.5.minutes,

            outOfTimeBehaviour = OutOfTimeBehaviour.OPPONENT_CALL_TIMEOUT,
            gameLimitReached = GameLimitReachedBehaviour.ROLL_OVER_STAND_UP,

            setupUseBuffer = true,
            setupActionTime = 5.minutes,
            setupFreeTime = 3.minutes,

            turnUseBuffer = true,
            turnActionTime = 5.minutes,
            turnFreeTime = 3.minutes,

            outOfTurnResponseUseBuffer = true,
            outOfTurnResponseActionTime = 1.minutes,
            outOfTurnResponseFreeTime = 30.seconds,
        )
    }
}
