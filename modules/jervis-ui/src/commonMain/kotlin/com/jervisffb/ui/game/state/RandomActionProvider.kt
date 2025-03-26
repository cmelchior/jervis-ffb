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
import com.jervisffb.engine.model.PlayerState
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

        // Use a pre-defined setup, because the chance of random actions hitting a correct setup
        // is basically zero.
        // TODO This goes into an infinite loop if the setup is no longer valid. Figure out
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

        val setup = listOf(
            PlayerNo(1) to FieldCoordinate(12, 6),
            PlayerNo(2) to FieldCoordinate(12, 7),
            PlayerNo(3) to FieldCoordinate(12, 8),
            PlayerNo(4) to FieldCoordinate(10, 1),
            PlayerNo(5) to FieldCoordinate(10, 4),
            PlayerNo(6) to FieldCoordinate(10, 10),
            PlayerNo(7) to FieldCoordinate(10, 13),
            PlayerNo(8) to FieldCoordinate(8, 1),
            PlayerNo(9) to FieldCoordinate(8, 4),
            PlayerNo(10) to FieldCoordinate(8, 10),
            PlayerNo(11) to FieldCoordinate(8, 13),
        )
        setupTeam(team, compositeActions, setup)
    }

    private suspend fun handleManualAwayKickingSetup(
        controller: GameEngineController,
        compositeActions: MutableList<GameAction>
    ) {
        val game: Game = controller.state
        val team = game.awayTeam

        val setup = listOf(
            PlayerNo(1) to FieldCoordinate(13, 6),
            PlayerNo(2) to FieldCoordinate(13, 7),
            PlayerNo(3) to FieldCoordinate(13, 8),
            PlayerNo(4) to FieldCoordinate(15, 1),
            PlayerNo(5) to FieldCoordinate(15, 4),
            PlayerNo(6) to FieldCoordinate(15, 10),
            PlayerNo(7) to FieldCoordinate(15, 13),
            PlayerNo(8) to FieldCoordinate(17, 1),
            PlayerNo(9) to FieldCoordinate(17, 4),
            PlayerNo(10) to FieldCoordinate(17, 10),
            PlayerNo(11) to FieldCoordinate(17, 13),
        )
        setupTeam(team, compositeActions, setup)
    }

    private fun setupTeam(team: Team, compositeActions: MutableList<GameAction>, setup: List<Pair<PlayerNo, FieldCoordinate>>) {
        val playersTaken = mutableSetOf<PlayerNo>()
        setup.forEach { (playerNo: PlayerNo, fieldCoordinate: FieldCoordinate) ->
            // Prevent crashing if the player is no longer available
            if (team[playerNo].state == PlayerState.RESERVE) {
                playersTaken.add(playerNo)
                compositeActions.add(PlayerSelected(team[playerNo]))
                compositeActions.add(FieldSquareSelected(fieldCoordinate))
            } else {
                // If the suggested player wasn't available, try to find the next one
                // Since we know that players 1-11 are supposed to be on the field.
                team.firstOrNull {
                    it.number.value > 11 && it.state == PlayerState.RESERVE && !playersTaken.contains(it.number)
                }?.let { replacementPlayer ->
                    playersTaken.add(replacementPlayer.number)
                    compositeActions.add(PlayerSelected(team[replacementPlayer.number]))
                    compositeActions.add(FieldSquareSelected(fieldCoordinate))
                }
            }
        }
    }
}
