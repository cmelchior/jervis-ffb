package com.jervisffb.test.bb2025.actions

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
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
import com.jervisffb.engine.rules.common.procedures.FullGame
import com.jervisffb.engine.rules.common.skills.TeamReroll
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.bb2020.advancedHumanTeamAway
import com.jervisffb.test.bb2020.createAdvancedHomeTeam
import com.jervisffb.test.bb2020.createDefaultGameStateBB2020
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.landingRoll
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.rushTo
import com.jervisffb.test.teamSetup
import com.jervisffb.test.utils.SelectTeamReroll
import com.jervisffb.test.utils.hasSkill
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class responsible for testing the Throw Team-mate action as described on
 * page 52-54 in the rulebook.
 */
class ThrowTeamMateActionTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        // Create new teams that have the option to throw a team-mate.
        // Remove Bone Head from the ogre to make testing easier.
        homeTeam = createAdvancedHomeTeam(rules)
        awayTeam = advancedHumanTeamAway(rules)
        homeTeam["H1".playerId].positionSkills.removeFirst()
        awayTeam["A1".playerId].positionSkills.removeFirst()
        state = createDefaultGameStateBB2020(rules, homeTeam, awayTeam)
        homeTeam = state.homeTeam
        awayTeam = state.awayTeam
        controller = GameEngineController(state)
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *arrayOf(*homeSetup(), *awaySetup()),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(17, 5),
                deviate = DiceRollResults(4.d8, 1.d6),
                bounce = 4.d8
            ),
        )
    }

    private fun homeSetup(endSetup: Boolean = true): Array<GameAction> {
        val setup = buildList {
            // Place Hafling behind Ogre, ready to be thrown
            add("H1".playerId to FieldCoordinate(12, 5))
            add("H13".playerId to  FieldCoordinate(11, 5))
            add("H2".playerId to FieldCoordinate(12, 6))
            add("H3".playerId to FieldCoordinate(12, 7))
            add("H4".playerId to FieldCoordinate(12, 8))
            add("H5".playerId to FieldCoordinate(12, 9))
            add("H6".playerId to FieldCoordinate(11, 1))
            add("H7".playerId to FieldCoordinate(10, 1))
            add("H8".playerId to FieldCoordinate(10, 13))
            add("H9".playerId to FieldCoordinate(11, 13))
            add("H10".playerId to  FieldCoordinate(9, 7))
        }
        return teamSetup(setup, endSetup)
    }

    private fun awaySetup(endSetup: Boolean = true): Array<GameAction> {
        val setup= listOf(
            // Place Hafling behind Ogre, ready to be thrown
            "A1".playerId to FieldCoordinate(13, 5),
            "A13".playerId to FieldCoordinate(14, 5),
            "A2".playerId to FieldCoordinate(13, 6),
            "A3".playerId to FieldCoordinate(13, 7),
            "A4".playerId to FieldCoordinate(13, 8),
            "A5".playerId to FieldCoordinate(13, 9),
            "A6".playerId to FieldCoordinate(14, 1),
            "A7".playerId to FieldCoordinate(15, 1),
            "A8".playerId to FieldCoordinate(15, 13),
            "A9".playerId to FieldCoordinate(14, 13),
            "A10".playerId to FieldCoordinate(16, 7),
        )
        return teamSetup(setup, endSetup)
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
    fun thrownPlayerMustHaveStr3OrLower() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
        )
        awayTeam["A13".playerId].strength = 4
        assertFalse(controller.getAvailableActions().contains<SelectPlayer>())
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
    fun passPreventsThrowTeamMate() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(15, 6),
            EndAction
        )
        assertEquals(0, awayTeam.turnData.passActions)
        controller.rollForward(PlayerSelected("A1".playerId))
        assertFalse(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerStandardActionType.THROW_TEAM_MATE })
    }

    @Test
    fun throwPlayerPreventsPass() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 5),
            *qualityRoll(6.d6),
            DiceRollResults(4.d8, 4.d8, 4.d8),
            *landingRoll(6.d6),
            PlayerSelected("A10".playerId),
        )
        assertTrue(controller.getAvailableActions().get<SelectPlayerAction>().actions.none { it.type == PlayerStandardActionType.PASS })
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
            SelectTeamReroll<TeamReroll>(),
            5.d6,
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            *landingRoll(6.d6)
        )
        assertEquals(PlayerState.STANDING, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(8, 4), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun successfulThrow() {
        awayTeam["A1".playerId].passing = 2
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(8, 4), // -1 Short Pass
            2.d6,
            SelectTeamReroll<TeamReroll>(),
            3.d6,
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            *landingRoll(6.d6)
        )
        assertEquals(PlayerState.STANDING, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(5, 4), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun terribleThrow() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(8, 4), // -1 Short Pass, -2 Marks
            *qualityRoll(4.d6), // Should result in a 1 after modifiers
            DiceRollResults(5.d8, 6.d6), // Deviate
            DiceRollResults(2.d8, 2.d8, 2.d8), // Always scatter
            *landingRoll(6.d6)
        )
        assertEquals(PlayerState.STANDING, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(19, 2), awayTeam["A13".playerId].coordinates)
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
        assertEquals(PlayerState.STANDING, awayTeam["A13".playerId].state)
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
            SelectTeamReroll<TeamReroll>(),
            3.d6
        )
        assertEquals(PlayerState.STANDING, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(8, 4), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun successfulLanding() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(8, 4), // -1 Short Pass
            *qualityRoll(5.d6), // Fail pass check
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            3.d6,
            SelectTeamReroll<TeamReroll>(),
            4.d6
        )
        assertEquals(PlayerState.STANDING, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(5, 4), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun terribleLanding() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(8, 4), // -1 Short Pass, -2 Marks
            *qualityRoll(4.d6), // Should result in a 1 after modifiers
            DiceRollResults(5.d8, 6.d6), // Deviate
            DiceRollResults(2.d8, 2.d8, 2.d8), // Always scatter
            4.d6,
            SelectTeamReroll<TeamReroll>(),
            5.d6
        )
        assertEquals(PlayerState.STANDING, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(19, 2), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun fumbledLanding() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(8, 4),
            *qualityRoll(1.d6), // A natural 1 is a fumble.
            3.d8, // Bounce
            3.d6,
            SelectTeamReroll<TeamReroll>(),
            4.d6
        )
        assertEquals(PlayerState.STANDING, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(14, 4), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun markedModifiersOnLanding() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(10, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Always scatter
            3.d6, // 1 marks on landing
            SelectTeamReroll<TeamReroll>(),
            4.d6
        )
        assertEquals(PlayerState.STANDING, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(10, 5), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun fallOverOnFailedLanding() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(10, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Always scatter
            *landingRoll(1.d6),
        )
        assertEquals(PlayerState.FALLEN_OVER, awayTeam["A13".playerId].state)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6),
        )
        assertEquals(PlayerState.PRONE, awayTeam["A13".playerId].state)
    }

    // Landing in an occupied square will knock down the player, then bounce. This will continue
    // until finding an empty square.
    @Test
    fun landInOccupiedSquare() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
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
        assertEquals(PlayerState.KNOCKED_DOWN, awayTeam["A13".playerId].state)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Armour roll for thrown player
        )
        // No turnover, if thrown player was not holding the ball
        assertEquals(awayTeam, state.activeTeam)
        assertNull(state.activePlayer)
        assertEquals(PlayerState.STUNNED, homeTeam["H1".playerId].state)
        assertEquals(PlayerState.PRONE, homeTeam["H2".playerId].state)
        assertEquals(PlayerState.PRONE, awayTeam["A13".playerId].state)
    }

    @Test
    fun fallOverOnCrashLanding() {
        awayTeam["A13".playerId].state = PlayerState.PRONE
        controller.rollForward(
            // Ogre throws prone hafling
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(10, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Initial scatter ends up on an empty space
            4.d8, // Player bounce after landing
        )
        assertEquals(PlayerState.FALLEN_OVER, awayTeam["A13".playerId].state)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Armour roll on thrown player
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(PlayerState.PRONE, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(9, 5), awayTeam["A13".playerId].coordinates)
    }

    // Landing in an Occupied Square takes precedence over Crash Landing
    @Test
    fun crashLandOnAnotherPlayer() {
        awayTeam["A13".playerId].state = PlayerState.PRONE
        controller.rollForward(
            // Ogre throws prone hafling
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 5), // Land on Hafling
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Initial scatter ends up on an empty space
            DiceRollResults(1.d6, 1.d6),
            4.d8, // Player bounce after landing
        )
        assertEquals(PlayerState.KNOCKED_DOWN, awayTeam["A13".playerId].state)
        controller.rollForward(
            DiceRollResults(7.d6, 1.d6), // Armour roll on thrown player
            DiceRollResults(1.d6, 1.d6), // Injury Roll on thrown player
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(PlayerState.STUNNED_OWN_TURN, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(10, 5), awayTeam["A13".playerId].coordinates)
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
        assertEquals(PlayerState.PRONE, awayTeam["A1".playerId].state)
        assertEquals(PlayerState.PRONE, awayTeam["A13".playerId].state)
    }

    @Test
    fun turnoverIfThrownPlayerIsKnockedDownWithBall() {
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
        assertEquals(PlayerState.PRONE, homeTeam["H13".playerId].state)
        assertEquals(PlayerState.PRONE, awayTeam["A13".playerId].state)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(10, 4), state.singleBall().location)
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
        assertEquals(PlayerState.STUNNED, homeTeam["H13".playerId].state)
        assertEquals(PlayerState.PRONE, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(10, 5), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun landInTheCrowd() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 0),
            *qualityRoll(6.d6),
            DiceRollResults(1.d8, 5.d8, 7.d8), // Scatter out of the field
            DiceRollResults(1.d6, 1.d6), // Injury roll
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(PlayerState.RESERVE, awayTeam["A13".playerId].state)
    }

    @Test
    fun bounceIntoTheCrowd() {
        awayTeam["A13".playerId].state = PlayerState.PRONE
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(11, 0),
            *qualityRoll(6.d6),
            DiceRollResults(7.d8, 5.d8, 1.d8), // Scatter to the same field
            3.d8, // Bounce out of the field
            DiceRollResults(1.d6, 1.d6), // Injury roll
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(PlayerState.RESERVE, awayTeam["A13".playerId].state)
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
            DiceRollResults(2.d6, 1.d6), // Throw-in distance
            2.d8 // Bounce ball
        )
        // Turn-over if player with ball is thrown out of the field
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(PlayerState.RESERVE, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(11, 2), state.singleBall().location)
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
        assertEquals(PlayerState.STANDING, thrownPlayer.state)
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
        thrownPlayer.state = PlayerState.PRONE
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected("A13".playerId),
            FieldSquareSelected(14, 0),
            *qualityRoll(6.d6),
            DiceRollResults(7.d8, 5.d8, 1.d8), // Scatter to the same field
            5.d8, // Bounce player
            DiceRollResults(1.d6, 1.d6),
        )
        assertEquals(Availability.AVAILABLE, thrownPlayer.available)
        assertEquals(PlayerState.PRONE, thrownPlayer.state)
        controller.rollForward(
            *activatePlayer("A13", PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STAND_UP),
            *moveTo(14, 0),
            EndAction
        )
        assertEquals(Availability.HAS_ACTIVATED, thrownPlayer.available)
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
        assertEquals(PlayerState.PRONE, homeTeam["H13".playerId].state)
        assertEquals(PlayerState.PRONE, awayTeam["A13".playerId].state)
        assertEquals(FieldCoordinate(17, 5), state.singleBall().location)
    }

    @Test
    fun playerWithBallLandsOnGroundWithBall() {
        // Add a 2nd ball
        val newBall = Ball().apply {
            location = FieldCoordinate(16, 5)
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
            assertEquals(FieldCoordinate(17, 5), ball.location)
        }
    }

    @Test
    fun playerWithBallLandsBadlyOnAnotherBall() {
        // Add a 2nd ball
        val newBall = Ball().apply {
            location = FieldCoordinate(16, 5)
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
            assertEquals(FieldCoordinate(15, 5), ball.location)
        }
        state.balls.last().let { ball ->
            assertEquals(newBall, ball)
            assertEquals(BallState.ON_GROUND, ball.state)
            assertEquals(FieldCoordinate(17, 5), ball.location)
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
            *landingRoll(6.d6) // Land successfully on the end-zone.
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
        assertEquals(FieldCoordinate(1, 7), state.singleBall().location)
    }
}
