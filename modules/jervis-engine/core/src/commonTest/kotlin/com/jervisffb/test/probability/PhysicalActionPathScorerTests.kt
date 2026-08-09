package com.jervisffb.test.probability

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.event.ActionPathEventScope
import com.jervisffb.engine.statistics.probability.event.ActualRerollUse
import com.jervisffb.engine.statistics.probability.event.ChanceBranch
import com.jervisffb.engine.statistics.probability.event.OutcomeRatio
import com.jervisffb.engine.statistics.probability.event.PhysicalD6Role
import com.jervisffb.engine.statistics.probability.event.RerollCategory
import com.jervisffb.engine.statistics.probability.event.RerollOption
import com.jervisffb.engine.statistics.probability.event.RerollResource
import com.jervisffb.engine.statistics.probability.event.RerollUsage
import com.jervisffb.engine.statistics.probability.scorer.PhysicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class PhysicalActionPathScorerTests {

    companion object {
        private const val HYBRID_EPSILON = 1e-9
        private val HYBRID_HOME = TeamId("home")
    }

    @Test
    fun actualRerollScoresBothPhysicalDiceAndConsumesItsResource() {
        val teamReroll = resource("team", RerollCategory.TEAM_REROLL)
        val events = listOf(
            physical(0, 2.d6, actualRecovery = ActualRerollUse(teamReroll, "Team reroll")),
            physical(1, 4.d6, role = PhysicalD6Role.REROLL, root = 0),
        )

        val result = score(events)

        assertEquals(5.0 / 6.0, result.baseProbability.value, HYBRID_EPSILON)
        assertEquals(5.0 / 12.0, result.demonstratedProbability.value, HYBRID_EPSILON)
        assertEquals(5.0 / 12.0, result.successProbability.value, HYBRID_EPSILON)
        assertEquals(1.0, result.actualExtraRollAdjustment.value, HYBRID_EPSILON)
        assertEquals(2, result.eventCount)
    }

    @Test
    fun naturalOneIsCertainButRemainsASeparateEvent() {
        val teamReroll = resource("team", RerollCategory.TEAM_REROLL)
        val result = score(
            listOf(
                physical(0, 1.d6, actualRecovery = ActualRerollUse(teamReroll, "Team reroll")),
                physical(1, 4.d6, role = PhysicalD6Role.REROLL, root = 0),
            ),
        )

        assertEquals(1.0, result.baseProbability.value, HYBRID_EPSILON)
        assertEquals(0.5, result.successProbability.value, HYBRID_EPSILON)
        assertEquals(2, result.eventCount)
    }

    @Test
    fun acceptedFreshDieStillReceivesHypotheticalRecovery() {
        val recovery = option(resource("dodge", RerollCategory.STANDARD_SKILL))
        val result = score(listOf(physical(0, 4.d6, success = true, recoveries = listOf(recovery))))

        assertEquals(0.5, result.demonstratedProbability.value, HYBRID_EPSILON)
        assertEquals(0.75, result.successProbability.value, HYBRID_EPSILON)
        assertEquals(-kotlin.math.log2(1.5), result.hypotheticalRecoveryAdjustment.value, HYBRID_EPSILON)
    }

    @Test
    fun earlierHypotheticalUseCanInvalidateALaterDemonstratedUse() {
        val teamReroll = resource("team", RerollCategory.TEAM_REROLL)
        val events = listOf(
            physical(0, 4.d6, success = true, recoveries = listOf(option(teamReroll))),
            physical(1, 4.d6, actualRecovery = ActualRerollUse(teamReroll, "Team reroll")),
            physical(2, 1.d6, role = PhysicalD6Role.REROLL, root = 1),
        )

        val result = score(events)

        // Only the direct first-roll branch retains the token required by the
        // demonstrated use. The recovery branch terminates at event 1.
        assertEquals(0.25, result.successProbability.value, HYBRID_EPSILON)
    }

    @Test
    fun anActualRerollDieNeverReceivesAnotherHypotheticalRecovery() {
        val recovery = option(resource("dodge", RerollCategory.STANDARD_SKILL))
        val result = score(
            listOf(physical(0, 4.d6, role = PhysicalD6Role.REROLL, recoveries = listOf(recovery))),
        )

        assertEquals(0.5, result.successProbability.value, HYBRID_EPSILON)
    }

    @Test
    fun activationAndNestedRerollDiceAreScoredChronologically() {
        val pro = resource("pro", RerollCategory.PRO)
        val proReroll = resource("pro-reroll", RerollCategory.STANDARD_SKILL)
        val events = listOf(
            physical(0, 2.d6, actualRecovery = ActualRerollUse(pro, "Pro")),
            physical(
                1,
                3.d6,
                role = PhysicalD6Role.ACTIVATION,
                actualRecovery = ActualRerollUse(proReroll, "Pro reroll"),
                root = 1,
            ),
            physical(2, 4.d6, role = PhysicalD6Role.REROLL, root = 1),
            physical(3, 5.d6, role = PhysicalD6Role.REROLL, root = 0),
        )

        val result = score(events)

        assertEquals((5.0 * 4.0 * 3.0 * 2.0) / (6.0 * 6.0 * 6.0 * 6.0), result.successProbability.value, HYBRID_EPSILON)
        assertEquals(4, result.eventCount)
    }

    @Test
    fun hybridResultRoundTripsWithItsPhysicalLedger() {
        val result = score(listOf(physical(0, 2.d6, success = true)))
        val original = ChallengeScore.ProbabilityScore(Instant.fromEpochMilliseconds(1234), result)

        val encoded = Json.encodeToString(ChallengeScore.ProbabilityScore.serializer(), original)
        val decoded = Json.decodeFromString(ChallengeScore.ProbabilityScore.serializer(), encoded)

        assertEquals(original, decoded)
    }

    private fun score(events: List<ActionPathEvent>): ProbabilityScoreResult.Scored = assertIs(
        PhysicalActionPathScorer.scoreNormalized(events, HYBRID_HOME, allowMultipleTeamRerollsPerTurn = true),
    )

    private fun physical(
        sequence: Int,
        value: D6Result,
        role: PhysicalD6Role = PhysicalD6Role.PRIMARY,
        success: Boolean? = null,
        actualRecovery: ActualRerollUse? = null,
        recoveries: List<RerollOption> = emptyList(),
        root: Int = sequence,
    ) = ActionPathEvent.PhysicalD6(
        index = sequence,
        traceRootSequence = root,
        rollType = DiceRollType.DODGE,
        owner = HYBRID_HOME,
        selectedValue = value,
        role = role,
        scope = scope(),
        observedSuccess = success,
        actualRecovery = actualRecovery,
        recoveries = recoveries,
        finalized = true,
    )

    private fun resource(id: String, category: RerollCategory) = RerollResource(
        id = RerollSourceId(id),
        owner = HYBRID_HOME,
        category = category,
        usage = RerollUsage.ONCE_PER_TURN,
    )

    private fun option(resource: RerollResource) = RerollOption(
        resource = resource,
        activation = OutcomeRatio.CERTAIN,
        appliesTo = setOf(ChanceBranch.SELECTED, ChanceBranch.ALTERNATIVE),
    )

    private fun scope() = ActionPathEventScope(
        half = 1,
        drive = 1,
        turn = 1,
        player = "player-1".playerId,
    )
}
