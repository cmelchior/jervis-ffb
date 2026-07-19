package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.HypnoticGaze
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.hypnoticGazeRoll
import com.jervisffb.test.utils.assertDistracted
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [HypnoticGaze] skill.
 */
class HypnoticGazeTests : JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun hypnoticGazeIsAvailableAsSpecialAction() {
        val gazer = awayTeam["A1".playerId].apply {
            addSkill(SkillType.HYPNOTIC_GAZE)
        }
        controller.rollForward(PlayerSelected(gazer))
        val actions = controller.getAvailableActions().get<SelectPlayerAction>().actions
        assertTrue(actions.any { it.type == PlayerSpecialActionType.HYPNOTIC_GAZE })
    }

    @Test
    fun hypnoticGazeAvailableWhenProne() {
        val gazer = awayTeam["A1".playerId].apply {
            addSkill(SkillType.HYPNOTIC_GAZE)
            putProne()
        }
        controller.rollForward(PlayerSelected(gazer))
        val actions = controller.getAvailableActions().get<SelectPlayerAction>().actions
        assertTrue(actions.any { it.type == PlayerSpecialActionType.HYPNOTIC_GAZE })
    }

    @Test
    fun targetBecomesDistractedOnSuccess() {
        val gazer = awayTeam["A1".playerId].apply {
            addSkill(SkillType.HYPNOTIC_GAZE)
        }
        val target = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(gazer, PlayerSpecialActionType.HYPNOTIC_GAZE),
            PlayerSelected(target),
            *hypnoticGazeRoll(3.d6),
        )
        state.assertNoActivePlayer()
        target.assertDistracted()
    }

    @Test
    fun nothingHappensOnFailure() {
        val gazer = awayTeam["A1".playerId].apply {
            addSkill(SkillType.HYPNOTIC_GAZE)
        }
        val target = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(gazer, PlayerSpecialActionType.HYPNOTIC_GAZE),
            PlayerSelected(target),
            *hypnoticGazeRoll(2.d6),
        )
        state.assertNoActivePlayer()
        target.assertStanding()
        assertFalse(rules.isDistracted(target))
    }

    @Test
    fun canMoveBeforeGazing() {
        // Player should be able to move first then declare the gaze.
        val gazer = awayTeam["A6".playerId].apply {
            addSkill(SkillType.HYPNOTIC_GAZE)
        }
        val target = homeTeam["H6".playerId]
        controller.rollForward(
            *activatePlayer(gazer, PlayerSpecialActionType.HYPNOTIC_GAZE),
            SmartMoveTo(12, 1),
            PlayerSelected(target),
            *hypnoticGazeRoll(3.d6),
        )
        state.assertNoActivePlayer()
        target.assertDistracted()
    }

    @Test
    fun canBeUsedByMultiplePlayersInSameTurn() {
        val gazer1 = awayTeam["A1".playerId].apply {
            addSkill(SkillType.HYPNOTIC_GAZE)
        }
        val gazer2 = awayTeam["A2".playerId].apply {
            addSkill(SkillType.HYPNOTIC_GAZE)
        }
        val target1 = homeTeam["H1".playerId]
        val target2 = homeTeam["H2".playerId]
        controller.rollForward(
            *activatePlayer(gazer1, PlayerSpecialActionType.HYPNOTIC_GAZE),
            PlayerSelected(target1),
            *hypnoticGazeRoll(3.d6),
            *activatePlayer(gazer2, PlayerSpecialActionType.HYPNOTIC_GAZE),
            PlayerSelected(target2),
            *hypnoticGazeRoll(3.d6),
        )
        state.assertNoActivePlayer()
        target1.assertDistracted()
        target2.assertDistracted()
    }
}
