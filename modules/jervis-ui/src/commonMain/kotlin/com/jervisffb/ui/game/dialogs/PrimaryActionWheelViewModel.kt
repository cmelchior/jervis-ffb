package com.jervisffb.ui.game.dialogs

import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.view.ActionWheelUiState
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Action Wheel View Model for "primary" actions, i.e. a game state
 * with input that _must_ be created using the action wheel. This means
 * the wheel is automatically shown (if not already visible) and it cannot
 * be dismissed.
 */
class PrimaryActionWheelViewModel(
    val eventFlow: Flow<List<ActionWheelUiState>>,
    team: Team,
    sharedFieldData: LocalFieldDataWrapper
) : AbstractActionWheelViewModel(team, sharedFieldData) {

    private val _eventFlow = MutableSharedFlow<ActionWheelUiState?>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)

    fun observe(): Flow<ActionWheelUiState?> {
        return _eventFlow
    }

    // Start processing events from the UIGameController
    suspend fun start() {
        eventFlow.collect { wheelEvents ->
            val event = wheelEvents.firstOrNull()
            _eventFlow.emit(event)
        }
    }

    override fun showWheel() {
        sharedFieldData.setActionWheelVisibility(true)
        isVisible.value = true
    }

    override fun hideWheel(userUiAction: Boolean) {
        sharedFieldData.let {
            if (it.isContentMenuVisible.value) {
                it.setActionWheelVisibility(false)
                isVisible.value = false
                if (userUiAction) {
                    onMenuHidden?.invoke()
                }
            }
        }
    }
}
