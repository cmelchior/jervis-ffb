package com.jervisffb.engine.utils

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.AdminGameAction
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
import com.jervisffb.engine.actions.ForegoActivationSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.InducementEffectSelected
import com.jervisffb.engine.actions.InducementsSelected
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectCoinSide
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.actions.SelectForgoActivation
import com.jervisffb.engine.actions.SelectInducementEffect
import com.jervisffb.engine.actions.SelectInducements
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPassType
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectPlayers
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.SelectSkill
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.actions.TossCoin
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillValue
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.JumpRollContext
import com.jervisffb.engine.model.context.LeapRollContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.DodgeRollModifier
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.statistics.probability.Probability
import com.jervisffb.engine.statistics.probability.Surprisal
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.collections.plus
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
            EndActionWhenReady -> false
            EndSetupWhenReady -> false
            EndTurnWhenReady -> false
            SelectCoinSide -> false
            SelectDogout -> false
            TossCoin -> true
            is DeselectPlayer -> false
            is RollDice -> true
            is SelectBlockType -> false
            is SelectDicePoolResult -> false
            is SelectDirection -> false
            is SelectForgoActivation -> false
            is SelectInducementEffect -> false
            is SelectInducements -> false
            is SelectMoveType -> false
            is SelectNoReroll -> false
            is SelectPassType -> false
            is SelectPitchLocation -> false
            is SelectPlayer -> false
            is SelectPlayerAction -> false
            is SelectPlayers -> false
            is SelectRandomPlayers -> true
            is SelectRerollOption -> false
            is SelectSkill -> false
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
        Cancel -> false
        Confirm -> false
        Continue -> false
        DogoutSelected -> false
        EndAction -> false
        EndSetup -> false
        EndTurn -> false
        Revert -> false
        Undo -> false
        is AdminGameAction -> false
        is BlockTypeSelected -> false
        is CalculatedAction -> false // Is only used by tests
        is CoinSideSelected -> false
        is CoinTossResult -> true
        is CompositeGameAction -> false // Composites should only contain deterministic actions
        is D12Result -> true
        is D16Result -> true
        is D20Result -> true
        is D2Result -> true
        is D3Result -> true
        is D4Result -> true
        is D6Result -> true
        is D8Result ->  true
        is DBlockResult -> true
        is DicePoolResultsSelected -> false
        is DiceRollResults -> true
        is DirectionSelected -> false
        is ForegoActivationSelected -> false
        is InducementEffectSelected -> false
        is InducementsSelected -> false
        is MoveTypeSelected -> false
        is NoRerollSelected -> false
        is PassTypeSelected -> false
        is PitchSquareSelected -> false
        is PlayerActionSelected -> false
        is PlayerDeselected -> false
        is PlayerSelected -> false
        is PlayersSelected -> false
        is RandomPlayersSelected -> true
        is RerollOptionSelected -> false
        is SkillSelected -> false
    }
}

const val enableAsserts = true

@Suppress("NOTHING_TO_INLINE")
inline fun assert(condition: Boolean, lazyMessage: () -> String = { "A invariant failed" }) {
    @Suppress("SimplifyBooleanWithConstants")
    if (enableAsserts && !condition) {
        throw IllegalStateException(lazyMessage())
    }
}

@JvmName("sumOfDieResults")
fun List<DieResult>.sum(): Int = this.sumOf { it.value }
fun List<DiceModifier>.sum(): Int = this.sumOf { it.modifier }
fun PersistentList<DiceModifier>.sum(): Int = this.sumOf { it.modifier }

@JvmName("sumOfD6DieRolls")
fun List<D6DieRoll>.sum(): Int = this.sumOf { it.result.value }

@JvmName("sumOfStatModifiers")
fun List<StatModifier>.sum(): Int = this.sumOf { it.modifier }

@JvmName("sumOfSurprisals")
fun Collection<Surprisal>.sum(): Surprisal = Surprisal(this.sumOf { it.value })
@JvmName("sumOfProbabilities")
fun Collection<Probability>.sum(): Probability = Probability(this.sumOf { it.value })

class InvalidActionException(message: String) : RuntimeException(message)

class InvalidGameStateException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause)

@Suppress("NOTHING_TO_INLINE")
inline fun INVALID_GAME_STATE(message: String = "Unexpected game state"): Nothing {
    throw InvalidGameStateException(message)
}

@Suppress("NOTHING_TO_INLINE")
inline fun INVALID_ACTION(action: GameAction, customMessage: String? = null): Nothing {
    throw InvalidActionException(customMessage?.let {
        customMessage
    } ?: "Invalid action selected: $action")
}

@Suppress("NOTHING_TO_INLINE")
inline fun requireGameState(bool: Boolean) {
    if (!bool) {
        INVALID_GAME_STATE()
    }
}

