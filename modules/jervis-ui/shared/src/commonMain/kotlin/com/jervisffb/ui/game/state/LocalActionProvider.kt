package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// For games fully controlled locally. This wraps home and away providers.
// Rules concerning timers are also handled here.
class LocalActionProvider(
    private val engine: GameEngineController,
    private val settings: GameSettings,
    private val homeProvider: UiActionProvider,
    private val awayProvider: UiActionProvider,
): UiActionProviderGroup() {

    override var currentProvider: UiActionProvider = homeProvider
        private set

    private var actionJob: Job? = null

    private var sharedData: LocalPitchDataWrapper? = null

    override fun init(controller: UiGameController) {
        homeProvider.init(controller)
        awayProvider.init(controller)
    }

    override fun startHandler() {
        homeProvider.startHandler()
        awayProvider.startHandler()
    }

    override fun stopHandler() {
        homeProvider.stopHandler()
        awayProvider.stopHandler()
        actionJob?.cancel()
        super.stopHandler()
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        homeProvider.actionHandled(team, action)
        awayProvider.actionHandled(team, action)
    }

    override fun updateSharedData(sharedData: LocalPitchDataWrapper) {
        this.sharedData = sharedData
        homeProvider.updateSharedData(sharedData)
        awayProvider.updateSharedData(sharedData)
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        currentProvider = if (actions.team?.isAwayTeam() == true) {
            awayProvider
        } else {
            homeProvider
        }
        currentProvider.prepareForNextAction(controller, actions)
    }

    override fun decorateAvailableActions(actions: ActionRequest, acc: UiSnapshotAccumulator) {
        currentProvider.decorateAvailableActions(actions, acc)
    }

    override fun decorateSelectedAction(action: GameAction, acc: UiSnapshotAccumulator) {
        currentProvider.decorateSelectedAction(action, acc)
    }

    override suspend fun getAction(id: GameActionId): GameAction {
        val provider = currentProvider
        // For now, disable timer actions as we need to implement timer infrastructure
        // in the network protocol first
        val timersEnabled = settings.timerSettings.timersEnabled && false
        if (timersEnabled) {
            @OptIn(DelicateCoroutinesApi::class)
            actionJob = GlobalScope.launch(CoroutineName("ActionJob")) {
                // TODO Need to figure out if we are using setup / turn / response timers and track it correctly
                // delay(settings.timerSettings.turnFreeTime ?: settings.timerSettings.turnActionTime)
                val action = createRandomAction(engine)
                val id = engine.nextActionIndex()
                provider.userActionSelected(id, action)
            }
        }
        return provider.getAction(id).also {
            actionJob?.cancel()
        }
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
}
