package com.jervisffb.ui.userinput

import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.ui.GameScreenModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

/**
 * Class responsible for handling action descriptors. For manual games, this means mapping the game descriptor
 * to UI actions that can be used to modify the UI to create the appropriate event.
 *
 * For other game modes, this class can just return the appropriate action immediately without involving the user.
 */
abstract class UiActionFactory(val model: GameScreenModel) {
    val errorHandler =
        CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
        }
    val scope = CoroutineScope(CoroutineName("ActionSelectorScope") + Dispatchers.Default + errorHandler)
    var blockEvents = false

    // Streams of actions (these roughly correspond to UI elements), so each UI element only need to listen
    // to the stream relevant to it.
    //
    // FieldActions:
    // - Select Player, Select ball, Select location
    // ----
    @Suppress("unused")
    private val _fieldActions: MutableSharedFlow<UserInput> = MutableSharedFlow(replay = 1)
    val fieldActions: Flow<UserInput> = _fieldActions.takeWhile { !blockEvents }
    private val _unknownActions: MutableSharedFlow<UserInput> = MutableSharedFlow(replay = 1)
    val unknownActions: Flow<UserInput> = _unknownActions.takeWhile { !blockEvents }
    val dialogActions: MutableSharedFlow<UserInputDialog?> = MutableSharedFlow(replay = 1)
    // ----

    // All actions selected by the user are sent to this channel.
    protected val userSelectedAction = Channel<GameAction>(1)

    // TODO
    abstract suspend fun start(scope: CoroutineScope)

    protected suspend fun emitToField(input: UserInput) {
        _fieldActions.emit(input)
    }

    protected suspend fun emitToUnknown(input: UserInput) {
        _unknownActions.emit(input)
    }

    // The user selected an action
    fun userSelectedAction(action: GameAction) {
        if (action is CompositeGameAction) {
            userSelectedMultipleActions(action.list)
        } else {
            scope.launch(errorHandler) {
                // Reset UI so it doesn't allow more input
                _unknownActions.emit(WaitingForUserInput)
                _fieldActions.emit(WaitingForUserInput)
                // By emitting `null`, recomposing will no longer show dialogs.
                // Hide the dialog before sending the event to prevent race conditions
                // with showing multiple dialogs (which can cause type case errors for the dice rolls)
                dialogActions.emit(null)
                userSelectedAction.send(action)
            }
        }
    }

    fun userSelectedMultipleActions(actions: List<GameAction>, delayEvent: Boolean = true) {
        when (actions.size) {
            0 -> return
            1 -> {
                userSelectedAction(actions.first())
                return
            }
        }
        scope.launch(errorHandler) {
            // Reset UI so it doesn't allow more input
            _unknownActions.emit(WaitingForUserInput)
            _fieldActions.emit(WaitingForUserInput)
            // By emitting `null`, recomposing will no longer show dialogs.
            // Hide the dialog before sending the event to prevent race conditions
            // with showing multiple dialogs (which can cause type case errors for the dice rolls)
            dialogActions.emit(null)

            _fieldActions.emit(IgnoreUserInput)
            actions.forEachIndexed { i, el ->
                if (i == actions.size - 1) {
                    _fieldActions.emit(ResumeUserInput) // This doesn't work because these events don't reach all fields
                }
                userSelectedAction.send(el)
                // Do not pause for flow-control events, only events that would appear "visible"
                // to the player
                if (el !is MoveTypeSelected && delayEvent) {
                    delay(200)
                }
            }
        }
    }
}
