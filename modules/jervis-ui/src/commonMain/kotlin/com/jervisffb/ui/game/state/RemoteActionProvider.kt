package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Action Provider that is responsible for forwarding appropriate events from the server
 * in P2P games.
 *
 * For now, it just stores all events from the server and returns them in order when asked
 * for it. Not 100% sure this is the best architecture though. Something to think about.
 */
class RemoteActionProvider(
    val clientMode: TeamActionMode,
    val controller: GameEngineController
): UiActionProvider() {

    companion object {
        val LOG = jervisLogger()
    }

    private var job: Job? = null
    private var paused = false
    private lateinit var actions: ActionRequest

    override fun startHandler() {
        // Do nothing
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        // Do nothing
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        this.actions = controller.getAvailableActions()
    }

    override fun decorateAvailableActions(actions: ActionRequest, acc: UiSnapshotAccumulator) {
        // Do nothing
    }

    override fun decorateSelectedAction(action: GameAction, acc: UiSnapshotAccumulator) {
        // Do nothing
    }

    override suspend fun getAction(): GameAction {
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

    override fun registerQueuedActionGenerator(generator: QueuedActionsGenerator) {
        TODO("Not yet implemented")
    }

    override fun hasQueuedActions(): Boolean {
        return false
    }
}
