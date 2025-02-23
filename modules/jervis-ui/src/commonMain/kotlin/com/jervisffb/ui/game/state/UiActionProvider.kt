package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiGameSnapshot
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

typealias QueuedActionsGenerator = (GameEngineController) -> QueuedActionsResult?

data class QueuedActionsResult(val actions: List<GameAction>, val delayBetweenActions: Boolean = false) {
    constructor(action: GameAction, delayEvent: Boolean = false): this(listOf(action), delayEvent)
}

/**
 * Action Providers are responsible for feeding game actions to the main game loop.
 * This can either be done automatically, through events sent from the server or through
 * the UI.
 *
 */
abstract class UiActionProvider {
    abstract fun startHandler()
    abstract fun actionHandled(team: Team?, action: GameAction)

    val errorHandler = CoroutineExceptionHandler { _, exception ->
        // TODO This doesn't seem to work?
        exception.printStackTrace()
    }

    // TODO This should probably be single threaded, so we are guaranteed the order of actions
    protected val actionScope = CoroutineScope(CoroutineName("ActionSelectorScope") + Dispatchers.Default + errorHandler)

    // Used to communicate internally in the ActionProvider. Needed so we can decouple the lifecycle of things.
    protected val actionRequestChannel = Channel<Pair<GameEngineController, ActionRequest>>(capacity = Channel.Factory.RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)
    protected val actionSelectedChannel = Channel<GameAction>(capacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.SUSPEND)

    abstract fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest)
    abstract fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionRequest)
    abstract fun decorateSelectedAction(state: UiGameSnapshot, action: GameAction)
    abstract suspend fun getAction(): GameAction
    abstract fun userActionSelected(action: GameAction)
    abstract fun userMultipleActionsSelected(actions: List<GameAction>, delayEvent: Boolean = true)
    abstract fun registerQueuedActionGenerator(generator: QueuedActionsGenerator)
}
