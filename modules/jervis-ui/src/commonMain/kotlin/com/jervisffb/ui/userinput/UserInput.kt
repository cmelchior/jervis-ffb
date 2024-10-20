package com.jervisffb.ui.userinput

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSubActionSelected
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.pathfinder.PathFinder


/**
 * [UserInput] subclasses represent a sp
 */
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

data class FieldSquareAction(val coordinate: FieldCoordinate, val action: GameAction, val requiresRoll: Boolean, val direction: Direction?)

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

sealed interface UserInputDialog : UserInput {
    var owner: Team?
}


