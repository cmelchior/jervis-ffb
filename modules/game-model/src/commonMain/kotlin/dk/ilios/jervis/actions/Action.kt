package dk.ilios.jervis.actions

import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Player
import kotlin.random.Random

sealed interface Action

// Action descriptors
sealed interface ActionDescriptor
data object ContinueWhenReady: ActionDescriptor // "internal event" for continuing the game state
data object ConfirmWhenReady: ActionDescriptor // An generic action that requires explicit confirmation by a player
data object EndSetupWhenReady: ActionDescriptor // Mark the setup phase as ended for a team
data object EndTurnWhenReady: ActionDescriptor // Mark the turn as ended for a team
data object RollD2: ActionDescriptor
data class SelectFieldLocation(val x: Int, val y: Int): ActionDescriptor
data object SelectDogout: ActionDescriptor
data class SelectPlayer(val player: Player): ActionDescriptor

// Available actions
abstract class DieResult(val result: Int, val min: Short, val max: Short): Number(), Action {
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
}

data object Continue: Action
data object Confirm: Action
data object EndTurn: Action
data object EndSetup: Action
class D2Result(result: Int = Random.nextInt(1, 3)): DieResult(result, 1, 2)
data class PlayerSelected(val player: Player): Action
data object DogoutSelected: Action
data class FieldSquareSelected(val x: Int, val y: Int): Action {
    constructor(coordinate: FieldCoordinate): this(coordinate.x, coordinate.y)
    override fun toString(): String {
        return "${this::class.simpleName}[$x, $y]"
    }
}
