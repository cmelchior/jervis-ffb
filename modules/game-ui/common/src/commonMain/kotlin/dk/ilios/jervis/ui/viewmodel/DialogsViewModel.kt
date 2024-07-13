package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.utils.safeTryEmit
import kotlinx.coroutines.flow.Flow

/**
 * Class responsible for handling and showing model dialogs.
 */
class DialogsViewModel(val uiActionFactory: UiActionFactory) {
    fun buttonActionSelected(action: GameAction) {
        // By emitting `null`, recomposing will no longer show dialogs.
        // Hide the dialog before sending the event to prevent race conditions
        // with showing multiple dialogs (which can cause type case errors for the dice rolls)
        uiActionFactory.dialogActions.safeTryEmit(null)
        uiActionFactory.userSelectedAction(action)
    }
    val availableActions: Flow<UserInputDialog?> = uiActionFactory.dialogActions
}