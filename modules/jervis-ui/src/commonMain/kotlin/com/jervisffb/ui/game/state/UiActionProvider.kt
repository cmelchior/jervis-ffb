package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.utils.singleThreadDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
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

    // Must be single threaded so we can guarantee the order of events in it.
    protected val actionScope = CoroutineScope(
        CoroutineName("ActionSelectorScope")
            + singleThreadDispatcher("ActionScope@${this::hashCode}")
            + errorHandler
    )

    // Used to communicate internally in the ActionProvider. Needed so we can decouple the lifecycle of things.
    // Like the lifecycle of the GameLoop vs. the lifecycle of UI actions.
    protected val actionRequestChannel = Channel<Pair<GameEngineController, ActionRequest>>(capacity = Channel.Factory.RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)
    protected val actionSelectedChannel = Channel<GameAction>(capacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.SUSPEND)

    // Hook allowing the UiActionProvider to change how the next action is selected.
    // E.g. automated or queued actions.
    abstract suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest)
    // Hook for the UI to set up the UI so the next action can be selected.
    // E.g. enable onClick listeners on Players, highlight certain aspects of the field or show Dialogs.
    abstract fun decorateAvailableActions(actions: ActionRequest, acc: UiSnapshotAccumulator)
    // Hook to manipulate the UI after an action has been selected
    // TODO Is this used for anything?
    abstract fun decorateSelectedAction(action: GameAction, acc: UiSnapshotAccumulator)
    // Block until the next action is generated. Normally by calling `userActionSelected`.
    abstract suspend fun getAction(): GameAction
    // Parse in an action that will returned to the next call to `getAction` (or if it is currently waiting for an action)
    // Normally used by the UI to pass in an action created by a coach.
    abstract fun userActionSelected(action: GameAction)
    // Similar to `userActionSelected` but for multiple actions. This can e.g. be used for moves and makes it possible
    // to queue them all up-front. It is up to the caller to ensure a legal sequence. Illegal actions will still throw an
    // exception from the rules engine. Delaying between each event makes it possible to "fake" animations or the user
    // clicking each step along the way.
    abstract fun userMultipleActionsSelected(actions: List<GameAction>, delayEvent: Boolean = true)
    // Similar to `userMultipleActionsSelected` but allows for flexibility in the generated sequence. In some cases,
    // you cannot fully predict the full sequence (e.g. if you need to roll dice along the way).
    abstract fun registerQueuedActionGenerator(generator: QueuedActionsGenerator)
    // Returns `true` if there are queued actions waiting be processed. `QueuedActionsGenerator` will
    // add actions to the queue in `prepareForNextAction`
    abstract fun hasQueuedActions(): Boolean
}
