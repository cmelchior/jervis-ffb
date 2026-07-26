package com.jervisffb.ui.game.dialogs

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.utils.assert
import com.jervisffb.ui.game.view.ActionWheelUiState
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Action-Wheel View Model for "primary" actions, i.e. a game state
 * with input that _must_ be created using the action wheel. This means
 * the wheel is automatically shown (if not already visible) and it cannot
 * be dismissed.
 */
class PrimaryActionWheelViewModel(
    val eventFlow: Flow<List<ActionWheelUiState>>,
    team: Team,
    sharedPitchData: LocalPitchDataWrapper,
    private val isReplay: Boolean = false,
) : AbstractActionWheelViewModel(
        team = team,
        sharedPitchData = sharedPitchData,
    ) {

    private val _eventFlow = MutableSharedFlow<ActionWheelUiState>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)
    // Channel used for the UI to communicate back to the ViewModel that an Action Wheel event has been handled.
    // This event will not trigger until all animations have finished.
    private val wheelEventDone = Channel<Unit>(capacity = Channel.CONFLATED)

    fun observe(): Flow<ActionWheelUiState> {
        return _eventFlow
    }

    fun notifyUiHandledActionWheelEvent() {
        val res = wheelEventDone.trySend(Unit)
        if (res.isFailure) {
            error("Unable to send event to channel. This should never happen on a CONFLATE channel.")
        }
    }

    // Start processing events from the UIGameController
    suspend fun start() {
        // Swallow initial event sent from the UI during setup. During replay the initial (non-animation)
        // setup wheel is not rendered, so no such ack is sent -> skip the swallow to avoid blocking forever.
        if (!isReplay) {
            wheelEventDone.receive()
        }
        eventFlow.collect { wheelEvents ->
            val event = wheelEvents.first()
            this.hideOnClickedOutside.value = event.hideWhenClickOutside
            val willShowWheel = !event.isHiding() && (event.topItems.isNotEmpty() || event.bottomItems.isNotEmpty())
            _eventFlow.emit(event)

            val isCurrentWheelVisible = sharedPitchData.isPrimaryActionWheelVisible.value
            val ignoreEvent = (!isCurrentWheelVisible && !willShowWheel)
            sharedPitchData.setPrimaryActionWheelVisibility(willShowWheel)
            // During replay only animation wheels are actually rendered (interactive wheels are skipped by the
            // ComposeDialogs replay guard and never ack), so only wait for those. Live games wait for all.
            if (!ignoreEvent && (!isReplay || event.animationOnly)) {
                // Wait for UI to process the Action Wheel update, including animations
                wheelEventDone.receive()
            }
        }
    }

    override fun showWheel() {
        error("Should never be called for a primary action wheel. It should always be visible")
    }

    override fun hideWheel(onDismiss: (() -> Unit)?) {
        sharedPitchData.let {
            assert(it.isActionWheelVisible.value) {
                "Action wheel is not visible, but hideWheel() was called"
            }
            onDismiss?.invoke()
        }
    }
}
