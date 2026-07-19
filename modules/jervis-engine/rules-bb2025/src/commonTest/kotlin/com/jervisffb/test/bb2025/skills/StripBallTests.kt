package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.bb2025.skills.StripBall
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultKickOffEvent
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [StripBall] skill
 */
class StripBallTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun bounceFromSquareAfterFollowUp() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.STRIP_BALL)
        val defender = homeTeam["H1".playerId]
        val ball = state.singleBall()
        SetBallState.carried(ball, defender).execute(state)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            Confirm, // Use Strip Ball
            5.d8, // Bounce
        )

        assertNull(state.activePlayer)
        assertEquals(PitchCoordinate(12, 5), ball.coordinates)
        assertEquals(BallState.ON_GROUND, ball.state)
    }

    @Test
    fun doesNotWorkAgainstChainPushedPlayer() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.STRIP_BALL)
        val ballPlayer = homeTeam["H10".playerId]
        SetPlayerLocation(homeTeam[4.playerNo], PitchCoordinate(11, 4)).execute(state)
        SetPlayerLocation(ballPlayer, PitchCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[11.playerNo], PitchCoordinate(11, 6)).execute(state)
        val ball = state.singleBall()
        SetBallState.carried(ball, ballPlayer).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.LEFT), // First push
            DirectionSelected(Direction.LEFT), // 2nd push
            Confirm // Follow up
        )
        assertEquals(PitchCoordinate(10, 5), ballPlayer.coordinates)
        assertTrue(ballPlayer.hasBall())
        assertEquals(BallState.CARRIED, ball.state)
    }

    // This behavior was clarified in Designer's Commentary May 2026.
    @Test
    fun doesNotWorkAgainstStandFirm() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.STRIP_BALL)
        val defender = homeTeam["H1".playerId]
        defender.addSkill(SkillType.STAND_FIRM)
        val ball = state.singleBall()
        SetBallState.carried(ball, defender).execute(state)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            Confirm, // Use Stand Firm
        )
        assertNull(state.activePlayer)
        assertTrue(defender.hasBall())
        assertEquals(BallState.CARRIED, ball.state)
    }

    @Test
    fun noScoreIfPushedPlayerIsStripped() {
        setupDefaultGame()
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = PitchSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 2.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null
            ),
            PlayerSelected("A6".playerId) // Give ball to A6
        )

        // Away turn has started. Fake the position of players, so we can push
        // an opponent into the away endzone.
        val attacker = awayTeam["A6".playerId]
        attacker.addSkill(SkillType.STRIP_BALL)
        val defender = homeTeam["H1".playerId]
        SetPlayerLocation(attacker, PitchCoordinate(23, 3)).execute(state)
        SetPlayerLocation(defender, PitchCoordinate(24, 3)).execute(state)
        SetBallState.carried(state.singleBall(), defender).execute(state)

        assertEquals(0, state.awayScore)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.RIGHT),
            Cancel, // Do not follow up
            Confirm, // Use Strip Ball = No touchdown
            4.d8, // Bounce
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(PitchCoordinate(24, 3), state.singleBall().coordinates)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(0, state.awayScore)
        assertEquals(0, state.homeScore)
    }
}
