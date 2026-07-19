package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.ext.ballId
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.bb2025.skills.DivingTackle
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.bounce
import com.jervisffb.test.catch
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.giveBallToPlayer
import com.jervisffb.test.jump
import com.jervisffb.test.leap
import com.jervisffb.test.moveTo
import com.jervisffb.test.pogoRoll
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [DivingTackle] skill.
 */
class DivingTackleTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun usedAfterOtherModifiersAndRerolls() {
        val mover = awayTeam["A1".playerId]
        val tackler = homeTeam["H1".playerId]
        tackler.addSkill(SkillType.DIVING_TACKLE)
        assertEquals(3, mover.agility)
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(4.d6),
            PlayerSelected(tackler),
            DiceRollResults(1.d6, 1.d6)
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PitchCoordinate(14, 5), mover.coordinates)
        mover.assertProne()
    }

    @Test
    fun useIsOptional() {
        val mover = awayTeam["A1".playerId]
        val tackler = homeTeam["H1".playerId]
        tackler.addSkill(SkillType.DIVING_TACKLE)
        assertEquals(3, mover.agility)
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(4.d6),
            Cancel, // Do not use Diving Tackle
            EndAction
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(PitchCoordinate(14, 5), mover.coordinates)
        mover.assertStanding()
    }

    @Test
    fun onlyOnePlayerCanUseIt() {
        val mover = awayTeam["A1".playerId]
        val tackler1 = homeTeam["H1".playerId]
        tackler1.addSkill(SkillType.DIVING_TACKLE)
        val tackler2 = homeTeam["H1".playerId]
        tackler2.addSkill(SkillType.DIVING_TACKLE)
        assertEquals(3, mover.agility)
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(4.d6),
            PlayerSelected(tackler2),
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PitchCoordinate(14, 5), mover.coordinates)
        mover.assertProne()
    }

    // Leap reduces negative modifiers before Diving Tackle is used, which means
    // that the -2 from Diving is not reduced to -1.
    @Test
    fun leapDoesNotConsiderDivingTackleModifier() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)
        homeTeam["H1".playerId].putProne()
        homeTeam["H2".playerId].addSkill(SkillType.DIVING_TACKLE)
        assertEquals(3, leapingPlayer.agility)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            PitchSquareSelected(11, 4),
            *leap(4.d6), // -1 Modifiers from leaving, no to enter, so should succeed (Leap modifier cannot be used)
            PlayerSelected("H2".playerId), // Use Diving Tackle (causing leap to fail)
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        leapingPlayer.assertProne()
        leapingPlayer.assertCoordinates(11, 4)
    }

    @Test
    fun workOnJump() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        homeTeam["H1".playerId].putProne()
        homeTeam["H2".playerId].addSkill(SkillType.DIVING_TACKLE)
        assertEquals(3, leapingPlayer.agility)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            PitchSquareSelected(11, 4),
            *jump(4.d6), // -1 Modifiers from leaving, no to enter, so should succeed
            PlayerSelected("H2".playerId), // Use Diving Tackle (causing Jump to fail)
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        leapingPlayer.assertProne()
        leapingPlayer.assertCoordinates(11, 4)
    }

    // While Diving Tackle can be used on Pogo, it doesn't apply any modifiers
    @Test
    fun workOnPogo() {
        val pogoPlayer = awayTeam["A1".playerId]
        pogoPlayer.addSkill(SkillType.POGO_STICK)
        homeTeam["H1".playerId].putProne()
        val tacklePlayer = homeTeam["H2".playerId]
        tacklePlayer.addSkill(SkillType.DIVING_TACKLE)
        assertEquals(3, pogoPlayer.agility)
        controller.rollForward(
            *activatePlayer(pogoPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.POGO),
            PitchSquareSelected(11, 4),
            *pogoRoll(3.d6), // No negative modifiers when using Pogo
            PlayerSelected(tacklePlayer), // Use Diving Tackle (modifier is ignore, but player is still prone)
        )
        assertEquals(pogoPlayer, state.activePlayer)
        pogoPlayer.assertStanding()
        pogoPlayer.assertCoordinates(11, 4)
        tacklePlayer.assertProne()
        tacklePlayer.assertCoordinates(13, 5)
    }

    @Test
    fun preventShadowing() {
        val mover = awayTeam["A1".playerId]
        val tackler = homeTeam["H1".playerId]
        tackler.addSkill(SkillType.DIVING_TACKLE)
        val shadower = homeTeam["H2".playerId]
        shadower.addSkill(SkillType.SHADOWING)
        assertEquals(3, mover.agility)
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(5.d6), // Dodge roll must be high enough to succeed even with -2 from Diving Tackle.
            PlayerSelected(tackler), // Use Diving Tackle, prevent Shadowing from being used
        )
        assertEquals(mover,state.activePlayer)
        assertEquals(PitchCoordinate(14, 5), mover.coordinates)
        mover.assertStanding()
    }

    @Test
    fun bounceBallIfTacklerHoldsBall() {
        val mover = awayTeam["A1".playerId]
        val tackler = homeTeam["H1".playerId].apply {
            addSkill(SkillType.DIVING_TACKLE)
            giveBallToPlayer(this)
        }
        assertEquals(3, mover.agility)
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(4.d6),
            PlayerSelected(tackler),
            bounce(5.d8), // Bounce from starting square onto player that dodged
            *catch(6.d6), // Dodging player hasn't failed their dodge yet, so can catch the ball
            DiceRollResults(1.d6, 1.d6),
            bounce(5.d8,) // Dodging player goes prone and ball bounces again
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PitchCoordinate(14, 5), mover.coordinates)
        state.singleBall().assertCoordinates(15, 5)
        mover.assertProne()
    }

    @Test
    fun tacklerLandsOnBallInSquare() {
        state.balls.add(Ball("temp-ball".ballId))
        val mover = awayTeam["A1".playerId].apply {
            addSkill(SkillType.FUMBLEROOSKI)
        }
        val tackler = homeTeam["H1".playerId].apply {
            addSkill(SkillType.DIVING_TACKLE)
        }
        SetBallState.carried(state.balls.first(), mover).execute(state)
        SetBallState.carried(state.balls.last(), tackler).execute(state)
        assertEquals(3, mover.agility)
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            Confirm, // Use Fumblerooski
            *dodge(4.d6),
            PlayerSelected(tackler),
            bounce(5.d8), // Bounce from starting square onto player that dodged
            *catch(6.d6), // Dodging player hasn't failed their dodge yet, so can catch the ball
            bounce(3.d8), // Bounce 2nd ball into empty square
            DiceRollResults(1.d6, 1.d6),
            bounce(5.d8,) // Dodging player goes prone and ball bounces again
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PitchCoordinate(14, 5), mover.coordinates)
        assertEquals(1, state.pitch[15, 5].balls.size)
        assertEquals(1, state.pitch[14, 4].balls.size)
        mover.assertProne()
    }
}
