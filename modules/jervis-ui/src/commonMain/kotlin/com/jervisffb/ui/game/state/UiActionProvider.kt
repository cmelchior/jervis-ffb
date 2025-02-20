package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.ui.game.UiGameSnapshot
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

/**
 * Interface for "Action Decorators", i.e. a class that is responsible for
 * modifying the UI depending on which actions are available.
 *
 * Each implementation is tied to a [GameMode]
 */
abstract class UiActionProvider {
    val errorHandler = CoroutineExceptionHandler { _, exception ->
        // TODO This doesn't seem to work?
        exception.printStackTrace()
    }
    protected val actionScope = CoroutineScope(CoroutineName("ActionSelectorScope") + Dispatchers.Default + errorHandler)

    // Used to communicate internally in the ActionProvider. Needed so we can decouple the lifecycle of things.
    protected val actionRequestChannel = Channel<Pair<GameEngineController, ActionRequest>>(capacity = Channel.Factory.RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)
    protected val actionSelectedChannel = Channel<GameAction>(capacity = Channel.Factory.RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)

    abstract fun prepareForNextAction(controller: GameEngineController)
    abstract fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionRequest)
    abstract fun decorateSelectedAction(state: UiGameSnapshot, action: GameAction)
    abstract suspend fun getAction(): GameAction
    abstract fun userActionSelected(action: GameAction)
    abstract fun userMultipleActionsSelected(actions: List<GameAction>, delayEvent: Boolean = true)
}
