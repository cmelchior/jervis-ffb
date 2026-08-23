package com.jervisffb.engine.actions

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.DicePoolId
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.actions.ActionType
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.statistics.probability.Probability
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.random.Random


sealed interface GameAction

inline fun <reified T : GameAction> GameAction.safeCast(): T = this as? T ?: error("Cannot cast $this to ${T::class.simpleName}")

inline fun <reified T : DieResult> GameAction.safeDiceRollCast(): T {
    if (this is T) return this
    return this.safeCast<DiceRollResults>().rolls.first() as T
}

/**
 * Game Action that can delay its value until called.
 * This is only for testing and should never be accepted by a [Procedure].
 */
class CalculatedAction(private val action: GameEngineController.(Game, Rules) -> GameAction) : GameAction {
    fun get(controller: GameEngineController, state: Game, rules: Rules): GameAction {
        return action(controller, state, rules)
    }
}

/**
 * Group multiple actions together as one.
 * The rule engine will this action as an atomic action. This means that when you
 * Undo this action, all "sub-actions" will all be undone as one.
 *
 * It is not allowed to put [AdminGameAction] inside of this action.
 */
@Serializable
data class CompositeGameAction(val actionList: List<GameAction>): GameAction {
    constructor(vararg actions: GameAction) : this(listOf(*actions))
}

/**
 * Special action that will undo the previous user action (and associated
 * side effects).
 */
@Serializable
data object Undo : GameAction

/**
 * This action is a special variant of [Undo]. Similar to [Undo] it also
 * reverts the last user action, but on top of this, it also decrements the
 * counter of the last seen [GameActionId] and starts a new ID generation.
 * This lets clients distinguish actions from the new timeline from actions
 * that were created before the revert.
 *
 * Since we use the [GameActionId] to synchronize state between distributed
 * clients, this action should be used with caution. Currently the only
 * valid use case, is rewinding the game state based on errors from the server.
 * E.g. if the client sends an action that is reverted by the server, the client
 * needs to remove the action again, while making sure that its internal action
 * id counter is the same as other clients.
 */
@Serializable
data object Revert : GameAction

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
    constructor() : this(Random.nextInt(1, SIDES + 1)) // Fix issues with serialization not serializing `result`. Figure out why
    override val min: Short = 1
    override val max: Short = SIDES.toShort()
    init { checkRange() }

    override fun allOptions(vararg except: DieResult): List<D2Result> {
        return Companion.allOptions().toMutableList().apply {
            removeAll(except.toList())
        }
    }

    companion object {
        const val SIDES = 2
        fun allOptions(): List<D2Result> {
            return (1..SIDES).map { D2Result(it) }
        }
    }
}

@Serializable
data class D3Result(override val value: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, 4)) // Fix issues with serialization not serializing `result`. Figure out why
    override val min: Short = 1
    override val max: Short = 3
    init { checkRange() }

    override fun allOptions(vararg except: DieResult): List<D3Result> {
        return Companion.allOptions().toMutableList().apply {
            removeAll(except.toList())
        }
    }

    companion object {
        fun random(random: Random = Random): D3Result {
            return random.nextInt(1, 3).d3
        }
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
    init { checkRange() }

    override fun allOptions(vararg except: DieResult): List<D4Result> {
        return D4Result.Companion.allOptions().toMutableList().apply {
            removeAll(except.toList())
        }
    }

    companion object {
        fun allOptions(): List<D4Result> {
            return (1..4).map { D4Result(it) }
        }
    }
}

