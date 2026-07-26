package com.jervisffb.ui.game.state

import com.jervisffb.ui.game.viewmodel.ReplayControllerViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * The direction a replay is currently moving in.
 */
enum class ReplayDirection {
    BACKWARD,
    PAUSED,
    FORWARD,
}

/**
 * Description of the current playback state.
 * [speed] is ignored while [direction] is [ReplayDirection.PAUSED].
 */
data class ReplayPlayback(
    val direction: ReplayDirection,
    val speed: Int = 1,
)

/**
 * Control surface for driving a replay.
 *
 * See [ReplayActionProvider]
 * See [ReplayControllerViewModel]
 */
interface ReplayController {
    // The current playback state
    val playback: StateFlow<ReplayPlayback>
    // Number of recorded actions currently applied (0 == at the start).
    val position: StateFlow<Int>
    // Total number of recorded actions in the replay.
    val totalActions: Int

    /** Rewind to the very start of the game as fast as possible. */
    fun jumpToStart()
    /** Play backwards, cycling the fast speeds 2x -> 3x -> 4x -> 2x. */
    fun fastBackward()
    /** Play backwards at 1x speed. */
    fun backward()
    /** Pause playback (the initial state when a replay is loaded). */
    fun pause()
    /** Play forwards at 1x speed. */
    fun forward()
    /** Play forwards, cycling the fast speeds 2x -> 3x -> 4x -> 2x. */
    fun fastForward()
    /** Fast-forward to the very end of the game as fast as possible. */
    fun jumpToEnd()
}
