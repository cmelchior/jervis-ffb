package com.jervisffb.test.probability

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.statistics.probability.Probability
import com.jervisffb.engine.statistics.probability.Surprisal
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.event.ChanceOutcomeCategory
import com.jervisffb.engine.statistics.probability.event.OutcomeRatio
import com.jervisffb.engine.statistics.probability.event.PhysicalRollRole
import com.jervisffb.engine.statistics.probability.event.RerollCategory
import com.jervisffb.engine.statistics.probability.normalizer.ChanceNormalizer
import com.jervisffb.engine.statistics.probability.observation.ChanceDieResult
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationScope
import com.jervisffb.engine.statistics.probability.observation.ChanceOutcome
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
            d6(sequence = 1, type = DiceRollType.LONER, value = 4, success = true, enclosingRollIndex = 0),
            d6(sequence = 2, type = DiceRollType.DODGE, value = 5, success = true, rerolledRollIndex = 0),
        )

        val fixed = assertIs<ProbabilityScoreResult.Scored>(
            LogicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )
        val logical = assertIs<ActionPathEvent.Logical>(fixed.events.single())
        assertEquals(5, assertIs<D6Result>(logical.results.single()).value)
        assertEquals(RerollCategory.TEAM_REROLL, logical.recoveries.single().resource.category)

        val hybrid = assertIs<ProbabilityScoreResult.Scored>(
            PhysicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )
        val physical = hybrid.events.map { assertIs<ActionPathEvent.Physical>(it) }
        assertEquals(
            listOf(PhysicalRollRole.PRIMARY, PhysicalRollRole.ACTIVATION, PhysicalRollRole.REROLL),
            physical.map { it.role },
        )
        assertEquals(RerollCategory.TEAM_REROLL, physical.first().actualRecovery?.resource?.category)
        assertEquals(physical.first().index, physical.last().traceRootIndex)
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
                enclosingRollIndex = 1,
            ),
            block(sequence = 3, rerolledRollIndex = 1, finalized = false),
        )

        val fixed = assertIs<ProbabilityScoreResult.Scored>(
            LogicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )
        val hybrid = assertIs<ProbabilityScoreResult.Scored>(
            PhysicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )

        assertEquals(DiceRollType.DODGE, assertIs<ActionPathEvent.Logical>(fixed.events.single()).rollType)
        assertEquals(DiceRollType.DODGE, assertIs<ActionPathEvent.Physical>(hybrid.events.single()).rollType)
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
    fun finalizedD6WithoutSuccessRemainsUnsupported() {
        val raw = listOf(
            d6(0, DiceRollType.DODGE, 4, success = true).copy(success = null),
        )

        listOf(
            LogicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
            PhysicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        ).forEach { result ->
            val unsupported = assertIs<ProbabilityScoreResult.Unsupported>(result)
            assertTrue(unsupported.reasons.any { it.contains("factual success") })
        }
    }

    @Test
    fun rawObservationRoundTripsWithAllRequiredFacts() {
        val observation = d6(0, DiceRollType.DODGE, 4, success = true)

        val encoded = Json.encodeToString(ChanceObservation.serializer(), observation)
        val decoded = Json.decodeFromString(ChanceObservation.serializer(), encoded)

        assertEquals(observation, decoded)
    }

    @Test
    fun blockRerollReplacesTheMatchingDieInAMultiDiePool() {
        val firstDie = DieId("block-0")
        val secondDie = DieId("block-1")
        val root = block(
            sequence = 0,
            dice = listOf(
                ChanceDieResult(ChanceResultId(0, 0), DBlockResult(1), firstDie),
                ChanceDieResult(ChanceResultId(0, 1), DBlockResult(2), secondDie),
            ),
            selectedResultIds = listOf(ChanceResultId(0, 1)),
            finalized = true,
        )
        val reroll = block(
            sequence = 1,
            dice = listOf(ChanceDieResult(ChanceResultId(1, 0), DBlockResult(6), secondDie)),
            rerolledRollIndex = 0,
            finalized = true,
        )

        val result = assertIs<ProbabilityScoreResult.Scored>(
            LogicalActionPathScorer.score(
                listOf(root, reroll),
                NORMALIZER_TEAM,
                allowMultipleTeamRerollsPerTurn = true,
            ),
        )
        val event = assertIs<ActionPathEvent.Logical>(result.events.single())
        val resolution = assertIs<ActionPathEvent.Resolution.Block>(event.resolution)
        assertEquals(BlockDice.POW, resolution.selectedFace)
        assertEquals(2, event.results.size)
        assertEquals(OutcomeRatio(11, 36), event.observedOutcome)

        val physicalResult = assertIs<ProbabilityScoreResult.Scored>(
            PhysicalActionPathScorer.score(
                listOf(root, reroll),
                NORMALIZER_TEAM,
                allowMultipleTeamRerollsPerTurn = true,
            ),
        )
        val physicalEvent = assertIs<ActionPathEvent.Physical>(physicalResult.events.single())
        assertIs<ActionPathEvent.Resolution.Block>(physicalEvent.resolution)
        assertEquals(OutcomeRatio(11, 36), physicalEvent.observedOutcome)
    }

    @Test
    fun structuredAtLeastOutcomeUsesTheProcedureSuppliedRatio() {
        val raw = listOf(
            outcomeRoll(
                sequence = 0,
                type = DiceRollType.CHARGE,
                values = listOf(D3Result(2)),
                category = ChanceOutcomeCategory.AT_LEAST,
                successProbability = OutcomeRatio(2, 3),
            ),
        )

        val logical = assertIs<ProbabilityScoreResult.Scored>(
            LogicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )
        val logicalEvent = assertIs<ActionPathEvent.Logical>(logical.events.single())
        assertIs<D3Result>(logicalEvent.results.single())
        assertEquals(OutcomeRatio(2, 3), logicalEvent.observedOutcome)

        val physical = assertIs<ProbabilityScoreResult.Scored>(
            PhysicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )
        val physicalEvent = assertIs<ActionPathEvent.Physical>(physical.events.single())
        val resolution = assertIs<ActionPathEvent.Resolution.Outcome>(physicalEvent.resolution)
        assertEquals(ChanceOutcomeCategory.AT_LEAST, resolution.category)
        assertEquals(OutcomeRatio(2, 3), physicalEvent.observedOutcome)
    }

    @Test
    fun targetSetOutcomeRemainsOneAtomicMultiDieEvent() {
        val raw = listOf(
            outcomeRoll(
                sequence = 0,
                type = DiceRollType.SCATTER,
                values = listOf(D8Result(1), D8Result(2), D8Result(3)),
                category = ChanceOutcomeCategory.TARGET_SET,
                successProbability = OutcomeRatio(2, 512),
            ),
        )

        val result = assertIs<ProbabilityScoreResult.Scored>(
            LogicalActionPathScorer.score(raw, NORMALIZER_TEAM, allowMultipleTeamRerollsPerTurn = true),
        )
        val event = assertIs<ActionPathEvent.Logical>(result.events.single())
        val resolution = assertIs<ActionPathEvent.Resolution.Outcome>(event.resolution)
        assertEquals(ChanceOutcomeCategory.TARGET_SET, resolution.category)
        assertEquals(3, event.results.size)
        assertTrue(event.results.all { it is D8Result })
        assertEquals(OutcomeRatio(2, 512), event.observedOutcome)
    }

    @Test
    fun structuredPhysicalOutcomeRoundTrips() {
        val raw = listOf(
            outcomeRoll(
                sequence = 0,
                type = DiceRollType.CHARGE,
                values = listOf(D3Result(3)),
                category = ChanceOutcomeCategory.AT_LEAST,
                successProbability = OutcomeRatio(2, 3),
            ),
        )
        val event = assertIs<ActionPathEvent.Physical>(
            assertIs<ProbabilityScoreResult.Scored>(
                PhysicalActionPathScorer.score(
                    raw,
                    NORMALIZER_TEAM,
                    allowMultipleTeamRerollsPerTurn = true,
                ),
            ).events.single(),
        )

        val encoded = Json.encodeToString(ActionPathEvent.serializer(), event)
        val decoded = Json.decodeFromString(ActionPathEvent.serializer(), encoded)
        assertEquals(event, decoded)
    }

    private fun d6(
        sequence: Int,
        type: DiceRollType,
        value: Int,
        success: Boolean,
        enclosingRollIndex: Int? = null,
        rerolledRollIndex: Int? = null,
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
        enclosingRollIndex = enclosingRollIndex,
        rerolledRollIndex = rerolledRollIndex,
        success = success,
        rerollOptions = rerollOptions,
        selectedReroll = selectedReroll,
        finalized = finalized,
    )

    private fun outcomeRoll(
        sequence: Int,
        type: DiceRollType,
        values: List<DieResult>,
        category: ChanceOutcomeCategory,
        successProbability: OutcomeRatio,
        success: Boolean = true,
    ) = ChanceObservation.DiceRoll(
        index = sequence,
        rollType = type,
        teamId = NORMALIZER_TEAM,
        playerId = NORMALIZER_PLAYER,
        dice = values.mapIndexed { index, result ->
            ChanceDieResult(ChanceResultId(sequence, index), result)
        },
        scope = ChanceObservationScope(
            half = 1,
            drive = 1,
            team = NORMALIZER_TEAM,
            turn = 1,
            player = NORMALIZER_PLAYER,
        ),
        success = success,
        outcome = ChanceOutcome(category, successProbability),
        finalized = true,
    )

    private fun block(
        sequence: Int,
        enclosingRollIndex: Int? = null,
        rerolledRollIndex: Int? = null,
        dice: List<ChanceDieResult> = listOf(ChanceDieResult(ChanceResultId(sequence, 0), DBlockResult(5))),
        selectedResultIds: List<ChanceResultId> = emptyList(),
        finalized: Boolean,
    ) = ChanceObservation.DiceRoll(
        index = sequence,
        rollType = DiceRollType.BLOCK,
        teamId = NORMALIZER_TEAM,
        playerId = NORMALIZER_PLAYER,
        dice = dice,
        scope = ChanceObservationScope(
            half = 1,
            drive = 1,
            team = NORMALIZER_TEAM,
            turn = 1,
            player = NORMALIZER_PLAYER,
        ),
        enclosingRollIndex = enclosingRollIndex,
        rerolledRollIndex = rerolledRollIndex,
        selectedResultIds = selectedResultIds,
        finalized = finalized,
    )
}
