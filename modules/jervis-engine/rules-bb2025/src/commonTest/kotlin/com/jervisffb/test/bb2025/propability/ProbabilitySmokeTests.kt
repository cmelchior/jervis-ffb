package com.jervisffb.test.bb2025.propability

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.bb2025.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.statistics.probability.normalizer.ActualRerollUsageNormalizerPolicy
import com.jervisffb.engine.statistics.probability.normalizer.ChanceNormalizer
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.scorer.LogicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.PhysicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.bounce
import com.jervisffb.test.breatheFireRoll
import com.jervisffb.test.catch
import com.jervisffb.test.chainsawRoll
import com.jervisffb.test.defaultDetermineKickingTeam
import com.jervisffb.test.defaultFanFactor
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.giveBallToPlayer
import com.jervisffb.test.landingRoll
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.proRoll
import com.jervisffb.test.puntDirection
import com.jervisffb.test.puntDistance
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.SelectSkillReroll
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * This class contains smoke tests for the probability score implementation.
 * In particular, it attempts to score action sequences which are known
 * to hit edge cases in probability calculations.
 *
 * These tests use [PhysicalActionPathScorer] as this the default scoring
 * mechanism for challenges.
 */
class ProbabilitySmokeTests: JervisGameBB2025Test() {

    private companion object {
        const val EPSILON = 1e-9
    }

    @BeforeTest
    override fun setUp() {
        setupDefaultGame(collectMetadata = true)
        state.collectChanceData = false
        startDefaultGame()
        state.collectChanceData = true
    }

    /**
     * Goal: 3 rolls all that all require 3+.
     * Team Rerolls: 1
     * Sequence:
     *   1. Roll a 2, use the team reroll, and roll a 3.
     *   2. Roll a 3 and accept it.
     *   3. Roll a 3 and accept it.
     */
    @Test
    fun failDodgeAndUseRerollAtStartOfSequence() {
        awayTeam.rerolls.subList(1, awayTeam.rerolls.size).clear()
        awayTeam["A1".playerId].addSkill(SkillType.TITCHY)
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(13, 4),
            2.d6,
            TeamRerollSelected<StandardTeamReroll>(),
            3.d6,
            *moveTo(12, 4),
            3.d6,
            *moveTo(11, 4),
            3.d6,
        )

