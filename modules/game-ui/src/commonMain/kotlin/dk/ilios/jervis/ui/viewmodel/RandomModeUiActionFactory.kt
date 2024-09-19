package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.SetupTeam
import dk.ilios.jervis.procedures.SetupTeamContext
import dk.ilios.jervis.ui.GameScreenModel
import dk.ilios.jervis.utils.createRandomAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class RandomModeUiActionFactory(model: GameScreenModel) : UiActionFactory(model) {
    override suspend fun start(scope: CoroutineScope) {
        val controller = model.controller
        val start = Clock.System.now()
        controller.startManualMode()
        emitToField(WaitingForUserInput)
        while (!controller.stack.isEmpty()) {
            val request = controller.getAvailableActions()
            if (!useManualAutomatedActions(controller)) {
                val selectedAction = createRandomAction(controller.state, request.actions)
                delay(100)
                controller.processAction(selectedAction)
            }
        }
        printStats(controller, start)
    }

    private suspend fun useManualAutomatedActions(controller: GameController): Boolean {
        if (controller.stack.peepOrNull()?.procedure == SetupTeam && controller.stack.peepOrNull()?.currentNode() == SetupTeam.SelectPlayerOrEndSetup) {
            val context = controller.state.getContext<SetupTeamContext>()
            if (context.team.isHomeTeam()) {
                handleManualHomeKickingSetup(controller)
            } else {
                handleManualAwayKickingSetup(controller)
            }
            controller.processAction(EndSetup)
            return true
//        } else if (controller.stack.firstOrNull()?.procedure == TheKickOffEvent && controller.stack.firstOrNull()?.currentNode() == TheKickOffEvent.ResolveKickOffEvent) {
//            actionSelectedChannel.send(DiceResults(listOf(D6Result(2), D6Result(4))))
//            return true
        } else {
            return false
        }
    }

    private fun handleManualHomeKickingSetup(controller: GameController) {
        val game: Game = controller.state
        val team = game.homeTeam

        setupPlayer(team, PlayerNo(1), FieldCoordinate(12, 6), controller)
        setupPlayer(team, PlayerNo(2), FieldCoordinate(12, 7), controller)
        setupPlayer(team, PlayerNo(3), FieldCoordinate(12, 8), controller)
        setupPlayer(team, PlayerNo(4), FieldCoordinate(10, 1), controller)
        setupPlayer(team, PlayerNo(5), FieldCoordinate(10, 4), controller)
        setupPlayer(team, PlayerNo(6), FieldCoordinate(10, 10), controller)
        setupPlayer(team, PlayerNo(7), FieldCoordinate(10, 13), controller)
        setupPlayer(team, PlayerNo(8), FieldCoordinate(8, 1), controller)
        setupPlayer(team, PlayerNo(9), FieldCoordinate(8, 4), controller)
        setupPlayer(team, PlayerNo(10), FieldCoordinate(8, 10), controller)
        setupPlayer(team, PlayerNo(11), FieldCoordinate(8, 13), controller)
    }

    private fun handleManualAwayKickingSetup(controller: GameController) {
        val game: Game = controller.state
        val team = game.awayTeam

        setupPlayer(team, PlayerNo(1), FieldCoordinate(13, 6), controller)
        setupPlayer(team, PlayerNo(2), FieldCoordinate(13, 7), controller)
        setupPlayer(team, PlayerNo(3), FieldCoordinate(13, 8), controller)
        setupPlayer(team, PlayerNo(4), FieldCoordinate(15, 1), controller)
        setupPlayer(team, PlayerNo(5), FieldCoordinate(15, 4), controller)
        setupPlayer(team, PlayerNo(6), FieldCoordinate(15, 10), controller)
        setupPlayer(team, PlayerNo(7), FieldCoordinate(15, 13), controller)
        setupPlayer(team, PlayerNo(8), FieldCoordinate(17, 1), controller)
        setupPlayer(team, PlayerNo(9), FieldCoordinate(17, 4), controller)
        setupPlayer(team, PlayerNo(10), FieldCoordinate(17, 10), controller)
        setupPlayer(team, PlayerNo(11), FieldCoordinate(17, 13), controller)
    }

    private fun setupPlayer(
        team: Team,
        playerNo: PlayerNo,
        fieldCoordinate: FieldCoordinate,
        controller: GameController,
    ) {
        controller.processAction(PlayerSelected(team[playerNo]))
        controller.processAction(FieldSquareSelected(fieldCoordinate))
    }

    private fun printStats(
        controller: GameController,
        start: Instant,
    ) {
        val end = Clock.System.now()
        val duration = (end - start).inWholeMilliseconds
        val commands = controller.commands.size
        val msPrCommand: Float = duration / commands.toFloat()
        println("Total time: $duration ms., Commands: $commands, timePrCommand: $msPrCommand ms.")
    }
}