inline fun requireGameState(bool: Boolean, lazyMessage: () -> String) {
    if (!bool) {
        INVALID_GAME_STATE(lazyMessage())
    }
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

    return this.withIndex()
        .flatMap { (index: Int, element: T) ->
            this.drop(index + 1)
                .combinations(size - 1)
                .map { setOf(element) + it }
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

/**
 * Finds the first element of the expected type.
 */
inline fun <reified T: Any> List<*>.singleInstanceOf(): T {
    return single { it is T } as T
}

/**
 * Finds the first element of the expected type or `null` if it couldn't be found.
 */
inline fun <reified T: Any> List<*>.singleInstanceOfOrNull(): T? {
    return singleOrNull { it is T } as T?
}

inline fun <reified T: Any?> List<Any?>.containsInstance(): Boolean {
    return this.any { it is T }
}

/**
 * Format dice rolls and modifiers into a nice looking String that can be used for log output
 */
fun formatDiceRoll(roll: D6DieRoll, modifiers: List<DiceModifier>): String {
    return buildString {
        append(roll.result.value)
        append(" Roll")
        for (modifier in modifiers) {
            val prefix = if (modifier.modifier < 0) "" else "+"
            append(" ${prefix}${modifier.modifier} ${modifier.description}")
        }
    }
}

/**
 * Check if using Diving Tackle would matter in the current context
 */
fun doDivingTackleHaveAnAffect(state: Game): Boolean {
    val context = state.getContext<MoveContext>()
    val movingPlayer = context.player
    val startingSquare = context.startingSquare

    // First check if the opponent even has Diving Tackle
    val opponentHasDivingTackle = startingSquare.getSurroundingCoordinates(state.rules)
        .filter { coord ->
            state.pitch[coord].player?.let { player ->
                player.team != movingPlayer.team
            } ?: false
        }
        .mapNotNull { state.pitch[it].player }
        .any { it.isSkillAvailable(SkillType.DIVING_TACKLE) }

    if (!opponentHasDivingTackle) {
        return false
    }

    // Then check rolls and modifiers. Dispatch on which roll context is
    // currently active — `DodgeRoll`, `JumpRoll`, or (BB2025 only) `LeapRoll`.
    val (rollValue, modifiedResult) = when {
        state.hasContext<DodgeRollContext>() -> state.getContext<DodgeRollContext>().let {
            (it.roll?.result?.value ?: 0) to it.modifiedResult
        }
        state.hasContext<JumpRollContext>() -> state.getContext<JumpRollContext>().let {
            (it.roll?.result?.value ?: 0) to it.modifiedResult
        }
        state.hasContext<LeapRollContext>() -> state.getContext<LeapRollContext>().let {
            (it.roll?.result?.value ?: 0) to it.modifiedResult
        }
        else -> error("Unexpected procedure: ${state.stack.currentProcedure()?.procedure}")
    }

    val isNaturalSix = (rollValue == 6)
    val modifier = DodgeRollModifier.DIVING_TACKLE.modifier * -1
    val divingTackleHasEffect = modifiedResult >= movingPlayer.agility && modifiedResult <= movingPlayer.agility + modifier
    if (isNaturalSix || !divingTackleHasEffect) {
        return false
    }

    return true
}

/**
 * Returns `true` if a reroll has been used on _any_ die in the dice pool,
 * `false` if not.
 */
fun List<DieRoll<*>>.anyRerollUsed(): Boolean {
    return any { it.rerollSource != null }
}

/**
 * Generally, it isn't allowed for a player to have the same skill multiple times, but in some cases
 * it might happen. In this case, we need to deduplicate the skills to figure out which skill to keep.
 *
 * The rule is:
 * 1. For skills with a target, we choose the lower value, i.e., Loner(3+) over Loner(4+)
 * 2. For skills with a value ajustment, we choose the higher value, i.e., Might Blow(2+) over Mighty Blow(+1).
 * 3. If the values are the same, we keep the original skill.
 */
data class DedupSkillResult(val positionalSkills: List<SkillId>, val extraSkills: List<SkillId>)
fun dedupSkillsByType(positionalSkills: List<SkillId>, extraSkills: List<SkillId>): DedupSkillResult {
    fun dedup(skills: List<SkillId>, filter: Map<SkillType, SkillId>): List<SkillId> {
        val selectedSkills = mutableListOf<SkillId>()
        skills.forEach { skill ->
            if (filter.contains(skill.type)) {
                val skillValue = skill.value
                val otherValue = filter[skill.type]!!.value
                when (skillValue) {
                    is SkillValue.IntTarget if otherValue is SkillValue.IntTarget -> if (otherValue.value > skillValue.value) selectedSkills.add(skill)
                    is SkillValue.IntAdjustment if otherValue is SkillValue.IntAdjustment -> if (otherValue.value < skillValue.value) selectedSkills.add(skill)
                    else -> error("Skills do not have the same value [${skill.type}]: $skillValue vs. $otherValue")
                }
            } else {
                selectedSkills.add(skill)
            }
        }
        return selectedSkills
    }
    val extraSkillList: List<SkillId> = dedup(
        skills = extraSkills,
        filter = positionalSkills.associateBy { it.type }
    )
    val positionalSkillList: List<SkillId> = dedup(
        skills = positionalSkills,
        filter = extraSkillList.associateBy { it.type }
    )
    return DedupSkillResult(positionalSkills, extraSkillList)
}
