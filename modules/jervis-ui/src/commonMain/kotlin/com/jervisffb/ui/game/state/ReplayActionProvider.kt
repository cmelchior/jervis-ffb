package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Team
import com.jervisffb.fumbbl.net.adapter.CalculatedJervisAction
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.fumbbl.net.adapter.JervisAction
import com.jervisffb.fumbbl.net.adapter.OptionalJervisAction
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ReplayActionProvider(private val menuViewModel: MenuViewModel, private val fumbbl: FumbblReplayAdapter?): UiActionProvider() {

    companion object {
        val LOG = jervisLogger()
    }

    private var job: Job? = null
    private var paused = false
    private lateinit var controller: GameEngineController
    private lateinit var actions: ActionRequest
    var started = false
    private val pauseMutex = Mutex(locked = true)
    private var sharedData: LocalFieldDataWrapper? = null

    override fun startHandler() {
        if (started) return
        started = true
        var index = 0
        val replayCommands = fumbbl?.getCommands()!!
        paused = true
        job = actionScope.launch {
            while (index < replayCommands.size) {
                pauseMutex.withLock {
                    // We just use the event here as a trigger from the UI to find the next available action
                    val ignore = actionRequestChannel.receive()
                    var j = index
                    var foundAction = false
                    while (j < replayCommands.size && !foundAction) {
                        val commandFromReplay = replayCommands[j]
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
                                    }
                                }
                                """.trimIndent(),
                            )
                        }
                        when (commandFromReplay) {
                            is CalculatedJervisAction -> {
                                foundAction = true
                                userActionSelected(commandFromReplay.actionFunc(controller.state, controller.rules))
                            }
                            is JervisAction -> {
                                foundAction = true
                                userActionSelected(commandFromReplay.action)
                            }
                            is OptionalJervisAction -> {
                                if (controller.currentNode() == commandFromReplay.expectedNode) {
                                    foundAction = true
                                    userActionSelected(commandFromReplay.action)
                                } else {
                                    LOG.d {
                                        """
                                    Skipping Optional Action: ${commandFromReplay.action}
                                    Current node: ${controller.state.stack.currentNode()::class.qualifiedName}
                                    Expected node: ${commandFromReplay.expectedNode::class.qualifiedName}
                                        """.trimIndent()
                                    }
                                    j++
                                }
                            }
                        }
                    }
                    index = j + 1
                    delay(100)
                }
            }
        }
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        // Do nothing
    }

    override fun updateSharedData(sharedData: LocalFieldDataWrapper) {
        this.sharedData = sharedData
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        this.controller = controller
        this.actions = controller.getAvailableActions()
    }

    override fun decorateAvailableActions(actions: ActionRequest, acc: UiSnapshotAccumulator) {
        println("Decorating available actions for ReplayActionProvider")
        // Do nothing
    }

    override fun decorateSelectedAction(action: GameAction, acc: UiSnapshotAccumulator) {
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
        // Do nothing
    }

    override fun registerQueuedActionGenerator(generator: QueuedActionsGenerator) {
        // Do nothing
    }

    override fun hasQueuedActions(): Boolean {
        return false
    }

    fun startActionProvider() {
        menuViewModel.navigatorContext.launch {
            if (paused) {
                paused = false
                pauseMutex.unlock() // Resume execution
            }
        }
    }

    fun pauseActionProvider() {
        menuViewModel.navigatorContext.launch {
            if (!paused) {
                paused = true
                pauseMutex.lock()
            }
        }
    }
}
