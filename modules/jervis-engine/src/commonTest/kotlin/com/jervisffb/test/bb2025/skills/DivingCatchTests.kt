package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.DivingCatch
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.catch
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.TeamRerollSelected
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [DivingCatch] skill.
 */
class DivingCatchTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun cannotUseSkillIfLandingOnAnotherPlayer() {
        homeTeam["H1".playerId].addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 5),
            *throwBall(6.d6),
            *catch(6.d6),
        )
        assertNull(state.activePlayer)
        assertTrue(awayTeam["A1".playerId].hasBall())
    }

    @Test
    fun cannotUseSkillIfBallIsBouncing() {
        homeTeam["H1".playerId].addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 5),
            *throwBall(6.d6),
            *catch(1.d6),
            1.d8 // Bounce
        )
        assertNull(state.activePlayer)
        assertEquals(FieldCoordinate(12, 4), state.singleBall().coordinates)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun catchThrowInInAdjacentSquare() {
        val catcher = awayTeam["A7".playerId]
        catcher.addSkill(SkillType.DIVING_CATCH)
        leaveFieldAt(25, 1)
        controller.rollForward(
            2.d3, // Direction
            DiceRollResults(4.d6, 6.d6), // Distance
        )
        assertEquals(BallState.THROW_IN, state.singleBall().state)
        controller.rollForward(
            PlayersSelected(listOf(catcher.id)),
            PlayerSelected(catcher.id),
            *catch(6.d6) // Must catch
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun catchPassInAdjacentSquare() {
        val catcher = awayTeam["A7".playerId]
        catcher.addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(15, 0),
            *throwBall(6.d6),
            PlayersSelected(listOf(catcher.id)),
            PlayerSelected(catcher),
            *catch(6.d6),
        )
        assertNull(state.activePlayer)
        assertTrue(catcher.hasBall())
    }

    @Test
    fun catchScatteredBallInAdjacentSquare() {
        val catcher = awayTeam["A6".playerId]
        catcher.addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 4),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(14, 1),
            *throwBall(3.d6), // Throw Quick pass, needs 4+
            DiceRollResults(2.d8, 8.d8, 1.d8), // Scatter to empty square
            PlayersSelected(listOf(catcher.id)),
            PlayerSelected(catcher.id),
            *catch(4.d6),
        )
        assertNull(state.activePlayer)
        assertTrue(catcher.hasBall())
    }

    @Test
    fun catchHailMaryPassInAdjacentSquare() {
        awayTeam["A10".playerId].addSkill(SkillType.HAIL_MARY_PASS)
        homeTeam["H11".playerId].addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.HAIL_MARY_PASS),
            FieldSquareSelected(3, 7), // H11 stands here
            *throwBall(6.d6),
            DiceRollResults(2.d8, 5.d8, 7.d8), // Lands at [4,7]
            PlayersSelected(listOf("H11".playerId)),
            PlayerSelected("H11".playerId), // Use Diving Catch
            *catch(6.d6, reroll = null),
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        assertTrue(homeTeam["H11".playerId].hasBall())
    }

    @Test
    fun addAddPlusOneOnBouncingBallInTargetSquare() {
        awayTeam["A1".playerId].addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 5),
            *throwBall(6.d6),
            Confirm, // Use Diving Catch
            *catch(1.d6),
            4.d8, // Bounce
            1.d6, // Fail catch
            5.d8, // Bounce back
            Confirm, // Use Diving Catch
            4.d6,
            TeamRerollSelected<RegularTeamReroll>(),
            5.d6  // 3 - 2 (marks) - 1 (bouncing) + 1 (diving catch)
        )
        assertNull(state.activePlayer)
        assertTrue(awayTeam["A1".playerId].hasBall())
    }

    @Test
    fun addPlusOneWhenCatchingBallOnTargetSquare() {
        awayTeam["A1".playerId].addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 5),
            *throwBall(6.d6),
            Confirm, // Use Diving Catch
            3.d6, // 3 - 2 + 1
            TeamRerollSelected<RegularTeamReroll>(),
            4.d6
        )
        assertNull(state.activePlayer)
        assertTrue(awayTeam["A1".playerId].hasBall())
    }

    @Test
    fun allPlayersWithSkillCanAttemptToCatch() {
        awayTeam["A1".playerId].addSkill(SkillType.DIVING_CATCH)
        homeTeam["H1".playerId].addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 4),
            *throwBall(6.d6),
            PlayersSelected(listOf("H1".playerId)),
            PlayersSelected(listOf("A1".playerId)),
            PlayerSelected("H1".playerId),
            1.d6, // Fail catch
            PlayerSelected("A1".playerId),
            *catch(6.d6),
        )
        assertNull(state.activePlayer)
        assertTrue(awayTeam["A1".playerId].hasBall())
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun activeCoachDeterminesCatchOrderIfMultiplePlayers() {
        awayTeam["A1".playerId].addSkill(SkillType.DIVING_CATCH)
        homeTeam["H1".playerId].addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 4),
            *throwBall(6.d6),
        )
        controller.rollForward(
            PlayersSelected(listOf("H1".playerId)),
            PlayersSelected(listOf("A1".playerId)),
        )
        assertEquals(awayTeam, controller.getAvailableActions().team)
        controller.rollForward(
            PlayerSelected("H1".playerId),
            1.d6 // Fail catch
        )
        assertEquals(awayTeam, controller.getAvailableActions().team)
        controller.rollForward(
            PlayerSelected("A1".playerId),
            *catch(1.d6), // Fail catch
            2.d8 // Bounce
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(FieldCoordinate(13, 3), state.singleBall().coordinates)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    // If an opponent has Diving Catch, it is that team that chooses to use the skill.
    // The active coach only gets to choose the order
    @Test
    fun opponentCoachChoosesToUseSkill() {
        awayTeam["A1".playerId].addSkill(SkillType.DIVING_CATCH)
        homeTeam["H1".playerId].addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 4),
            *throwBall(6.d6),
        )
        assertEquals(homeTeam, controller.getAvailableActions().team)
        controller.rollForward(
            PlayersSelected(listOf("H1".playerId)),
        )
        assertEquals(awayTeam, controller.getAvailableActions().team)
        controller.rollForward(
            PlayersSelected(listOf("A1".playerId)),
        )
        assertEquals(awayTeam, controller.getAvailableActions().team)
        controller.rollForward(
            PlayerSelected("H1".playerId),
            1.d6, // Fail catch
            PlayerSelected("A1".playerId),
            *catch(1.d6), // Fail catch
            2.d8 // Bounce
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(FieldCoordinate(13, 3), state.singleBall().coordinates)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }


    @Test
    fun catchKickOffInAdjacentSquare() {
        setupDefaultGame()
        val catcher = awayTeam["A10".playerId]
        catcher.addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                deviate = DiceRollResults(4.d8, 2.d6),
                bounce = null
            ),
            PlayersSelected(listOf(catcher.id)),
            PlayerSelected(catcher),
            6.d6 // No Team re-roll during Kick-off
        )
        assertNull(state.activePlayer)
        assertTrue(catcher.hasBall())
    }

    @Test
    fun kickingTeamCatchesBallResultsInTouchback() {
        setupDefaultGame()
        val catcher = homeTeam["H1".playerId]
        catcher.addSkill(SkillType.DIVING_CATCH)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(14, 4),
                deviate = DiceRollResults(4.d8, 1.d6),
                bounce = null
            ),
            PlayersSelected(listOf(catcher.id)),
            PlayerSelected(catcher),
            6.d6 // No Team re-roll during Kick-off
        )
        assertNull(state.activePlayer)
        assertTrue(catcher.hasBall())
    }

    // Manipulate game flow so the ball leaves the field after bouncing from a throw.
    // This method assumes the exit field is empty.
    private fun leaveFieldAt(x: Int, y: Int) {
        val bounceDirection = when {
            y == 0 -> 2.d8
            y == rules.fieldHeight - 1 -> 7.d8
            x == 0 -> 4.d8
            x == rules.fieldWidth - 1 -> 5.d8
            else -> error("Unsupported coordinate: ($x, $y)")
        }

        // For this we need to use the home team since they can reach the ball
        if (x == 0) {
            SetBallLocation(state.singleBall(), FieldCoordinate(x, y)).execute(state)
            controller.rollForward(
                EndTurn,
                PlayerSelected("H11".playerId),
                PlayerActionSelected(PlayerStandardActionType.MOVE),
                SmartMoveTo(x, y),
                *pickup(1.d6), // Fail pickup
                1.d8, // Bounce out of field
            )
        } else {
            controller.rollForward(
                PlayerSelected("A10".playerId),
                PlayerActionSelected(PlayerStandardActionType.PASS),
                *moveTo(17, 7),
                *pickup(6.d6),
                PassTypeSelected(PassType.STANDARD),
                FieldSquareSelected(x, y), // Throw into the corner
                *throwBall(6.d6),
                bounceDirection,
            )
        }
    }
}

