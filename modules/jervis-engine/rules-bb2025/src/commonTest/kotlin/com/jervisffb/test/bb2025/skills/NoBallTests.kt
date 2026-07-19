package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.bb2025.skills.NoBall
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [NoBall] skill.
 */
class NoBallTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun failCatch() {
        awayTeam["A7".playerId].addSkill(SkillType.NO_BALL)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            SmartMoveTo(17, 7),
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            *throwBall(6.d6),
            5.d8, // Bounce due to No Ball
        )
        val ball = state.singleBall()
        assertEquals(BallState.ON_GROUND, ball.state)
        assertEquals(PitchCoordinate(16, 1), ball.coordinates)
    }

    @Test
    fun ignoreOtherSkillsOnCatch() {
        awayTeam["A7".playerId].apply {
            addSkill(SkillType.EXTRA_ARMS)
            addSkill(SkillType.NO_BALL)
        }
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            SmartMoveTo(17, 7),
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            *throwBall(6.d6),
            5.d8, // Bounce due to No Ball
        )
        assertEquals(homeTeam, state.activeTeam)
        val ball = state.singleBall()
        assertEquals(BallState.ON_GROUND, ball.state)
        assertEquals(PitchCoordinate(16, 1), ball.coordinates)
    }

    @Test
    fun failPickup() {
        awayTeam["A10".playerId].addSkill(SkillType.NO_BALL)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            2.d8 // Bounce due to No Ball
        )
        assertEquals(homeTeam, state.activeTeam)
        val ball = state.singleBall()
        assertEquals(BallState.ON_GROUND, ball.state)
        assertEquals(PitchCoordinate(17, 6), ball.coordinates)
    }

    @Test
    fun ignoreOtherSkillsOnPickup() {
        awayTeam["A10".playerId].apply {
            addSkill(SkillType.EXTRA_ARMS)
            addSkill(SkillType.BIG_HAND)
            addSkill(SkillType.NO_BALL)
        }
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            2.d8 // Bounce due to No Ball
        )
        assertEquals(homeTeam, state.activeTeam)
        val ball = state.singleBall()
        assertEquals(BallState.ON_GROUND, ball.state)
        assertEquals(PitchCoordinate(17, 6), ball.coordinates)
    }

    @Test
    fun failSecureTheBall() {
        awayTeam["A8".playerId].addSkill(SkillType.NO_BALL)
        controller.rollForward(
            *activatePlayer("A8", PlayerStandardActionType.SECURE_THE_BALL),
            SmartMoveTo(17, 7),
            8.d8 // Bounce due to No Ball
        )
        val ball = state.singleBall()
        assertEquals(BallState.ON_GROUND, ball.state)
        assertEquals(PitchCoordinate(18, 8), ball.coordinates)
    }

    @Test
    fun ignoreOtherSkillsOnSecureTheBall() {
        awayTeam["A8".playerId].apply {
            addSkill(SkillType.NO_BALL)
            addSkill(SkillType.BIG_HAND)
            addSkill(SkillType.EXTRA_ARMS)
        }
        controller.rollForward(
            *activatePlayer("A8", PlayerStandardActionType.SECURE_THE_BALL),
            SmartMoveTo(17, 7),
            8.d8 // Bounce due to No Ball
        )
        val ball = state.singleBall()
        assertEquals(BallState.ON_GROUND, ball.state)
        assertEquals(PitchCoordinate(18, 8), ball.coordinates)
    }

    @Test
    fun cannotIntercept() {
        (1..5).forEach {
            homeTeam["H${it}".playerId].addSkill(SkillType.NO_BALL)
        }
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(13, 9),
            *throwBall(6.d6),
            *catch(6.d6),
        )
        assertTrue(awayTeam["A5".playerId].hasBall())
    }
}
