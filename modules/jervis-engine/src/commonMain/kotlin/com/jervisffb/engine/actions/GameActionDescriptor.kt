package com.jervisffb.engine.actions

import com.jervisffb.engine.GameController
import com.jervisffb.engine.actions.SelectFieldLocation.Type
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.PlayerAction
import com.jervisffb.engine.rules.bb2020.skills.DiceRerollOption
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory
import com.jervisffb.engine.utils.combinations

/**
 * Interface describing all legal [GameAction] events of a certain type that an
 * [ActionNode] should accept as a valid action.
 *
 * An [ActionNode] can return multiple action descriptors if it accepts different
 * types of events.
 *
 * @see [ActionNode.getAvailableActions]
 * @see [GameController.getAvailableActions]
 */
sealed interface GameActionDescriptor {
    /**
     * Creates a random game action from the pool of actions described by this
     * descriptor.
     */
    fun createRandom(): GameAction

    /**
     * Generates all valid game actions represented by this descriptor.
     */
    fun createAll(): List<GameAction>
}

// "internal event" for continuing the game state
data object ContinueWhenReady : GameActionDescriptor {
    override fun createRandom(): GameAction = Continue
    override fun createAll(): List<GameAction> = listOf(Continue)
}

// An generic action representing "Accept" or "Yes"
data object ConfirmWhenReady : GameActionDescriptor {
    override fun createRandom(): GameAction = Confirm
    override fun createAll(): List<GameAction> = listOf(Confirm)
}

// An generic action representing "Cancel" or "No"
data object CancelWhenReady : GameActionDescriptor {
    override fun createRandom(): GameAction = Cancel
    override fun createAll(): List<GameAction> = listOf(Cancel)
}

// Mark the setup phase as ended for a team
data object EndSetupWhenReady : GameActionDescriptor {
    override fun createRandom(): GameAction = EndSetup
    override fun createAll(): List<GameAction> = listOf(EndSetup)
}

// Mark the turn as ended for a team
data object EndTurnWhenReady : GameActionDescriptor {
    override fun createRandom(): GameAction = EndTurn
    override fun createAll(): List<GameAction> = listOf(EndTurn)
}

// Mark the current action for the active player as done.
data object EndActionWhenReady : GameActionDescriptor {
    override fun createRandom(): GameAction = EndAction
    override fun createAll(): List<GameAction> = listOf(EndAction)
}

// Action owner must select a coin side
data object SelectCoinSide : GameActionDescriptor {
    override fun createRandom(): GameAction = CoinSideSelected.allOptions().random()
    override fun createAll(): List<GameAction> = CoinSideSelected.allOptions()
}

data class SelectSkill(
    val skills: List<SkillFactory>
) : GameActionDescriptor {
    override fun createRandom(): GameAction = SkillSelected(skills.random())
    override fun createAll(): List<GameAction> = skills.map { SkillSelected(it) }
}

// TODO Need to figure out how buying indicuments work. One step or multiple?
data class SelectInducement(
    val id: List<String>
): GameActionDescriptor {
    override fun createRandom(): GameAction {
        TODO("Not yet implemented")
    }
    override fun createAll(): List<GameAction> {
        TODO("Not yet implemented")
    }
}

data object TossCoin : GameActionDescriptor {
    override fun createRandom(): GameAction = CoinTossResult.allOptions().random()
    override fun createAll(): List<GameAction> = CoinTossResult.allOptions()
}

// Roll a number of dice and return their result
data class RollDice(
    val dice: List<Dice>
) : GameActionDescriptor {
    constructor(vararg dice: Dice) : this(dice.toList())
    override fun createRandom(): GameAction {
        return dice.map {
            when(it) {
                Dice.D2 -> D2Result.allOptions().random()
                Dice.D3 -> D3Result.allOptions().random()
                Dice.D4 -> D4Result.allOptions().random()
                Dice.D6 -> D6Result.allOptions().random()
                Dice.D8 -> D8Result.allOptions().random()
                Dice.D12 -> D12Result.allOptions().random()
                Dice.D16 -> D16Result.allOptions().random()
                Dice.D20 -> D20Result.allOptions().random()
                Dice.BLOCK -> DBlockResult.allOptions().random()
            }
        }.let { diceRolls ->
            DiceRollResults(diceRolls)
        }
    }

    override fun createAll(): List<GameAction> {
        TODO("Not yet implemented")
    }
}

data class SelectMoveType(
    val type: List<MoveType>
): GameActionDescriptor {
    override fun createRandom(): GameAction = MoveTypeSelected(type.random())
    override fun createAll(): List<GameAction> = type.map { MoveTypeSelected(it) }
}

data class SelectDirection(
    val origin: OnFieldLocation,
    val directions: List<Direction>
): GameActionDescriptor {
    override fun createRandom(): GameAction = DirectionSelected(directions.random())
    override fun createAll(): List<GameAction> = directions.map { DirectionSelected(it) }
}


data class Square(
    val x: Int,
    val y: Int,
    val type: Type,
    val requiresRush: Boolean = false,
    val requiresDodge: Boolean = false,
)

