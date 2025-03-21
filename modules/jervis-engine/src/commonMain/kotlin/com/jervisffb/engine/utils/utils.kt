package com.jervisffb.engine.utils

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.InducementSelected
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayerSubActionSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectCoinSide
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectInducement
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.SelectSkill
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.actions.TossCoin
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.D6DieRoll
import com.jervisffb.engine.rules.bb2020.skills.DiceRerollOption
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.rules.bb2020.skills.RerollSource
import com.jervisffb.engine.rules.bb2020.skills.Skill
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.jvm.JvmName

fun ActionRequest.containsActionWithRandomBehavior(): Boolean {
    return this.actions.containsActionWithRandomBehavior()
}

// Returns true, if any of the action descriptors require using randomness, i.e., something
// that is outside a coaches control
fun List<GameActionDescriptor>.containsActionWithRandomBehavior(): Boolean {
    val randomActions = this.map {
        when (it) {
            CancelWhenReady -> false
            ConfirmWhenReady -> false
            ContinueWhenReady -> false
            is DeselectPlayer -> false
            EndActionWhenReady -> false
            EndSetupWhenReady -> false
            EndTurnWhenReady -> false
            is RollDice -> true
            is SelectBlockType -> false
            SelectCoinSide -> false
            is SelectDicePoolResult -> false
            is SelectDirection -> false
            SelectDogout -> false
            is SelectFieldLocation -> false
            is SelectInducement -> false
            is SelectMoveType -> false
            is SelectNoReroll -> false
            is SelectPlayer -> false
            is SelectPlayerAction -> false
            is SelectRandomPlayers -> true
            is SelectRerollOption -> false
            is SelectSkill -> false
            TossCoin -> true
        }
    }
    if (randomActions.contains(true) && randomActions.contains(false)) {
        // Unclear if this is actually the case, so just catch it for now
        throw IllegalStateException("Random behavior is mixed in the action descriptors.")
    }
    return randomActions.any { it  == true }
}

/**
 * Returns `true` if this action normally requires randomness to be generated.
 */
fun GameAction.isRandomAction(): Boolean {
    return when (this) {
        is BlockTypeSelected -> false
        is CalculatedAction -> false // Is only used by tests
        Cancel -> false
        is CoinSideSelected -> false
        is CoinTossResult -> true
        is CompositeGameAction -> false // Composites should only contain deterministic actions
        Confirm -> false
        Continue -> false
        is DicePoolResultsSelected -> false
        is DiceRollResults -> true
        is D12Result -> true
        is D16Result -> true
        is D20Result -> true
        is D2Result -> true
        is D3Result -> true
        is D4Result -> true
        is D6Result -> true
        is D8Result ->  true
        is DBlockResult -> true
        is DirectionSelected -> false
        DogoutSelected -> false
        EndAction -> false
        EndSetup -> false
        EndTurn -> false
        is FieldSquareSelected -> false
        is InducementSelected -> false
        is MoveTypeSelected -> false
        is NoRerollSelected -> false
        is PlayerActionSelected -> false
        is PlayerDeselected -> false
        is PlayerSelected -> false
        is PlayerSubActionSelected -> false
        is RandomPlayersSelected -> true
        is RerollOptionSelected -> false
        is SkillSelected -> false
        Undo -> false
        Revert -> false
    }
}


fun createRandomAction(
    state: Game,
    availableActions: List<GameActionDescriptor>,
): GameAction {

    // Select a random action but disallow certain ones
    var actionDesc: GameActionDescriptor? = null
    val filtered = availableActions.filter { it != EndActionWhenReady }
    if (filtered.isEmpty()) {
        actionDesc = availableActions.random()
    } else {
        actionDesc = filtered.random()
    }

    return actionDesc.createRandom()

}

const val enableAsserts = true

inline fun assert(condition: Boolean) {
    if (enableAsserts && !condition) {
        throw IllegalStateException("A invariant failed")
    }
}

@JvmName("sumOfDieResults")
fun List<DieResult>.sum(): Int = fold(0) { acc, el -> acc + el.value }

fun List<DiceModifier>.sum(): Int = this.sumOf { it.modifier }

@JvmName("sumOfStatModifiers")
fun List<StatModifier>.sum(): Int = this.sumOf { it.modifier }

class InvalidActionException(message: String) : RuntimeException(message)

class InvalidGameStateException(message: String) : IllegalStateException(message)

inline fun INVALID_GAME_STATE(message: String = "Unexpected game state"): Nothing {
    throw InvalidGameStateException(message)
}

