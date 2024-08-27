package dk.ilios.jervis.actions

import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.PlayerAction
import dk.ilios.jervis.rules.skills.DiceRerollOption
import kotlinx.serialization.Serializable

// Action descriptors
sealed interface ActionDescriptor

data object ContinueWhenReady : ActionDescriptor // "internal event" for continuing the game state

data object ConfirmWhenReady : ActionDescriptor // An generic action that requires explicit confirmation by a player

data object CancelWhenReady : ActionDescriptor // An generic action that requires explicit confirmation by a player

data object EndSetupWhenReady : ActionDescriptor // Mark the setup phase as ended for a team

data object EndTurnWhenReady : ActionDescriptor // Mark the turn as ended for a team

data object EndActionWhenReady : ActionDescriptor

data object SelectCoinSide : ActionDescriptor

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
    RUSH,
    STANDARD,
    STAND_UP,
}

data class SelectMoveType(val type: MoveType): ActionDescriptor

data class SelectFieldLocation private constructor(val x: Int, val y: Int, val type: Type) : ActionDescriptor {
    // What is causing this field location to be selectable
    // This is in order so the UI can filter or show options in different ways.
    enum class Type {
        SETUP,
        PUSH,
        STAND_UP,
        MOVE,
        RUSH,
        JUMP,
        LEAP,
        KICK
    }
    val coordinate: FieldCoordinate = FieldCoordinate(x, y)
    private constructor(coordinate: FieldCoordinate, type: Type) : this(coordinate.x, coordinate.y, type)

    companion object {
        fun setup(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.SETUP)
        fun push(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.PUSH)
        fun standUp(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.STAND_UP)
        fun move(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.MOVE)
        fun rush(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.RUSH)
        fun jump(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.JUMP)
        fun leap(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.LEAP)
        fun kick(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.KICK)
    }
}

// Give a number of dice results, the user needs to select 1 or more of them
data class SelectDiceResult(val choices: List<DieResult>, val count: Int = 1): ActionDescriptor

data object SelectDogout : ActionDescriptor

data class SelectPlayer(val player: Player) : ActionDescriptor

data class DeselectPlayer(val player: Player) : ActionDescriptor

data class SelectAction(val action: PlayerAction) : ActionDescriptor

data class SelectRandomPlayers(val count: Int, val players: List<Player>) : ActionDescriptor // This is not a single action

// data class SelectSkillRerollSource(val skill: Skill): ActionDescriptor
// data class SelectTeamRerollSource(val reroll: TeamReroll): ActionDescriptor
data class SelectRerollOption(val option: DiceRerollOption) : ActionDescriptor

data object SelectNoReroll : ActionDescriptor

// Available actions
@Serializable
sealed class DieResult : Number(), GameAction {
    abstract val result: Int
    abstract val min: Short
    abstract val max: Short

    init {
        if (result < min || result > max) {
            throw IllegalArgumentException("Result outside range: $min <= $result <= $max")
        }
    }

    override fun toByte(): Byte = result.toByte()

    override fun toDouble(): Double = result.toDouble()

    override fun toFloat(): Float = result.toFloat()

    override fun toInt(): Int = result.toInt()

    override fun toLong(): Long = result.toLong()

    override fun toShort(): Short = result.toShort()

    override fun toString(): String {
        return "${this::class.simpleName}[$result]"
    }

    fun toLogString(): String = "[$result]"
}
