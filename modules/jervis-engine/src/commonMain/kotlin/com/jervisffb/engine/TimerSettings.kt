package com.jervisffb.engine

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

enum class OutOfTimeBehaviour {
    NONE,
    SHOW_WARNING,
    OPPONENT_CALL_TIMEOUT,
    AUTOMATIC_TIMEOUT
}

enum class GameLimitReachedBehaviour {
    NONE,
    ROLL_OVER_STAND_UP,
    AUTOMATIC_END_TURN,
    FORFEIT_GAME,
}

/**
 * This class describes which timer rules apply to a specific game (if any).
 *
 * Tracking used time is done inside the [GameEngineController], but reacting to time-outs is done
 * by upper layers. For the UI, this is done through the appropriate `UiActionProvider`, and the server does it
 * through `GameActionHandlers`
 */
data class TimerSettings(
    val timersEnabled: Boolean = false,

    val gameLimit: Duration? = null,
    val gameBuffer: Duration = Duration.ZERO,
    val extraOvertimeLimit: Duration = Duration.ZERO, // Will be added to total limit if overtime is reached
    val extraOvertimeBuffer: Duration = Duration.ZERO, // Will be added to total buffer if overtime is reached

    val outOfTimeBehaviour: OutOfTimeBehaviour = OutOfTimeBehaviour.NONE,
    val gameLimitReached: GameLimitReachedBehaviour = GameLimitReachedBehaviour.NONE,

    val setupUseBuffer: Boolean = false,
    val setupFreeTime: Duration = Duration.ZERO,
    val setupMaxTime: Duration? = null, // How much time is allowed in total to be used before `outOfTime` is triggered

    val turnUseBuffer: Boolean = false,
    val turnFreeTime: Duration = Duration.ZERO,
    val turnMaxTime: Duration? = null,

    val outOfTurnResponseUseBuffer: Boolean = false,
    val outOfTurnResponseFreeTime: Duration = Duration.ZERO,
    val outOfTurnResponseMaxTime: Duration? = null,
) {
    /**
     * Default configurations for common scenarios.
     */
    companion object {
        val HARD_LIMIT = TimerSettings(
            timersEnabled = true,

            gameLimit = null,
            gameBuffer = 0.minutes,
            extraOvertimeLimit = 0.minutes,
            extraOvertimeBuffer = 0.minutes,

            outOfTimeBehaviour = OutOfTimeBehaviour.AUTOMATIC_TIMEOUT,
            gameLimitReached = GameLimitReachedBehaviour.FORFEIT_GAME,

            setupUseBuffer = false,
            setupFreeTime = 4.minutes,
            setupMaxTime = null,

            turnUseBuffer = false,
            turnFreeTime = 4.minutes,
            turnMaxTime = null,

            outOfTurnResponseUseBuffer = false,
            outOfTurnResponseFreeTime = 30.seconds,
            outOfTurnResponseMaxTime = null,
        )
        val CHESS_CLOCK = TimerSettings(
            timersEnabled = true,

            gameLimit = null,
            gameBuffer = (16*4.5+4).minutes, // Chess clock set to 4.5 minutes pr. turn + 4 minutes of initial setup
            extraOvertimeLimit = 0.minutes,
            extraOvertimeBuffer = (8*4.5+4).minutes,

            outOfTimeBehaviour = OutOfTimeBehaviour.AUTOMATIC_TIMEOUT,
            gameLimitReached = GameLimitReachedBehaviour.FORFEIT_GAME,

            setupUseBuffer = true,
            setupFreeTime = 0.minutes,
            setupMaxTime = null,

            turnUseBuffer = true,
            turnFreeTime = 0.minutes,
            turnMaxTime = null,

            outOfTurnResponseUseBuffer = true,
            outOfTurnResponseFreeTime = 0.minutes,
            outOfTurnResponseMaxTime = null,

        )
        val BB_CLOCK = TimerSettings(
            timersEnabled = true,

            gameLimit = null,
            gameBuffer = 15.minutes,
            extraOvertimeLimit = 0.minutes,
            extraOvertimeBuffer = 7.5.minutes,

            outOfTimeBehaviour = OutOfTimeBehaviour.OPPONENT_CALL_TIMEOUT,
            gameLimitReached = GameLimitReachedBehaviour.ROLL_OVER_STAND_UP,

            setupUseBuffer = true,
            setupFreeTime = 3.minutes,
            setupMaxTime = 5.minutes,

            turnUseBuffer = true,
            turnFreeTime = 3.minutes,
            turnMaxTime = 5.minutes,

            outOfTurnResponseUseBuffer = true,
            outOfTurnResponseFreeTime = 30.seconds,
            outOfTurnResponseMaxTime = 1.minutes,
        )
    }
}
