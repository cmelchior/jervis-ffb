package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.blitzBlock
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.jump
import com.jervisffb.test.landingRoll
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.rushRoll
import com.jervisffb.test.standardBlock
import com.jervisffb.test.steadyFootingRoll
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [SteadyFootingTests] skill.
 */
class SteadyFootingTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun preventPlayerDown() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.STEADY_FOOTING)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 1.dblock),
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
    }

    @Test
    fun preventBothDown() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.STEADY_FOOTING)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 2.dblock),
        )
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6),
        )
        assertEquals(PlayerState.PRONE, defender.state)
        assertEquals(PlayerState.STANDING, attacker.state)
        controller.rollForward(
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6),
        )
        assertEquals(PlayerState.PRONE, defender.state)
        assertEquals(PlayerState.STANDING, attacker.state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun preventPow() {
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.STEADY_FOOTING)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.DOWN_LEFT),
            Cancel,
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6, reroll = null),
        )
        assertEquals(FieldCoordinate(11, 6), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
    }

    @Test
    fun canContinueBlitz() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.STEADY_FOOTING)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLITZ),
            PlayerSelected(defender.id), // Select target of blitz
            *blitzBlock("H1", 1.dblock),
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6),
            *moveTo(13, 4),
            *dodge(6.d6),
            EndAction
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(0, attacker.team.turnData.blitzActions)
        assertEquals(FieldCoordinate(13, 4), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
    }

    @Test
    fun keepBallOnPow() {
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.STEADY_FOOTING)
        SetBallState.carried(state.singleBall(), defender).execute(state)
        assertTrue(defender.hasBall())
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.DOWN_LEFT),
            Cancel,
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6, reroll = null)
        )
        assertEquals(FieldCoordinate(11, 6), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
        assertTrue(defender.hasBall())
    }

    @Test
    fun keepBallOnRush() {
        val player = awayTeam["A8".playerId]
        player.addSkill(SkillType.STEADY_FOOTING)
        controller.rollForward(
            PlayerSelected("A8".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            *pickup(6.d6),
            *moveTo(18, 7),
            *moveTo(19, 7),
            *rushRoll(1.d6), // Fail Rush
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6),
            *moveTo(20, 7),
            *rushRoll(6.d6),
            EndAction
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(PlayerState.STANDING, player.state)
        assertTrue(player.hasBall())
    }

    @Test
    fun notAvailableIfDistracted_knockedDown() {
        val defender = state.getPlayerById("H1".playerId)
        defender.apply {
            addSkill(SkillType.STEADY_FOOTING)
            hasTackleZones = false
        }
        assertTrue(rules.isDistracted(defender))
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.DOWN_LEFT),
            Cancel,
            DiceRollResults(1.d6, 1.d6),
        )
        assertEquals(FieldCoordinate(11, 6), defender.location)
        assertEquals(PlayerState.PRONE, defender.state)
    }

    @Test
    fun preventJump_targetSquare() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A1".playerId)
        jumpingPlayer.addSkill(SkillType.STEADY_FOOTING)
        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 5),
            *jump(3.d6), // 1 Marked Modifiers from leaving/entering
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6),
        )
        assertEquals(jumpingPlayer, state.activePlayer)
        assertEquals(PlayerState.STANDING, jumpingPlayer.state)
        assertEquals(FieldCoordinate(11, 5), jumpingPlayer.location)
    }

    @Test
    fun preventJump_startingSquare() {
        state.getPlayerById("H1".playerId).state = PlayerState.PRONE
        val jumpingPlayer = state.getPlayerById("A1".playerId)
        jumpingPlayer.addSkill(SkillType.STEADY_FOOTING)
        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 5),
            *jump(1.d6),
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6),
        )
        assertEquals(jumpingPlayer, state.activePlayer)
        assertEquals(PlayerState.STANDING, jumpingPlayer.state)
        assertEquals(FieldCoordinate(13, 5), jumpingPlayer.location)
    }

    @Test
    fun preventFailedLandingAfterBeingThrown() {
        setupAndStartThrowTeamMateGame()
        awayTeam["A13".playerId].addSkill(SkillType.STEADY_FOOTING)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(10, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Always scatter
            *landingRoll(1.d6),
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6),
        )
        assertEquals(PlayerState.STANDING, awayTeam["A13".playerId].state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun preventCrashLandingAfterLandingOnAnotherPlayer() {
        setupAndStartThrowTeamMateGame()
        awayTeam["A13".playerId].addSkill(SkillType.STEADY_FOOTING)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(12, 5),
            *qualityRoll(6.d6),
            DiceRollResults(1.d8, 7.d8, 5.d8), // Hit target square
            DiceRollResults(6.d6, 6.d6), // Armour roll
            DiceRollResults(1.d6, 1.d6), // Stunned
            2.d8, // Bounce to empty square
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6),
        )
        assertEquals(awayTeam, state.activeTeam)
        assertNull(state.activePlayer)
        assertEquals(PlayerState.STUNNED, homeTeam["H1".playerId].state)
        assertEquals(PlayerState.STANDING, awayTeam["A13".playerId].state)
    }

    @Test
    fun notAvailableIfDistracted_fallingOver() {
        setupAndStartThrowTeamMateGame()
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.apply {
            addSkill(SkillType.STEADY_FOOTING)
            hasTackleZones = false
        }
        assertTrue(rules.isDistracted(awayTeam["A13".playerId]))
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(12, 5),
            *qualityRoll(6.d6),
            DiceRollResults(1.d8, 7.d8, 5.d8), // Hit target square
            DiceRollResults(6.d6, 6.d6), // Armour roll
            DiceRollResults(1.d6, 1.d6), // Stunned
            2.d8, // Bounce to empty square
        )
        assertEquals(PlayerState.FALLEN_OVER, thrownPlayer.state)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6),
        )
        assertEquals(awayTeam, state.activeTeam)
        assertNull(state.activePlayer)
        assertEquals(PlayerState.STUNNED, homeTeam["H1".playerId].state)
        assertEquals(PlayerState.PRONE, thrownPlayer.state)
    }

    @Test
    fun preventFailedDodge() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.STEADY_FOOTING)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(12, 4),
            *dodge(1.d6),
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6),
        )
        assertEquals(player, state.activePlayer)
        assertEquals(PlayerState.STANDING, player.state)
        assertEquals(FieldCoordinate(12, 4), player.location)
    }
}
