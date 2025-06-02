package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.model.Team
import com.jervisffb.net.messages.GameActionServerError
import com.jervisffb.net.messages.GameActionSyncMessage
import com.jervisffb.net.messages.GameTimerSyncMessage
import com.jervisffb.net.messages.ServerError
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.UiSnapshotTimerData
import com.jervisffb.ui.menu.p2p.AbstractClintNetworkMessageHandler
import com.jervisffb.ui.menu.p2p.P2PClientNetworkAdapter
import com.jervisffb.utils.jervisLogger
import com.jervisffb.utils.singleThreadDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * This action provider is the primary action provider for P2P games and are responsible for switching
 * between the local provider and the remote one (that is piping events from the server)
 */
class P2PActionProvider(
    private val engine: GameEngineController,
    private val settings: GameSettings,
    private val homeProvider: UiActionProvider,
    private val awayProvider: UiActionProvider,
    private val networkAdapter: P2PClientNetworkAdapter,
): UiActionProvider() {

    companion object {
        val LOG = jervisLogger()
    }
    var lastServerActionIndex: GameActionId = GameActionId(-1)

    private val serverActionScope = CoroutineScope(singleThreadDispatcher("ServerRevertScope"))
    // For now, this only track Revert's from the server. Game Sync messages are handled
    // by just calling `userActionSelected`. Server Reverts will be run after all queued up
    // messages from the server has been sent. This avoids the race condition where the server
    // stats sending back Revert in the middle of a queue up client actions.
    // This should be safe, but will just take slightly longer to recover from since the
    // server needs to send multiple errors.
    private val revertServerActionsQueue = Channel<QueuedActionsGenerator>(capacity = Int.MAX_VALUE)
    private val queuedServerActions = mutableListOf<GameAction>()
    private var handlingServerRevert = false // Set during `prepareForNextAction`. If `true`, the action will not be sent to the server again

    private var currentProvider = homeProvider
    private lateinit var timerFlow: MutableSharedFlow<UiSnapshotTimerData>
    private val deadlines = mutableMapOf<GameActionId, Instant?>()

    override fun startHandler(uiTimerData: MutableSharedFlow<UiSnapshotTimerData>) {
        timerFlow = uiTimerData
        networkAdapter.addMessageHandler(object: AbstractClintNetworkMessageHandler() {
            override fun onGameAction(serverAction: GameActionSyncMessage) {
                // We need to track the timer information alongside the event somehow :/
                // TODO Should this be moved into RemoteActionProvider somehow?
                lastServerActionIndex = serverAction.serverIndex
                if (serverAction.producer == engine.state.awayTeam.coach.id) { // TODO Is this check always correct?
                    awayProvider.userActionSelected(serverAction.serverIndex, serverAction.action)
                } else {
                    homeProvider.userActionSelected(serverAction.serverIndex, serverAction.action)
                }
            }

            @OptIn(ExperimentalTime::class)
            override fun onTimerSync(serverAction: GameTimerSyncMessage) {
                // Must be called last in `prepareForNextAction` since action provider is
                // switching in `prepareForNextAction`.
                // In a remote scenario, we might not have the latest timer data
                val teamId = if (engine.state.homeTeam.coach.id == serverAction.producer) {
                    engine.state.homeTeam.id
                } else {
                    engine.state.awayTeam.id
                }

                val userActionsAvailable = if (serverAction.expectedBy != null) {
                    val deadline = serverAction.expectedBy!!
                    val now = Clock.System.now()
                    deadline.toEpochMilliseconds() - now.toEpochMilliseconds() > 0
                } else {
                    true
                }
                val timerData = UiSnapshotTimerData(
                    serverAction.serverIndex,
                    teamId,
                    serverAction.expectedBy,
                    engine.rules.timers.outOfTimeBehaviour,
                    userActionsAvailable
                )
                timerFlow.tryEmit(timerData)
            }

            override fun onServerError(error: ServerError) {
                // If actions are rejected on the server, we queue them up here to
                // be undone. The UI experience for this will probably be a bit
                // weird, since it will display all the intermediate UI step for
                // each step backwards, but since it is impossible to know when the
                // rollback is done, it is hard to do anything about. Unless some
                // kind of "action-list" is supported in the network protocol. Something
                // for the future.
                when (error) {
                    is GameActionServerError -> {
                        LOG.i { "Queuing up Revert of action: ${error.actionId}" }
                        serverActionScope.launch {
                            revertServerActionsQueue.send { controller: GameEngineController ->
                                QueuedActions(Revert)
                            }
                        }
                    }
                    else -> { /* Ignore */ }
                }
            }
        })
        homeProvider.startHandler(uiTimerData)
        awayProvider.startHandler(uiTimerData)
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        // If we are handling a server Revert, we are trying to get the client into the correct
        // state. This means we do not want to send any events to the server during this period.
        if (handlingServerRevert) return

        val clientActionIndex = engine.currentActionIndex()
        // Should only send this if the event is truly from this client and not just a sync message
        // TODO lastServerActionIndex seems to be out of syn
        LOG.d("Sending message to server ($clientActionIndex > $lastServerActionIndex): $action")
        if (clientActionIndex > lastServerActionIndex) {
            actionScope.launch {
                networkAdapter.sendActionToServer(clientActionIndex, action)
            }
        }
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        currentProvider = if (actions.team.isAwayTeam()) {
            awayProvider
        } else {
            homeProvider
        }
        currentProvider.prepareForNextAction(controller, actions)
        if (!currentProvider.hasQueuedActions()) {
            while (true) {
                val actionChannelResult = revertServerActionsQueue.tryReceive()
                if (actionChannelResult.isSuccess) {
                    actionChannelResult.getOrThrow()(controller)?.actions?.forEach { action ->
                        queuedServerActions.add(action)
                    }
                } else {
                    break
                }
            }
        }
        handlingServerRevert = queuedServerActions.isNotEmpty()
    }

    override fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionRequest) {
        if (!handlingServerRevert) {
            currentProvider.decorateAvailableActions(state, actions)
        }
    }

    override fun decorateSelectedAction(state: UiGameSnapshot, action: GameAction) {
        if (!handlingServerRevert) {
            currentProvider.decorateSelectedAction(state, action)
        }
    }

    override suspend fun getAction(id: GameActionId): GeneratedAction {
        if (handlingServerRevert) {
            val action = queuedServerActions.removeFirst()
            LOG.i { "Handling revert: $handlingServerRevert -> $action" }
            // As we want Revert to interrupt the current flow, we just fake the
            // ID so it is always correct.
            return GeneratedAction(id, action)
        } else {
            LOG.i { "GetAction($currentProvider)" }
            return currentProvider.getAction(id)
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
        return queuedServerActions.isNotEmpty()
    }

    override fun clearQueuedActions() {
        // TODO Need to determine what happens here
    }
}
