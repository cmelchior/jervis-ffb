package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.utils.safeTryEmit
import kotlinx.coroutines.flow.Flow

/**
 * Class responsible for handling and showing model dialogs.
 */
class DialogsViewModel(val uiActionFactory: UiActionFactory) {
    fun buttonActionSelected(action: GameAction) {
        uiActionFactory.userSelectedAction(action)
        // By emitting `null`, recomposing will no longer show dialogs.
        uiActionFactory.dialogActions.safeTryEmit(null)
    }
    val availableActions: Flow<UserInputDialog?> = uiActionFactory.dialogActions
}