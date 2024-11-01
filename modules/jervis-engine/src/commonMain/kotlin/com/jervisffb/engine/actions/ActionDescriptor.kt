package com.jervisffb.engine.actions

import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.PlayerAction
import com.jervisffb.engine.rules.bb2020.procedures.BlockDieRoll
import com.jervisffb.engine.rules.bb2020.procedures.D6DieRoll
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll
import com.jervisffb.engine.rules.bb2020.skills.DiceRerollOption
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory
import kotlinx.serialization.Serializable

// Action descriptors
/**
 * Interface describing all legal inputs to a [ActionNode] of a given type.
 *
 *
 *
 *
 *
 */
sealed interface ActionDescriptor {
//    /**
//     * Creates a random game action from pool of
//     */
//    fun createRandom(): GameAction
//    fun createAll(): List<GameAction>
}

// "internal event" for continuing the game state
data object ContinueWhenReady : ActionDescriptor

// An generic action representing "Accept" or "Yes"
data object ConfirmWhenReady : ActionDescriptor

// An generic action representing "Cancel" or "No"
data object CancelWhenReady : ActionDescriptor

// Mark the setup phase as ended for a team
data object EndSetupWhenReady : ActionDescriptor

// Mark the turn as ended for a team
data object EndTurnWhenReady : ActionDescriptor

// Mark the current action for the active player as done.
data object EndActionWhenReady : ActionDescriptor

// Action owner must select a coin side
data object SelectCoinSide : ActionDescriptor

data class SelectSkill(val skill: SkillFactory) : ActionDescriptor

data class SelectInducement(val id: String): ActionDescriptor

data object TossCoin : ActionDescriptor

// Roll a number of dice and return their result
data class RollDice(val dice: List<Dice>) : ActionDescriptor {
    constructor(vararg dice: Dice) : this(dice.toList())
}

/**
 * What kind of move does the player want to perform
 */
@Serializable
enum class MoveType {
    JUMP,
    LEAP,
    STANDARD,
    STAND_UP,
}

data class SelectMoveType(val type: MoveType): ActionDescriptor

data class SelectDirection(
    val origin: OnFieldLocation,
    val directions: List<Direction>
): ActionDescriptor

data class SelectFieldLocation private constructor(
    val x: Int,
    val y: Int,
    val type: Type,
    val requiresRush: Boolean = false,
    val requiresDodge: Boolean = false,
) : ActionDescriptor {
    // What is causing this field location to be selectable
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
}

// Give a number of dice pools, the user needs to select 1 or more of them
sealed interface DicePool<D: DieResult, out T: DieRoll<D>> {
    val id: Int
    val dice: List<T>
    val selectDice: Int
}

data class BlockDicePool(
    override val dice: List<BlockDieRoll>,
    override val selectDice: Int = 1,
    override val id: Int = 0
): DicePool<DBlockResult, DieRoll<DBlockResult>>

data class D6DicePool(
    override val dice: List<D6DieRoll>,
    override val selectDice: Int = 1,
    override val id: Int = 0,
): DicePool<D6Result, DieRoll<D6Result>>

/**
 * Select final result from 1 or more dice pools
 */
data class SelectDicePoolResult(val pools: List<DicePool<*, *>>): ActionDescriptor {
    constructor(pool: DicePool<*, *>) : this(listOf(pool))
}

data object SelectDogout : ActionDescriptor

data class SelectPlayer(val player: PlayerId) : ActionDescriptor {
    constructor(player: Player): this(player.id)
}

data class DeselectPlayer(val player: Player) : ActionDescriptor

data class SelectPlayerAction(val action: PlayerAction) : ActionDescriptor

data class SelectBlockType(val type: BlockType): ActionDescriptor

data class SelectRandomPlayers(val count: Int, val players: List<PlayerId>) :
    ActionDescriptor // This is not a single action

data class SelectRerollOption(
    val option: DiceRerollOption,
    // Identifier for the dice pool being rerolled
    // This is only used in the cases, where you might be juggling multiple dicerolls
    // at the same time, like during Multiple Block
    val dicePoolId: Int = 0,
) : ActionDescriptor

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
) : ActionDescriptor

// Available actions
@Serializable
sealed class DieResult : Number(), GameAction {
    abstract val value: Int
    abstract val min: Short
    abstract val max: Short

    init {
        if (value < min || value > max) {
            throw IllegalArgumentException("Result outside range: $min <= $value <= $max")
        }
    }

    override fun toByte(): Byte = value.toByte()
    override fun toDouble(): Double = value.toDouble()
    override fun toFloat(): Float = value.toFloat()
    override fun toInt(): Int = value.toInt()
    override fun toLong(): Long = value.toLong()
    override fun toShort(): Short = value.toShort()
    override fun toString(): String {
        return "${this::class.simpleName}[$value]"
    }

    fun toLogString(): String = "[$value]"
}
