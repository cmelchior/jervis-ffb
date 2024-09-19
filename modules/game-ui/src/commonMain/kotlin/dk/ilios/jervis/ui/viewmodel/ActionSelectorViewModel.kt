package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerSubActionSelected
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.rules.pathfinder.PathFinder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

//
sealed interface UserInput {
    val actions: List<GameAction>
}

class CompositeUserInput(val inputs: List<UserInput>) : UserInput {
    override val actions: List<GameAction>
        get() = TODO("Not supported")
}

data object WaitingForUserInput : UserInput {
    override val actions: List<GameAction> = emptyList()
}

data object IgnoreUserInput : UserInput {
    override val actions: List<GameAction>
        get() = TODO()
}

data object ResumeUserInput : UserInput {
    override val actions: List<GameAction>
        get() = TODO()
}

class SelectPlayerActionInput(val activePlayerLocation: FieldCoordinate, override val actions: List<PlayerActionSelected>) : UserInput

class SelectPlayerSubActionInput(val activePlayerLocation: FieldCoordinate, override val actions: List<PlayerSubActionSelected>) : UserInput {}

class EndActionInput(val activePlayerLocation: FieldCoordinate, override val actions: List<EndAction>) : UserInput

class SelectPlayerInput(override val actions: List<GameAction>) : UserInput

class DeselectPlayerInput(override val actions: List<GameAction>) : UserInput

data class FieldSquareAction(val coordinate: FieldCoordinate, val action: GameAction, val requiresRoll: Boolean)

class SelectFieldLocationInput(val wrapperAction: List<FieldSquareAction>) : UserInput {
    override val actions = wrapperAction.map { it.action }
    // Map action to each field
    val fieldAction: Map<FieldCoordinate, FieldSquareAction> = wrapperAction.associateBy { it.coordinate }
}

// Class wrapping all user input when selecting moves
// If MoveType.STANDARD is one of the options, we will also calculate all reachable squares that can
// be reached without rolling any dice.
class SelectMoveActionFieldLocationInput(
    val wrapperAction: List<FieldSquareAction>,
    distances: PathFinder.AllPathsResult?,
) : UserInput {
    override val actions = wrapperAction.map { it.action }
    // Map action to each field
    val fieldAction: Map<FieldCoordinate, FieldSquareAction> = wrapperAction.associateBy { it.coordinate }
}

/**
 * View model for the unknown action selector part of the UI. Eventually, this should be removed.
 */
class ActionSelectorViewModel(
    private val uiActionFactory: UiActionFactory,
) {
    val availableActions: Flow<UserInput> = uiActionFactory.unknownActions

    fun start() {
        uiActionFactory.scope.launch {
            uiActionFactory.start(this)
        }
    }

    init {
        start()
    }

    fun actionSelected(action: GameAction) {
        uiActionFactory.userSelectedAction(action)
    }
}
