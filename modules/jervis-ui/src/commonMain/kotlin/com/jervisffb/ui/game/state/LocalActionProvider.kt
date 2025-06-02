package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.OutOfTimeBehaviour
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.OutOfTime
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.timer.TimerTracker
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.UiSnapshotTimerData
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.random.Random

/**
 * For games fully controlled locally. This wraps home and away providers.
 * Rules concerning timers are also handled here.
 */
class LocalActionProvider(
    private val engine: GameEngineController,
    private val settings: GameSettings,
    private val homeProvider: UiActionProvider,
    private val awayProvider: UiActionProvider,
): UiActionProvider() {

    companion object {
        val LOG = jervisLogger()
    }

    private var currentProvider = homeProvider

    val timerTracker = TimerTracker(engine.rules.timers, random = Random)
    private lateinit var timerFlow: MutableSharedFlow<UiSnapshotTimerData>

    override fun startHandler(uiTimerData: MutableSharedFlow<UiSnapshotTimerData>) {
        timerFlow = uiTimerData
        homeProvider.startHandler(timerFlow)
        awayProvider.startHandler(timerFlow)
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        homeProvider.actionHandled(team, action)
        awayProvider.actionHandled(team, action)
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        currentProvider = if (actions.team.isAwayTeam()) {
            awayProvider
        } else {
            homeProvider
        }

        // Start tracking time and check if we are already out of time for the current action selection.
        // We only check if out of time in `prepareForNextAction` and then otherwise start the timer
        // in `getAction()`. This means that we do not need to check for race conditions in
        // `prepareForNextAction()` and `decorateAvailableActions()`
        val coachActionsAvailable = timerTracker.startNextAction(controller) { id->
            if (settings.timerSettings.outOfTimeBehaviour == OutOfTimeBehaviour.AUTOMATIC_TIMEOUT) {
                currentProvider.userActionSelected(id, OutOfTime)
            }
        }

        if (coachActionsAvailable) {
            currentProvider.prepareForNextAction(controller, actions)
        } else {
            clearQueuedActions()
            val outOfTimeAction = timerTracker.getOutOfTimeAction(controller.state, actions)
            currentProvider.userActionSelected(actions.nextActionId, outOfTimeAction)
        }

        // Must be called last in `prepareForNextAction` since action provider is
        // switching in `prepareForNextAction`.
        val nextActionBy = timerTracker.getDeadlineForNextAction()
        val timerData = UiSnapshotTimerData(
            actions.nextActionId,
            actions.team.id,
            nextActionBy,
            engine.rules.timers.outOfTimeBehaviour,
            coachActionsAvailable
        )
        timerFlow.emit(timerData)
    }

    override fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionRequest) {
        if (
            settings.timerSettings.outOfTimeBehaviour != OutOfTimeBehaviour.AUTOMATIC_TIMEOUT
                || !timerTracker.isOutOfTime()
        ) {
            currentProvider.decorateAvailableActions(state, actions)
        }
    }

    override fun decorateSelectedAction(state: UiGameSnapshot, action: GameAction) {
        currentProvider.decorateSelectedAction(state, action)
    }

    override suspend fun getAction(id: GameActionId): GeneratedAction {
//        try {
//            return if (timerTracker.isOutOfTime() && settings.timerSettings.outOfTimeBehaviour == OutOfTimeBehaviour.AUTOMATIC_TIMEOUT) {
//                val action = timerTracker.getOutOfTimeAction(engine.state, engine.getAvailableActions())
//                GeneratedAction(id, action)
//            } else {
                // Out-dated actions should have been filtered by other providers before getting here.
                val action = currentProvider.getAction(id)

                //


                action
//            }
//        } finally {
            timerTracker.stopTimer()
//        }
        return action
    }

    override fun userActionSelected(id: GameActionId, action: GameAction) {
        currentProvider.userActionSelected(id, action)
    }

    override fun userMultipleActionsSelected(startingId: GameActionId, actions: List<GameAction>, delayEvent: Boolean) {
        currentProvider.userMultipleActionsSelected(startingId, actions, delayEvent)
    }

    override fun registerQueuedActionGenerator(generator: QueuedActionsGenerator) {
        currentProvider.registerQueuedActionGenerator(generator)
    }

    override fun hasQueuedActions(): Boolean {
        return currentProvider.hasQueuedActions()
    }

    override fun clearQueuedActions() {
        homeProvider.clearQueuedActions()
        awayProvider.clearQueuedActions()
    }

    override fun onDispose() {
        homeProvider.onDispose()
        awayProvider.onDispose()
        timerTracker.stopTimer()
    }
}
