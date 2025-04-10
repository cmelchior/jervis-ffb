package com.jervisffb.engine.actions

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.PlayerAction
import com.jervisffb.engine.rules.bb2020.skills.DiceRerollOption
import com.jervisffb.engine.utils.cartesianProduct
import com.jervisffb.engine.utils.combinations
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.random.Random


/**
 * These ID's uniquely identify a [GameAction] that has been handled by the
 * [GameEngineController]. The IDs should always be increasing. This means that
 * looking at the action history should have a list of action ids ranging from 1 until
 * count(actions).
 *
 * This also makes it possible to reason about multiple events arriving
 * at the GameController. If it sees a GameAction with an ID that has already
 * been processed, the next action with the same ID should be ignored (or throw
 * an error).
 */
@Serializable
@JvmInline
value class GameActionId(val value: Int) {
    operator fun plus(increment: Int): GameActionId {
        return GameActionId(value + increment)
    }
    operator fun minus(increment: Int): GameActionId {
        return GameActionId(value - increment)
    }
    operator fun compareTo(other: GameActionId): Int {
        return value.compareTo(other.value)
    }
}

/**
 * Interface describing all legal [GameAction] events of a certain type that an
 * [ActionNode] should accept as a valid action.
 *
 * An [ActionNode] can return multiple action descriptors if it accepts different
 * types of events.
 *
 * @see [ActionNode.getAvailableActions]
 * @see [GameEngineController.getAvailableActions]
 */
sealed interface GameActionDescriptor {

    // Returns how many GameActions are described by this descriptor
    val size: Int

    /**
     * Creates a random game action from the pool of actions described by this
     * descriptor.
     */
    fun createRandom(random: Random = Random): GameAction

    /**
     * Generates all valid game actions represented by this descriptor.
     */
    fun createAll(): List<GameAction>
}

// "internal event" for continuing the game state
data object ContinueWhenReady : GameActionDescriptor {
    override val size: Int = 1
    override fun createRandom(random: Random): GameAction = Continue
    override fun createAll(): List<GameAction> = listOf(Continue)
}

// An generic action representing "Accept" or "Yes"
data object ConfirmWhenReady : GameActionDescriptor {
    override val size: Int = 1
    override fun createRandom(random: Random): GameAction = Confirm
    override fun createAll(): List<GameAction> = listOf(Confirm)
}

// An generic action representing "Cancel" or "No"
data object CancelWhenReady : GameActionDescriptor {
    override val size: Int = 1
    override fun createRandom(random: Random): GameAction = Cancel
    override fun createAll(): List<GameAction> = listOf(Cancel)
}

// Mark the setup phase as ended for a team
data object EndSetupWhenReady : GameActionDescriptor {
    override val size: Int = 1
    override fun createRandom(random: Random): GameAction = EndSetup
    override fun createAll(): List<GameAction> = listOf(EndSetup)
}

// Mark the turn as ended for a team
data object EndTurnWhenReady : GameActionDescriptor {
    override val size: Int = 1
    override fun createRandom(random: Random): GameAction = EndTurn
    override fun createAll(): List<GameAction> = listOf(EndTurn)
}

// Mark the current action for the active player as done.
data object EndActionWhenReady : GameActionDescriptor {
    override val size: Int = 1
    override fun createRandom(random: Random): GameAction = EndAction
    override fun createAll(): List<GameAction> = listOf(EndAction)
}

// Action owner must select a coin side
data object SelectCoinSide : GameActionDescriptor {
    override val size: Int = 2
    override fun createRandom(random: Random): GameAction = CoinSideSelected.allOptions().random(random)
    override fun createAll(): List<GameAction> = CoinSideSelected.allOptions()
}

data class SelectSkill(
    val skills: List<SkillId>
) : GameActionDescriptor {
    override val size: Int = skills.size
    override fun createRandom(random: Random): GameAction = SkillSelected(skills.random(random))
    override fun createAll(): List<GameAction> = skills.map { SkillSelected(it) }
}

