package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.ui.GameScreenModel
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Class responsible for handling action descriptors. For manual games, this means mapping the game descriptor
 * to UI actions that can be used to modify the UI to create the appropriate event.
 *
 * For other game modes, this class can just return the appropriate action immediately without involving the user.
 */
abstract class UiActionFactory(protected val model: GameScreenModel) {

    val scope = CoroutineScope(CoroutineName("ActionSelectorScope") + Dispatchers.Default)

    // Streams of actions (these roughly correspond to UI elements), so each UI element only need to listen
    // to the stream relevant to it.
    //
    // FieldActions:
    // - Select Player, Select ball, Select location
    // ----
    protected val _fieldActions: MutableSharedFlow<UserInput> = MutableSharedFlow(replay = 1)
    val fieldActions: Flow<UserInput> = _fieldActions
    protected val _unknownActions: MutableSharedFlow<UserInput> = MutableSharedFlow(replay = 1)
    val unknownActions: Flow<UserInput> = _unknownActions
    val dialogActions: MutableSharedFlow<UserInputDialog?> = MutableSharedFlow(replay = 1)
    // ----

    // All actions selected by the user are sent to this channel.
    protected val userSelectedAction = Channel<GameAction>(1)

    // TODO
    abstract suspend fun start(scope: CoroutineScope)

    // The user selected an action
    fun userSelectedAction(action: GameAction) {
        scope.launch {
            userSelectedAction.send(action)
        }
    }
}
