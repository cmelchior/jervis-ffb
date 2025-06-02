package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.UiSnapshotTimerData
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
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

    override fun startHandler(uiTimerData: MutableSharedFlow<UiSnapshotTimerData>) {
        // Do nothing
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        // Do nothing
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        this.actions = controller.getAvailableActions()
    }

    override fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionRequest) {
        // Do nothing
    }

    override fun decorateSelectedAction(state: UiGameSnapshot, action: GameAction) {
        // Do nothing
    }

    override suspend fun getAction(id: GameActionId): GeneratedAction {
        var action: GeneratedAction? = null
        while (action == null) {
            val newAction = actionSelectedChannel.receive()
            when {
                newAction.id < id -> LOG.i { "[RemoteProvider] Dropping outdated action (${newAction.id} < $id: ${newAction.action}" }
                newAction.id > id -> error("Received future event. This should never happen (${newAction.id} > $id): ${newAction.action}")
                else -> {
                    action = newAction
                }
            }
        }
        return action
    }

    override fun userActionSelected(id: GameActionId, action: GameAction) {
        actionScope.launch {
            actionSelectedChannel.send(GeneratedAction(id,action))
        }
    }

    override fun userMultipleActionsSelected(startingId: GameActionId, actions: List<GameAction>, delayEvent: Boolean) {
        TODO("Not yet supported")
    }

    override fun registerQueuedActionGenerator(generator: QueuedActionsGenerator) {
        TODO("Not yet implemented")
    }

    override fun hasQueuedActions(): Boolean {
        return false
    }

    override fun clearQueuedActions() {
        // Do nothing
    }
}