data class SelectFieldLocation private constructor(
    val x: Int,
    val y: Int,
    val type: Type,
    val requiresRush: Boolean = false,
    val requiresDodge: Boolean = false,
) : GameActionDescriptor {

    // This is in order so the UI can filter or show options in different ways.
    enum class Type {
        SETUP,
        DIRECTION,
        STAND_UP,
        MOVE,
        RUSH,
        JUMP,
        LEAP,
        KICK,
        THROW_TARGET
    }
    val coordinate: FieldCoordinate = FieldCoordinate(x, y)
    private constructor(coordinate: FieldCoordinate, type: Type, needRush: Boolean = false, needDodge: Boolean = false) : this(coordinate.x, coordinate.y, type, needRush, needDodge)

    companion object {
        fun setup(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.SETUP)
        fun direction(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.DIRECTION)
        fun standUp(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.STAND_UP)
        fun move(coordinate: FieldCoordinate, needRush: Boolean, needDodge: Boolean) = SelectFieldLocation(coordinate, Type.MOVE, needRush, needDodge)
        fun rush(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.RUSH)
        fun jump(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.JUMP)
        fun leap(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.LEAP)
        fun kick(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.KICK)
        fun throwTarget(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.THROW_TARGET)
    }

    override fun createRandom(): GameAction {
        TODO("Not yet implemented")
    }

    override fun createAll(): List<GameAction> {
        TODO("Not yet implemented")
    }
}

/**
 * Select final result from 1 or more dice pools
 */
data class SelectDicePoolResult(
    val pools: List<DicePool<*, *>>
): GameActionDescriptor {
    constructor(pool: DicePool<*, *>) : this(listOf(pool))
    override fun createRandom(): GameAction {
        TODO("Not yet implemented")
    }
    override fun createAll(): List<GameAction> {
        TODO("Not yet implemented")
    }
}

data object SelectDogout : GameActionDescriptor {
    override fun createRandom(): GameAction = DogoutSelected
    override fun createAll(): List<GameAction> = listOf(DogoutSelected)
}

data class SelectPlayer(
    val players: List<PlayerId>
) : GameActionDescriptor {
    constructor(player: Player): this(listOf(player.id))
    // constructor(player: List<Player>): this(player.map { it.id })

    override fun createRandom(): GameAction = PlayerSelected(players.random())
    override fun createAll(): List<GameAction> = players.map { PlayerSelected(it) }
}

data class DeselectPlayer(
    val players: List<Player>
) : GameActionDescriptor {
    constructor(player: Player): this(listOf(player))
    override fun createRandom(): GameAction = PlayerDeselected(players.random())
    override fun createAll(): List<GameAction> = players.map { PlayerDeselected(it) }
}

data class SelectPlayerAction(
    val actions: List<PlayerAction>
) : GameActionDescriptor {
    constructor(action: PlayerAction): this(listOf(action))
    override fun createRandom(): GameAction = PlayerActionSelected(actions.random().type)
    override fun createAll(): List<GameAction> = actions.map { PlayerActionSelected(it.type) }
}

data class SelectBlockType(
    val types: List<BlockType>
): GameActionDescriptor {
    override fun createRandom(): GameAction = BlockTypeSelected(types.random())
    override fun createAll(): List<GameAction> = types.map { BlockTypeSelected(it) }
}

data class SelectRandomPlayers(
    val count: Int,
    val players: List<PlayerId>
): GameActionDescriptor {
    override fun createRandom(): GameAction {
        return RandomPlayersSelected(players.shuffled().subList(0, count))
    }
    override fun createAll(): List<GameAction> {
        return players.combinations(count).map { players ->
            RandomPlayersSelected(players.toList())
        }
    }
}

data class SelectRerollOption(
    val options: List<DiceRerollOption>,
    // Identifier for the dice pool being rerolled
    // This is only used in the cases, where you might be juggling multiple dice
    // rolls at the same time, like during Multiple Block
    val dicePoolId: Int = 0,
) : GameActionDescriptor {
    override fun createRandom(): GameAction = RerollOptionSelected(options.random(), dicePoolId)
    override fun createAll(): List<GameAction> {
        return options.map {
            RerollOptionSelected(it, dicePoolId)
        }
    }
}

// Successful might be hard to interpret in some cases, in which this is `null`
// Otherwise it contains
data class SelectNoReroll(
    // Whether the first roll was considered a "succcess".
    // This is technically just state, but since this is normally
    // defined inside various custom contexts. It is very tricky
    // to get to this state from whoever is creating the GameAction.
    val rollSuccessful: Boolean? = null,
    // Optional dice pool id that can be used to identify the pool
    // of dice. This is only relevant if multiple pools are being rolled
    // at the same time.
    val dicePoolId: Int = 0,
) : GameActionDescriptor {
    override fun createRandom(): GameAction = NoRerollSelected(dicePoolId)
    override fun createAll(): List<GameAction> = listOf(NoRerollSelected(dicePoolId))
}
