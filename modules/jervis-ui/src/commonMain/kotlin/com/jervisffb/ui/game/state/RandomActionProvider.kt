package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeam
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeamContext
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.utils.containsActionWithRandomBehavior
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RandomActionProvider(
    val clientMode: TeamActionMode,
    val controller: GameEngineController,
    val isServer: Boolean = false // For local games, if this is set. This provider will also generate "server-only" events
): UiActionProvider() {

    companion object {
        val LOG = jervisLogger()
    }

    private var job: Job? = null
    private var paused = false
    private lateinit var actions: ActionRequest

    override fun startHandler() {
        // Do nothing
    }

    override fun actionHandled(team: Team?, action: GameAction) {
        // Do nothing
    }

    override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) {
        this.actions = controller.getAvailableActions()
    }

    override fun decorateAvailableActions(state: UiGameSnapshot, actions: ActionRequest) {
        // Do nothing
    }

    override fun decorateSelectedAction(state: UiGameSnapshot, action: GameAction) {
        // Do nothing
    }

    override suspend fun getAction(): GameAction {
        actionRequestChannel.send(Pair(controller, actions))
        return actionSelectedChannel.receive()
    }

    override fun userActionSelected(action: GameAction) {
        actionScope.launch {
            actionSelectedChannel.send(action)
        }
    }

    override fun userMultipleActionsSelected(actions: List<GameAction>, delayEvent: Boolean) {
        TODO("Not yet supported")
    }

    override fun registerQueuedActionGenerator(generator: QueuedActionsGenerator) {
        TODO("Not yet implemented")
    }

    override fun hasQueuedActions(): Boolean {
        return false
    }

    fun startActionProvider() {
        paused = false
        job = actionScope.launch {
            while (!paused) {
                val (controller, request) = actionRequestChannel.receive()

                // For P2P games, if the action is handled by the server, we do not need to send back any
                // event here; it will be sent from the server (eventually).

                // First check if this UiActionProvider is responsible for the current team
                val teamActionHandledHere = when (clientMode) {
                    TeamActionMode.HOME_TEAM -> true
                    TeamActionMode.AWAY_TEAM -> request.team?.isAwayTeam() == true
                    TeamActionMode.ALL_TEAMS -> true
                }

                // Then, even if it is the correct UiActionProvider, it also needs to be a type
                // of action that isn't handled by the server
                val actionHandledOnServer = controller.state.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER
                    && request.containsActionWithRandomBehavior()

                if (teamActionHandledHere && (!actionHandledOnServer || isServer)) {
                    if (!useManualAutomatedActions(controller)) {
                        val selectedAction = createRandomAction(controller.state, request.actions)
                        delay(50)
                        actionSelectedChannel.send(selectedAction)
                    }
                }
            }
        }
    }

    private suspend fun useManualAutomatedActions(controller: GameEngineController): Boolean {
        val state = controller.state
        val stack = controller.state.stack

        // Use a pre-defined setup, because the chance of Random actions getting a correct setup
        // are basically zero.
        if (!stack.isEmpty() && stack.currentNode() == SetupTeam.SelectPlayerOrEndSetup) {
            val context = state.getContext<SetupTeamContext>()
            val compositeActions = mutableListOf<GameAction>()
            if (context.team.isHomeTeam()) {
                handleManualHomeKickingSetup(controller, compositeActions)
            } else {
                handleManualAwayKickingSetup(controller, compositeActions)
            }
            compositeActions.add(EndSetup)
            actionSelectedChannel.send(CompositeGameAction(compositeActions))
            return true
        } else {
            return false
        }
    }

    private suspend fun handleManualHomeKickingSetup(
        controller: GameEngineController,
        compositeActions: MutableList<GameAction>
    ) {
        val game: Game = controller.state
        val team = game.homeTeam

        setupPlayer(team, compositeActions, PlayerNo(1), FieldCoordinate(12, 6))
        setupPlayer(team, compositeActions, PlayerNo(2), FieldCoordinate(12, 7))
        setupPlayer(team, compositeActions, PlayerNo(3), FieldCoordinate(12, 8))
        setupPlayer(team, compositeActions, PlayerNo(4), FieldCoordinate(10, 1))
        setupPlayer(team, compositeActions, PlayerNo(5), FieldCoordinate(10, 4))
        setupPlayer(team, compositeActions, PlayerNo(6), FieldCoordinate(10, 10))
        setupPlayer(team, compositeActions, PlayerNo(7), FieldCoordinate(10, 13))
        setupPlayer(team, compositeActions, PlayerNo(8), FieldCoordinate(8, 1))
        setupPlayer(team, compositeActions, PlayerNo(9), FieldCoordinate(8, 4))
        setupPlayer(team, compositeActions, PlayerNo(10), FieldCoordinate(8, 10))
        setupPlayer(team, compositeActions, PlayerNo(11), FieldCoordinate(8, 13))
    }

    private suspend fun handleManualAwayKickingSetup(
        controller: GameEngineController,
        compositeActions: MutableList<GameAction>
    ) {
        val game: Game = controller.state
        val team = game.awayTeam

        setupPlayer(team, compositeActions, PlayerNo(1), FieldCoordinate(13, 6))
        setupPlayer(team, compositeActions, PlayerNo(2), FieldCoordinate(13, 7))
        setupPlayer(team, compositeActions, PlayerNo(3), FieldCoordinate(13, 8))
        setupPlayer(team, compositeActions, PlayerNo(4), FieldCoordinate(15, 1))
        setupPlayer(team, compositeActions, PlayerNo(5), FieldCoordinate(15, 4))
        setupPlayer(team, compositeActions, PlayerNo(6), FieldCoordinate(15, 10))
        setupPlayer(team, compositeActions, PlayerNo(7), FieldCoordinate(15, 13))
        setupPlayer(team, compositeActions, PlayerNo(8), FieldCoordinate(17, 1))
        setupPlayer(team, compositeActions, PlayerNo(9), FieldCoordinate(17, 4))
        setupPlayer(team, compositeActions, PlayerNo(10), FieldCoordinate(17, 10))
        setupPlayer(team, compositeActions, PlayerNo(11), FieldCoordinate(17, 13))
    }

    private suspend fun setupPlayer(
        team: Team,
        compositeActions: MutableList<GameAction>,
        playerNo: PlayerNo,
        fieldCoordinate: FieldCoordinate,
    ) {
        compositeActions.add(PlayerSelected(team[playerNo]))
        compositeActions.add(FieldSquareSelected(fieldCoordinate))
    }
}
