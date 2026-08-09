package com.jervisffb.engine.common.procedures.dicerolls

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.probabiliy.UpdateChanceObservation
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.rerolls.TeamReroll
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.statistics.probability.observation.ChanceDieResult
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationScope
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollOption
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSelection
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSource
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSourceKind
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollTest
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollTestEffect
import com.jervisffb.engine.statistics.probability.observation.ChanceResultId

/**
 * Describes a pending replacement of a dice-roll observation, e.g. if either
 * a reroll is used or the rolled die is accepted.
 *
 * Keeping both versions together allows callers to update their local reroll
 * context with [updated] while applying the same reversible change to
 * [GameDelta.chanceObservations] with [command].
 */
internal data class ChanceObservationUpdate(
    val previous: ChanceObservation.DiceRoll,
    val updated: ChanceObservation.DiceRoll,
) {
    val command: Command = UpdateChanceObservation(previous.index, previous, updated)
}

/**
 * Creates an unfinalized observation for a physical D6 roll in a player's
 * active reroll flow.
 *
 * The observation receives the next global sequence number and retains links
 * to both an enclosing reroll flow and the physical roll it replaces, when
 * applicable.
 *
 * Returns the new observation, or `null` when chance-data collection is
 * disabled.
 */
internal fun createD6ChanceObservation(
    state: Game,
    rollType: DiceRollType,
    player: Player,
    result: D6Result,
    rerollContext: UseRerollContext,
    rerolledRollIndex: Int? = null,
): ChanceObservation.DiceRoll? {
    if (!state.collectChanceData) return null
    val sequence = state.nextAvailableChanceObservationIndex
    return ChanceObservation.DiceRoll(
        index = sequence,
        rollType = rollType,
        teamId = player.team.id,
        playerId = player.id,
        dice = listOf(
            ChanceDieResult(
                id = ChanceResultId(sequence, 0),
                result = result,
            ),
        ),
        scope = chanceScope(state, player),
        enclosingRollIndex = rerollContext.chanceEnclosingRollIndex,
        rerolledRollIndex = rerolledRollIndex,
    )
}

/**
 * Records the result of the reroll decision for the initial D6 observation in
 * [rerollContext].
 *
 * The update captures whether the roll succeeded, every reroll option that
 * applies to the observed result, and the selected source, if any. The returned
 * value contains both the updated observation and the command needed to apply
 * [GameDelta.chanceObservations].
 *
 * Returns the pending update, or `null` when chance-data collection is
 * disabled or the context does not contain its initial observation.
 */
internal fun updateD6ChanceDecision(
    state: Game,
    rules: Rules,
    rollType: DiceRollType,
    data: RerollData,
    rerollContext: UseRerollContext,
    selectedSource: RerollSource? = null,
): ChanceObservationUpdate? {
    if (!state.collectChanceData) return null
    val rootSequence = rerollContext.chanceRollIndex ?: return null
    val root = rerollContext.chanceObservations.firstOrNull { it.index == rootSequence } ?: return null
    val resultId = root.dice.single().id
    val options = captureChanceRerollOptions(
        state = state,
        rules = rules,
        rollType = rollType,
        player = data.player,
        dicePool = listOf(data.roll),
        resultIdsByDieId = mapOf(data.roll.id to resultId),
        observedSuccess = data.isSuccess,
    )
    val updated = root.copy(
        success = data.isSuccess,
        rerollOptions = options,
        selectedReroll = selectedSource?.let { source ->
            ChanceRerollSelection(
                sourceId = source.id,
                resultIds = listOf(resultId),
            )
        },
    )
    return ChanceObservationUpdate(root, updated)
}

/**
 * Builds a command that finalizes every D6 observation collected by
 * [rerollContext].
 *
 * The final physical roll receives the resolved success value. The initial
 * roll's selected reroll is also annotated with whether its use was allowed or
 * aborted. All observations are then marked as finalized.
 *
 * Returns the command to finalize the dice, or `null` when chance-data
 * collection is disabled or the context contains no observations to finalize.
 */
internal fun finalizeD6ChanceObservations(
    state: Game,
    data: RerollData,
    rerollContext: UseRerollContext,
): Command? {
    if (!state.collectChanceData) return null
    val rootSequence = rerollContext.chanceRollIndex ?: return null
    val localObservations = rerollContext.chanceObservations
    if (localObservations.isEmpty()) return null
    val finalRollSequence = localObservations.maxOf { it.index }
    return buildCompositeCommand {
        localObservations.forEach { observation ->
            val selectedReroll = if (observation.index == rootSequence) {
                observation.selectedReroll?.copy(
                    allowed = rerollContext.rerollAllowed,
                    aborted = rerollContext.rerollAborted,
                )
            } else {
                observation.selectedReroll
            }
            val updated = observation.copy(
                success = if (observation.index == finalRollSequence) data.isSuccess else observation.success,
                selectedReroll = selectedReroll,
                finalized = true,
            )
            if (updated != observation) {
                add(UpdateChanceObservation(observation.index, observation, updated))
            }
        }
    }
}

/**
 * Captures the reroll choices offered by a player's skills and team rerolls for
 * a physical dice roll.
 *
 * Applicability is evaluated for both successful and failed outcomes, so each
 * [ChanceRerollOption] records the branches where it can appear. Logical dice
 * in those choices are translated to their observation result IDs using
 * [resultIdsByDieId], while [observedSuccess] determines whether the option is
 * currently usable.
 */
