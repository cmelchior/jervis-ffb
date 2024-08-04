package dk.ilios.jervis.actions

import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.PlayerAction
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable
import kotlin.random.Random

enum class Dice {
    D2,
    D3,
    D4,
    D6,
    D8,
    D12,
    D16,
    D20,
    BLOCK,
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
    POW,
    ;

    companion object {
        fun fromD6(roll: D6Result): BlockDice {
            return when (roll.result) {
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

data object ContinueWhenReady : ActionDescriptor // "internal event" for continuing the game state

data object ConfirmWhenReady : ActionDescriptor // An generic action that requires explicit confirmation by a player

data object CancelWhenReady : ActionDescriptor // An generic action that requires explicit confirmation by a player

data object EndSetupWhenReady : ActionDescriptor // Mark the setup phase as ended for a team

data object EndTurnWhenReady : ActionDescriptor // Mark the turn as ended for a team

data object EndActionWhenReady : ActionDescriptor

data object SelectCoinSide : ActionDescriptor

data object TossCoin : ActionDescriptor

data class RollDice(val dice: List<Dice>) : ActionDescriptor {
    constructor(vararg dice: Dice) : this(dice.toList())
}

data class SelectFieldLocation(val x: Int, val y: Int) : ActionDescriptor {
    constructor(coordinate: FieldCoordinate) : this(coordinate.x, coordinate.y)
}

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

sealed interface GameAction

@Serializable
data object Undo : GameAction

@Serializable
data object Continue : GameAction

@Serializable
data object Confirm : GameAction

@Serializable
data object Cancel : GameAction

@Serializable
data object EndTurn : GameAction

@Serializable
data object EndAction : GameAction

@Serializable
data object EndSetup : GameAction

@Serializable
data class CoinSideSelected(val side: Coin) : GameAction {
    companion object {
        fun allOptions(): List<CoinSideSelected> {
            return Coin.entries.map { CoinSideSelected(it) }
        }
    }
}

@Serializable
data class CoinTossResult(val result: Coin) : GameAction {
    companion object {
        fun allOptions(): List<CoinTossResult> {
            return Coin.entries.map { CoinTossResult(it) }
        }
    }
}

@Serializable
data class D2Result(override val result: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, 3)) // Fix issues with serialization not serializing `result`. Figure out why

    override val min: Short = 1
    override val max: Short = 2

    companion object {
        fun allOptions(): List<D2Result> {
            return (1..2).map { D2Result(it) }
        }
    }
}

@Serializable
data class D3Result(override val result: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, 4)) // Fix issues with serialization not serializing `result`. Figure out why

    override val min: Short = 1
    override val max: Short = 3

    companion object {
        fun allOptions(): List<D3Result> {
            return (1..3).map { D3Result(it) }
        }
    }
}

@Serializable
data class D4Result(override val result: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, 5)) // Fix issues with serialization not serializing `result`. Figure out why

    override val min: Short = 1
    override val max: Short = 4

    companion object {
        fun allOptions(): List<D4Result> {
            return (1..4).map { D4Result(it) }
        }
    }
}

@Serializable
data class D6Result(override val result: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, 7)) // Fix issues with serialization not serializing `result`. Figure out why

    override val min: Short = 1
    override val max: Short = 6

    companion object {
        fun allOptions(): List<D6Result> {
            return (1..6).map { D6Result(it) }
        }
    }
}

@Serializable
data class D8Result(override val result: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, 9)) // Fix issues with serialization not serializing `result`. Figure out why

    override val min: Short = 1
    override val max: Short = 8

    companion object {
        fun allOptions(): List<D8Result> {
            return (1..8).map { D8Result(it) }
        }
    }
}

@Serializable
data class D12Result(override val result: Int) : DieResult() {
    constructor() : this(
        Random.nextInt(1, 13),
    ) // Fix issues with serialization not serializing `result`. Figure out why

    override val min: Short = 1
    override val max: Short = 12

    companion object {
        fun allOptions(): List<D12Result> {
            return (1..12).map { D12Result(it) }
        }
    }
}

@Serializable
data class D16Result(override val result: Int) : DieResult() {
    constructor() : this(
        Random.nextInt(1, 17),
    ) // Fix issues with serialization not serializing `result`. Figure out why

    override val min: Short = 1
    override val max: Short = 16

    companion object {
        fun allOptions(): List<D16Result> {
            return (1..16).map { D16Result(it) }
        }
    }
}

@Serializable
data class D20Result(override val result: Int) : DieResult() {
    constructor() : this(
        Random.nextInt(1, 21),
    ) // Fix issues with serialization not serializing `result`. Figure out why

    override val min: Short = 1
    override val max: Short = 20

    companion object {
        fun allOptions(): List<D20Result> {
            return (1..20).map { D20Result(it) }
        }
    }
}

@Serializable
data class DBlockResult(override val result: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, 7)) // Fix issues with serialization not serializing `result`. Figure out why

    override val min: Short = 1
    override val max: Short = 6

    val blockResult: BlockDice = BlockDice.fromD6(D6Result(result))

    companion object {
        fun allOptions(): List<DBlockResult> {
            return (1..6).map { DBlockResult(it) }
        }
    }
}

@Serializable
data class DiceResults(val rolls: List<DieResult>) : GameAction, List<DieResult> by rolls {
    constructor(vararg roll: DieResult) : this(listOf(*roll))
}

@Serializable
data class PlayerSelected(val player: Player) : GameAction

@Serializable
data object PlayerDeselected : GameAction

@Serializable
data class PlayerActionSelected(val action: PlayerAction) : GameAction

@Serializable
data object DogoutSelected : GameAction

@Serializable
data class FieldSquareSelected(val coordinate: FieldCoordinate) : GameAction {
    constructor(x: Int, y: Int) : this(FieldCoordinate(x, y))

    val x: Int = coordinate.x
    val y: Int = coordinate.y

    override fun toString(): String {
        return "${this::class.simpleName}[$x, $y]"
    }
}

@Serializable
data class RandomPlayersSelected(val players: List<Player>) : GameAction

@Serializable
data class RerollOptionSelected(val option: DiceRerollOption) : GameAction

@Serializable
data object NoRerollSelected : GameAction
