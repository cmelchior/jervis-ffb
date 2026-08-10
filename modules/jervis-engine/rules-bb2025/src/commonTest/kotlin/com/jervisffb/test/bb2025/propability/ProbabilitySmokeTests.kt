package com.jervisffb.test.bb2025.propability

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.bb2025.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.statistics.probability.normalizer.ActualRerollUsageNormalizerPolicy
import com.jervisffb.engine.statistics.probability.normalizer.ChanceNormalizer
import com.jervisffb.engine.statistics.probability.scorer.PhysicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.catch
import com.jervisffb.test.defaultDetermineKickingTeam
import com.jervisffb.test.defaultFanFactor
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * This class contains smoke tests for the probability score implementation.
 * In particular, it attempts to score action sequences which are known
 * to hit edge cases in probability calculations.
 *
 * These tests use [PhysicalActionPathScorer] as this the default scoring
 * mechanism for challenges.
 */
class ProbabilitySmokeTests: JervisGameBB2025Test() {

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

    private fun scoreActions(): ProbabilityScoreResult.Scored = assertIs<ProbabilityScoreResult.Scored>(
        PhysicalActionPathScorer.score(
            state.rules,
            controller.statistics!!.diceProbabilities.observations,
            awayTeam.id,
        ),
    )
}