@Serializable
data class D6Result(override val value: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, SIDES + 1)) // Fix issues with serialization not serializing `result`. Figure out why
    override val min: Short = 1
    override val max: Short = SIDES.toShort()
    init { checkRange() }

    override fun allOptions(vararg except: DieResult): List<D6Result> {
        return D6Result.Companion.allOptions().toMutableList().apply {
            removeAll(except.toList())
        }
    }

    /**
     * Convert a D6 roll into a D3 value using the rules described on page 26 in the BB2025 rulebook.
     */
    fun toD3(): D3Result = ceil(value / 2f).toInt().d3

    companion object {
        const val SIDES = 6

        fun allOptions(): List<D6Result> {
            return (1..SIDES).map { D6Result(it) }
        }
        fun random(random: Random = Random): D6Result {
            return random.nextInt(1, SIDES + 1).d6
        }
        fun randomExcept(except: D6Result): D6Result {
            return allOptions().filter { it == except }.random()
        }

        /**
         * Calculate the probability of rolling a D6 result that is greater than
         * or equal to the given target. Returned probability is [0.0, 1.0]
         */
        fun successProbability(target: D6Result): Probability {
            return Probability((SIDES + 1 - target.value).toDouble() / SIDES)
        }

        /**
         * When rolling [dice], return how many dice combinations exist, where
         * the sum of the dice is equal to [total].
         */
        fun combinationsEqualToTotal(dice: Int, total: Int): Int {
            return combinations(dice, total, includeAbove = false)
        }

        /**
         * When rolling [dice], return how many dice combinations exist, where
         * the sum of the dice is equal or above [total].
         */
        fun combinationsAtLeastTotal(dice: Int, total: Int): Int {
            return combinations(dice, total, includeAbove = true)
        }

        /**
         * Returns all combinations of [dice] D6 rolls, where the sum of the
         * dice is equal to [total].
         *
         * If [includeAbove] is `true` all values above [total] are also
         * included.
         */
        private fun combinations(
            dice: Int,
            total: Int,
            includeAbove: Boolean
        ): Int {
            require(dice > 0) { "At least one D6 is required: $dice" }

            var possibleOutcomes = 1
            repeat(dice) {
                require(possibleOutcomes <= Int.MAX_VALUE / SIDES) {
                    "The combinations for $dice D6 rolls cannot be represented as an Int"
                }
                possibleOutcomes *= SIDES
            }

            val maximumTotal = dice * SIDES
            if (total < dice) error("`total` is less than the possible range of values: $total < $dice")
            if (total > maximumTotal) error("`total` is greater than the possible range of values: $total > $maximumTotal")

            var combinationsByTotal = intArrayOf(1)
            repeat(dice) {
                val nextCombinationsByTotal = IntArray(combinationsByTotal.size + SIDES)
                for (currentTotal in combinationsByTotal.indices) {
                    for (result in 1..SIDES) {
                        nextCombinationsByTotal[currentTotal + result] += combinationsByTotal[currentTotal]
                    }
                }
                combinationsByTotal = nextCombinationsByTotal
            }

            return when (includeAbove) {
                true -> (total..maximumTotal).sumOf { combinationsByTotal[it] }
                false -> combinationsByTotal[total]
            }
        }


    }
}

@Serializable
data class D8Result(override val value: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, SIDES + 1)) // Fix issues with serialization not serializing `result`. Figure out why
    override val min: Short = 1
    override val max: Short = SIDES.toShort()
    init { checkRange() }

    override fun allOptions(vararg except: DieResult): List<D8Result> {
        return D8Result.allOptions().toMutableList().apply {
            removeAll(except.toList())
        }
    }

    companion object {
        const val SIDES = 8
        fun allOptions(startWith: Int = 1): List<D8Result> {
            require(startWith in 1..SIDES) { "startWith must be in 1..8, was $startWith" }
            return (0 until SIDES).map { D8Result(((startWith - 1 + it) % SIDES) + 1) }
        }
        fun random(random: Random = Random): D8Result {
            return random.nextInt(1, SIDES).d8
        }
    }
}

@Serializable
data class D12Result(override val value: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, 13)) // Fix issues with serialization not serializing `result`. Figure out why
    override val min: Short = 1
    override val max: Short = 12
    init { checkRange() }

    override fun allOptions(vararg except: DieResult): List<D12Result> {
        return Companion.allOptions().toMutableList().apply {
            removeAll(except.toList())
        }
    }

    companion object {
        fun allOptions(): List<D12Result> {
            return (1..12).map { D12Result(it) }
        }
    }
}

