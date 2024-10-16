package com.jervisffb.ui.viewmodel

import com.jervisffb.fumbbl.net.adapter.CalculatedJervisAction
import com.jervisffb.fumbbl.net.adapter.JervisAction
import com.jervisffb.fumbbl.net.adapter.OptionalJervisAction
import com.jervisffb.ui.GameScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReplayModeUiActionFactory(model: GameScreenModel) : UiActionFactory(model) {
    override suspend fun start(scope: CoroutineScope) {
        var index = 0
        val replayCommands = model.fumbbl?.getCommands()
        scope.launch(Dispatchers.Default) {
            val controller = model.controller
            emitToField(WaitingForUserInput) // Init flows
            controller.startManualMode()
            while (!controller.state.stack.isEmpty()) {
                if (replayCommands != null && index <= replayCommands.size) {
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
                            controller.processAction(
                                commandFromReplay.actionFunc(controller.state, controller.rules),
                            )
                        is JervisAction -> controller.processAction(commandFromReplay.action)
                        is OptionalJervisAction -> {
                            if (controller.currentNode() == commandFromReplay.expectedNode) {
                                controller.processAction(commandFromReplay.action)
                            }
                        }
                    }
                    controller.state.notifyUpdate()
                    index += 1
                    delay(20)
                    continue
                }
            }
        }
    }
}