        // Expected Rolls: (5/6) × (4/6) × (4/6) × (4/6) = 20/81 = 24.69%
        val result = scoreActions()
        assertEquals(4, result.eventCount)
        assertEquals(20.0 / 81.0, result.successProbability.value, 1e-9)
    }

    /**
     * Goal: Player X must hold the ball, held by Player Y.
     * Sequence:
     *   1. Pass the ball to Player Y.
     *   2. Roll in-accurate throw
     *   3. Scatter the ball 3 times, still ending up on Player Y.
     *   4. Player Y catches it.
     */
    @Test
    fun catchInaccuratePass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 4),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(14, 1),
            *throwBall(3.d6),
            DiceRollResults(2.d8, 8.d8, 4.d8),
            *catch(4.d6),
        )

        val result = scoreActions()
        assertEquals(4, result.eventCount)
        // Scatter has 24 favorable ordered outcomes out of 8³; the catch roll succeeds on 4+.
        assertEquals((24.0 / 512.0) * (3.0 / 6.0), result.successProbability.value, 1e-9)
    }

    @Test
    fun breatheFireIsScoreable() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.BREATHE_FIRE)
        }
        val defender = homeTeam["H1".playerId]

        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender),
            *breatheFireRoll(3.d6),
        )

        val result = scoreActions()
        assertEquals(1, result.eventCount)
        // Breathe Fire succeeds on 3+: the selected 3+ branch, plus the
        // hypothetical team-reroll branch for a failed roll.
        assertEquals((4 / 6.0) + (2 / 6.0) * (4 / 6.0), result.successProbability.value, EPSILON)
    }

    @Test
    fun d3PuntDirectionIsScoreable() {
        val punter = awayTeam["A10".playerId].apply {
            addSkill(SkillType.PUNT)
        }
        giveBallToPlayer(punter)

        controller.rollForward(
            *activatePlayer(punter, PlayerSpecialActionType.PUNT),
            PassTypeSelected(PassType.PUNT),
            com.jervisffb.engine.actions.DirectionSelected(com.jervisffb.engine.model.Direction.RIGHT),
            *puntDirection(2.d3),
            *puntDistance(2.d6),
            bounce(5.d8),
        )

        val result = scoreActions()
        assertEquals(3, result.eventCount)
        // Punt direction (2/3+) and distance (2/6+) each include a
        // hypothetical team-reroll branch; the bounce direction is 1/8.
        assertEquals(
            ((2 / 3.0) + (1 / 3.0) * (2 / 3.0)) *
                ((5 / 6.0) + (1 / 6.0) * (5 / 6.0)) *
                (1 / 8.0),
            result.successProbability.value,
            EPSILON,
        )
    }

    @Test
    fun thrownPlayerBounceIsScoreable() {
        setupAndStartThrowTeamMateGame(collectMetadata = true)
        state.collectChanceData = true

        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            PitchSquareSelected(8, 4),
            *qualityRoll(1.d6),
            3.d8,
            *landingRoll(6.d6),
        )

        val result = scoreActions()
        assertEquals(3, result.eventCount)
        // Quality Roll (1.d6), Bounce (3.d8), Landing (6.d6) (with reroll)
        assertEquals((6 / 6.0) * (1 / 8.0) * (11 / 36.0),
            result.successProbability.value,
            EPSILON,
        )
    }

    @Test
    fun pregameChanceRollsAreScoreable() {
        setupDefaultGame(collectMetadata = true)
        state.collectChanceData = true
        controller.rollForward(
            *defaultFanFactor(), // 1d3, 2.d3
            DiceRollResults(4.d6, 4.d6), // Weather roll
            *defaultDetermineKickingTeam(),
        )

        val result = scoreActions()
        assertEquals(4, result.eventCount)
        // Fan Factor + Weather + Coin Toss: 1.d3, 2.d3, [ 4.d6, 4.d6 ], 1/2
        assertEquals((3 / 3.0) * (2 / 3.0) * (7 / 11.0) * (1 / 2.0), result.successProbability.value, 1e-9)
    }

    /**
     * Goal: Player Y (with Lone Fouler) must foul Player X without any assists and Stun them.
     * Sequence:
     *   0. Player is put Prone.
     *   1. Player Y selects foul.
     *   2. Player Y move to stand next to Player X without any assists.
     *   3. Player Y fouls Player X.
     *   4. Roll Armour, that doesn't break armour.
     *   5. Use Lone Fouler and reroll the Armour Roll that now breaks armour.
     *   6. Roll 6 (below 7) to stun Player X.
     */
    @Test
    fun loneFoulerToStunPlayer() {
        val fouler = awayTeam["A6".playerId]
        fouler.addSkill(SkillType.LONE_FOULER)
        val target = homeTeam["H1".playerId]
        target.putProne()

        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target),
            DiceRollResults(2.d6, 2.d6), // AV Roll
            Confirm, // Use Lone Fouler
            DiceRollResults(5.d6, 6.d6), // AV Reroll
            DiceRollResults(1.d6, 5.d6), // Injury Roll
        )

        val policy = ActualRerollUsageNormalizerPolicy(
            ignoredRollTypes = ActualRerollUsageNormalizerPolicy.DEFAULT.ignoredRollTypes - setOf(
                DiceRollType.ARMOUR,
                DiceRollType.INJURY,
            ),
        )
        val scored = assertIs<ProbabilityScoreResult.Scored>(
            PhysicalActionPathScorer.scoreNormalized(
                rules = state.rules,
                events = ChanceNormalizer(policy).normalize(
                    controller.statistics!!.diceProbabilities.observations,
                ),
                solvingTeamId = awayTeam.id,
            ),
        )

        assertEquals(3, scored.eventCount)
        // This is the probability of the demonstrated physical sequence:
        //  - Armour fails against AV9: 26 of 36 2D6 combinations total less than 9.
        //  - Lone Fouler reroll breaks AV9: 10 of 36 combinations total at least 9.
        //  - Injury totals at least 6: 26 of 36 combinations. The selected 6 gives a Stunned.
        assertEquals(
            (26.0 / 36.0) * (10.0 / 36.0) * (26.0 / 36.0),
            scored.successProbability.value,
            1e-9,
        )
    }

    /**
     * This test is mostly to document the understanding of the different score
     * values when using [PhysicalActionPathScorer] vs. [LogicalActionPathScorer].
     */
    @Test
    fun compareScoringAlgorithms() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.PRO)
        }
        assertTrue(awayTeam.availableRerolls.any { !it.rerollUsed})
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Failed Dodge.
            SelectSkillReroll(SkillType.PRO),
            *proRoll(3.d6), // Successful Pro activation.
            3.d6, // Successful Dodge reroll.
        )

        // Physical Action Path Scorer
        val physicalScore = assertIs<ProbabilityScoreResult.Scored>(
            PhysicalActionPathScorer.score(
                rules = state.rules,
                observations = controller.statistics!!.diceProbabilities.observations,
                solvingTeamId = awayTeam.id,
            ),
        )
        assertEquals(3, physicalScore.eventCount)
        // The primary Dodge roll, excluding the Pro activation and the physical Dodge reroll.
        assertEquals(5/6.0, physicalScore.baseProbability.value, EPSILON)
        // The exact physical sequence, including the successful Pro activation and the successful Dodge reroll.
        assertEquals((5/6.0) * (4/6.0) * (4/6.0), physicalScore.demonstratedProbability.value, EPSILON)
        // The demonstrated line plus the hypothetical team-reroll branch:
        // - 5/6: the primary Dodge roll follows the observed branch.
        // - 4/6 * 4/6: Pro activates and the Dodge reroll succeeds.
        // - 2/6 * 4/6 * 4/6: Pro fails, the team reroll activates successfully,
        val physicalSuccess = (5/6.0) * ((4/6.0) * (4/6.0) + (2/6.0) * (4/6.0) * (4/6.0))
        assertEquals(40/81.0, physicalSuccess)
        assertEquals(physicalSuccess,physicalScore.successProbability.value, EPSILON)

        // Logical Action Path Scorer
        val logicalScore = assertIs<ProbabilityScoreResult.Scored>(
            LogicalActionPathScorer.score(
                state.rules,
                controller.statistics!!.diceProbabilities.observations,
                awayTeam.id,
            ),
        )
        assertEquals(1, logicalScore.eventCount)
        // The single normalized Dodge event containing the final value from the physical Dodge reroll.
        assertEquals(4/6.0, logicalScore.baseProbability.value, EPSILON)
        // Is identical to `baseProbability` because the logical scorer omits Pro activation and collapses the physical reroll.
        assertEquals(4/6.0, logicalScore.demonstratedProbability.value, EPSILON)
        // The normalized Dodge plus its hypothetical team-reroll branch:
        // - 4/6: the normalized Dodge succeeds.
        // - 2/6 * 4/6: it fails, then the team reroll succeeds.
        val logicalSuccess = (4/6.0) + (2/6.0) * (4/6.0)
        assertEquals(72/81.0, logicalSuccess)
        assertEquals(logicalSuccess, logicalScore.successProbability.value, EPSILON)
    }

    @Test
    fun chainsawRerollIsScoreable() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
            addSkill(SkillType.PRO)
        }
        val defender = homeTeam["H1".playerId]

        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHAINSAW),
            PlayerSelected(defender),
            *chainsawRoll(3.d6, SelectSkillReroll(SkillType.PRO)),
            *proRoll(4.d6),
            2.d6,
            DiceRollResults(1.d6, 2.d6),
        )

        val chainsawRolls = controller.statistics!!.diceProbabilities.observations
            .filterIsInstance<ChanceObservation.DiceRoll>()
            .filter { it.rollType == DiceRollType.CHAINSAW }

        assertEquals(listOf(true, true), chainsawRolls.map { it.success })

        val score = scoreActions(scoreArmour = true)
        val armourProbability = D6Result.combinationsAtLeastTotal(dice = 2, total = 6).toDouble() / (D6Result.SIDES * D6Result.SIDES)
        assertEquals(
            // Chainsaw (3+), Pro (4+), Chainsaw (2+), Armour (6+)
            (4/6.0) * (3/6.0) * (5/6.0) * armourProbability,
            score.demonstratedProbability.value,
            EPSILON,
        )
        // The success probability also includes the hypothetical team reroll after Pro fails.
        assertEquals(
            (5.0 / 12.0) * armourProbability,
            score.successProbability.value,
            EPSILON,
        )
    }

    private fun scoreActions(scoreArmour: Boolean = false): ProbabilityScoreResult.Scored =
        assertIs<ProbabilityScoreResult.Scored>(
            PhysicalActionPathScorer.scoreNormalized(
                rules = state.rules,
                events = ChanceNormalizer(
                    ActualRerollUsageNormalizerPolicy(
                        ignoredRollTypes = ActualRerollUsageNormalizerPolicy.DEFAULT.ignoredRollTypes
                            .let { ignored -> if (scoreArmour) ignored - DiceRollType.ARMOUR else ignored },
                    ),
                ).normalize(controller.statistics!!.diceProbabilities.observations),
                solvingTeamId = awayTeam.id,
            ),
        )
}
