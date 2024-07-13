package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

//
sealed interface UserInput {
    val actions: List<GameAction>
}

object WaitingForUserInput: UserInput {
    override val actions: List<GameAction> = emptyList()
}

class SelectPlayerInput(override val actions: List<GameAction>): UserInput
class SelectFieldLocationInput(override val actions: List<FieldSquareSelected>): UserInput {
    val fieldActions: Map<Pair<Int, Int>, FieldSquareSelected> = actions.associateBy { Pair(it.x, it.y) }
}

//class SelectKickingPlayer(val team: Team, override val actions: List<GameAction>) : UserInput

/**
 * View model for the unknown action selector part of the UI. Eventually this should be removed.
 */
class ActionSelectorViewModel(
    private val uiActionFactory: UiActionFactory,
) {
    val availableActions: Flow<List<UserInput>> = uiActionFactory.unknownActions.scan(emptyList()) { list, element ->
        if (element == WaitingForUserInput) {
            emptyList()
        } else {
            list + element
        }
    }

    fun start() {
        uiActionFactory.scope.launch {
            uiActionFactory.start(this)
        }
    }

//    private suspend fun useAutomatedActions(controller: GameController): Boolean {
//        if (controller.stack.firstOrNull()?.procedure == SetupTeam && controller.stack.firstOrNull()?.currentNode() == SetupTeam.SelectPlayerOrEndSetup) {
//            if (controller.state.activeTeam.isHomeTeam()) {
//                handleHomeKickingSetup(controller, actionRequestChannel, actionSelectedChannel)
//            } else {
//                handleAwayKickingSetup(controller, actionRequestChannel, actionSelectedChannel)
//            }
//            actionSelectedChannel.send(EndSetup)
//            return true
////        } else if (controller.stack.firstOrNull()?.procedure == TheKickOffEvent && controller.stack.firstOrNull()?.currentNode() == TheKickOffEvent.ResolveKickOffEvent) {
////            actionSelectedChannel.send(DiceResults(listOf(D6Result(2), D6Result(4))))
////            return true
//        } else {
//            return false
//        }
//    }

//    private suspend fun setupPlayer(
//        team: Team,
//        playerNo: PlayerNo,
//        fieldCoordinate: FieldCoordinate,
//        actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
//        actionSelectedChannel: Channel<GameAction>
//    ) {
//        actionSelectedChannel.send(PlayerSelected(team[playerNo]!!))
//        actionRequestChannel.receive()
//        actionSelectedChannel.send(FieldSquareSelected(fieldCoordinate))
//        actionRequestChannel.receive()
//    }
//
//    private suspend fun handleHomeKickingSetup(
//        controller: GameController,
//        actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
//        actionSelectedChannel: Channel<GameAction>
//    ) {
//        val game: Game = controller.state
//        val team = game.activeTeam
//
//        setupPlayer(team, PlayerNo(1), FieldCoordinate(12, 6), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(2), FieldCoordinate(12, 7), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(3), FieldCoordinate(12, 8), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(4), FieldCoordinate(10, 1), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(5), FieldCoordinate(10, 4), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(6), FieldCoordinate(10, 10), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(7), FieldCoordinate(10, 13), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(8), FieldCoordinate(8, 1), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(9), FieldCoordinate(8, 4), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(10), FieldCoordinate(8, 10), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(11), FieldCoordinate(8, 13), actionRequestChannel, actionSelectedChannel)
//    }
//
//    private suspend fun handleAwayKickingSetup(
//        controller: GameController,
//        actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
//        actionSelectedChannel: Channel<GameAction>
//    ) {
//        val game: Game = controller.state
//        val team = game.activeTeam
//
//        setupPlayer(team, PlayerNo(1), FieldCoordinate(13, 6), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(2), FieldCoordinate(13, 7), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(3), FieldCoordinate(13, 8), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(4), FieldCoordinate(15, 1), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(5), FieldCoordinate(15, 4), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(6), FieldCoordinate(15, 10), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(7), FieldCoordinate(15, 13), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(8), FieldCoordinate(17, 1), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(9), FieldCoordinate(17, 4), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(10), FieldCoordinate(17, 10), actionRequestChannel, actionSelectedChannel)
//        setupPlayer(team, PlayerNo(11), FieldCoordinate(17, 13), actionRequestChannel, actionSelectedChannel)
//    }

    fun actionSelected(action: GameAction) {
        uiActionFactory.userSelectedAction(action)
    }
}