fun captureChanceRerollOptions(
    state: Game,
    rules: Rules,
    rollType: DiceRollType,
    player: Player,
    dicePool: List<DieRoll<*>>,
    resultIdsByDieId: Map<DieId, ChanceResultId>,
    observedSuccess: Boolean?,
): List<ChanceRerollOption> {
    val sources = buildList {
        addAll(player.skills.filterIsInstance<RerollSource>())
        addAll(player.team.rerolls)
    }.distinctBy { it.id }

    return sources.flatMap { source ->
        val successOptions = source.optionsWhenApplicable(state, rollType, dicePool, wasSuccess = true)
        val failureOptions = source.optionsWhenApplicable(state, rollType, dicePool, wasSuccess = false)
        if (successOptions.isEmpty() && failureOptions.isEmpty()) return@flatMap emptyList()
        val choices = (successOptions + failureOptions)
            .map { option -> option.dice.orEmpty().map { it.id }.toSet() }
            .distinct()
        val successChoices = successOptions.map { option -> option.dice.orEmpty().map { it.id }.toSet() }
        val failureChoices = failureOptions.map { option -> option.dice.orEmpty().map { it.id }.toSet() }
        val sourceSnapshot = source.toChanceSnapshot(rules, player)
        choices.mapNotNull { choice ->
            val resultIds = choice.mapNotNull(resultIdsByDieId::get)
            if (resultIds.isEmpty()) return@mapNotNull null
            ChanceRerollOption(
                source = sourceSnapshot,
                resultIds = resultIds,
                appliesOnSuccess = choice in successChoices,
                appliesOnFailure = choice in failureChoices,
                currentlyAvailable =
                    rules.isRerollAllowed(dicePool) &&
                        source.canReroll(state, rollType, dicePool, observedSuccess),
            )
        }
    }
}

/**
 * Calculates this source's reroll choices for a hypothetical success or
 * failure, returning no choices when the source does not apply to that branch.
 */
private fun RerollSource.optionsWhenApplicable(
    state: Game,
    rollType: DiceRollType,
    dicePool: List<DieRoll<*>>,
    wasSuccess: Boolean,
) = when (isApplicableTo(state, rollType, dicePool, wasSuccess)) {
    true -> calculateRerollOptions(rollType, dicePool, wasSuccess)
    false -> emptyList()
}

/**
 * Creates a self-contained chance-data snapshot of this reroll source.
 *
 * The snapshot includes conditional tests associated with using the source,
 * such as Pro, Loner, and Team Captain rolls that are relevant to [player].
 */
private fun RerollSource.toChanceSnapshot(
    rules: Rules,
    player: Player,
): ChanceRerollSource {
    val skill = this as? Skill<*>
    val tests = buildList {
        if (skill?.type == SkillType.PRO) {
            add(
                ChanceRerollTest(
                    rollType = DiceRollType.PRO,
                    dieSides = 6,
                    successTarget = rules.proSuccessTarget,
                    effect = ChanceRerollTestEffect.ALLOWS_REROLL,
                ),
            )
        }
        if (this@toChanceSnapshot is TeamReroll) {
            (player.getSkillOrNull(SkillType.LONER)?.value as? Int)?.let { target ->
                add(
                    ChanceRerollTest(
                        rollType = DiceRollType.LONER,
                        dieSides = 6,
                        successTarget = target,
                        effect = ChanceRerollTestEffect.ALLOWS_REROLL,
                    ),
                )
            }
            if (player.team.any {
                    PlayerSpecialRule.TEAM_CAPTAIN in it.specialRules && it.location.isOnPitch(rules)
                }
            ) {
                add(
                    ChanceRerollTest(
                        rollType = DiceRollType.TEAM_CAPTAIN,
                        dieSides = 6,
                        successTarget = 6,
                        effect = ChanceRerollTestEffect.RESTORES_SOURCE,
                    ),
                )
            }
        }
    }
    return ChanceRerollSource(
        id = id,
        owner = player.team.id,
        kind = when {
            this is TeamReroll -> ChanceRerollSourceKind.TEAM_REROLL
            skill != null -> ChanceRerollSourceKind.SKILL
            else -> ChanceRerollSourceKind.OTHER
        },
        description = rerollDescription,
        resetAt = rerollResetAt,
        skillId = skill?.skillId,
        tests = tests,
    )
}

/**
 * Converts the results of one physical roll into chance-data results.
 *
 * Each result is assigned an ID composed of [sequence] and its position in
 * [results]. A non-null logical die ID is retained so later reroll choices can
 * refer back to the corresponding result.
 */
fun createChanceDiceResults(
    sequence: Int,
    results: List<Pair<DieId?, DieResult>>,
): List<ChanceDieResult> = results.mapIndexed { index, (dieId, result) ->
    ChanceDieResult(
        id = ChanceResultId(sequence, index),
        result = result,
        dieId = dieId,
    )
}

/**
 * Captures enough metadata to ensure we can detect if a reroll boundary has
 * been reached, i.e., those lifetimes define in [Duration].
 *
 * TOOD This is not enough to capture everything, e.g. [Duration.END_OF_ACTION]
 * (as a player might have multiple actions) or [Duration.SPECIAL]
 */
fun chanceScope(state: Game, player: Player): ChanceObservationScope {
    val team = player.team
    return ChanceObservationScope(
        half = state.halfNo,
        drive = state.driveNo,
        team = team.id,
        turn = team.turnMarker,
        player = player.id,
    )
}