// TODO Need to figure out how buying indicuments work. One step or multiple?
data class SelectInducement(
    val id: List<String>
): GameActionDescriptor {
    override val size: Int = id.size
    override fun createRandom(random: Random): GameAction {
        TODO("Not yet implemented")
    }
    override fun createAll(): List<GameAction> {
        TODO("Not yet implemented")
    }
}

data object TossCoin : GameActionDescriptor {
    override val size: Int = 2
    override fun createRandom(random: Random): GameAction = CoinTossResult.allOptions().random(random)
    override fun createAll(): List<GameAction> = CoinTossResult.allOptions()
}

// Roll a number of dice and return their result
data class RollDice(
    val dice: List<Dice>
) : GameActionDescriptor {
    constructor(vararg dice: Dice) : this(dice.toList())
    override val size: Int
        get() {
            return dice.fold(1) { acc, die ->
                val sides = when (die) {
                    Dice.D2 -> 2
                    Dice.D3 -> 3
                    Dice.D4 -> 4
                    Dice.D6 -> 6
                    Dice.D8 -> 8
                    Dice.D12 -> 12
                    Dice.D16 -> 16
                    Dice.D20 -> 20
                    Dice.BLOCK -> 6
                }
                acc * sides
            }
        }

    override fun createRandom(random: Random): GameAction {
        return dice.map {
            when (it) {
                Dice.D2 -> D2Result.allOptions().random(random)
                Dice.D3 -> D3Result.allOptions().random(random)
                Dice.D4 -> D4Result.allOptions().random(random)
                Dice.D6 -> D6Result.allOptions().random(random)
                Dice.D8 -> D8Result.allOptions().random(random)
                Dice.D12 -> D12Result.allOptions().random(random)
                Dice.D16 -> D16Result.allOptions().random(random)
                Dice.D20 -> D20Result.allOptions().random(random)
                Dice.BLOCK -> DBlockResult.allOptions().random(random)
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
    val types: List<MoveType>
): GameActionDescriptor {
    override val size: Int = types.size
    override fun createRandom(random: Random): GameAction = MoveTypeSelected(types.random(random))
    override fun createAll(): List<GameAction> = types.map { MoveTypeSelected(it) }
}

data class SelectDirection(
    val origin: OnFieldLocation,
    val directions: List<Direction>
): GameActionDescriptor {
    override val size: Int = directions.size
    override fun createRandom(random: Random): GameAction = DirectionSelected(directions.random(random))
    override fun createAll(): List<GameAction> = directions.map { DirectionSelected(it) }
}

data class TargetSquare(
    val x: Int,
    val y: Int,
    val type: Type,
    val requiresRush: Boolean = false,
    val requiresDodge: Boolean = false,
    val requiresJump: Boolean = false
) {
    constructor(coordinate: FieldCoordinate, type: Type, requiresRush: Boolean = false, requiresDodge: Boolean = false, requiresJump: Boolean = false) : this(
        coordinate.x,
        coordinate.y,
        type,
        requiresRush,
        requiresDodge,
        requiresJump
    )

    val coordinate: FieldCoordinate = FieldCoordinate(x, y)

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

    companion object {
        fun setup(coordinate: FieldCoordinate) = TargetSquare(coordinate, Type.SETUP)
        fun direction(coordinate: FieldCoordinate) = TargetSquare(coordinate, Type.DIRECTION)
        fun standUp(coordinate: FieldCoordinate) = TargetSquare(coordinate, Type.STAND_UP)
        fun move(coordinate: FieldCoordinate, needRush: Boolean, needDodge: Boolean) = TargetSquare(coordinate, Type.MOVE, needRush, needDodge)
        fun rush(coordinate: FieldCoordinate) = TargetSquare(coordinate, Type.RUSH)
        fun jump(coordinate: FieldCoordinate, needRush: Boolean) = TargetSquare(coordinate, Type.JUMP, needRush, requiresDodge = false, requiresJump = true)
        fun leap(coordinate: FieldCoordinate) = TargetSquare(coordinate, Type.LEAP)
        fun kick(coordinate: FieldCoordinate) = TargetSquare(coordinate, Type.KICK)
        fun throwTarget(coordinate: FieldCoordinate) = TargetSquare(coordinate, Type.THROW_TARGET)
    }
}

data class SelectFieldLocation(val squares: List<TargetSquare>) : GameActionDescriptor {
    override val size: Int = squares.size
    init {
        if (squares.isEmpty()) {
            throw IllegalArgumentException("SelectFieldLocation must contain at least one target")
        }
    }
    override fun createRandom(random: Random): GameAction {
        val target = squares.random(random).coordinate
        return FieldSquareSelected(target)
    }

    override fun createAll(): List<GameAction> {
        return squares.map {
            FieldSquareSelected(it.coordinate)
        }
    }
}

/**
 * Select a final result from 1 or more dice pools.
 *
 * Currently, we only have "dice pools" when rolling Block dice, but
 * this class has been generalized to any type of dice.
 *
 * We have multiple dice pools during "Multiple Block" as rolling both
 * block dice happen "at the same time".
 */
data class SelectDicePoolResult(
    val pools: List<DicePool<*, *>>
): GameActionDescriptor {
    override val size: Int = pools.size
    constructor(pool: DicePool<*, *>) : this(listOf(pool))
    override fun createRandom(random: Random): GameAction {
        return createAll().random(random)
    }
    override fun createAll(): List<GameAction> {
        // Each entry is all combinations inside the given pool.
        // We need to find all combinations between all pools
        val availableChoicesPrPool = pools.map { pool ->
            val combinations = pool.dice.combinations(pool.selectDice)
            combinations.map { randomChoice ->
                DicePoolChoice(pool.id, randomChoice.toList().map { it.result })
            }
        }
        return cartesianProduct(availableChoicesPrPool).map {
            DicePoolResultsSelected(it)
        }
    }
}

data object SelectDogout : GameActionDescriptor {
    override val size: Int = 1
    override fun createRandom(random: Random): GameAction = DogoutSelected
    override fun createAll(): List<GameAction> = listOf(DogoutSelected)
}

data class SelectPlayer(
    val players: List<PlayerId>
) : GameActionDescriptor {
    constructor(player: Player): this(listOf(player.id))
    override val size: Int = players.size
    init {
        if (players.isEmpty()) {
            throw IllegalArgumentException("SelectPlayer must contain at least one player")
        }
    }
    override fun createRandom(random: Random): GameAction {
        val selectedPlayer = players.random(random)
        return PlayerSelected(selectedPlayer)
    }
    override fun createAll(): List<GameAction> = players.map { PlayerSelected(it) }
}

data class DeselectPlayer(
    val players: List<Player>
) : GameActionDescriptor {
    constructor(player: Player): this(listOf(player))
    override val size: Int = players.size
    override fun createRandom(random: Random): GameAction = PlayerDeselected(players.random(random))
    override fun createAll(): List<GameAction> = players.map { PlayerDeselected(it) }
}

data class SelectPlayerAction(
    val actions: List<PlayerAction>
) : GameActionDescriptor {
    constructor(action: PlayerAction): this(listOf(action))
    override val size: Int = actions.size
    override fun createRandom(random: Random): GameAction = PlayerActionSelected(actions.random(random).type)
    override fun createAll(): List<GameAction> = actions.map { PlayerActionSelected(it.type) }
}

data class SelectBlockType(
    val types: List<BlockType>
): GameActionDescriptor {
    override val size: Int = types.size
    override fun createRandom(random: Random): GameAction = BlockTypeSelected(types.random(random))
    override fun createAll(): List<GameAction> = types.map { BlockTypeSelected(it) }
}

data class SelectRandomPlayers(
    val count: Int,
    val players: List<PlayerId>
): GameActionDescriptor {
    override val size: Int
        get() {
            var results = 0
            for (i in 0 until count) {
                results *= (players.size - i)
            }
            return results
        }
    override fun createRandom(random: Random): GameAction {
        return RandomPlayersSelected(players.shuffled(random).subList(0, count))
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
    override val size: Int = options.size
    override fun createRandom(random: Random): GameAction = RerollOptionSelected(options.random(random), dicePoolId)
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
    override val size: Int = 1
    override fun createRandom(random: Random): GameAction = NoRerollSelected(dicePoolId)
    override fun createAll(): List<GameAction> = listOf(NoRerollSelected(dicePoolId))
}
