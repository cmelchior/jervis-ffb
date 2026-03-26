package com.jervisffb.ui.game.dialogs

import com.jervisffb.engine.utils.safeTryEmit
import com.jervisffb.ui.game.dialogs.wheel.ActionButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.view.ActionWheelUiState
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.game.view.ContextWheelMenu
import com.jervisffb.ui.game.view.ContextWheelUiState
import com.jervisffb.ui.game.view.NoContextMenu
import com.jervisffb.ui.game.view.ToggleContextMenuOption
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Action-Wheel View Model for "context" actions, i.e., actions that are not
 * considered primary at the current Node. This wheel thus needs to be opened
 * by clicking a player before actions can be used. It can also be dismissed
 * without generating a [com.jervisffb.engine.actions.GameAction].
 */
class SecondaryActionWheelViewModel(
    private val fieldViewModel: FieldViewModel,
    private val eventFlow: Flow<ContextWheelUiState>,
    sharedFieldData: LocalFieldDataWrapper,
): AbstractActionWheelViewModel(null, sharedFieldData) {

    private val hideAction = com.jervisffb.ui.game.view.HideActionWheel(hideImmediately = true)
    private var currentWheelData: ContextWheelMenu? = null
    private var currentWheel: ActionWheelUiStateData? = null
    private val _eventFlow = MutableSharedFlow<ActionWheelUiState>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)

    fun observe(): Flow<ActionWheelUiState> {
        return _eventFlow
    }

    // Start processing events from the UIGameController
    suspend fun start() {
        // Swallow initial event sent from the UI during setup.
        eventFlow.collect { event ->
            when (event) {
                is ContextWheelMenu -> {
                    // Store reference to current ContextMenu, so we can easily toggle visibility later
                    currentWheelData = event
                    val wheelEvent = recalculateContextMenu(event)
                    currentWheel = wheelEvent
                    _eventFlow.emit(wheelEvent)
                    sharedFieldData.setContextActionWheelVisibility(true)
                }
                NoContextMenu -> {
                    currentWheelData = null
                    currentWheel = null
                    sharedFieldData.setContextActionWheelVisibility(false)
                    _eventFlow.emit(hideAction)
                }
            }
        }
    }

    init {
        hideOnClickedOutside.value = true
    }

    private fun recalculateContextMenu(wheelData: ContextWheelMenu): ActionWheelUiStateData {
        val wheelState = ActionWheelUiStateData(
            center = wheelData.coordinates,
            bottomItems = wheelData.options.map { contextOption ->
                ActionButtonData(
                    id = ButtonId("context-${contextOption.title}"),
                    label = { contextOption.title },
                    icon = contextOption.icon,
                    action = {
                        hideWheel()
                        fieldViewModel.triggerHoverExit()
                        contextOption.command()
                        if (contextOption is ToggleContextMenuOption) {
                            contextOption.recalculateState()
                            val newData = recalculateContextMenu(currentWheelData!!)
                            currentWheel = newData
                        }
                    },
                    enabled = true
                )
            },
            bottomExpandMode = MenuExpandMode.FanOut(spread = 360f),
            bottomAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
            onDismiss = { /* Do nothing */ },
            hideWhenClickOutside = true
        )
        return wheelState
    }

    fun showWheel(menuOptions: ContextWheelMenu) {
        currentWheelData = menuOptions
        val event = recalculateContextMenu(menuOptions)
        currentWheel = event
        _eventFlow.safeTryEmit(event)
        sharedFieldData.setContextActionWheelVisibility(true)
    }

    override fun showWheel() {
        sharedFieldData.setContextActionWheelVisibility(true)
        _eventFlow.safeTryEmit(currentWheel!!)
    }

    override fun hideWheel(onDismiss: (() -> Unit)?) {
        sharedFieldData.let {
            it.setContextActionWheelVisibility(false)
            _eventFlow.safeTryEmit(hideAction)
            // Do not use onDismiss here as the Context Menu doesn't generate GameActions
        }
    }

}
