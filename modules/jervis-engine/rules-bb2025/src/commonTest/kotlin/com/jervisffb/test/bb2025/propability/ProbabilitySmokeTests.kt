package com.jervisffb.test.bb2025.propability

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.bb2025.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.statistics.probability.scorer.PhysicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.defaultDetermineKickingTeam
import com.jervisffb.test.defaultFanFactor
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.TeamRerollSelected
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
    @Ignore // Scatter not supported yet
    @Test
    fun catchInaccuratePass() {
        TODO()
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
        assertEquals((3 / 3.0) * (2 / 3.0) * (30 / 36.0) * (1 / 2.0), result.successProbability.value, 1e-9)
    }

    private fun scoreActions(): ProbabilityScoreResult.Scored = assertIs<ProbabilityScoreResult.Scored>(
        PhysicalActionPathScorer.score(
            state.rules,
            controller.statistics!!.diceProbabilities.observations,
            awayTeam.id,
        ),
    )
}
