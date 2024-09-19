package dk.ilios.jervis.actions

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.rules.BlockType
import dk.ilios.jervis.rules.PlayerAction
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.rules.skills.SkillFactory
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
    // RUSH,
    STANDARD,
    STAND_UP,
}

data class SelectMoveType(val type: MoveType): ActionDescriptor

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
        PUSH,
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
        fun push(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.PUSH)
        fun standUp(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.STAND_UP)
        fun move(coordinate: FieldCoordinate, needRush: Boolean, needDodge: Boolean) = SelectFieldLocation(coordinate, Type.MOVE, needRush, needDodge)
        fun rush(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.RUSH)
        fun jump(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.JUMP)
        fun leap(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.LEAP)
        fun kick(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.KICK)
        fun throwTarget(coordinate: FieldCoordinate) = SelectFieldLocation(coordinate, Type.THROW_TARGET)
    }
}

// Give a number of dice results, the user needs to select 1 or more of them
data class SelectDiceResult(val choices: List<DieResult>, val count: Int = 1): ActionDescriptor

data object SelectDogout : ActionDescriptor

data class SelectPlayer(val player: PlayerId) : ActionDescriptor {
    constructor(player: Player): this(player.id)
}

data class DeselectPlayer(val player: Player) : ActionDescriptor

data class SelectAction(val action: PlayerAction) : ActionDescriptor

data class SelectBlockType(val types: List<BlockType>): ActionDescriptor

data class SelectRandomPlayers(val count: Int, val players: List<PlayerId>) : ActionDescriptor // This is not a single action

// data class SelectSkillRerollSource(val skill: Skill): ActionDescriptor
// data class SelectTeamRerollSource(val reroll: TeamReroll): ActionDescriptor
data class SelectRerollOption(val option: DiceRerollOption) : ActionDescriptor

// Successful might be hard to interpret in some cases, in which this is `null`
// Otherwise it contains
// TODO Do we want to send things that are effectively just "state"?
//  Could this open up a door for a lot of things.
//  On the other side, it might just be useful information
data class SelectNoReroll(val rollSuccessful: Boolean? = null) : ActionDescriptor

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
