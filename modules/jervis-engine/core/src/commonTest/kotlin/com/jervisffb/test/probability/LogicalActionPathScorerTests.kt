package com.jervisffb.test.probability

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.event.ActionPathEventScope
import com.jervisffb.engine.statistics.probability.event.ChanceBranch
import com.jervisffb.engine.statistics.probability.event.OutcomeRatio
import com.jervisffb.engine.statistics.probability.event.RerollCategory
import com.jervisffb.engine.statistics.probability.event.RerollOption
import com.jervisffb.engine.statistics.probability.event.RerollResource
import com.jervisffb.engine.statistics.probability.event.RerollUsage
import com.jervisffb.engine.statistics.probability.scorer.LogicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.PriorityListRerollUsagePolicy
import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class LogicalActionPathScorerTests {

    companion object {
        private val EPSILON = 1e-9
        private val HOME = TeamId("home")
        private val AWAY = TeamId("away")
    }

    @Test
    fun selectedValueDefinesSuccessAndFailureProbability() {
        (2..6).forEach { value ->
            val event = d6(sequence = 0, value = value, success = true)
            assertEquals((7 - value) / 6.0, event.observedOutcome.probability.value, EPSILON)
        }
        (1..5).forEach { value ->
            val event = d6(sequence = 0, value = value, success = false)
            assertEquals(value / 6.0, event.observedOutcome.probability.value, EPSILON)
        }
    }

    @Test
    fun standardDiceUseTheSameLogicalEventShape() {
        val d3 = ActionPathEvent.Logical.die(
            index = 0,
            rollType = DiceRollType.CHARGE,
            owner = HOME,
            result = D3Result(2),
            isSuccess = true,
            scope = scope(),
        )
        val d8 = ActionPathEvent.Logical.die(
            index = 1,
            rollType = DiceRollType.SCATTER,
            owner = HOME,
            result = D8Result(5),
            isSuccess = true,
            scope = scope(),
        )

        assertEquals(OutcomeRatio(2, 3), d3.observedOutcome)
        assertEquals(OutcomeRatio(4, 8), d8.observedOutcome)
        assertIs<ActionPathEvent.Resolution.Dice>(d3.resolution)
        assertIs<ActionPathEvent.Resolution.Dice>(d8.resolution)
    }

    @Test
    fun recoveryPriorityIsSkillThenGuaranteedTeamThenBestGate() {
        val skill = recovery("skill", RerollCategory.STANDARD_SKILL, 1, 1)
        val guaranteedTeam = recovery("team", RerollCategory.TEAM_REROLL, 1, 1)
        val pro = recovery("pro", RerollCategory.PRO, 4, 6)
        val lonerFourPlus = recovery("loner-team", RerollCategory.TEAM_REROLL, 3, 6)
        val lonerTwoPlus = recovery("better-loner-team", RerollCategory.TEAM_REROLL, 5, 6)
        val tiedTeam = recovery("tied-team", RerollCategory.TEAM_REROLL, 4, 6)

        assertEquals(skill, PriorityListRerollUsagePolicy.select(listOf(pro, guaranteedTeam, skill)))
        assertEquals(guaranteedTeam, PriorityListRerollUsagePolicy.select(listOf(pro, guaranteedTeam)))
        assertEquals(pro, PriorityListRerollUsagePolicy.select(listOf(lonerFourPlus, pro)))
        assertEquals(lonerTwoPlus, PriorityListRerollUsagePolicy.select(listOf(lonerTwoPlus, pro)))
        assertEquals(pro, PriorityListRerollUsagePolicy.select(listOf(tiedTeam, pro)))
    }

    @Test
    fun failedProActivationStopsWithoutTeamRerollFallback() {
        val event = d6(
            sequence = 0,
            value = 4,
            success = true,
            recoveries = listOf(
                recovery("pro", RerollCategory.PRO, 4, 6),
                recovery("loner-team", RerollCategory.TEAM_REROLL, 3, 6),
            ),
        )

        val result = score(listOf(event))

        // 1/2 succeeds directly. Of the other half, Pro activates 4/6 and
        // rerolls another 1/2. Failed Pro does not fall through to Loner.
        assertEquals(2.0 / 3.0, result.successProbability.value, EPSILON)
    }

    @Test
    fun dodgeProAndLonerExampleUsesStopPolicy() {
        val dodge = recovery(
            id = "dodge",
            category = RerollCategory.STANDARD_SKILL,
            activationNumerator = 1,
            activationDenominator = 1,
            usage = RerollUsage.ONCE_PER_TURN,
        )
        val pro = recovery("pro", RerollCategory.PRO, 4, 6)
        val lonerTeam = recovery("loner-team", RerollCategory.TEAM_REROLL, 3, 6)
        val events = listOf(
            d6(0, 3, true, listOf(dodge)),
            d6(1, 4, true, listOf(pro, lonerTeam)),
        )

        val result = score(events)

        assertEquals(16.0 / 27.0, result.successProbability.value, EPSILON)
    }

    @Test
    fun multipleTeamRerollsFollowRulesConfiguration() {
        val first = d6(
            sequence = 0,
            value = 4,
            success = true,
            recoveries = listOf(
                recovery("team-0", RerollCategory.TEAM_REROLL, 1, 1),
                recovery("team-1", RerollCategory.TEAM_REROLL, 1, 1),
            ),
        )
        val second = first.copy(index = 1)

        val multiple = score(listOf(first, second), allowMultipleTeamRerollsPerTurn = true)
        val single = score(listOf(first, second), allowMultipleTeamRerollsPerTurn = false)

        assertEquals(9.0 / 16.0, multiple.successProbability.value, EPSILON)
        assertEquals(1.0 / 2.0, single.successProbability.value, EPSILON)
    }

    @Test
    fun consumedSkillResetsWithItsDeclaredScope() {
        val dodge = recovery(
            id = "dodge",
            category = RerollCategory.STANDARD_SKILL,
            activationNumerator = 1,
            activationDenominator = 1,
            usage = RerollUsage.ONCE_PER_TURN,
        )
        val first = d6(0, 4, true, listOf(dodge), scope(turn = 1))
        val sameTurn = d6(1, 4, true, listOf(dodge), scope(turn = 1))
        val nextTurn = d6(1, 4, true, listOf(dodge), scope(turn = 2))

        assertEquals(1.0 / 2.0, score(listOf(first, sameTurn)).successProbability.value, EPSILON)
        assertEquals(9.0 / 16.0, score(listOf(first, nextTurn)).successProbability.value, EPSILON)
    }

    @Test
    fun opponentUsesRecoveryAdversarially() {
        val event = d6(
            sequence = 0,
            value = 4,
            success = true,
            owner = AWAY,
            recoveries = listOf(
                recovery("away-team", RerollCategory.TEAM_REROLL, 1, 1, owner = AWAY),
            ),
        )

        val result = score(listOf(event))

        // The demonstrated branch first occurs on 1/2. The opponent rerolls it,
        // and only another demonstrated result (1/2) preserves the line.
        assertEquals(1.0 / 4.0, result.successProbability.value, EPSILON)
        assertEquals(2.0, result.surprisal.value, EPSILON)
    }

    @Test
    fun equallyNamedResourcesOwnedByDifferentTeamsDoNotCollide() {
        val homeEvent = d6(
            sequence = 0,
            value = 4,
            success = true,
            recoveries = listOf(
                recovery("shared-id", RerollCategory.TEAM_REROLL, 1, 1),
            ),
        )
        val awayEvent = d6(
            sequence = 1,
            value = 4,
            success = true,
            owner = AWAY,
            recoveries = listOf(
                recovery("shared-id", RerollCategory.TEAM_REROLL, 1, 1, owner = AWAY),
            ),
        )

        val result = score(listOf(homeEvent, awayEvent))

        assertEquals(3.0 / 16.0, result.successProbability.value, EPSILON)
    }

    @Test
    fun unsupportedChanceProducesAnUnrankedResult() {
        val result = LogicalActionPathScorer.scoreNormalized(
            events = listOf(ActionPathEvent.Unsupported(0, DiceRollType.ARMOUR, "Multi-outcome table roll")),
            solvingTeamId = HOME,
            allowMultipleTeamRerollsPerTurn = true,
        )

        assertIs<ProbabilityScoreResult.Unsupported>(result)
    }

    @Test
    fun stateCeilingReturnsUnsupportedInsteadOfAnApproximation() {
        val event = d6(
            sequence = 0,
            value = 4,
            success = true,
            recoveries = listOf(recovery("team", RerollCategory.TEAM_REROLL, 1, 1)),
        )
        val laterEvent = event.copy(index = 1)

        val result = LogicalActionPathScorer.scoreNormalized(
            events = listOf(event, laterEvent),
            solvingTeamId = HOME,
            allowMultipleTeamRerollsPerTurn = true,
            stateCeiling = 1,
        )

        assertIs<ProbabilityScoreResult.Unsupported>(result)
    }

    @Test
    fun expiredResourceScopesDoNotInflateTheLiveStateCount() {
        val events = (0 until 20).map { index ->
            val turn = index
            d6(
                sequence = index,
                value = 4,
                success = true,
                recoveries = listOf(
                    recovery(
                        id = "dodge",
                        category = RerollCategory.STANDARD_SKILL,
                        activationNumerator = 1,
                        activationDenominator = 1,
                        usage = RerollUsage.ONCE_PER_TURN,
                    ),
                ),
                eventScope = scope(turn),
            )
        }

        val result = LogicalActionPathScorer.scoreNormalized(
            events = events,
            solvingTeamId = HOME,
            allowMultipleTeamRerollsPerTurn = true,
            stateCeiling = 1,
        )

        assertIs<ProbabilityScoreResult.Scored>(result)
    }

    @Test
    fun challengeScoreRoundTripsWithNormalizedLedger() {
        val result = score(listOf(d6(0, 3, true)))
        val original = ChallengeScore.ProbabilityScore(Instant.fromEpochMilliseconds(1234), result)

        val encoded = Json.encodeToString(ChallengeScore.ProbabilityScore.serializer(), original)
        val decoded = Json.decodeFromString(ChallengeScore.ProbabilityScore.serializer(), encoded)

        assertEquals(original, decoded)
    }

    private fun score(
        events: List<ActionPathEvent>,
        allowMultipleTeamRerollsPerTurn: Boolean = true,
    ): ProbabilityScoreResult.Scored = assertIs(
        LogicalActionPathScorer.scoreNormalized(events, HOME, allowMultipleTeamRerollsPerTurn),
    )

    private fun d6(
        sequence: Int,
        value: Int,
        success: Boolean,
        recoveries: List<RerollOption> = emptyList(),
        eventScope: ActionPathEventScope = scope(),
        owner: TeamId = HOME,
    ) = ActionPathEvent.Logical.die(
        index = sequence,
        rollType = DiceRollType.DODGE,
        owner = owner,
        result = D6Result(value),
        isSuccess = success,
        scope = eventScope,
        recoveries = recoveries,
    )

    private fun recovery(
        id: String,
        category: RerollCategory,
        activationNumerator: Int,
        activationDenominator: Int,
        usage: RerollUsage = RerollUsage.ONCE_PER_HALF,
        owner: TeamId = HOME,
    ) = RerollOption(
        resource = RerollResource(RerollSourceId(id), owner, category, usage),
        activation = OutcomeRatio(activationNumerator, activationDenominator),
        appliesTo = setOf(ChanceBranch.SELECTED, ChanceBranch.ALTERNATIVE),
    )

    private fun scope(turn: Int = 1) = ActionPathEventScope(
        half = 1,
        drive = 1,
        turn = turn,
        player = "player-1".playerId,
    )
}
