package com.jervisffb.engine.actions

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.ActionType
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.DiceRerollOption
import com.jervisffb.engine.rules.bb2020.skills.RerollSource
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory
import kotlinx.serialization.Serializable
import kotlin.random.Random


sealed interface GameAction

/**
 * Game Action that can delay its value until called.
 * This is only for testing and should never be accepted by a [Procedure].
 */
class CalculatedAction(private val action: (Game, Rules) -> GameAction) : GameAction {
    fun get(state: Game, rules: Rules): GameAction {
        return action(state, rules)
    }
}

// Group multiple actions together as one.
@Serializable
data class CompositeGameAction(val list: List<GameAction>): GameAction {
    constructor(vararg actions: GameAction) : this(listOf(*actions))
}

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
data class D2Result(override val value: Int) : DieResult() {
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
data class D3Result(override val value: Int) : DieResult() {
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
data class D4Result(override val value: Int) : DieResult() {
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
data class D6Result(override val value: Int) : DieResult() {
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
data class D8Result(override val value: Int) : DieResult() {
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
data class D12Result(override val value: Int) : DieResult() {
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
data class D16Result(override val value: Int) : DieResult() {
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
data class D20Result(override val value: Int) : DieResult() {
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

// This class is a bit annoying, it is treated as a special D6, where the result can be found in `blockResult`
@Serializable
data class DBlockResult(override val value: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, 7)) // Fix issues with serialization not serializing `result`. Figure out why

    override val min: Short = 1
    override val max: Short = 6

    val blockResult: BlockDice = BlockDice.fromD6(D6Result(value))

    companion object {
        fun allOptions(): List<DBlockResult> {
            return (1..6).map { DBlockResult(it) }
        }
    }
}

@Serializable
data class DicePoolChoice(val id: Int, val diceSelected: List<DieResult>)

@Serializable
data class DicePoolResultsSelected(val results: List<DicePoolChoice>): GameAction {
    fun singleResult(): DieResult = results.single().diceSelected.single()
}

@Serializable
data class DiceRollResults(val rolls: List<DieResult>) : GameAction, List<DieResult> by rolls {
    constructor(vararg roll: DieResult) : this(listOf(*roll))
    fun sum(): Int {
        return rolls.sumOf { it.value }
    }
}

@Serializable
data class PlayerSelected(val playerId: PlayerId) : GameAction {
    constructor(player: Player): this(player.id)
    fun getPlayer(state: Game): Player {
        return state.getPlayerById(playerId)
    }
}

@Serializable
data class PlayerDeselected(val playerId: PlayerId) : GameAction {
    constructor(player: Player): this(player.id)
    fun getPlayer(state: Game): Player {
        return state.getPlayerById(playerId)
    }
}

@Serializable
data class PlayerActionSelected(val action: ActionType) : GameAction

// TODO Merge with PlayerActionSelected
@Serializable
data class PlayerSubActionSelected(val name: String, val action: GameAction) : GameAction

@Serializable
data object DogoutSelected : GameAction

// TODO This should propably also include the origin
@Serializable
data class DirectionSelected(val direction: Direction) : GameAction

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
data class RandomPlayersSelected(val players: List<PlayerId>) : GameAction {
    fun getPlayers(state: Game): List<Player> {
        return players.map {
            state.getPlayerById(it) ?: error("No player with id $it")
        }
    }
}

@Serializable
data class RerollOptionSelected(val option: DiceRerollOption, val dicePoolId: Int = 0) : GameAction {
    fun getRerollSource(state: Game): RerollSource {
        return option.source
//        return state.getRerollSourceById(option.source)
    }
}

@Serializable
data class NoRerollSelected(val dicePoolId: Int = 0) : GameAction

@Serializable
data class MoveTypeSelected(val moveType: MoveType) : GameAction

@Serializable
data class SkillSelected(val skill: SkillFactory): GameAction

@Serializable
data class InducementSelected(val name: String): GameAction

@Serializable
data class BlockTypeSelected(val type: BlockType): GameAction

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