@Serializable
data class D16Result(override val value: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, SIDES + 1)) // Fix issues with serialization not serializing `result`. Figure out why
    override val min: Short = 1
    override val max: Short = SIDES.toShort()
    init { checkRange() }

    override fun allOptions(vararg except: DieResult): List<D16Result> {
        return D16Result.Companion.allOptions().toMutableList().apply {
            removeAll(except.toList())
        }
    }

    companion object {
        val SIDES: Int = 16
        fun allOptions(): List<D16Result> {
            return (1..SIDES).map { D16Result(it) }
        }
    }
}

@Serializable
data class D20Result(override val value: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, 21)) // Fix issues with serialization not serializing `result`. Figure out why
    override val min: Short = 1
    override val max: Short = 20
    init { checkRange() }

    override fun allOptions(vararg except: DieResult): List<D20Result> {
        return D20Result.Companion.allOptions().toMutableList().apply {
            removeAll(except.toList())
        }
    }

    companion object {
        fun allOptions(): List<D20Result> {
            return (1..20).map { D20Result(it) }
        }
    }
}

// This class is a bit annoying; it is treated as a special D6, where the result can be found in `blockResult`
@Serializable
data class DBlockResult(override val value: Int) : DieResult() {
    constructor() : this(Random.nextInt(1, 7)) // Fix issues with serialization not serializing `result`. Figure out why
    override val min: Short = 1
    override val max: Short = 6
    init { checkRange() }

    override fun allOptions(vararg except: DieResult): List<DBlockResult> {
        return DBlockResult.Companion.allOptions().toMutableList().apply {
            removeAll(except.toList())
        }
    }
    val blockResult: BlockDice = BlockDice.fromD6(D6Result(value))

    companion object {
        private val SIDES = 6

        fun allOptions(): List<DBlockResult> {
            return (1..SIDES).map { DBlockResult(it) }
        }
        fun random(random: Random = Random): DBlockResult {
            return random.nextInt(1, SIDES + 1).dblock
        }

        /**
         * Probability of a single block die showing [face].
         */
        fun faceProbability(face: BlockDice): Probability {
            val sides = if (face == BlockDice.PUSH_BACK) 2 else 1
            return Probability(sides.toDouble() / SIDES)
        }

        /**
         * Probability that a block pool of [diceCount] dice yields [face].
         *
         * When the coach picks the die, any one of them showing [face] is
         * enough. When the opponent picks, every die has to show it.
         */
        fun successProbability(
            face: BlockDice,
            diceCount: Int,
            opponentChooses: Boolean = false,
        ): Probability {
            require(diceCount >= 1) { "A block needs at least one die: $diceCount" }
            val faceProbability = faceProbability(face)
            return when (opponentChooses) {
                true -> faceProbability.pow(diceCount)
                false -> Probability(1.0 - (1.0 - faceProbability.value).pow(diceCount))
            }
        }

    }
}

@Serializable
data class DicePoolChoice(val id: DicePoolId, val diceSelected: List<SelectedDiceRoll>) {
    @Serializable
    data class SelectedDiceRoll(
        val id: DieId,
        val result: DieResult
    ) {
        constructor(roll: DieRoll<*>): this(roll.id, roll.result)
    }

}


// TODO Is it safe to return DieResult from here? Shouldn't it be DieRoll instead?
//  Otherwise there is no way to connect the result to the "exact" die, e.g. in case
//  you are allowed to reroll multiple times and there are several die with the same
//  value
/**
 * We only use multiple results during "Multiple Block" where blocks happen at the same
 * time, but this class has been generalized for all dice roll types.
 */
@Serializable
data class DicePoolResultsSelected(val results: List<DicePoolChoice>): GameAction {
    fun singleResult(): DieResult = results.single().diceSelected.single().result
    companion object {
        /**
         * Factory method for easily creating the simple case, where there is only
         * one dice pool with a single die.
         */
        fun fromSingleDice(die: DieRoll<*>): DicePoolResultsSelected {
            return DicePoolResultsSelected(listOf(DicePoolChoice(DicePoolId(0), listOf(DicePoolChoice.SelectedDiceRoll(die)))))
        }
    }
}

@Serializable
data class DiceRollResults(val rolls: List<DieResult>) : GameAction, List<DieResult> by rolls {
    constructor(vararg roll: DieResult) : this(listOf(*roll))
    fun sum(): Int {
        return rolls.sumOf { it.value }
    }
}

