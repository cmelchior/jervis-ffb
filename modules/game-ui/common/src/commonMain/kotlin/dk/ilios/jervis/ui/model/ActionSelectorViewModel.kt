package dk.ilios.jervis.ui.model

import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D2Result
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndSetupWhenReady
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.EndTurnWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RollD2
import dk.ilios.jervis.actions.SelectDogout
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.procedures.SetupKickingTeam
import dk.ilios.jervis.utils.createRandomAction
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class ActionSelectorViewModel(
    private val controller: GameController,
    private val actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
    private val actionSelectedChannel: Channel<Action>
) {

    val scope = CoroutineScope(CoroutineName("ActionSelectorScope") + Dispatchers.Default)
    private val _availableActions: MutableSharedFlow<List<Action>> = MutableSharedFlow(extraBufferCapacity = 1)
    private val userSelectedAction = Channel<Action>(1)
    val availableActions: Flow<List<Action>> = _availableActions

    fun start(randomActions: Boolean) {
        if (randomActions) {
            startRandomActionSelector()
        } else {
            startUserActionSelector()
        }
        scope.launch {
            controller.start()
        }
    }

    private fun startUserActionSelector() {
        scope.launch {
            while(true) {
                val (controller, actions) = actionRequestChannel.receive()
                if (controller.stack.firstOrNull()?.procedure == SetupKickingTeam && controller.stack.firstOrNull()?.currentNode() == SetupKickingTeam.SelectPlayerOrEndSetup) {
                    handleHomeKickingSetup(controller, actionRequestChannel, actionSelectedChannel)
                    handleAwayKickingSetup(controller, actionRequestChannel, actionSelectedChannel)
                    actionSelectedChannel.send(EndSetup)
                } else {
                    val availableActions: List<Action> = actions.map { action ->
                        when (action) {
                            ContinueWhenReady -> Continue
                            EndTurnWhenReady -> EndTurn
                            RollD2 -> D2Result(Random.nextInt(1, 2))
                            ConfirmWhenReady -> Confirm
                            EndSetupWhenReady -> EndSetup
                            SelectDogout -> DogoutSelected
                            is SelectFieldLocation -> FieldSquareSelected(action.x, action.y)
                            is SelectPlayer -> PlayerSelected(action.player)
                        }
                    }
                    _availableActions.emit(availableActions)
                    val selectedAction = userSelectedAction.receive()
                    actionSelectedChannel.send(selectedAction)
                }
            }
        }
    }

    private suspend fun handleHomeKickingSetup(
        controller: GameController,
        actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
        actionSelectedChannel: Channel<Action>
    ) {
        val game: Game = controller.state
        if (game.kickingTeam != game.homeTeam) return

        val team = game.kickingTeam

        setupPlayer(team, PlayerNo(1), FieldCoordinate(12, 6), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(2), FieldCoordinate(12, 7), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(3), FieldCoordinate(12, 8), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(4), FieldCoordinate(10, 1), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(5), FieldCoordinate(10, 4), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(6), FieldCoordinate(10, 10), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(7), FieldCoordinate(10, 13), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(8), FieldCoordinate(8, 1), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(9), FieldCoordinate(8, 4), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(10), FieldCoordinate(8, 10), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(11), FieldCoordinate(8, 13), actionRequestChannel, actionSelectedChannel)
    }

    private suspend fun handleAwayKickingSetup(
        controller: GameController,
        actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
        actionSelectedChannel: Channel<Action>
    ) {
        val game: Game = controller.state
        if (game.kickingTeam != game.awayTeam) return

        val team = game.kickingTeam

        setupPlayer(team, PlayerNo(1), FieldCoordinate(13, 6), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(2), FieldCoordinate(13, 7), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(3), FieldCoordinate(13, 8), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(4), FieldCoordinate(15, 1), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(5), FieldCoordinate(15, 4), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(6), FieldCoordinate(15, 10), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(7), FieldCoordinate(15, 13), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(8), FieldCoordinate(17, 1), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(9), FieldCoordinate(17, 4), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(10), FieldCoordinate(17, 10), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(11), FieldCoordinate(17, 13), actionRequestChannel, actionSelectedChannel)
    }

    private suspend fun setupPlayer(
        team: Team,
        playerNo: PlayerNo,
        fieldCoordinate: FieldCoordinate,
        actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
        actionSelectedChannel: Channel<Action>
    ) {
        actionSelectedChannel.send(PlayerSelected(team.players[playerNo]!!))
        actionRequestChannel.receive()
        actionSelectedChannel.send(FieldSquareSelected(fieldCoordinate))
        actionRequestChannel.receive()
    }

    private fun startRandomActionSelector() {
        scope.launch {
            while(this.isActive) {
                val (controller, actions) = actionRequestChannel.receive()
                delay(20)
                // TODO This wrong as we will create the wrong random action at the end
                if (controller.stack.firstOrNull()?.procedure == SetupKickingTeam && controller.stack.firstOrNull()?.currentNode() == SetupKickingTeam.SelectPlayerOrEndSetup) {
                    handleHomeKickingSetup(controller, actionRequestChannel, actionSelectedChannel)
                    handleAwayKickingSetup(controller, actionRequestChannel, actionSelectedChannel)
                    actionSelectedChannel.send(EndSetup)
                } else {
                    val action: Action = createRandomAction(controller.state, actions)
                    actionSelectedChannel.send(action)
                }
            }
        }
    }

    fun actionSelected(action: Action) {
        scope.launch {
            userSelectedAction.send(action)
        }
    }

}