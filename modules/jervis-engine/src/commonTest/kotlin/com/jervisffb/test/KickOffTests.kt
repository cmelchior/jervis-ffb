package com.jervisffb.test

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.assertTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests for the Kick-off sequence as described on page 40 in the rulebook.
 * This covers:
 * - Nominating kicking player
 * - Placing the kick
 * - Deviating the kick
 * - What goes up, must come down
 * - Touchbacks
 *
 * All kick-off events have special tests in [com.jervisffb.test.tables.KickOffEventTests].
 */
class KickOffTests: JervisGameTest() {

    @Test
    fun selectKickingPlayer_fromCenterField() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
        )
        val actions = controller.getAvailableActions().actions
        val selectPlayersAction = actions.first { it is SelectPlayer }.createAll()
        assertEquals(2, selectPlayersAction.size)
        (selectPlayersAction.first() as PlayerSelected).let {
            assertEquals("H10".playerId, it.playerId)
            assertEquals(FieldCoordinate(9, 7), state.homeTeam[10.playerNo].location)
        }
        assertEquals("H11".playerId, (selectPlayersAction.last() as PlayerSelected).playerId)
        controller.handleAction(selectPlayersAction.first())
        assertEquals(state.kickingPlayer!!.id, "H10".playerId)
    }

    @Test
    fun selectKickingPlayer_3Players_onLoS() {
        controller.rollForward(*defaultPregame())
        // Adjust the home team so we only have 3 players available and then
        // place them on LoS
        (1..9).forEach {
            homeTeam[it.playerNo].state = PlayerState.KNOCKED_OUT
        }
        controller.rollForward(
            *teamSetup(
                "H10".playerId to FieldCoordinate(12, 4),
                "H11".playerId to FieldCoordinate(12, 7),
                "H12".playerId to FieldCoordinate(12, 10),
            ),
            *defaultAwaySetup()
        )

        val actions = controller.getAvailableActions().actions
        val selectPlayersAction = actions.first { it is SelectPlayer }.createAll()
        assertEquals(3, selectPlayersAction.size)
        selectPlayersAction.forEach {
            assertTypeOf<PlayerSelected>(it)
            assertTrue(homeTeam[it.playerId].location.isInCenterField(rules))
            assertTrue(homeTeam[it.playerId].location.isOnLineOfScrimmage(rules))
        }
        controller.handleAction(selectPlayersAction.last())
        assertEquals(state.kickingPlayer!!.id, "H12".playerId)
    }

    @Test
    fun selectKickingPlayer_11Players_onLoS() {
        controller.rollForward(*defaultPregame())
        controller.rollForward(
            *teamSetup(
                "H1".playerId to FieldCoordinate(12, 2),
                "H2".playerId to FieldCoordinate(12, 3),
                "H3".playerId to FieldCoordinate(12, 4),
                "H4".playerId to FieldCoordinate(12, 5),
                "H5".playerId to FieldCoordinate(12, 6),
                "H6".playerId to FieldCoordinate(12, 7),
                "H7".playerId to FieldCoordinate(12, 8),
                "H8".playerId to FieldCoordinate(12, 9),
                "H9".playerId to FieldCoordinate(12, 10),
                "H10".playerId to  FieldCoordinate(12, 11),
                "H11".playerId to  FieldCoordinate(12, 12)
            ),
            *defaultAwaySetup()
        )

        val actions = controller.getAvailableActions().actions
        val selectPlayersAction = actions.first { it is SelectPlayer }.createAll()
        assertEquals(7, selectPlayersAction.size)
        selectPlayersAction.forEach {
            assertTypeOf<PlayerSelected>(it)
            assertTrue(homeTeam[it.playerId].location.isInCenterField(rules))
            assertTrue(homeTeam[it.playerId].location.isOnLineOfScrimmage(rules))
        }
        controller.handleAction(selectPlayersAction[1])
        assertEquals("H4".playerId, state.kickingPlayer!!.id)
    }

    @Test
    fun placeKick() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            PlayerSelected(PlayerId("H8")) // Select Kicker
        )
        val squares = controller.getAvailableActions().first() as? SelectFieldLocation ?: fail("Wrong type: ${controller.getAvailableActions().first()}")
        // All squares on the other side should be available
        assertEquals(13*15, squares.size)
        squares.squares.forEach {
            assertTrue(it.coordinate.isOnAwaySide(rules))
        }
    }

    @Test
    fun ballLanding_onField() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            PlayerSelected(PlayerId("H8")) // Select Kicker
        )
        val squares = controller.getAvailableActions().first() as? SelectFieldLocation ?: fail("Wrong type: ${controller.getAvailableActions().first()}")
        assertEquals(13*15, squares.size)
        squares.squares.forEach {
            assertTrue(it.coordinate.isOnAwaySide(rules))
        }
        controller.rollForward(
            FieldSquareSelected(19, 7), // Center of Away Half,
            DiceRollResults(4.d8, 1.d6), // Land on [18,7]
            *defaultKickOffEvent(),
            4.d8 // Bounce to [17,7]
        )
        assertEquals(FieldCoordinate(17, 7), state.getBall().location)
        assertEquals(BallState.ON_GROUND, state.getBall().state)
    }

    @Test
    fun ballLanding_onPlayerWhoCatchesIt() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            // Make sure the ball lands on a player
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(14, 2),
                deviate = DiceRollResults(2.d8, 1.d6),
                bounce = null
            ),
            4.d6 // Catch roll
        )
        assertTrue(awayTeam["A6".playerId].hasBall())
    }

    // Ball landed on a player who failed to catch and ball bounced to another
    // player who is also allowed to catch it.
    @Test
    fun ballLanding_onSecondaryPlayerWhoCatchesIt() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            // Make sure the ball lands on a player
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(16, 1),
                deviate = DiceRollResults(4.d8, 1.d6),
                bounce = null
            ),
            3.d6, // First Catch roll fails
            NoRerollSelected(0), // Player has catch, but do not use it.
            4.d8, // Bounce
            6.d6 // 2nd catch roll
        )
        assertTrue(awayTeam["A6".playerId].hasBall())
        assertEquals(1, state.awayTeam.turnMarker)
    }

    @Test
    fun ballLanding_teamRerollNotAvailable() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            // Make sure the ball lands on a player
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(14, 2),
                deviate = DiceRollResults(2.d8, 1.d6),
                bounce = null
            ),
            3.d6, // First roll fails
        )
        // Instead of asking for reroll (since no team reroll is available), we
        // should go directly to bounce
        assertEquals(Bounce.RollDirection, controller.currentNode())
    }

    @Test
    fun touchback_outOfBounds() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            // Place ball in end-zone and make it deviate out back of it
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 7),
                deviate = DiceRollResults(5.d8, 1.d6),
                bounce = null
            ),
            PlayerSelected("A1".playerId), // Touchback
        )
        assertEquals(1, awayTeam.turnMarker)
        assertTrue(awayTeam["A1".playerId].hasBall())
    }

    @Test
    fun touchback_afterBounce() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 0),
                deviate = DiceRollResults(5.d8, 6.d6), // Deviate to [19, 0]
                bounce = 3.d8 // Bounce to [19, -1] (out of bounds)
            ),
            PlayerSelected("A1".playerId), // Touchback
        )
        assertEquals(1, awayTeam.turnMarker)
        assertTrue(awayTeam["A1".playerId].hasBall())
    }

    // Player failed to catch the ball and ball then bounced out-of-bounds
    @Test
    fun touchback_outOfBounds_afterFailedCatch() {
        controller.rollForward(
            *defaultPregame(),
            *defaultHomeSetup(),
            *defaultAwaySetup(endSetup = false),
            // Move a single player to stand next to the edge of the field
            PlayerSelected("A8".playerId),
            FieldSquareSelected(15, 14),
            EndSetup,
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(15, 13),
                deviate = DiceRollResults(7.d8, 1.d6),
                bounce = null // Manually handle catches and bounces
            ),
            2.d6, // Fail first catch
            7.d8, // Bounce out of bounds
            PlayerSelected("A8".playerId), // Touchback
        )
        assertEquals(1, awayTeam.turnMarker)
        assertTrue(awayTeam["A8".playerId].hasBall())
    }

    // Ball deviated back into kickers half and landed there
    @Test
    fun touchback_acrossLoS_emptySquare() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 0),
                deviate = DiceRollResults(7.d8, 1.d6), // Deviate to [13, 1]
                bounce = 4.d8 // Bounce to [12, 1] (Kicking teams side)
            ),
            PlayerSelected("A1".playerId), // Touchback
        )
        assertEquals(1, awayTeam.turnMarker)
        assertTrue(awayTeam["A1".playerId].hasBall())
    }

    @Test
    fun touchback_acrossLoS_onKickingTeamPlayer() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 0),
                deviate = DiceRollResults(7.d8, 4.d6), // Deviate to [13, 4]
                bounce = 6.d8 // Bounce to [12, 5] (Kicking teams side, player standing there)
            ),
            PlayerSelected("A1".playerId), // Touchback
        )
        assertEquals(1, awayTeam.turnMarker)
        assertTrue(awayTeam["A1".playerId].hasBall())
    }

    // Player failed to catch ball and ball bounced into opponent teams half
    @Test
    fun touchback_acrossLoS_afterSecondaryBounce() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            // Place ball in end-zone and make it deviate out the back of it
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 5),
                deviate = DiceRollResults(7.d8, 3.d6),
                bounce = null // Manually handle catches and bounces
            ),
            2.d6, // Fail first catch
            7.d8, // Bounce to another player
            1.d6, // Fail 2nd catch,
            4.d8, // Bounce across LoS, on top on player from Kicking team,
            PlayerSelected("A1".playerId), // Touchback
        )
        assertEquals(1, awayTeam.turnMarker)
        assertTrue(awayTeam["A1".playerId].hasBall())
    }

    @Test
    fun touchback_giveToPlayer() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 7),
                deviate = DiceRollResults(5.d8, 1.d6),
                bounce = null
            ),
        )
        assertEquals(BallState.OUT_OF_BOUNDS, state.getBall().state)
        controller.rollForward(PlayerSelected("A1".playerId)) // Touchback
        assertEquals(1, awayTeam.turnMarker)
        assertTrue(awayTeam["A1".playerId].hasBall())
        assertEquals(BallState.CARRIED, state.getBall().state)
    }

    @Test
    fun touchback_pronePlayersAreNotEligible() {
        // Prone/Stunned players cannot be given the ball as long as there are
        // standing players.
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 7),
                deviate = DiceRollResults(5.d8, 1.d6),
                bounce = null
            ),
        )
        awayTeam["A1".playerId].state = PlayerState.STUNNED
        val availablePlayers = (controller.getAvailableActions().first() as SelectPlayer).players
        assertEquals(10, availablePlayers.size)
        assertFalse(availablePlayers.contains("A1".playerId))
    }

    @Test
    fun touchback_giveToProneOrStunnedPlayer() {
        // In theory, it is possible for a Kickoff Event to leave the entire
        // receiving team prone (e.g., Pitch Invasion with a very little team).
        // We just fake it here, by setting the prone state for all players
        // after the kickoff event.
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 0),
                deviate = DiceRollResults(1.d8, 1.d6),
                bounce = null
            ),
        )
        assertEquals(BallState.OUT_OF_BOUNDS, state.getBall().state)
        state.receivingTeam.forEach { it.state = PlayerState.PRONE }
        state.receivingTeam["A2".playerId].state = PlayerState.STUNNED
        val availablePlayers = (controller.getAvailableActions().first() as SelectPlayer).players
        assertEquals(11, availablePlayers.size)
        controller.rollForward(
            PlayerSelected("A1".playerId), // Touchback to prone player
            7.d8, // Bounce to [13,6] (Where there is a stunned player)
            5.d8, // Bounce to [14,6] from stunned player
        )
        assertEquals(1, awayTeam.turnMarker)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun touchback_secondTouchbackAfterBounceFromPronePlayer() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 0),
                deviate = DiceRollResults(1.d8, 1.d6),
                bounce = null
            ),
        )
        assertEquals(BallState.OUT_OF_BOUNDS, state.getBall().state)
        state.receivingTeam.forEach { it.state = PlayerState.PRONE }
        controller.rollForward(
            PlayerSelected("A1".playerId), // Touchback to prone player
            1.d8, // Bounce to [12,5] (Which is on kicking team side)
            PlayerSelected("A2".playerId), // Touchback to another prone player
            5.d8, // Bounce to [14,6]
        )
        assertEquals(1, awayTeam.turnMarker)
        assertEquals(BallState.ON_GROUND, state.singleBall().state,)
        assertEquals(FieldCoordinate(14,6), state.singleBall().location)
    }

    // Bugfix: Check that the ball location is updated correctly after awarding a touchback
    // from going out-of-bounds. The ball should bounce from the square of the falling
    // player and not from where it went out of bounds.
    //
    @Test
    fun ballKnockedFreeAfterAwardedAsTouchback() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(3.d8, 4.d6),
                bounce = null
            ),
            PlayerSelected("A1".playerId), // Touchback
            PlayerSelected("A1".playerId), // Select Player
            PlayerActionSelected(PlayerStandardActionType.BLOCK), // Select Block Action
            PlayerSelected("H1".playerId), // Target
            BlockTypeSelected(BlockType.STANDARD), // Select Block type
            1.dblock, // Roll Skull
            NoRerollSelected(0), // Do not reroll
            DicePoolResultsSelected.fromSingleDice(1.dblock),  // Select Skull
            DiceRollResults(1.d6, 1.d6), // Armour roll
            5.d8 // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state,)
        assertEquals(FieldCoordinate(14,5), state.singleBall().location)
    }
}

