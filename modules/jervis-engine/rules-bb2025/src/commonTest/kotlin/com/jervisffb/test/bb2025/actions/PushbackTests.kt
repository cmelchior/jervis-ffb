package com.jervisffb.test.bb2025.actions

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.utils.singleInstanceOf
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.blitzBlock
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.assertStunned
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * This class is responsible for testing pushbacks as defined on page 58 and 59 in the rulebook.
 * Pushbacks will only happen as part of a Block or Blitz action, but are tested separately here.
 */
class PushbackTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun pushDirection_corner() {
        controller.rollForward(
            *activatePlayer("A2", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
        )

        val actions = controller.getAvailableActions().actions
        val availableDirections = actions.singleInstanceOf<SelectDirection>()
        assertEquals(3, availableDirections.directions.size)
        val expectedTargets = setOf(
            PitchCoordinate(11, 5),
            PitchCoordinate(11, 4),
            PitchCoordinate(12, 4),
        )
        availableDirections.directions.forEach {
            val target = availableDirections.origin.move(it, 1)
            assertTrue(expectedTargets.contains(target), "Unexpected target: $target")
        }
    }

    @Test
    fun pushDirection_line() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
        )
        val actions = controller.getAvailableActions().actions
        val availableDirections = actions.singleInstanceOf<SelectDirection>()
        assertEquals(3, availableDirections.directions.size)
        val expectedTargets = setOf(
            PitchCoordinate(11, 4),
            PitchCoordinate(11, 5),
            PitchCoordinate(11, 6),
        )
        availableDirections.directions.forEach {
            val target = availableDirections.origin.move(it, 1)
            assertTrue(expectedTargets.contains(target), "Unexpected target: $target")
        }
    }

    // If some of the normal push options are occupied, they are removed
    // from the list of options unless all options are occupied
    @Test
    fun onlyPushIntoEmptySquares() {
        SetPlayerLocation(homeTeam[5.playerNo], PitchCoordinate(11, 5)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
        )
        val actions = controller.getAvailableActions().actions
        val availableDirections = actions.singleInstanceOf<SelectDirection>()
        assertEquals(2, availableDirections.directions.size)
        val expectedTargets = setOf(
            PitchCoordinate(11, 4),
            PitchCoordinate(11, 6),
        )
        availableDirections.directions.forEach {
            val target = availableDirections.origin.move(it, 1)
            assertTrue(expectedTargets.contains(target), "Unexpected target: $target")
        }
    }

    @Test
    fun followUp() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.LEFT),
            Confirm // Follow up
        )
        assertEquals(PitchCoordinate(12, 5), awayTeam["A1".playerId].coordinates)
        assertEquals(PitchCoordinate(11, 5), homeTeam["H1".playerId].coordinates)
        assertNull(state.activePlayer)
    }

    @Test
    fun pushLeavesPlayerStanding() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.LEFT),
            Confirm // Follow up
        )
        assertEquals(PitchCoordinate(12, 5), awayTeam["A1".playerId].coordinates)
        assertEquals(PitchCoordinate(11, 5), homeTeam["H1".playerId].coordinates)
        homeTeam["H1".playerId].assertStanding()
        assertNull(state.activePlayer)
    }

    @Test
    fun pushIntoBallBouncesIt() {
        // Place ball where it will bounce when H1 is pushed into it.
        SetBallLocation(state.singleBall(), PitchCoordinate(11, 5)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.LEFT),
            Confirm, // Follow up
            2.d8 // Bounce
        )
        assertEquals(PitchCoordinate(11, 4), state.singleBall().coordinates)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun followUpBeforeBounce() {
        // Place ball where it will bounce when H1 is pushed into it.
        SetBallLocation(state.singleBall(), PitchCoordinate(11, 5)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.LEFT),
        )
        assertEquals(PitchCoordinate(11, 5), state.singleBall().coordinates)
        assertEquals(BallState.BOUNCING, state.singleBall().state)
        controller.rollForward(
            Confirm, // Follow up
            2.d8 // Bounce
        )
        assertEquals(PitchCoordinate(11, 4), state.singleBall().coordinates)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun cannotPushIntoCrowdIfEmptySquares() {
        SetPlayerLocation(homeTeam[1.playerNo], PitchCoordinate(12, 0)).execute(state)
        SetPlayerLocation(awayTeam[1.playerNo], PitchCoordinate(13, 0)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
        )
        val actions = controller.getAvailableActions().actions
        val availableDirections = actions.singleInstanceOf<SelectDirection>()
        assertEquals(1, availableDirections.directions.size)
        val expectedTargets = setOf(
            PitchCoordinate(11, 0),
        )
        availableDirections.directions.forEach {
            val target = availableDirections.origin.move(it, 1)
            assertTrue(expectedTargets.contains(target), "Unexpected target: $target")
        }
    }

    @Test
    fun pushIntoCrowdIfNoOtherOptions() {
        SetPlayerLocation(homeTeam[8.playerNo], PitchCoordinate(1, 14)).execute(state)
        SetPlayerLocation(awayTeam[1.playerNo], PitchCoordinate(1, 13)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H8", 4.dblock),
        )
        val actions = controller.getAvailableActions().actions
        val availableDirections = actions.singleInstanceOf<SelectDirection>()
        assertEquals(3, availableDirections.directions.size)
        assertEquals(
            PitchCoordinate(0, 15),
            availableDirections.origin.move(availableDirections.directions.first(), 1)
        )
        controller.rollForward(
            DirectionSelected(Direction.DOWN),
            Confirm, // Follow up
            DiceRollResults(1.d6, 1.d6), // Injury roll
        )
        assertEquals(PitchCoordinate(1, 14), awayTeam["A1".playerId].coordinates)
        homeTeam["H8".playerId].let {
            assertEquals(Dogout, it.location)
            assertEquals(PlayerDogoutState.RESERVE, it.state)
        }
    }

    @Test
    fun chainPush() {
        SetPlayerLocation(homeTeam[4.playerNo], PitchCoordinate(11, 4)).execute(state)
        SetPlayerLocation(homeTeam[10.playerNo], PitchCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[11.playerNo], PitchCoordinate(11, 6)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.LEFT), // First push
            DirectionSelected(Direction.UP_LEFT), // 2nd push
            Confirm // Follow up
        )
        assertEquals(PitchCoordinate(12, 5), awayTeam["A1".playerId].coordinates)
        assertEquals(PitchCoordinate(11, 5), homeTeam["H1".playerId].coordinates)
        assertEquals(PitchCoordinate(10, 4), homeTeam["H10".playerId].coordinates)
    }

    @Test
    fun chainPushPronePlayer() {
        SetPlayerLocation(homeTeam[4.playerNo], PitchCoordinate(11, 4)).execute(state)
        SetPlayerLocation(homeTeam[10.playerNo], PitchCoordinate(11, 5)).execute(state)
        homeTeam[10.playerNo].apply {
            state = PlayerPitchState.STUNNED
            hasTackleZones = false
        }
        SetPlayerLocation(homeTeam[11.playerNo], PitchCoordinate(11, 6)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.LEFT), // First push
            DirectionSelected(Direction.UP_LEFT), // 2nd push
            Confirm // Follow up
        )

        assertEquals(PitchCoordinate(12, 5), awayTeam["A1".playerId].coordinates)
        assertEquals(PitchCoordinate(11, 5), homeTeam["H1".playerId].coordinates)
        assertEquals(PitchCoordinate(10, 4), homeTeam["H10".playerId].coordinates)
        homeTeam[10.playerNo].assertStunned()
    }

    @Test
    fun pushOpponentPlayerWithBallIntoCrowd() {
        SetPlayerLocation(homeTeam[4.playerNo], PitchCoordinate(15, 0)).execute(state)
        SetBallState.carried(state.singleBall(), homeTeam[4.playerNo]).execute(state)
        controller.rollForward(
            *activatePlayer("A7", PlayerStandardActionType.BLOCK),
            *standardBlock("H4", 4.dblock),
            DirectionSelected(Direction.UP),
            Confirm, // Follow up
            DiceRollResults(1.d6, 1.d6), // Crowd Injury roll
            2.d3, // Throw-in direction
            DiceRollResults(1.d6, 2.d6), // Throw-in distance
            7.d8 // Bounce
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(PitchCoordinate(15, 3), state.singleBall().coordinates)
        assertEquals(Dogout, homeTeam[4.playerNo].location)
        assertEquals(PitchCoordinate(15, 0), awayTeam[7.playerNo].coordinates)
    }

    @Test
    fun pushOpponentPlayerWithBallIntoCrowdUsingBlitz() {
        SetPlayerLocation(homeTeam[4.playerNo], PitchCoordinate(20, 0)).execute(state)
        SetBallState.carried(state.singleBall(), homeTeam[4.playerNo]).execute(state)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.BLITZ),
            PlayerSelected("H4".playerId),
            SmartMoveTo(20, 1),
            *blitzBlock("H4", 4.dblock),
            DirectionSelected(Direction.UP),
            Confirm, // Follow up
            DiceRollResults(1.d6, 1.d6), // Crowd Injury roll
            2.d3, // Throw-in direction
            DiceRollResults(1.d6, 2.d6), // Throw-in distance
            7.d8, // Bounce
            EndAction
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(PitchCoordinate(20, 3), state.singleBall().coordinates)
        assertEquals(Dogout, homeTeam[4.playerNo].location)
        assertEquals(PitchCoordinate(20, 0), awayTeam[10.playerNo].coordinates)
    }

    @Test
    fun chainPushOwnPlayerWithBallIntoCrowd() {
        SetPlayerLocation(awayTeam[1.playerNo], PitchCoordinate(23, 0)).execute(state)
        SetPlayerLocation(awayTeam[2.playerNo], PitchCoordinate(23, 1)).execute(state)
        SetPlayerLocation(awayTeam[3.playerNo], PitchCoordinate(23, 2)).execute(state)
        SetPlayerLocation(homeTeam[1.playerNo], PitchCoordinate(24, 0)).execute(state)
        SetPlayerLocation(homeTeam[2.playerNo], PitchCoordinate(24, 1)).execute(state)
        SetPlayerLocation(homeTeam[3.playerNo], PitchCoordinate(24, 2)).execute(state)
        SetPlayerLocation(awayTeam[4.playerNo], PitchCoordinate(25, 0)).execute(state)
        SetPlayerLocation(awayTeam[5.playerNo], PitchCoordinate(25, 1)).execute(state)
        SetPlayerLocation(awayTeam[7.playerNo], PitchCoordinate(25, 2)).execute(state)
        SetBallState.carried(state.singleBall(), awayTeam[5.playerNo]).execute(state)
        controller.rollForward(
            *activatePlayer("A2", PlayerStandardActionType.BLOCK),
            *standardBlock("H2", 4.dblock),
            DirectionSelected(Direction.RIGHT), // First push
            DirectionSelected(Direction.RIGHT), // 2nd push (into the crowd)
            Cancel, // Do not follow up
            DiceRollResults(1.d6, 1.d6), // Crowd Injury roll, causes turnover
            2.d3, // Throw-in direction
            DiceRollResults(1.d6, 2.d6), // Throw-in distance
            *catch(6.d6)
        )
        assertEquals(homeTeam, state.activeTeam)
        assertNull(state.activePlayer)
        assertEquals(PitchCoordinate(23, 1), awayTeam["A2".playerId].coordinates)
        assertTrue(awayTeam["A2".playerId].hasBall())
        assertEquals(Dogout, awayTeam["A5".playerId].location)
    }

    @Test
    fun chainPushOwnPlayerIntoCrowd() {
        SetPlayerLocation(awayTeam[1.playerNo], PitchCoordinate(23, 0)).execute(state)
        SetPlayerLocation(awayTeam[2.playerNo], PitchCoordinate(23, 1)).execute(state)
        SetPlayerLocation(awayTeam[3.playerNo], PitchCoordinate(23, 2)).execute(state)
        SetPlayerLocation(homeTeam[1.playerNo], PitchCoordinate(24, 0)).execute(state)
        SetPlayerLocation(homeTeam[2.playerNo], PitchCoordinate(24, 1)).execute(state)
        SetPlayerLocation(homeTeam[3.playerNo], PitchCoordinate(24, 2)).execute(state)
        SetPlayerLocation(awayTeam[4.playerNo], PitchCoordinate(25, 0)).execute(state)
        SetPlayerLocation(awayTeam[5.playerNo], PitchCoordinate(25, 1)).execute(state)
        SetPlayerLocation(awayTeam[7.playerNo], PitchCoordinate(25, 2)).execute(state)
        controller.rollForward(
            *activatePlayer("A2", PlayerStandardActionType.BLOCK),
            *standardBlock("H2", 4.dblock),
            DirectionSelected(Direction.RIGHT), // First push
            DirectionSelected(Direction.RIGHT), // 2nd push (into the crowd)
            Cancel, // Do not follow up
            DiceRollResults(1.d6, 1.d6), // Crowd Injury roll
        )
        assertEquals(awayTeam, state.activeTeam)
        assertNull(state.activePlayer)
        assertEquals(PitchCoordinate(23, 1), awayTeam["A2".playerId].coordinates)
        assertEquals(Dogout, awayTeam["A5".playerId].location)
    }
}
