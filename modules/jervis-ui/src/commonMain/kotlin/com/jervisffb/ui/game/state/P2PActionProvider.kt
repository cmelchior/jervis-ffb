package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.menu.p2p.AbstractClintNetworkMessageHandler
import com.jervisffb.ui.menu.p2p.P2PClientGameController
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.launch

// For P2P Games, this means the server will be sending events if timers are hit
class P2PActionProvider(
    private val engine: GameEngineController,
    private val settings: GameSettings,
    private val homeProvider: UiActionProvider,
    private val awayProvider: UiActionProvider,
    private val connection: P2PClientGameController,
): UiActionProvider() {

    companion object {
        val LOG = jervisLogger()
    }
    var lastServerActionIndex: Int = -1

    private var currentProvider = homeProvider

    override fun startHandler() {
        connection.addMessageHandler(object: AbstractClintNetworkMessageHandler() {
            override fun onGameAction(producer: CoachId, serverIndex: Int, action: GameAction) {
                lastServerActionIndex = serverIndex
                if (producer == engine.state.homeTeam.coach.id) {
                    homeProvider.userActionSelected(action)
                } else {
                    awayProvider.userActionSelected(action)
                }
            }
        })
        homeProvider.startHandler()
        awayProvider.startHandler()
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        val clientIndex = engine.history.last().id
        // Should only send this if the event is truly from this client and not just a sync message
        LOG.d("Handling action ($clientIndex > $lastServerActionIndex): $action")
        if (clientIndex > lastServerActionIndex) {
            actionScope.launch {
                connection.sendActionToServer(clientIndex, action)
            }
        }
    }

    override fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        currentProvider = if (actions.team?.isAwayTeam() == true) {
            awayProvider
        } else {
            homeProvider
        }
    }

    override fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionRequest) {
        currentProvider.decorateAvailableActions(state, actions)
    }

    override fun decorateSelectedAction(state: UiGameSnapshot, action: GameAction) {
        currentProvider.decorateSelectedAction(state, action)
    }

    override suspend fun getAction(): GameAction {
        return currentProvider.getAction()
    }

    override fun userActionSelected(action: GameAction) {
        currentProvider.userActionSelected(action)
    }

    override fun userMultipleActionsSelected(actions: List<GameAction>, delayEvent: Boolean) {
        currentProvider.userMultipleActionsSelected(actions, delayEvent)
    }

    override fun registerQueuedActionGenerator(generator: QueuedActionsGenerator) {
        currentProvider.registerQueuedActionGenerator(generator)
    }
}
