package dk.ilios.jervis.actions

import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.rules.BlockType
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.rules.skills.RerollSource
import dk.ilios.jervis.rules.skills.SkillFactory
import kotlinx.serialization.Serializable
import kotlin.random.Random


sealed interface GameAction

/**
 * Game Action that can delay its value until called.
 * This is only for testing and should never be accepted by a `Procedure.
 */
class CalculatedAction(private val action: (Game, Rules) -> GameAction) : GameAction {
    fun get(state: Game, rules: Rules): GameAction {
        return action(state, rules)
    }
}

@Serializable
data class CompositeGameAction(val list: List<GameAction>): GameAction

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
data class DiceResults(val rolls: List<DieResult>) : GameAction, List<DieResult> by rolls {
    constructor(vararg roll: DieResult) : this(listOf(*roll))
    fun sum(): Int {
        return rolls.sumOf { it.value }
    }
}

@Serializable
data class PlayerSelected(val playerId: PlayerId) : GameAction {
    constructor(player: Player): this(player.id)
    fun getPlayer(state: Game): Player {
        return state.getPlayerById(playerId) ?: error("No player with id $playerId")
    }
}

@Serializable
data object PlayerDeselected : GameAction

@Serializable
data class PlayerActionSelected(val action: PlayerActionType) : GameAction

// TODO Merge with PlayerActionSelected
@Serializable
data class PlayerSubActionSelected(val name: String, val action: GameAction) : GameAction

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
data class RandomPlayersSelected(val players: List<PlayerId>) : GameAction {
    fun getPlayers(state: Game): List<Player> {
        return players.map {
            state.getPlayerById(it) ?: error("No player with id $it")
        }
    }
}

@Serializable
data class RerollOptionSelected(val option: DiceRerollOption) : GameAction {
    fun getRerollSource(state: Game): RerollSource {
        return option.source
//        return state.getRerollSourceById(option.source)
    }
}

@Serializable
data object NoRerollSelected : GameAction

@Serializable
data class MoveTypeSelected(val moveType: MoveType) : GameAction

@Serializable
data class SkillSelected(val skill: SkillFactory): GameAction

@Serializable
data class InducementSelected(val name: String): GameAction

@Serializable
data class BlockTypeSelected(val type: BlockType): GameAction
