package com.jervisffb.ui.game.viewmodel

import com.jervisffb.ui.game.state.ReplayController
import com.jervisffb.ui.game.state.ReplayPlayback
import kotlinx.coroutines.flow.StateFlow

/**
 * View model for controlling replaying a Jervis game.
 *
 * It delegates to whatever [ReplayController] is driving the current replay (the
 * native [com.jervisffb.ui.game.state.ReplayActionProvider] for `.jrg` save
 * files, or the FUMBBL [com.jervisffb.ui.game.state.ReplayActionProvider] for the
 * developer FUMBBL replays). It is only created when the game is in replay mode.
 */
class ReplayControllerViewModel(
    private val controller: ReplayController,
) {
    val playback: StateFlow<ReplayPlayback> = controller.playback
    val position: StateFlow<Int> = controller.position
    val totalActions: Int = controller.totalActions
    fun jumpToStart() = controller.jumpToStart()
    fun fastBackward() = controller.fastBackward()
    fun backward() = controller.backward()
    fun pause() = controller.pause()
    fun forward() = controller.forward()
    fun fastForward() = controller.fastForward()
    fun jumpToEnd() = controller.jumpToEnd()
}