@Serializable
data class PlayersSelected(
    val players: List<PlayerId>,
): GameAction {
    init {
        require(players.isNotEmpty()) { "PlayersSelected must have at least one player" }
    }

    fun getPlayers(state: Game): List<Player> {
        return players.map {
            state.getPlayerById(it)
        }
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
data class ForegoActivationSelected(val player: PlayerId) : GameAction {
    constructor(player: Player): this(player.id)
    fun getPlayer(state: Game): Player {
        return state.getPlayerById(player)
    }
}

@Serializable
data class PlayerActionSelected(val action: ActionType) : GameAction

@Serializable
data object DogoutSelected : GameAction

// TODO This should propably also include the origin
@Serializable
data class DirectionSelected(val direction: Direction) : GameAction

@Serializable
data class PitchSquareSelected(val x: Int, val y: Int) : GameAction {
    constructor(coordinate: PitchCoordinate): this(coordinate.x, coordinate.y)

    val coordinate: PitchCoordinate = PitchCoordinate(x, y)

    override fun toString(): String {
        return "${this::class.simpleName}[$x, $y]"
    }
}

@Serializable
data class RandomPlayersSelected(val players: List<PlayerId>) : GameAction {
    fun getPlayers(state: Game): List<Player> {
        return players.map {
            state.getPlayerById(it)
        }
    }
}

@Serializable
data class RerollOptionSelected(val option: DiceRerollOption, val dicePoolId: Int = 0) : GameAction {
    fun getRerollSource(state: Game): RerollSource {
        return state.getRerollSourceById(option.rerollId)
    }
    fun getRerollDice(): List<DieRoll<*>> {
        return option.dice.orEmpty()
    }
}

@Serializable
data class NoRerollSelected(val dicePoolId: Int = 0) : GameAction

@Serializable
data class MoveTypeSelected(val moveType: MoveType) : GameAction

@Serializable
data class SkillSelected(val skill: SkillId): GameAction

/**
 * Action carrying a team's full pre-game inducement purchase as one atomic
 * submission. The reason for this is so we avoid having to introduce a lot of
 * more granular actions needed to support the UI.
 *
 * The downside is that the UI is required to know a lot of the details about
 * how inducements work. The game controller will still validate the inducements
 * using the [InducementsSelected.isValid] method, but it will validate all of
 * them in one go.
 *
 * Developer's Commentary:
 * It still isn't clear if this approach is the best, but it seems the one that
 * introduces the least amount of complexity, even though the UI gets more
 * complex. We should revisit this design once more inducements have been
 * added.
 */
@Serializable
data class InducementsSelected(val inducements: List<InducementSelection<*>>) : GameAction {
    constructor(vararg inducement: InducementSelection<*>): this(inducement.toList())

    init {
        require(inducements.isNotEmpty()) { "InducementsSelected must have at least one inducement" }
    }

    // Calculate the total price of all selected inducements
    fun totalPrice(team: Team): Int {
        return inducements.sumOf {
            it.getPrice(team)
        }
    }
}

@Serializable
data class BlockTypeSelected(val type: BlockType): GameAction

@Serializable
data class PassTypeSelected(val type: PassType): GameAction

// Available actions
@Serializable
sealed class DieResult : Number(), GameAction {
    abstract val value: Int
    abstract val min: Short
    abstract val max: Short
    val range: IntRange
        get() = min..max

    protected fun checkRange() {
        if (value !in range) {
            throw IllegalArgumentException("Dice value outside range: $min <= $value <= $max")
        }
    }

    abstract fun allOptions(vararg except: DieResult): List<DieResult>
    override fun toByte(): Byte = value.toByte()
    override fun toDouble(): Double = value.toDouble()
    override fun toFloat(): Float = value.toFloat()
    override fun toInt(): Int = value
    override fun toLong(): Long = value.toLong()
    override fun toShort(): Short = value.toShort()
    override fun toString(): String {
        return "${this::class.simpleName}[$value]"
    }

    fun toLogString(): String = "[$value]"
}
