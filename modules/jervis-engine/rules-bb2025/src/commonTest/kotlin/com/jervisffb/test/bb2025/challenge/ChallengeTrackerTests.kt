package com.jervisffb.test.bb2025.challenge

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.bb2025.challenge.goal.BlockGoalBuilder
import com.jervisffb.engine.challenge.ChallengeBuilder
import com.jervisffb.engine.challenge.ChallengeCategory
import com.jervisffb.engine.challenge.ChallengeOutcome
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.engine.challenge.ChallengeScoring
import com.jervisffb.engine.challenge.ChallengeTracker
import com.jervisffb.engine.challenge.GoalTarget
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertCoordinates
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ChallengeTrackerTests : JervisGameBB2025Test() {

    private lateinit var challenge: ChallengeTracker

    @BeforeTest
    override fun setUp() {
        setupDefaultGame(
            initialActions = arrayOf(
                *defaultPregame(),
                *defaultSetup(),
                *defaultKickOffHomeTeam(),
            ).filterNotNull().toList(),
            protectInitialActions = true,
            collectMetadata = true
        )
    }

    fun rollForward(vararg actions: GameAction?) {
        actions.forEach {
            controller.rollForward(it)
            challenge.evaluate(controller.state, controller.getDelta())
        }
    }

    @Test
    fun undoUpdatesProbability() {
        challenge = createChallenge()
        assertEquals(ChallengeOutcome.IN_PROGRESS, challenge.currentOutcome)
        val attacker = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId]
        rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *moveTo(14, 5),
            *dodge(6.d6),
            *moveTo(13, 5),
            *moveTo(14, 5),
            *dodge(6.d6),
            Undo,
            Undo,
            Undo,
            Undo,
            Undo,
            Undo,
            Undo,
            Undo,
            Undo,
            Undo,
        )
        assertEquals(ChallengeOutcome.IN_PROGRESS, challenge.currentOutcome)
        attacker.assertActive()
        attacker.assertCoordinates(13, 5)
        rollForward(
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.STANDARD),
            3.dblock,
        )
        assertEquals(ChallengeOutcome.COMPLETED, challenge.currentOutcome)
        val score = challenge.score
        assertIs<ChallengeScore.ProbabilityScore>(score)
        assertIs<ProbabilityScoreResult.Scored>(score.result)
        assertEquals(1.0, (score.result as ProbabilityScoreResult.Scored).successProbability.value)
    }

    @Test
    fun revertUpdatesProbability() {
        challenge = createChallenge()
        assertEquals(ChallengeOutcome.IN_PROGRESS, challenge.currentOutcome)
        val attacker = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId]
        rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *moveTo(14, 5),
            *dodge(6.d6),
            *moveTo(13, 5),
            *moveTo(14, 5),
            *dodge(6.d6),
            Revert,
            Revert,
            Revert,
            Revert,
            Revert,
            Revert,
            Revert,
            Revert,
            Revert,
            Revert,
        )
        assertEquals(ChallengeOutcome.IN_PROGRESS, challenge.currentOutcome)
        attacker.assertActive()
        attacker.assertCoordinates(13, 5)
        rollForward(
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.STANDARD),
            3.dblock,
        )
        assertEquals(ChallengeOutcome.COMPLETED, challenge.currentOutcome)
        val score = challenge.score
        assertIs<ChallengeScore.ProbabilityScore>(score)
        assertIs<ProbabilityScoreResult.Scored>(score.result)
        assertEquals(1.0, (score.result as ProbabilityScoreResult.Scored).successProbability.value)
    }

    private fun createChallenge(): ChallengeTracker {
        val challenge = ChallengeBuilder(ChallengeId("tracker-undo")).apply {
            name = "Tracker Undo"
            description = "Undo the action which completed the debug goal."
            category = ChallengeCategory.BLOCKING
            gameRules = rules
            homeTeam = this@ChallengeTrackerTests.homeTeam
            awayTeam = this@ChallengeTrackerTests.awayTeam
            goal = BlockGoalBuilder(awayTeam!!, GoalTarget.AnyPlayers(1))
                .build()
            scoring = ChallengeScoring.ProbabilityScoring(awayTeam!!.id)
        }.build()
        return ChallengeTracker(challenge).also { tracker ->
            tracker.initialize(state, controller.statistics!!)
        }
    }
}
