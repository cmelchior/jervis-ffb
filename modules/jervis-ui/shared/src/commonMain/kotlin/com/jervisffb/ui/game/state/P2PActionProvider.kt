package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Team
import com.jervisffb.net.messages.GameActionServerError
import com.jervisffb.net.messages.ServerError
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import com.jervisffb.ui.menu.p2p.AbstractClintNetworkMessageHandler
import com.jervisffb.ui.menu.p2p.P2PClientNetworkAdapter
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

// Events are kept in the order they were received from the server.
private sealed interface P2PServerEvent {
    // A game action sent by the server that will move the local state forward.
    data class ServerGameAction(
        val serverIndex: GameActionId,
        val action: GameAction,
    ) : P2PServerEvent

    // The server rejected a game action created optimistically by this client.
    data class RejectedAction(
        // ID of the action that was rejected by the server.
        val rejectedActionId: GameActionId,
    ) : P2PServerEvent
}

/**
 * This [UiActionProvider] is the primary action provider for P2P games and are responsible for switching
 * between the local provider and the remote one (that is just receiving events from the server)
 */
class P2PActionProvider(
    private val engine: GameEngineController,
    private val settings: GameSettings,
    private val homeProvider: UiActionProvider,
    private val awayProvider: UiActionProvider,
    private val networkAdapter: P2PClientNetworkAdapter,
): UiActionProviderGroup() {

    companion object {
        val LOG = jervisLogger()
    }

    private var sharedData: LocalPitchDataWrapper? = null

    // Server events are kept separate from local provider actions and consumed
    // in the order they are received. A server action can arrive before the
    // rejection of the conflicting local action; therefore, that action stays
    // at the head while Revert actions moves the engine back to a state that
    // will accept that server action.
    private val serverEvents = Channel<P2PServerEvent>(capacity = Channel.UNLIMITED)
    private val queuedServerEvents = mutableListOf<P2PServerEvent>()
    private var handlingServerAction = false
    private var handlingServerRevert = false

    override var currentProvider: UiActionProvider = homeProvider
        private set

    // Returns `true` if _this_ client is responsible for creating the current action.
    // `false` if the action is expected to come from the server.
    fun currentClientIsCreatingAction(): Boolean {
        return currentProvider !is RemoteActionProvider
    }

    override fun init(controller: UiGameController) {
        homeProvider.init(controller)
        awayProvider.init(controller)
    }

    override fun startHandler() {
        networkAdapter.addMessageHandler(object: AbstractClintNetworkMessageHandler() {
            override fun onGameAction(producer: CoachId, serverIndex: GameActionId, action: GameAction) {
                queueServerAction(serverIndex, action)
            }

            override fun onServerError(error: ServerError) {
                // If actions are rejected on the server, we queue them up here to
                // be undone. The UI experience for this will probably be a bit
                // weird, since it will display the intermediate UI actions for
                // each step backwards, but since it is impossible to know when the
                // rollback is done, it is hard to do anything about it. Unless some
                // kind of "action-list" is supported in the network protocol. Something
                // for the future.
                when (error) {
                    is GameActionServerError -> {
                        LOG.i { "Queuing up Revert of action: ${error.actionId}" }
                        queueRejectedAction(error.actionId)
                    }
                    else -> { /* Ignore */ }
                }
            }
        })
        homeProvider.startHandler()
        awayProvider.startHandler()
    }

    override fun stopHandler() {
        homeProvider.stopHandler()
        awayProvider.stopHandler()
        super.stopHandler()
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        // Actions received from the server, including a local Revert, must never be sent back.
        if (handlingServerAction) return

        val clientActionIndex = engine.currentActionId()
        LOG.d("Sending message to server ($clientActionIndex): $action")
        actionScope.launch {
            networkAdapter.sendActionToServer(clientActionIndex, action)
        }
    }

    override fun updateSharedData(sharedData: LocalPitchDataWrapper) {
        this.sharedData = sharedData
        homeProvider.updateSharedData(sharedData)
        awayProvider.updateSharedData(sharedData)
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        handlingServerAction = false
        handlingServerRevert = false
        currentProvider = if (actions.team?.isAwayTeam() == true) {
            awayProvider
        } else {
            homeProvider
        }
        currentProvider.prepareForNextAction(controller, actions)
        if (!currentProvider.hasQueuedActions()) {
            drainServerEvents()
            discardReconciledRejections()
            handlingServerRevert = firstServerEventRequiresRevert(controller.nextActionIndex())
        }
    }

    override fun decorateAvailableActions(actions: ActionRequest, acc: UiSnapshotAccumulator) {
        if (!handlingServerRevert) {
            currentProvider.decorateAvailableActions(actions, acc)
        }
    }

    override fun decorateSelectedAction(action: GameAction, acc: UiSnapshotAccumulator) {
        if (!handlingServerRevert) {
            currentProvider.decorateSelectedAction(action, acc)

            if (handlingServerAction) {
                acc.actionWasSelectedWithoutUserInput = true
            }
        }
    }

    override suspend fun getAction(id: GameActionId): GameAction {
        // Finish a locally queued action sequence before reacting to the server. This keeps
        // generated multi-action paths atomic from the UI's perspective; any rejected actions
        // are reverted immediately afterwards.
        if (currentProvider.hasQueuedActions()) {
            return currentProvider.getAction(id)
        }

        drainServerEvents()
        takeServerAction(id)?.let { return it }

        return coroutineScope {
            val providerAction = async { currentProvider.getAction(id) }
            try {
                var selectedAction: GameAction? = null
                while (selectedAction == null) {
                    selectedAction = takeServerAction(id) ?: select {
                        // The select is intentionally biased toward the authoritative server when
                        // both it and the local provider have an action ready.
                        serverEvents.onReceive { event ->
                            queuedServerEvents.add(event)
                            null
                        }
                        providerAction.onAwait { action ->
                            handlingServerAction = false
                            handlingServerRevert = false
                            action
                        }
                    }
                }
                selectedAction
            } finally {
                // If a server event won, any local selection for this now-obsolete action id must
                // not leak into the new timeline.
                providerAction.cancel()
            }
        }
    }

    internal fun queueServerAction(serverIndex: GameActionId, action: GameAction) {
        check(serverEvents.trySend(P2PServerEvent.ServerGameAction(serverIndex, action)).isSuccess) {
            "Unable to queue synchronized server action $serverIndex: $action"
        }
    }

    internal fun queueRejectedAction(actionId: GameActionId) {
        check(serverEvents.trySend(P2PServerEvent.RejectedAction(actionId)).isSuccess) {
            "Unable to queue server rejection for action $actionId"
        }
    }

    private fun drainServerEvents() {
        while (true) {
            val event = serverEvents.tryReceive()
            when (event.isSuccess) {
                true ->queuedServerEvents.add(event.getOrThrow())
                false -> return
            }
        }
    }

    private fun takeServerAction(id: GameActionId): GameAction? {
        while (true) {
            discardReconciledRejections()
            val event = queuedServerEvents.firstOrNull() ?: return null

            when (event) {
                is P2PServerEvent.RejectedAction -> {
                    handlingServerAction = true
                    handlingServerRevert = true
                    LOG.i { "Reverting rejected action: ${event.rejectedActionId}" }
                    return Revert
                }
                is P2PServerEvent.ServerGameAction -> {
                    when {
                        event.serverIndex.counter < id.counter -> {
                            val actionIsStillApplied = engine.history.any { delta ->
                                delta.id.counter == event.serverIndex.counter
                            }
                            if (!actionIsStillApplied) {
                                LOG.w { "Ignoring outdated synchronized action ${event.serverIndex}: ${event.action}" }
                                queuedServerEvents.removeFirst()
                                continue
                            }

                            handlingServerAction = true
                            handlingServerRevert = true
                            LOG.i {
                                "Reverting local timeline for synchronized action " +
                                    "${event.serverIndex}: ${event.action}"
                            }
                            return Revert
                        }
                        event.serverIndex.counter > id.counter -> return null
                        else -> {
                            queuedServerEvents.removeFirst()
                            handlingServerAction = true
                            handlingServerRevert = event.action is Revert
                            LOG.i { "Handling synchronized action ${event.serverIndex}: ${event.action}" }
                            return event.action
                        }
                    }
                }
            }
        }
    }

    private fun discardReconciledRejections() {
        while (true) {
            val rejection = queuedServerEvents.firstOrNull() as? P2PServerEvent.RejectedAction ?: return
            val rejectedActionIsStillApplied = engine.history.any { delta ->
                delta.id == rejection.rejectedActionId
            }
            if (rejectedActionIsStillApplied) return

            LOG.d { "Ignoring rejection for reconciled action: ${rejection.rejectedActionId}" }
            queuedServerEvents.removeFirst()
        }
    }

    private fun firstServerEventRequiresRevert(id: GameActionId): Boolean {
        return when (val event = queuedServerEvents.firstOrNull()) {
            is P2PServerEvent.RejectedAction -> true
            is P2PServerEvent.ServerGameAction -> event.serverIndex.counter < id.counter
            null -> false
        }
    }

    override fun userActionSelected(id: GameActionId, action: GameAction) {
        currentProvider.userActionSelected(id, action)
    }

    override fun userMultipleActionsSelected(startingId: GameActionId, actions: List<GameAction>, delayEvent: Boolean) {
        currentProvider.userMultipleActionsSelected(startingId, actions, delayEvent)
    }

    override fun registerQueuedActionGenerator(generator: QueuedActionsGenerator) {
        currentProvider.registerQueuedActionGenerator(generator)
    }

    override fun hasQueuedActions(): Boolean {
        discardReconciledRejections()
        return currentProvider.hasQueuedActions() || when (val event = queuedServerEvents.firstOrNull()) {
            is P2PServerEvent.RejectedAction -> true
            is P2PServerEvent.ServerGameAction -> event.serverIndex.counter <= engine.nextActionIndex().counter
            null -> false
        }
    }
}
