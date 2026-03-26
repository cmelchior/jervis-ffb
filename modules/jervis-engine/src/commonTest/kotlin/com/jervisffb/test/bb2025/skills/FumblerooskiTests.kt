package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.bb2025.skills.Fumblerooski
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.jump
import com.jervisffb.test.jumpTo
import com.jervisffb.test.leap
import com.jervisffb.test.leapTo
import com.jervisffb.test.moveTo
import com.jervisffb.test.pogoRoll
import com.jervisffb.test.pogoTo
import com.jervisffb.test.rushRoll
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.putProne
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [Fumblerooski] skill.
 */
class FumblerooskiTests: JervisGameBB2025Test() {

    lateinit var ballCarrier: Player

    @Test
    override fun setUp() {
        super.setUp()
        startDefaultGame()

        // Give ball to A1
        ballCarrier = awayTeam["A1".playerId]
        ballCarrier.addSkill(SkillType.FUMBLEROOSKI)
        SetBallState.carried(state.singleBall(), ballCarrier).execute(state)
    }

    @Ignore
    @Test
    fun doesNotWorkIfTentaclesPreventMove() {
        TODO("Tentacles not supported yet")
    }

    @Test
    fun decideBeforeRush() {
        ballCarrier = awayTeam["A6".playerId]
        ballCarrier.apply {
            addSkill(SkillType.FUMBLEROOSKI)
            move = 0
            movesLeft = 0
            rushesLeft = 2
        }
        SetBallState.carried(state.singleBall(), ballCarrier).execute(state)
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.MOVE),
            *moveTo(15, 2),
            Confirm, // Use Fumblerooski
            *rushRoll(6.d6),
            EndAction
        )
        assertNull(state.activePlayer)
        ballCarrier.assertCoordinates(15, 2)
        state.singleBall().assertCoordinates(14, 1)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun decideBeforeDodge() {
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            Confirm, // Use Fumblerooski
            *dodge(6.d6),
            EndAction
        )
        assertNull(state.activePlayer)
        ballCarrier.assertCoordinates(14, 5)
        state.singleBall().assertCoordinates(13, 5)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun shadowingWillBounceBall() {
        val shadower = homeTeam["H1".playerId]
        shadower.addSkill(SkillType.SHADOWING)
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            Confirm, // Use Fumblerooski
            *rushRoll(6.d6),
            PlayerSelected(shadower), // Use Shadowing
            6.d6, // Shadowing roll,
            3.d8, // Bounce
            EndAction
        )
        assertNull(state.activePlayer)
        ballCarrier.assertCoordinates(14, 5)
        state.singleBall().assertCoordinates(14, 4)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun divingTackleWillBounceBall() {
        val tackler = homeTeam["H1".playerId]
        tackler.addSkill(SkillType.DIVING_TACKLE)
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            Confirm, // Use Fumblerooski
            *dodge(5.d6),
            PlayerSelected(tackler), // Use Diving Tackle
            3.d8, // Bounce
            EndAction
        )
        assertNull(state.activePlayer)
        ballCarrier.assertCoordinates(14, 5)
        state.singleBall().assertCoordinates(14, 4)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun workOnSuccessfulJump() {
        homeTeam["H1".playerId].putProne()
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.FOUL),
            *jumpTo(11, 5),
            *jump(6.d6),
            Confirm, // Use Fumblerooski
            EndAction
        )
        assertNull(state.activePlayer)
        ballCarrier.assertCoordinates(11, 5)
        state.singleBall().assertCoordinates(13, 5)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun workOnFallingOverAfterJumpToTargetSquare() {
        homeTeam["H1".playerId].putProne()
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.FOUL),
            *jumpTo(11, 5),
            *jump(3.d6), // Fail, but still end up in targt square
            Confirm, // Use Fumblerooski
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        homeTeam.assertActive()
        ballCarrier.assertCoordinates(11, 5)
        ballCarrier.assertProne()
        state.singleBall().assertCoordinates(13, 5)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun doesNotWorkIfFallingOverInStartingSquareAfterJump() {
        homeTeam["H1".playerId].putProne()
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.FOUL),
            *jumpTo(11, 5),
            *jump(1.d6), // Fail in the starting square
            DiceRollResults(1.d6, 1.d6),
            5.d8 // Bounce ball
        )
        assertNull(state.activePlayer)
        homeTeam.assertActive()
        ballCarrier.assertCoordinates(13, 5)
        ballCarrier.assertProne()
        state.singleBall().assertCoordinates(14, 5)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun workOnSuccessfulLeap() {
        ballCarrier.addSkill(SkillType.LEAP)
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.BLITZ),
            PlayerSelected("H1".playerId),
            *leapTo(15, 5),
            Confirm, // Use Leap modifier
            *leap(6.d6),
            Confirm, // Use Fumblerooski
            EndAction
        )
        assertNull(state.activePlayer)
        ballCarrier.assertCoordinates(15, 5)
        state.singleBall().assertCoordinates(13, 5)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun workOnFallingOverAfterLeapToTargetSquare() {
        ballCarrier.addSkill(SkillType.LEAP)
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.PASS),
            *leapTo(15, 5),
            Confirm, // Use Leap modifier
            *leap(3.d6), // Fail, but still end up in target square
            Confirm, // Use Fumblerooski
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        homeTeam.assertActive()
        ballCarrier.assertCoordinates(15, 5)
        ballCarrier.assertProne()
        state.singleBall().assertCoordinates(13, 5)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun doesNotWorkIfFallingOverInStartingSquareAfterLeap() {
        ballCarrier.addSkill(SkillType.LEAP)
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.HAND_OFF),
            *leapTo(15, 4),
            Confirm, // Use Leap modifier
            *leap(1.d6), // Fail in the starting square
            DiceRollResults(1.d6, 1.d6),
            5.d8 // Bounce ball
        )
        assertNull(state.activePlayer)
        homeTeam.assertActive()
        ballCarrier.assertCoordinates(13, 5)
        ballCarrier.assertProne()
        state.singleBall().assertCoordinates(14, 5)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun workOnSuccessfulPogo() {
        ballCarrier.addSkill(SkillType.POGO_STICK)
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.BLITZ),
            PlayerSelected("H1".playerId),
            *pogoTo(15, 5),
            *pogoRoll(6.d6),
            Confirm, // Use Fumblerooski
            EndAction
        )
        assertNull(state.activePlayer)
        ballCarrier.assertCoordinates(15, 5)
        state.singleBall().assertCoordinates(13, 5)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun workOnFallingOverAfterPogoToTargetSquare() {
        ballCarrier.addSkill(SkillType.POGO_STICK)
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.PASS),
            *pogoTo(15, 5),
            *pogoRoll(2.d6), // Fail, but still end up in target square
            Confirm, // Use Fumblerooski
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        homeTeam.assertActive()
        ballCarrier.assertCoordinates(15, 5)
        ballCarrier.assertProne()
        state.singleBall().assertCoordinates(13, 5)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun doesNotWorkIfFallingOverInStartingSquareAfterPogo() {
        ballCarrier.addSkill(SkillType.POGO_STICK)
        controller.rollForward(
            *activatePlayer(ballCarrier, PlayerStandardActionType.HAND_OFF),
            *pogoTo(15, 4),
            *pogoRoll(1.d6), // Fail in the starting square
            DiceRollResults(1.d6, 1.d6),
            5.d8 // Bounce ball
        )
        assertNull(state.activePlayer)
        homeTeam.assertActive()
        ballCarrier.assertCoordinates(13, 5)
        ballCarrier.assertProne()
        state.singleBall().assertCoordinates(14, 5)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }
}
