package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.GameAction
import kotlinx.coroutines.flow.Flow

/**
 * Class responsible for handling and showing model dialogs.
 */
class DialogsViewModel(val uiActionFactory: UiActionFactory) {
    fun buttonActionSelected(action: GameAction) {
        uiActionFactory.userSelectedAction(action)
    }
    val availableActions: Flow<UserInputDialog?> = uiActionFactory.dialogActions
}