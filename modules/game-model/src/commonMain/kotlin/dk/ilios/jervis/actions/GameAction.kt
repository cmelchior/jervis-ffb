package dk.ilios.jervis.actions

import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.PlayerAction
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlin.random.Random

enum class Dice {
    D2, D3, D4, D6, D8, D12, D16, D20, BLOCK
}

/**
 * Representation of a Block die.
 * See page 57 in the rulebook.
 */
enum class BlockDice {
    PLAYER_DOWN,
    BOTH_DOWN,
    PUSH_BACK,
    STUMBLE,
    POW;
    companion object {
        fun fromD6(roll: D6Result): BlockDice {
            return when(roll.result) {
                1 -> PLAYER_DOWN
                2 -> BOTH_DOWN
                3, 4 -> PUSH_BACK
                5 -> STUMBLE
                6 -> POW
                else -> INVALID_GAME_STATE("Illegal roll: $roll")
            }
        }
    }
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
data class SelectRandomPlayers(val count: Int, val players: List<Player>): ActionDescriptor // This is not a single action

//data class SelectSkillRerollSource(val skill: Skill): ActionDescriptor
//data class SelectTeamRerollSource(val reroll: TeamReroll): ActionDescriptor
data class SelectRerollOption(val option: DiceRerollOption<*>): ActionDescriptor
data object SelectNoReroll: ActionDescriptor

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
data class CoinSideSelected(val side: Coin): GameAction {
    companion object {
        fun allOptions(): List<CoinSideSelected> { return Coin.entries.map { CoinSideSelected(it) }}
    }
}
data class CoinTossResult(val result: Coin): GameAction {
    companion object {
        fun allOptions(): List<CoinTossResult> { return Coin.entries.map { CoinTossResult(it) }}
    }
}
class D2Result(result: Int = Random.nextInt(1, 3)): DieResult(result, 1, 2) {
    companion object {
        fun allOptions(): List<D2Result> { return (1..2).map { D2Result(it) } }
    }
}
class D3Result(result: Int = Random.nextInt(1, 4)): DieResult(result, 1, 3) {
    companion object {
        fun allOptions(): List<D3Result> { return (1..3).map { D3Result(it) } }
    }
}
class D4Result(result: Int = Random.nextInt(1, 5)): DieResult(result, 1, 4) {
    companion object {
        fun allOptions(): List<D4Result> { return (1..4).map { D4Result(it) } }
    }
}
class D6Result(result: Int = Random.nextInt(1, 7)): DieResult(result, 1, 6) {
    companion object {
        fun allOptions(): List<D6Result> { return (1..6).map { D6Result(it) } }
        fun allOptions(dice: Int): List<List<D6Result>> {
            val list = mutableListOf<List<D6Result>>()
            (1 .. 6).forEach { d1 ->
                (1 .. 6).forEach { d2 ->
                    list.add(listOf(D6Result(d1), D6Result(d2)))
                }
            }
            return list
        }
    }
}
class D8Result(result: Int = Random.nextInt(1, 9)): DieResult(result, 1, 8) {
    companion object {
        fun allOptions(): List<D8Result> { return (1..8).map { D8Result(it) } }
    }
}
class D12Result(result: Int = Random.nextInt(1, 13)): DieResult(result, 1, 12) {
    companion object {
        fun allOptions(): List<D12Result> { return (1..12).map { D12Result(it) } }
    }
}
class D16Result(result: Int = Random.nextInt(1, 17)): DieResult(result, 1, 16) {
    companion object {
        fun allOptions(): List<D16Result> { return (1..16).map { D16Result(it) } }
    }
}
class D20Result(result: Int = Random.nextInt(1, 21)): DieResult(result, 1, 20) {
    companion object {
        fun allOptions(): List<D20Result> { return (1..20).map { D20Result(it) } }
    }
}
class DBlockResult(result: Int = Random.nextInt(1, 7)): DieResult(result, 1, 6) {
    val blockResult: BlockDice = BlockDice.fromD6(D6Result(result))
    companion object {
        fun allOptions(): List<DBlockResult> { return (1..6).map { DBlockResult(it) } }
    }
}
class DiceResults(val rolls: List<DieResult>): GameAction {
    constructor(vararg roll: DieResult): this(listOf(*roll))
}
data class PlayerSelected(val player: Player): GameAction
data object PlayerDeselected: GameAction
data class PlayerActionSelected(val action: PlayerAction): GameAction
data object DogoutSelected: GameAction
data class FieldSquareSelected(val coordinate: FieldCoordinate): GameAction {
    constructor(x: Int, y: Int): this(FieldCoordinate(x, y))
    val x: Int = coordinate.x
    val y: Int = coordinate.y
    override fun toString(): String {
        return "${this::class.simpleName}[$x, $y]"
    }
}
data class RandomPlayersSelected(val players: List<Player>): GameAction
data class RerollOptionSelected(val option: DiceRerollOption<*>): GameAction
data object NoRerollSelected: GameAction
