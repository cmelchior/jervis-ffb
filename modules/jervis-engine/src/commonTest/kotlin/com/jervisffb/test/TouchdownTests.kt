package com.jervisffb.test

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
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
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class responsible for testing scenarios where touchdowns should be detected
 * (and a few cases where it shouldn't).
 */
class TouchdownTests: JervisGameTest() {

    @Test
    fun movingAwayPlayerIntoHomeEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 1.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null,
            ),
            PlayerSelected("A6".playerId), // Give ball to this player and start turn
        )
        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)
        assertTrue(player.hasBall())

        // Give player enough move to reach the End Zone in one turn.
        player.movesLeft = 20
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(0, 3) // Score
        )
        assertTouchdown()
    }

    @Test
    fun movingAwayPlayerIntoAwayEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 1.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null,
            ),
            PlayerSelected("A6".playerId), // Give ball to this player and start turn
        )
        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)
        assertTrue(player.hasBall())

        // Give player enough move to reach the End Zone in one turn.
        player.movesLeft = 20
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(25, 3), // Own End Zone
            EndAction
        )
        assertEquals(0, state.awayScore)
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }

    @Test
    fun movingHomePlayerIntoAwayEndZone() {
        controller.rollForward(
            *defaultPregame(
                determineKickingTeam = arrayOf(
                    CoinSideSelected(Coin.HEAD), // Away: Select side
                    CoinTossResult(Coin.HEAD), // Home flips coin
                    Confirm // Away choices to kick
                )
            ),
            *defaultSetup(homeFirst = false),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(0, 0),
                deviate = DiceRollResults(2.d8, 1.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null,
            ),
            PlayerSelected("H6".playerId), // Give ball to this player and start turn
        )
        val player = homeTeam[6.playerNo]
        assertEquals(0, state.homeScore)
        assertTrue(player.hasBall())

        // Give player enough move to reach the End Zone in one turn.
        player.movesLeft = 20
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(25, 3), // Score
        )
        assertTouchdown(homeTeam = true)
    }

    @Test
    fun movingHomePlayerIntoHomeEndZone() {
        controller.rollForward(
            *defaultPregame(
                determineKickingTeam = arrayOf(
                    CoinSideSelected(Coin.HEAD), // Away: Select side
                    CoinTossResult(Coin.HEAD), // Home flips coin
                    Confirm // Away choices to kick
                )
            ),
            *defaultSetup(homeFirst = false),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(0, 0),
                deviate = DiceRollResults(2.d8, 1.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null,
            ),
            PlayerSelected("H6".playerId), // Give ball to this player and start turn
        )
        val player = homeTeam[6.playerNo]
        assertEquals(0, state.homeScore)
        assertTrue(player.hasBall())

        // Give player enough move to reach the End Zone in one turn.
        player.movesLeft = 20
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(0, 3),
            EndAction,
        )
        assertEquals(0, state.homeScore)
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }

    @Test
    fun failRushInEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 1.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null,
            ),
            PlayerSelected("A6".playerId), // Give ball to this player and start turn
        )
        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)
        assertTrue(player.hasBall())

        // Give player enough move to reach the End Zone with a Rush
        player.movesLeft = 13
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(1, 3),
            *moveTo(0, 3), // Rush move
            1.d6, // Fail Rush
            NoRerollSelected(),
            DiceRollResults(1.d6, 1.d6), // Armour roll
            2.d8, // Bounce
        )
        assertEquals(homeTeam, state.activeTeamOrThrow())
        assertEquals(0, state.awayScore)
        assertEquals(BallState.ON_GROUND, state.field[0, 2].balls.single().state)
    }

    @Test
    fun failDodgeInOpponentEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 1.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null,
            ),
            PlayerSelected("A6".playerId), // Give ball to this player and start turn
        )
        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)
        assertTrue(player.hasBall())

        // Move opponent player in the way, so it forces a dodge
        SetPlayerLocation(homeTeam[11.playerNo], FieldCoordinate(0, 4)).execute(state)

        // Give player enough move to reach the End Zone
        player.movesLeft = 20
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(1, 3),
            *moveTo(0, 3), // Move that requires a dodge
            1.d6, // Fail Dodge
            NoRerollSelected(),
            DiceRollResults(1.d6, 1.d6),
            2.d8, // Bounce
        )
        assertEquals(homeTeam, state.activeTeamOrThrow())
        assertEquals(0, state.awayScore)
        assertEquals(BallState.ON_GROUND, state.field[0, 2].balls.single().state)
    }

    @Test
    fun jumpIntoOpponentEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 1.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null,
            ),
            PlayerSelected("A6".playerId), // Give ball to this player and start turn
        )
        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)
        assertTrue(player.hasBall())

        // Move opponent player in the way and put them prone, so we can jump over them
        SetPlayerLocation(homeTeam[11.playerNo], FieldCoordinate(1, 4)).execute(state)
        homeTeam[11.playerNo].state = PlayerState.PRONE

        // Give player enough moves to reach the End Zone
        player.movesLeft = 20
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(2, 4),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(0, 4),
            4.d6, // Jump
            NoRerollSelected(),
        )
        assertTouchdown()
    }

    @Test
    @Ignore
    fun leapIntoOpponentEndZone() {
        TODO("Should be moved into LeapTests")
    }

    @Test
    fun catchPassInOpponentEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 2.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null
            ),
            PlayerSelected("A6".playerId) // Give ball to A6
        )

        // Away turn has started. Fake the position of two away players
        // to make it easier to do a pass
        SetPlayerLocation(awayTeam[5.playerNo], FieldCoordinate(0, 3)).execute(state)
        SetPlayerLocation(awayTeam[6.playerNo], FieldCoordinate(2, 3)).execute(state)
        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)

        // Give player enough move to reach the End Zone in one turn.
        controller.rollForward(
            *activatePlayer(player.id.value, PlayerStandardActionType.PASS),
            Confirm, // Start Pass
            FieldSquareSelected(0, 3),
            6.d6, // Accurate Throw
            NoRerollSelected(),
            6.d6, // Catch + Score
            NoRerollSelected(),
        )
        assertTouchdown()
    }

    @Test
    fun catchScatterInOpponentEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 2.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null
            ),
            PlayerSelected("A6".playerId) // Give ball to A6
        )

        // Away turn has started. Fake the position of two away players
        // to make it easier to do a pass
        SetPlayerLocation(awayTeam[5.playerNo], FieldCoordinate(0, 3)).execute(state)
        SetPlayerLocation(awayTeam[6.playerNo], FieldCoordinate(2, 3)).execute(state)
        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)

        // Give player enough move to reach the End Zone in one turn.
        controller.rollForward(
            *activatePlayer(player.id.value, PlayerStandardActionType.PASS),
            Confirm, // Start Pass
            FieldSquareSelected(1, 3), // Hit square in front of player in end zone
            3.d6, // Inaccurate Throw
            NoRerollSelected(),
            DiceRollResults(4.d8, 5.d8, 4.d8), // Scatter on top of player ind end zone
            6.d6, // Catch + Score
            NoRerollSelected(),
        )
        assertTouchdown()
    }

    @Test
    fun catchDeviatedBallInOpponentEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 2.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null
            ),
            PlayerSelected("A6".playerId) // Give ball to A6
        )

        // Away turn has started. Fake the position of two away players
        // to make it easier to do a pass
        SetPlayerLocation(awayTeam[5.playerNo], FieldCoordinate(0, 3)).execute(state)
        SetPlayerLocation(awayTeam[6.playerNo], FieldCoordinate(6, 3)).execute(state)
        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)

        // Throw ball as wildly inaccurate, but it still hits a player in the end zone
        controller.rollForward(
            *activatePlayer(player.id.value, PlayerStandardActionType.PASS),
            Confirm, // Start Pass
            FieldSquareSelected(1, 3), // Hit square in front of player in end zone
            2.d6, // Wildly Inaccurate
            NoRerollSelected(),
            DiceRollResults(4.d8, 6.d6), // Deviate into end zone
            6.d6, // Catch + Score
            NoRerollSelected(),
        )
        assertTouchdown()
    }

    @Test
    fun catchHandOffInOpponentEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 2.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null
            ),
            PlayerSelected("A6".playerId) // Give ball to A6
        )

        // Away turn has started. Fake the position of two away players
        // to make it easier to do a hand-off
        SetPlayerLocation(awayTeam[5.playerNo], FieldCoordinate(0, 3)).execute(state)
        SetPlayerLocation(awayTeam[6.playerNo], FieldCoordinate(1, 3)).execute(state)
        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)

        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.HAND_OFF),
            PlayerSelected("A5".playerId),
            5.d6, // Catch + Score
            NoRerollSelected(),
        )
        assertTouchdown()
    }

    @Test
    fun catchBounceInOpponentEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 2.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null
            ),
            PlayerSelected("A6".playerId) // Give ball to A6
        )

        // Away turn has started. Fake the position of two away players
        // to make it easier to do a pass
        SetPlayerLocation(awayTeam[5.playerNo], FieldCoordinate(0, 3)).execute(state)
        SetPlayerLocation(awayTeam[6.playerNo], FieldCoordinate(2, 3)).execute(state)
        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)

        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            Confirm, // Start Pass
            FieldSquareSelected(1, 3), // Target empty square
            6.d6, // Throw
            NoRerollSelected(),
            4.d8, // Bounce to player
            6.d6, // Catch + Score
            NoRerollSelected(),
        )
        assertTouchdown()
    }

    @Test
    fun catchThrowInInInOpponentEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 2.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null
            ),
            PlayerSelected("A6".playerId) // Give ball to A6
        )

        // Away turn has started. Fake the position of two away players
        // to make it easier to do a pass
        SetPlayerLocation(awayTeam[5.playerNo], FieldCoordinate(0, 3)).execute(state)
        SetPlayerLocation(awayTeam[6.playerNo], FieldCoordinate(2, 3)).execute(state)
        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)

        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            Confirm, // Start Pass
            FieldSquareSelected(0, 0), // Hit empty square
            6.d6, // Throw
            NoRerollSelected(),
            2.d8, // Bounce Out-of-Bounds
            3.d3, // Throw-in direction
            DiceRollResults(1.d6, 2.d6), // Throw-in distance
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertTouchdown()
    }

    @Test
    fun pickupBallInOpponentEndZone() {
        assertEquals(0, state.awayScore)
        assertEquals(0, state.homeScore)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 2),
                deviate = DiceRollResults(7.d8, 2.d6), // Ball lands in [25,4]
                kickoffEvent = defaultKickOffEvent(),
                bounce = 2.d8, // Bounce to [25,3]
            ),
        )
        // Fake moving the ball into the Home team End Zone
        SetBallLocation(state.singleBall(), FieldCoordinate(0, 3)).execute(state)

        val player = awayTeam[6.playerNo]
        assertEquals(0, state.awayScore)
        assertTrue(!player.hasBall())

        // Give player enough move to reach the End Zone in one turn.
        player.movesLeft = 20
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(0, 3),
            6.d6, // Pickup and Score
            NoRerollSelected(),
        )
        assertTouchdown()
    }

    @Test
    fun chainPushOwnBallCarrierIntoEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 2.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null
            ),
            PlayerSelected("A6".playerId) // Give ball to A6
        )

        // Away turn has started. Fake the position of players, so we can chain-push
        // A6 into the end zone.
        SetPlayerLocation(awayTeam[6.playerNo], FieldCoordinate(1, 3)).execute(state)
        SetPlayerLocation(homeTeam[1.playerNo], FieldCoordinate(2, 3)).execute(state)
        SetPlayerLocation(homeTeam[2.playerNo], FieldCoordinate(1, 4)).execute(state)
        SetPlayerLocation(homeTeam[3.playerNo], FieldCoordinate(2, 4)).execute(state)
        SetPlayerLocation(awayTeam[5.playerNo], FieldCoordinate(3, 5)).execute(state)

        assertEquals(0, state.awayScore)
        controller.rollForward(
            PlayerSelected("A5".playerId),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected("H3".playerId),
            BlockTypeSelected(BlockType.STANDARD),
            3.dblock, // Pushback
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.UP_LEFT),
            DirectionSelected(Direction.LEFT),
            Confirm // Follow up
        )
        assertTouchdown()
    }

    @Test
    fun pushOpponentBallCarrierIntoEndZone() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 2.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null
            ),
            PlayerSelected("A6".playerId) // Give ball to A6
        )

        // Away turn has started. Fake the position of players, so we can push
        // an opponent into the away endzone.
        SetPlayerLocation(awayTeam[6.playerNo], FieldCoordinate(23, 3)).execute(state)
        SetPlayerLocation(homeTeam[1.playerNo], FieldCoordinate(24, 3)).execute(state)
        SetBallState.carried(state.singleBall(), homeTeam[1.playerNo]).execute(state)

        assertEquals(0, state.awayScore)
        controller.rollForward(
            PlayerSelected("A6".playerId),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
            BlockTypeSelected(BlockType.STANDARD),
            3.dblock, // Pushback
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.RIGHT),
            Confirm // Follow up
        )
        assertTouchdown(homeTeam = true)
        assertEquals(1, awayTeam.turnMarker)
        assertEquals(1, homeTeam.turnMarker)
    }

    @Ignore
    @Test
    fun chainPushOpponentBallCarrierIntoEndZone() {
        TODO()
    }

    @Ignore
    @Test
    fun followUpIntoEndZone() {
        TODO()
    }

    @Ignore
    @Test
    fun pushbackAndKnockdownPlayerInEndZone() {
        TODO()
    }

    @Ignore
    @Test
    fun throwTeamMate_landWithBall() {
        TODO()
    }

    @Ignore
    @Test
    fun ballAndChainMoveIntoEndZone() {
        TODO()
    }

    @Ignore
    @Test
    fun scoreInOpponentTurn_endOfHalf() {
        TODO()
    }

    private fun assertTouchdown(homeTeam: Boolean = false) {
        if (homeTeam) {
            assertEquals(0, state.awayScore)
            assertEquals(1, state.homeScore)
        } else {
            assertEquals(1, state.awayScore)
            assertEquals(0, state.homeScore)
        }
        assertNull(state.activeTeam)
    }
}

