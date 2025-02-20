package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.fumbbl.net.adapter.CalculatedJervisAction
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.fumbbl.net.adapter.JervisAction
import com.jervisffb.fumbbl.net.adapter.OptionalJervisAction
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReplayActionProvider(private val uiState: UiGameController, private val fumbbl: FumbblReplayAdapter?): UiActionProvider() {

    private var job: Job? = null
    private var paused = false
    private lateinit var controller: GameEngineController
    private lateinit var actions: ActionRequest
    var started = false


    override fun prepareForNextAction(controller: GameEngineController) {
        this.controller = controller
        this.actions = controller.getAvailableActions()
    }

    override fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionRequest) {
        // Do nothing
    }

    override fun decorateSelectedAction(state: UiGameSnapshot, action: GameAction) {
        // Do nothing
    }

    override suspend fun getAction(): GameAction {
        actionRequestChannel.send(Pair(controller, actions))
        return actionSelectedChannel.receive()
    }

    override fun userActionSelected(action: GameAction) {
        actionScope.launch {
            actionSelectedChannel.send(action)
        }
    }

    override fun userMultipleActionsSelected(actions: List<GameAction>, delayEvent: Boolean) {
        TODO("Not yet supported")
    }

    fun startActionProvider() {
        if (started) return
        started = true
        var index = 0
        val replayCommands = fumbbl?.getCommands()!!
        paused = false
        job = actionScope.launch {
            while (!paused && index <= replayCommands.size) {
                val ignore = actionRequestChannel.receive()
                val commandFromReplay = replayCommands[index]
                if (commandFromReplay !is OptionalJervisAction && commandFromReplay.expectedNode != controller.state.stack.currentNode()) {
                    throw IllegalStateException(
                        """
                        Current node: ${controller.state.stack.currentNode()::class.qualifiedName}
                        Expected node: ${commandFromReplay.expectedNode::class.qualifiedName}
                        Action: ${
                            when (commandFromReplay) {
                                is CalculatedJervisAction ->
                                    commandFromReplay.actionFunc(
                                        controller.state,
                                        controller.rules,
                                    )
                                is JervisAction -> commandFromReplay.action
                                is OptionalJervisAction -> commandFromReplay.action
                            }}
                        """.trimIndent(),
                    )
                }
                when (commandFromReplay) {
                    is CalculatedJervisAction ->
                        userActionSelected(commandFromReplay.actionFunc(controller.state, controller.rules))
                    is JervisAction -> userActionSelected(commandFromReplay.action)
                    is OptionalJervisAction -> {
                        if (controller.currentNode() == commandFromReplay.expectedNode) {
                            userActionSelected(commandFromReplay.action)
                        }
                    }
                }
                index += 1
                delay(100)
            }
        }
    }

    fun pauseActionProvider() {
        if (paused) {
            startActionProvider()
        } else {
            paused = true
        }
    }
}
