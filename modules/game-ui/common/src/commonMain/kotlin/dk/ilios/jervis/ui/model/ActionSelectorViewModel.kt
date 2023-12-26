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
import dk.ilios.jervis.utils.createRandomAction
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
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

    private fun startRandomActionSelector() {
        scope.launch {
            while(this.isActive) {
                val (controller, actions) = actionRequestChannel.receive()
                val action: Action = createRandomAction(controller.state, actions)
                actionSelectedChannel.send(action)
            }
        }
    }

    fun actionSelected(action: Action) {
        scope.launch {
            userSelectedAction.send(action)
        }
    }

}