inline fun INVALID_ACTION(action: GameAction, customMessage: String? = null): Nothing {
    throw InvalidActionException(customMessage?.let {
        customMessage
    } ?: "Invalid action selected: $action")
}

fun <T : Any?> MutableStateFlow<T>.safeTryEmit(value: T) {
    if (!this.tryEmit(value)) {
        throw IllegalStateException("Failed to emit value: $value")
    }
}

fun <T : Any?> MutableSharedFlow<T>.safeTryEmit(value: T) {
    if (!this.tryEmit(value)) {
        throw IllegalStateException("Failed to emit value: $value")
    }
}

fun List<Skill>.getRerollOptions(type: DiceRollType, roll: D6DieRoll, successOnFirstRoll: Boolean?): List<DiceRerollOption> {
    return this.asSequence().filter { it is RerollSource }
        .map { it as RerollSource }
        .filter { it.canReroll(type, listOf(roll), successOnFirstRoll) }
        .flatMap { it: RerollSource -> it.calculateRerollOptions(type, roll, successOnFirstRoll) }
        .toList()
}

/**
 * Calculate all available re-rolls options for a given roll type.
 * If no re-rolls are available, an empty list is returned.
 *
 * This method doesn't work for BLOCK rolls.
 */
fun calculateAvailableRerollsFor(
    rules: Rules, // Ruleset used
    player: Player, // Player rolling the dice
    type: DiceRollType, // Which type of dice roll
    roll: D6DieRoll, // The result of the first dice
    firstRollWasSuccess: Boolean? // Whether the first roll was a success.
): SelectRerollOption? {
    if (type == DiceRollType.BLOCK) throw IllegalArgumentException("Use XX instead")

    // Check any skills available to the player
    val skillRerolls: List<DiceRerollOption> = player.skills.getRerollOptions(
        type,
        roll,
        firstRollWasSuccess
    )

    // Check if there is any team re-rolls available
    val team = player.team
    val hasTeamRerolls = team.availableRerollCount > 0
    val allowedToUseTeamReroll = rules.canUseTeamReroll(player.team.game, player)

    // Calculate the full list of re-roll options
    val allOptions = if (skillRerolls.isEmpty() && (!hasTeamRerolls || !allowedToUseTeamReroll)) {
        emptyList()
    } else {
        val teamReroll = if (hasTeamRerolls && allowedToUseTeamReroll) {
            listOf(
                DiceRerollOption(rules.getAvailableTeamReroll(team), listOf(roll))
            )
        } else {
            emptyList()
        }
        skillRerolls + teamReroll
    }

    return if (allOptions.isEmpty()) {
        null
    } else {
        SelectRerollOption(allOptions)
    }
}

/**
 * Returns all possible combinations from a given list, excluding the empty set.
 */
fun <T> List<T>.allCombinations(): List<List<T>> {
    if (this.isEmpty()) return emptyList()
    val result = mutableListOf<List<T>>()
    val rest = this.drop(1).allCombinations()
    result.addAll(rest)
    result.addAll(rest.map { listOf(this.first()) + it }) // Ensure order is maintained
    result.add(listOf(this.first())) // Add single-element combination
    return result
}

/**
 * Return all combinations of the provided [size] from the list.
 *
 * @param size how many elements should be in the sublist. Must be <= list.size.
 */
fun <T> List<T>.combinations(size: Int): List<Set<T>> {
    if (size == 0) return listOf(emptySet())
    if (this.size < size) return emptyList()

    return this.withIndex().flatMap { (index, element) ->
        this.drop(index + 1).combinations(size - 1).map { setOf(element) + it }
    }
}

/**
 * Returns the cartesian product of a list of lists.
 */
fun <T> cartesianProduct(lists: List<List<T>>, n: Int = 1): List<List<T>> {
    fun combinations(list: List<T>, n: Int): List<List<T>> {
        if (n == 0) return listOf(emptyList())
        if (list.size < n) return emptyList()
        return list.indices.flatMap { i ->
            combinations(list.drop(i + 1), n - 1).map { listOf(list[i]) + it }
        }
    }

    val allCombinations = lists.map { combinations(it, n) }
    return allCombinations.fold(listOf(listOf())) { acc, list ->
        acc.flatMap { prefix -> list.map { element -> prefix + element } }
    }
}

fun MutableList<GameActionDescriptor>.addIfNotNull(descriptor: GameActionDescriptor?) {
    descriptor?.let { this.add(it)}
}
