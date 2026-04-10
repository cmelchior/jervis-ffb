package com.jervisffb.test.bb2025.actions

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.ThrowTeamMate
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.rerolls.TeamReroll
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.landingRoll
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.rushTo
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertFallenOver
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertReserves
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.assertStunned
import com.jervisffb.test.utils.hasSkill
import com.jervisffb.test.utils.makeDistracted
import com.jervisffb.test.utils.putProne
import com.jervisffb.test.utils.putStunned
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class responsible for testing the Throw Team-mate action as described on
 * page 76-79 in the BB2025 rulebook.
 */
class ThrowTeamMateActionTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        setupAndStartThrowTeamMateGame()
    }

    @Test
    fun throwTeammateActionRequiresSkill() {
        controller.rollForward(PlayerSelected("A3".playerId))
        assertTrue(controller.getAvailableActions().get<SelectPlayerAction>().actions.none { it.type == PlayerStandardActionType.THROW_TEAM_MATE })
        controller.rollForward(
            PlayerDeselected("A3".playerId),
            PlayerSelected("A1".playerId),
        )
        assertTrue(awayTeam["A1".playerId].hasSkill<ThrowTeamMate>())
        assertTrue(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerStandardActionType.THROW_TEAM_MATE })
    }

    @Test
    fun thrownPlayerMustHaveRightStuff() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
        )
        val availablePlayersForThrowing = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(1, availablePlayersForThrowing.size)
        assertEquals("A13".playerId, availablePlayersForThrowing.first())
    }

    @Test
    fun thrownPlayerRequiresRightStuff() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
        )
        awayTeam["A13".playerId].strength = 4
        assertTrue(controller.getAvailableActions().contains<SelectPlayer>())
        awayTeam["A13".playerId].strength = 3
        val availablePlayersForThrowing = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(1, availablePlayersForThrowing.size)
    }

    @Test
    fun cancelBeforeMoveOrThrowDoesNotUseAction() {
        assertEquals(1, state.awayTeam.turnData.throwTeamMateActions)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            EndAction
        )
        assertEquals(1, state.awayTeam.turnData.throwTeamMateActions)
        assertEquals(Availability.AVAILABLE, awayTeam["A1".playerId].available)
    }

    @Test
    fun moveUsesAction() {
        assertEquals(1, state.awayTeam.turnData.throwTeamMateActions)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            EndAction
        )
        assertEquals(0, state.awayTeam.turnData.throwTeamMateActions)
        assertNull(state.activePlayer)
    }

    @Test
    fun actionEndsAfterThrow() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 5),
            *qualityRoll(6.d6),
            DiceRollResults(4.d8, 4.d8, 4.d8),
            *landingRoll(6.d6)
        )
        assertEquals(0, state.awayTeam.turnData.throwTeamMateActions)
        assertEquals(Availability.HAS_ACTIVATED, awayTeam["A1".playerId].available)
    }

    @Test
    fun combinePassAndThrowTeammate() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(15, 6),
            EndAction
        )
        assertEquals(0, awayTeam.turnData.passActions)
        controller.rollForward(PlayerSelected("A1".playerId))
        assertTrue(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerStandardActionType.THROW_TEAM_MATE })
    }

    @Test
    fun throwPlayerDoesNotPreventPass() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 5),
            *qualityRoll(6.d6),
            DiceRollResults(4.d8, 4.d8, 4.d8),
            *landingRoll(6.d6),
            PlayerSelected("A10".playerId),
        )
        assertTrue(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerStandardActionType.PASS })
    }

    @Test
    fun canCancelThrow() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            Cancel,
            EndAction
        )
        assertEquals(Availability.AVAILABLE,  awayTeam["A1".playerId].available)
        assertEquals(1, awayTeam.turnData.throwTeamMateActions)
    }

    @Test
    fun canThrowAfterAllMovesAreUsed() {
        awayTeam["A1".playerId].movesLeft = 0
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *rushTo(14, 4),
            *dodge(),
            *rushTo(15, 4),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 5),
            *qualityRoll(6.d6),
            DiceRollResults(4.d8, 4.d8, 4.d8),
            *landingRoll(6.d6)
        )
        assertEquals(0, awayTeam.turnData.throwTeamMateActions)
        assertEquals(Availability.HAS_ACTIVATED,  awayTeam["A1".playerId].available)
    }

    @Test
    fun superbThrow() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 4), // Quick Pass - No modifiers
            4.d6,
            TeamRerollSelected<TeamReroll>(),
            5.d6,
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            *landingRoll(6.d6)
        )
        awayTeam["A13".playerId].assertStanding()
        assertEquals(FieldCoordinate(8, 4), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun subparThrow() {
        awayTeam["A1".playerId].passing = 2
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(8, 4), // -1 Short Pass
            2.d6,
            TeamRerollSelected<TeamReroll>(),
            3.d6,
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            *landingRoll(6.d6)
        )
        awayTeam["A13".playerId].assertStanding()
        assertEquals(FieldCoordinate(5, 4), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun fumbledThrow() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(8, 4),
            *qualityRoll(1.d6), // A natural 1 is a fumble.
            3.d8, // Bounce
            *landingRoll(6.d6)
        )
        awayTeam["A13".playerId].assertStanding()
        assertEquals(FieldCoordinate(14, 4), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun superbLanding() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 4),
            *qualityRoll(6.d6),
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            2.d6, // No modifiers on landing
            TeamRerollSelected<TeamReroll>(),
            3.d6
        )
        awayTeam["A13".playerId].assertStanding()
        assertEquals(FieldCoordinate(8, 4), awayTeam["A13".playerId].coordinates)
    }

    // In BB2025, it is not allowed to pick up the ball when successfully landing on it. This is a rule change from BB2020.
    @Test
    fun superbLanding_onBall() {
        SetBallLocation(state.singleBall(), FieldCoordinate(8, 4)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 4),
            *qualityRoll(6.d6),
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            *landingRoll(6.d6),
            4.d8 // Ball bounce
        )
        awayTeam["A13".playerId].assertStanding()
        state.singleBall().assertCoordinates(7, 4)
    }

    @Test
    fun failLanding_bounceIfLandingOnBall() {
        SetBallLocation(state.singleBall(), FieldCoordinate(8, 4)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 4),
            *qualityRoll(6.d6),
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            *landingRoll(1.d6),
            DiceRollResults(1.d6, 1.d6), // Fail armour roll
            4.d8 // Ball bounce
        )
        awayTeam["A13".playerId].assertProne()
        state.singleBall().assertCoordinates(7, 4)
    }

    @Test
    fun subparLanding() {
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(8, 4), // -1 Short Pass
            *qualityRoll(5.d6), // Fail pass check
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            3.d6,
            TeamRerollSelected<TeamReroll>(),
            4.d6
        )
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(5, 4)
    }

    @Test
    fun fumbledLanding() {
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(8, 4),
            *qualityRoll(1.d6), // A natural 1 is a fumble.
            3.d8, // Bounce
            3.d6,
            TeamRerollSelected<TeamReroll>(),
            4.d6
        )
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(14, 4)
    }

    @Test
    fun markedModifiersOnLanding() {
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(10, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Always scatter
            3.d6, // 1 marks on landing
            TeamRerollSelected<TeamReroll>(),
            4.d6
        )
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(10, 5)
    }

    @Test
    fun fallOverOnFailedLanding() {
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(10, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Always scatter
            *landingRoll(1.d6),
        )
        thrownPlayer.assertFallenOver()
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6),
        )
        thrownPlayer.assertProne()
    }

    // Landing in an occupied square will knock down the player, then bounce. This will continue
    // until finding an empty square.
    @Test
    fun landInOccupiedSquare() {
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(12, 5),
            *qualityRoll(6.d6),
            DiceRollResults(1.d8, 7.d8, 5.d8), // Hit target square
            DiceRollResults(6.d6, 6.d6), // Armour roll
            DiceRollResults(1.d6, 1.d6), // Stunned
            7.d8, // Bounce to another player
            DiceRollResults(1.d6, 1.d6), // Armour roll
            4.d8, // Bounce to empty square
        )
        // Thrown player is knocked down on landing after hitting another player
        thrownPlayer.assertFallenOver()
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Armour roll for thrown player
        )
        // No turnover, if thrown player was not holding the ball
        assertEquals(awayTeam, state.activeTeam)
        assertNull(state.activePlayer)
        homeTeam["H1".playerId].assertStunned()
        homeTeam["H2".playerId].assertProne()
        thrownPlayer.assertProne()
    }

    @Test
    fun throwPronePlayer() {
        val thrownPlayer = awayTeam["A13".playerId].apply {
            putProne()
        }
        controller.rollForward(
            // Ogre throws prone Hafling
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(10, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Initial scatter ends up on an empty space
        )
        thrownPlayer.assertFallenOver()
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Armour roll on thrown player
        )
        assertEquals(awayTeam, state.activeTeam)
        thrownPlayer.assertProne()
        thrownPlayer.assertCoordinates(10, 5)
    }

    // Landing in an Occupied Square takes precedence over Crash Landing
    @Test
    fun crashLandOnAnotherPlayer() {
        val thrownPlayer = awayTeam["A13".playerId].apply {
            makeDistracted()
        }
        controller.rollForward(
            // Ogre throws prone hafling
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(11, 5), // Land on Hafling
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Initial scatter ends up on an empty space
            DiceRollResults(1.d6, 1.d6),
            4.d8, // Player bounce after landing
        )
        thrownPlayer.assertFallenOver()
        controller.rollForward(
            DiceRollResults(6.d6, 2.d6), // Armour roll on thrown player
            DiceRollResults(1.d6, 1.d6), // Injury Roll on thrown player
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(PlayerState.STUNNED_OWN_TURN, thrownPlayer.state)
        thrownPlayer.assertCoordinates(10, 5)
    }

    @Test
    fun turnoverIfLandingOnPlayerFromOwnTeam() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(12, 5),
            *qualityRoll(6.d6),
            DiceRollResults(5.d8, 5.d8, 4.d8), // Hit thrower
            DiceRollResults(1.d6, 1.d6), // Armour roll
            2.d8, // Bounce
            DiceRollResults(1.d6, 1.d6), // Armour roll
        )
        assertEquals(homeTeam, state.activeTeam)
        assertNull(state.activePlayer)
        awayTeam["A1".playerId].assertProne()
        awayTeam["A13".playerId].assertProne()
    }

    @Test
    fun turnoverIfThrownPlayerFallsOverWithBall() {
        controller.rollForward(
            // Hafling picks up ball
            *activatePlayer("A13", PlayerStandardActionType.MOVE),
            *moveTo(15, 5),
            *pickup(),
            *moveTo(14, 5),
            EndAction,
            // Ogre throws hafling on top of another player
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Land on top of Home hafling
            DiceRollResults(1.d6, 1.d6), // Armour roll for player in landing square
            4.d8, // Bounce player
            DiceRollResults(1.d6, 1.d6), // Armour roll on thrown player
            2.d8 // Bounce ball
        )
        assertEquals(homeTeam, state.activeTeam)
        homeTeam["H13".playerId].assertProne()
        awayTeam["A13".playerId].assertProne()
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(10, 4), state.singleBall().coordinates)
    }

    @Test
    fun landOnPronePlayer() {
        homeTeam["H13".playerId].state = PlayerState.PRONE
        controller.rollForward(
            // Ogre throws hafling on top of the prone player
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Land on top of Home hafling
            DiceRollResults(6.d6, 6.d6), // Armour roll
            DiceRollResults(1.d6, 1.d6), // Injury roll
            4.d8, // Bounce player
            DiceRollResults(1.d6, 1.d6), // Armour roll on thrown player
        )
        assertEquals(awayTeam, state.activeTeam)
        homeTeam["H13".playerId].assertStunned()
        awayTeam["A13".playerId].assertProne()
        assertEquals(FieldCoordinate(10, 5), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun turnoverIfLandingInTheCrowd() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 0),
            *qualityRoll(6.d6),
            DiceRollResults(1.d8, 5.d8, 7.d8), // Scatter out of the field
            DiceRollResults(1.d6, 1.d6), // Injury roll
        )
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PlayerState.RESERVE, awayTeam["A13".playerId].state)
    }

    @Test
    fun bounceIntoTheCrowd() {
        val thrownPlayer = awayTeam["A13".playerId]
        SetPlayerLocation(homeTeam["H10".playerId], FieldCoordinate(11, 0)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(11, 0),
            *qualityRoll(6.d6),
            DiceRollResults(7.d8, 5.d8, 1.d8), // Scatter to the same field
            DiceRollResults(1.d6, 1.d6), // Hit H10 and roll for AV
            3.d8, // Bounce out-of-bounds
            DiceRollResults(1.d6, 1.d6), // Injury roll
        )
        assertEquals(homeTeam, state.activeTeam)
        thrownPlayer.assertReserves()
    }

    @Test
    fun landInTheCrowdWithBall() {
        controller.rollForward(
            // Hafling picks up ball
            *activatePlayer("A13", PlayerStandardActionType.MOVE),
            *moveTo(15, 5),
            *pickup(),
            *moveTo(14, 5),
            EndAction,
            // Ogre throws hafling out of the field
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 0),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 2.d8, 2.d8),
            DiceRollResults(1.d6, 1.d6), // Armour roll
            2.d3, // Throw-in direction
            DiceRollResults(3.d6, 1.d6), // Throw-in distance
            2.d8 // Bounce ball
        )
        // Turn-over if player with ball is thrown out of the field
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PlayerState.RESERVE, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(11, 2), state.singleBall().coordinates)
    }

    @Test
    fun thrownPlayerCanMoveIfNotActivated() {
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(14, 0),
            *qualityRoll(6.d6),
            DiceRollResults(7.d8, 5.d8, 1.d8), // Scatter to the same field
            *landingRoll(6.d6)
        )
        assertEquals(Availability.AVAILABLE, thrownPlayer.available)
        thrownPlayer.assertStanding()
        controller.rollForward(
            *activatePlayer("A13", PlayerStandardActionType.MOVE),
            *moveTo(15, 0),
            EndAction
        )
        assertEquals(Availability.HAS_ACTIVATED, thrownPlayer.available)
    }

    @Test
    fun thrownPronePlayerCanMoveIfNotActivated() {
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.putProne()
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(14, 0),
            *qualityRoll(6.d6),
            DiceRollResults(7.d8, 5.d8, 1.d8), // Scatter to the same square
            DiceRollResults(1.d6, 1.d6), // AV roll
        )
        assertEquals(Availability.AVAILABLE, thrownPlayer.available)
        thrownPlayer.assertProne()
        controller.rollForward(
            *activatePlayer(thrownPlayer, PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STAND_UP),
            *moveTo(13, 0),
            EndAction
        )
        assertEquals(Availability.HAS_ACTIVATED, thrownPlayer.available)
    }

    @Test
    fun throwDistractedPlayer() {
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.makeDistracted()
        controller.rollForward(
            // Ogre throws prone Hafling
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(10, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Initial scatter ends up on an empty space
        )
        thrownPlayer.assertFallenOver()
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Armour roll on thrown player
        )
        assertEquals(awayTeam, state.activeTeam)
        thrownPlayer.assertProne()
        thrownPlayer.assertCoordinates(10, 5)
    }

    @Test
    fun throwStunnedPlayer() {
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.putStunned()
        controller.rollForward(
            // Ogre throws prone Hafling
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(10, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Initial scatter ends up on an empty space
        )
        thrownPlayer.assertFallenOver()
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // AV roll on thrown player
        )
        assertEquals(awayTeam, state.activeTeam)
        thrownPlayer.assertStunned() // Player stays stunned if AV isn't broken
        thrownPlayer.assertCoordinates(10, 5)
    }

    @Test
    fun landsOnPlayerWithBallKnocksItLoose() {
        controller.rollForward(
            // Hafling picks up the ball
            *activatePlayer("A13", PlayerStandardActionType.MOVE),
            *moveTo(15, 5),
            *pickup(),
            EndAction,
            EndTurn,
            // Ogre throws Home hafling on top of Away hafling with the ball
            *activatePlayer("H1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("H13".playerId),
            FieldSquareSelected(15, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 5.d8, 6.d8), // Land on top of Away hafling
            DiceRollResults(1.d6, 1.d6), // Armour roll
            5.d8, // Bounce ball
            5.d8, // Bounce player, landing on the ball again
            DiceRollResults(1.d6, 1.d6), // Armour roll on thrown player
            5.d8 // Bounce ball
        )
        assertEquals(homeTeam, state.activeTeam)
        homeTeam["H13".playerId].assertProne()
        awayTeam["A13".playerId].assertProne()
        assertEquals(FieldCoordinate(17, 5), state.singleBall().coordinates)
    }

    @Test
    fun playerWithBallLandsOnGroundWithBall() {
        // Add a 2nd ball
        val newBall = Ball().apply {
            coordinates = FieldCoordinate(16, 5)
        }
        state.balls.add(newBall)
        state.field[16, 5].balls.add(newBall)
        // Throw a player with a ball on top of the other ball
        controller.rollForward(
            // Hafling picks up ball
            *activatePlayer("A13", PlayerStandardActionType.MOVE),
            *moveTo(15, 5),
            *pickup(),
            *moveTo(14, 5),
            EndAction,
            // Ogre throws hafling on top of another player
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(16, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 5.d8, 6.d8), // Scatter back to starting point
            *landingRoll(6.d6),
            5.d8, // Bounce ball already in square
        )
        state.balls.first().let { ball ->
            assertEquals(BallState.CARRIED, ball.state)
            assertEquals(awayTeam["A13".playerId], ball.carriedBy)
        }
        state.balls.last().let { ball ->
            assertEquals(newBall, ball)
            assertEquals(BallState.ON_GROUND, ball.state)
            assertEquals(FieldCoordinate(17, 5), ball.coordinates)
        }
    }

    @Test
    fun playerWithBallLandsBadlyOnAnotherBall() {
        // Add a 2nd ball
        val newBall = Ball().apply {
            coordinates = FieldCoordinate(16, 5)
        }
        state.balls.add(newBall)
        state.field[16, 5].balls.add(newBall)
        // Throw a player with a ball on top of the other ball
        controller.rollForward(
            // Hafling picks up first ball
            *activatePlayer("A13", PlayerStandardActionType.MOVE),
            *moveTo(15, 5),
            *pickup(),
            *moveTo(14, 5),
            EndAction,
            // Ogre throws hafling on top of another player
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(16, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 5.d8, 6.d8), // Scatter back to the starting point
            *landingRoll(1.d6), // Fail landing on ball
            DiceRollResults(6.d6, 6.d6),
            DiceRollResults(6.d6, 6.d6),
            16.d16, // Thrown player is killed
            Cancel, // Do not use apothecary, thrown player leaves the field
            4.d8, // Bounce ball carried by player
            5.d8, // Bounce ball already in square
        )
        state.balls.first().let { ball ->
            assertEquals(BallState.ON_GROUND, ball.state)
            assertEquals(FieldCoordinate(15, 5), ball.coordinates)
        }
        state.balls.last().let { ball ->
            assertEquals(newBall, ball)
            assertEquals(BallState.ON_GROUND, ball.state)
            assertEquals(FieldCoordinate(17, 5), ball.coordinates)
        }
    }

    @Test
    fun landInEndZoneTriggersTouchdown() {
        // Prepare the field by moving players and ball closer to the end-zone
        SetPlayerLocation(awayTeam["A13".playerId], FieldCoordinate(5, 7)).execute(state)
        SetPlayerLocation(awayTeam["A1".playerId], FieldCoordinate(4, 7)).execute(state)
        SetBallLocation(state.singleBall(), FieldCoordinate(6, 7)).execute(state)

        controller.rollForward(
            *activatePlayer("A13", PlayerStandardActionType.MOVE),
            *moveTo(6, 7),
            *pickup(),
            *moveTo(5, 7),
            EndAction,
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(0, 7),
            *qualityRoll(6.d6),
            DiceRollResults(3.d8, 7.d8, 4.d8), // Scatter back to the starting point.
            *landingRoll(6.d6), // Land successfully on the end-zone.
            Confirm // Accept Touchdown
        )
        assertEquals(1, state.awayScore)
    }

    @Test
    fun failLandingInEndZoneWithBallDoesNotTriggersTouchdown() {
        // Prepare the field by moving players and ball closer to the end-zone
        SetPlayerLocation(awayTeam["A13".playerId], FieldCoordinate(5, 7)).execute(state)
        SetPlayerLocation(awayTeam["A1".playerId], FieldCoordinate(4, 7)).execute(state)
        SetBallLocation(state.singleBall(), FieldCoordinate(6, 7)).execute(state)

        controller.rollForward(
            *activatePlayer("A13", PlayerStandardActionType.MOVE),
            *moveTo(6, 7),
            *pickup(),
            *moveTo(5, 7),
            EndAction,
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(0, 7),
            *qualityRoll(6.d6),
            DiceRollResults(3.d8, 7.d8, 4.d8), // Scatter back to the starting point.
            *landingRoll(1.d6), // Land successfully on the end-zone.
            DiceRollResults(1.d6, 1.d6),
            5.d8 // Bounce ball
        )
        assertEquals(0, state.awayScore)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(1, 7), state.singleBall().coordinates)
    }
}
