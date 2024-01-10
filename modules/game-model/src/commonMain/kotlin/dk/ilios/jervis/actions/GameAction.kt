package dk.ilios.jervis.actions

import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.PlayerAction
import kotlin.random.Random

enum class Dice {
    D2, D3, D4, D6, D8, D12, D16, D20
}

// Action descriptors
sealed interface ActionDescriptor
data object ContinueWhenReady: ActionDescriptor // "internal event" for continuing the game state
data object ConfirmWhenReady: ActionDescriptor // An generic action that requires explicit confirmation by a player
data object CancelWhenReady: ActionDescriptor // An generic action that requires explicit confirmation by a player
data object EndSetupWhenReady: ActionDescriptor // Mark the setup phase as ended for a team
data object EndTurnWhenReady: ActionDescriptor // Mark the turn as ended for a team
data object EndActionWhenReady: ActionDescriptor
data object SelectCoinSide: ActionDescriptor
data object TossCoin: ActionDescriptor
data class RollDice(val dice: List<Dice>): ActionDescriptor {
    constructor(vararg dice: Dice): this(dice.toList())
}

data class SelectFieldLocation(val x: Int, val y: Int): ActionDescriptor {
    constructor(coordinate: FieldCoordinate): this(coordinate.x, coordinate.y)
}
data object SelectDogout: ActionDescriptor
data class SelectPlayer(val player: Player): ActionDescriptor
data class DeselectPlayer(val player: Player): ActionDescriptor
data class SelectAction(val action: PlayerAction): ActionDescriptor

// Available actions
open class DieResult(val result: Int, val min: Short, val max: Short): Number(), GameAction {
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

sealed interface GameAction
data object Continue: GameAction
data object Confirm: GameAction
data object Cancel: GameAction
data object EndTurn: GameAction
data object EndAction: GameAction
data object EndSetup: GameAction
data class CoinSideSelected(val side: Coin): GameAction
data class CoinTossResult(val result: Coin): GameAction
class D2Result(result: Int = Random.nextInt(1, 3)): DieResult(result, 1, 2)
class D3Result(result: Int = Random.nextInt(1, 4)): DieResult(result, 1, 3)
class D4Result(result: Int = Random.nextInt(1, 5)): DieResult(result, 1, 4)
class D6Result(result: Int = Random.nextInt(1, 7)): DieResult(result, 1, 6)
class D8Result(result: Int = Random.nextInt(1, 9)): DieResult(result, 1, 8)
class D12Result(result: Int = Random.nextInt(1, 13)): DieResult(result, 1, 12)
class D16Result(result: Int = Random.nextInt(1, 17)): DieResult(result, 1, 16)
class D20Result(result: Int = Random.nextInt(1, 21)): DieResult(result, 1, 20)
class DiceResults(val rolls: List<DieResult>): GameAction {
    constructor(vararg roll: DieResult): this(listOf(*roll))
}
data class PlayerSelected(val player: Player): GameAction
data object PlayerDeselected: GameAction
data class PlayerActionSelected(val action: PlayerAction): GameAction
data object DogoutSelected: GameAction
data class FieldSquareSelected(val x: Int, val y: Int): GameAction {
    constructor(coordinate: FieldCoordinate): this(coordinate.x, coordinate.y)
    override fun toString(): String {
        return "${this::class.simpleName}[$x, $y]"
    }
}
