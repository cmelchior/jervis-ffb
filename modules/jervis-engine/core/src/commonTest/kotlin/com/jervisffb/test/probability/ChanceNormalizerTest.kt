package com.jervisffb.test.probability

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.statistics.probability.Probability
import com.jervisffb.engine.statistics.probability.Surprisal
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.event.PhysicalD6Role
import com.jervisffb.engine.statistics.probability.event.RerollCategory
import com.jervisffb.engine.statistics.probability.normalizer.ChanceNormalizer
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
import com.jervisffb.engine.statistics.probability.scorer.LogicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.PhysicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Class responsible for testing the [ChanceNormalizer].
 */
class ChanceNormalizerTest {

    companion object {
        private val NORMALIZER_TEAM = TeamId("home")
        private val NORMALIZER_PLAYER = PlayerId("player-1")
    }

    @Test
    fun scorersInterpretTheSameRawNestedRerollDifferently() {
        val teamReroll = ChanceRerollSource(
            id = RerollSourceId("team-reroll"),
            owner = NORMALIZER_TEAM,
            kind = ChanceRerollSourceKind.TEAM_REROLL,
            description = "Team reroll",
            resetAt = Duration.END_OF_HALF,
            tests = listOf(
                ChanceRerollTest(
                    rollType = DiceRollType.LONER,
                    dieSides = 6,
                    successTarget = 4,
                    effect = ChanceRerollTestEffect.ALLOWS_REROLL,
                ),
            ),
        )
        val rootResult = ChanceResultId(0, 0)
        val raw = listOf(
            d6(
                sequence = 0,
                type = DiceRollType.DODGE,
                value = 2,
                success = false,
                rerollOptions = listOf(
                    ChanceRerollOption(
                        source = teamReroll,
                        resultIds = listOf(rootResult),
                        appliesOnSuccess = true,
                        appliesOnFailure = true,
                        currentlyAvailable = true,
                    ),
                ),
                selectedReroll = ChanceRerollSelection(
                    sourceId = teamReroll.id,
                    resultIds = listOf(rootResult),
                    allowed = true,
                ),
            ),
            d6(sequence = 1, type = DiceRollType.LONER, value = 4, success = true, enclosingRollId = 0),
            d6(sequence = 2, type = DiceRollType.DODGE, value = 5, success = true, rerolledRollId = 0),
        )

        val fixed = assertIs<ProbabilityScoreResult.Scored>(
            LogicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )
        val logical = assertIs<ActionPathEvent.D6>(fixed.events.single())
        assertEquals(5, logical.selectedValue)
        assertEquals(RerollCategory.TEAM_REROLL, logical.recoveries.single().resource.category)

        val hybrid = assertIs<ProbabilityScoreResult.Scored>(
            PhysicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )
        val physical = hybrid.events.map { assertIs<ActionPathEvent.PhysicalD6>(it) }
        assertEquals(
            listOf(PhysicalD6Role.PRIMARY, PhysicalD6Role.ACTIVATION, PhysicalD6Role.REROLL),
            physical.map { it.role },
        )
        assertEquals(RerollCategory.TEAM_REROLL, physical.first().actualRecovery?.resource?.category)
        assertEquals(physical.first().index, physical.last().traceRootSequence)
    }

    @Test
    fun ignoredAndUnsupportedRollTypesAreScorerDecisions() {
        val raw = listOf(
            d6(0, DiceRollType.REGENERATION, 4, success = true),
            d6(1, DiceRollType.ARMOUR, 6, success = true),
        )

        val result = assertIs<ProbabilityScoreResult.Unsupported>(
            LogicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )

        assertEquals(1, result.events.size)
        assertTrue(result.reasons.single().contains("Armour"))
    }

