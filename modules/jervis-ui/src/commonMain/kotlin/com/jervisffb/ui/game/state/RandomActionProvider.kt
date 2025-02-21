package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
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
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.ui.game.UiGameSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RandomActionProvider(): UiActionProvider() {

    private var job: Job? = null
    private var paused = false
    private lateinit var controller: GameEngineController
    private lateinit var actions: ActionRequest

    override fun prepareForNextAction(controller: GameEngineController) {
        this.controller = controller
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

    fun startActionProvider() {
        paused = false
        job = actionScope.launch {
            while (!paused) {
                val (controller, request) = actionRequestChannel.receive()
                if (!useManualAutomatedActions(controller)) {
                    val selectedAction = createRandomAction(controller.state, request.actions)
                    delay(50)
                    actionSelectedChannel.send(selectedAction)
                }
            }
        }
    }

    fun pauseActionProvider() {
        if (paused) {
            startActionProvider()
        } else {
            paused = true
        }
    }

    private suspend fun useManualAutomatedActions(controller: GameEngineController): Boolean {
        val state = controller.state
        val stack = controller.state.stack
        if (stack.peepOrNull()?.procedure == SetupTeam && stack.peepOrNull()?.currentNode() == SetupTeam.SelectPlayerOrEndSetup) {
            val context = state.getContext<SetupTeamContext>()
            if (context.team.isHomeTeam()) {
                handleManualHomeKickingSetup(controller)
            } else {
                handleManualAwayKickingSetup(controller)
            }
            actionRequestChannel.receive()
            actionSelectedChannel.send(EndSetup)
            return true
//        } else if (controller.stack.firstOrNull()?.procedure == TheKickOffEvent && controller.stack.firstOrNull()?.currentNode() == TheKickOffEvent.ResolveKickOffEvent) {
//            actionSelectedChannel.send(DiceResults(listOf(D6Result(2), D6Result(4))))
//            return true
        } else {
            return false
        }
    }

    private suspend fun handleManualHomeKickingSetup(controller: GameEngineController) {
        val game: Game = controller.state
        val team = game.homeTeam

        setupPlayer(team, PlayerNo(1), FieldCoordinate(12, 6))
        setupPlayer(team, PlayerNo(2), FieldCoordinate(12, 7))
        setupPlayer(team, PlayerNo(3), FieldCoordinate(12, 8))
        setupPlayer(team, PlayerNo(4), FieldCoordinate(10, 1))
        setupPlayer(team, PlayerNo(5), FieldCoordinate(10, 4))
        setupPlayer(team, PlayerNo(6), FieldCoordinate(10, 10))
        setupPlayer(team, PlayerNo(7), FieldCoordinate(10, 13))
        setupPlayer(team, PlayerNo(8), FieldCoordinate(8, 1))
        setupPlayer(team, PlayerNo(9), FieldCoordinate(8, 4))
        setupPlayer(team, PlayerNo(10), FieldCoordinate(8, 10))
        setupPlayer(team, PlayerNo(11), FieldCoordinate(8, 13), isLast = true)
    }

    private suspend fun handleManualAwayKickingSetup(controller: GameEngineController) {
        val game: Game = controller.state
        val team = game.awayTeam

        setupPlayer(team, PlayerNo(1), FieldCoordinate(13, 6))
        setupPlayer(team, PlayerNo(2), FieldCoordinate(13, 7))
        setupPlayer(team, PlayerNo(3), FieldCoordinate(13, 8))
        setupPlayer(team, PlayerNo(4), FieldCoordinate(15, 1))
        setupPlayer(team, PlayerNo(5), FieldCoordinate(15, 4))
        setupPlayer(team, PlayerNo(6), FieldCoordinate(15, 10))
        setupPlayer(team, PlayerNo(7), FieldCoordinate(15, 13))
        setupPlayer(team, PlayerNo(8), FieldCoordinate(17, 1))
        setupPlayer(team, PlayerNo(9), FieldCoordinate(17, 4))
        setupPlayer(team, PlayerNo(10), FieldCoordinate(17, 10))
        setupPlayer(team, PlayerNo(11), FieldCoordinate(17, 13), isLast = true)
    }

    private suspend fun setupPlayer(
        team: Team,
        playerNo: PlayerNo,
        fieldCoordinate: FieldCoordinate,
        isLast: Boolean = false,
    ) {
        actionSelectedChannel.send(PlayerSelected(team[playerNo]))
        actionRequestChannel.receive()
        actionSelectedChannel.send(FieldSquareSelected(fieldCoordinate))
        if (!isLast) {
            actionRequestChannel.receive()
        }
    }
}