    @Test
    fun terminalUnfinalizedRollFamilyIsIgnored() {
        val raw = listOf(
            d6(0, DiceRollType.DODGE, 4, success = true),
            block(sequence = 1, finalized = false),
            d6(
                sequence = 2,
                type = DiceRollType.PRO,
                value = 4,
                success = true,
                enclosingRollId = 1,
            ),
            block(sequence = 3, rerolledRollId = 1, finalized = false),
        )

        val fixed = assertIs<ProbabilityScoreResult.Scored>(
            LogicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )
        val hybrid = assertIs<ProbabilityScoreResult.Scored>(
            PhysicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )

        assertEquals(DiceRollType.DODGE, assertIs<ActionPathEvent.D6>(fixed.events.single()).rollType)
        assertEquals(DiceRollType.DODGE, assertIs<ActionPathEvent.PhysicalD6>(hybrid.events.single()).rollType)
    }

    // If a dice roll is started, but not finalized, this roll is ignored in
    // the final score.
    @Test
    fun terminalUnfinalizedRollByItselfProducesZeroRiskScore() {
        val raw = listOf(block(sequence = 0, finalized = false))

        listOf(
            LogicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
            PhysicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        ).forEach { result ->
            val score = assertIs<ProbabilityScoreResult.Scored>(result)
            assertTrue(score.events.isEmpty())
            assertEquals(Probability.ALWAYS, score.successProbability)
            assertEquals(Surprisal.ZERO, score.surprisal,)
        }
    }

    @Test
    fun nonTerminalUnfinalizedRollRemainsUnsupported() {
        val raw = listOf(
            d6(0, DiceRollType.DODGE, 2, success = false, finalized = false),
            d6(1, DiceRollType.PICKUP, 4, success = true),
        )

        listOf(
            LogicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
            PhysicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        ).forEach { result ->
            val unsupported = assertIs<ProbabilityScoreResult.Unsupported>(result)
            assertTrue(unsupported.reasons.any { it.contains("not finalized") })
        }
    }

    @Test
    fun rawObservationRoundTripsWithAllRequiredFacts() {
        val observation = d6(0, DiceRollType.DODGE, 4, success = true)

        val encoded = Json.encodeToString(ChanceObservation.serializer(), observation)
        val decoded = Json.decodeFromString(ChanceObservation.serializer(), encoded)

        assertEquals(observation, decoded)
    }

    private fun d6(
        sequence: Int,
        type: DiceRollType,
        value: Int,
        success: Boolean,
        enclosingRollId: Int? = null,
        rerolledRollId: Int? = null,
        rerollOptions: List<ChanceRerollOption> = emptyList(),
        selectedReroll: ChanceRerollSelection? = null,
        finalized: Boolean = true,
    ) = ChanceObservation.DiceRoll(
        index = sequence,
        rollType = type,
        teamId = NORMALIZER_TEAM,
        playerId = NORMALIZER_PLAYER,
        dice = listOf(ChanceDieResult(ChanceResultId(sequence, 0), D6Result(value))),
        scope = ChanceObservationScope(
            half = 1,
            drive = 1,
            team = NORMALIZER_TEAM,
            turn = 1,
            player = NORMALIZER_PLAYER,
        ),
        enclosingRollId = enclosingRollId,
        rerolledRollId = rerolledRollId,
        success = success,
        rerollOptions = rerollOptions,
        selectedReroll = selectedReroll,
        finalized = finalized,
    )

    private fun block(
        sequence: Int,
        enclosingRollId: Int? = null,
        rerolledRollId: Int? = null,
        finalized: Boolean,
    ) = ChanceObservation.DiceRoll(
        index = sequence,
        rollType = DiceRollType.BLOCK,
        teamId = NORMALIZER_TEAM,
        playerId = NORMALIZER_PLAYER,
        dice = listOf(ChanceDieResult(ChanceResultId(sequence, 0), DBlockResult(5))),
        scope = ChanceObservationScope(
            half = 1,
            drive = 1,
            team = NORMALIZER_TEAM,
            turn = 1,
            player = NORMALIZER_PLAYER,
        ),
        enclosingRollId = enclosingRollId,
        rerolledRollId = rerolledRollId,
        finalized = finalized,
    )
}
