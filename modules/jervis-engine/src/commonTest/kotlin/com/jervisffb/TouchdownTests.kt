package com.jervisffb

import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
import com.jervisffb.engine.rules.PlayerStandardActionType
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class responsible for testing scenarios where touchdowns should be detected
 * (and a few cases where it shouldn't).
 */
class TouchdownTests: JervisGameTest() {

    @Test
    fun movingAwayPlayerIntoHomeEndZone() {
        controller.startTestMode(FullGame)
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
            *moveTo(13,2),
            *moveTo(12,3),
            *moveTo(11,3),
            *moveTo(10,3),
            *moveTo(9,3),
            *moveTo(8,3),
            *moveTo(7,3),
            *moveTo(6,3),
            *moveTo(5,3),
            *moveTo(4,3),
            *moveTo(3,3),
            *moveTo(2,3),
            *moveTo(1,3),
            *moveTo(0,3), // Score
        )
        assertEquals(1, state.awayScore)
    }

    @Test
    fun movingAwayPlayerIntoAwayEndZone() {
        controller.startTestMode(FullGame)
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
            *moveTo(13,2),
            *moveTo(14, 3),
            *moveTo(15,3),
            *moveTo(16,3),
            *moveTo(17,3),
            *moveTo(18,3),
            *moveTo(19,3),
            *moveTo(20,3),
            *moveTo(21,3),
            *moveTo(22,3),
            *moveTo(23,3),
            *moveTo(24,3),
            *moveTo(25,3), // Own End Zone
            EndAction
        )
        assertEquals(0, state.awayScore)
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }

    @Test
    fun movingHomePlayerIntoAwayEndZone() {
        controller.startTestMode(FullGame)
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
            *moveTo(12,2),
            *moveTo(13,3),
            *moveTo(14,3),
            *moveTo(15,3),
            *moveTo(16,3),
            *moveTo(17,3),
            *moveTo(18,3),
            *moveTo(19,3),
            *moveTo(20,3),
            *moveTo(21,3),
            *moveTo(22,3),
            *moveTo(23,3),
            *moveTo(24,3),
            *moveTo(25,3), // Score
        )
        assertEquals(1, state.homeScore)
    }

    @Test
    fun movingHomePlayerIntoHomeEndZone() {
        controller.startTestMode(FullGame)
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
            *moveTo(10,2),
            *moveTo(9,2),
            *moveTo(8,3),
            *moveTo(7,3),
            *moveTo(6,3),
            *moveTo(5,3),
            *moveTo(4,3),
            *moveTo(3,3),
            *moveTo(2,3),
            *moveTo(1,3),
            *moveTo(0,3),
            EndAction,
        )
        assertEquals(0, state.homeScore)
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }

    @Test
    fun failRushInEndZone() {
        controller.startTestMode(FullGame)
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
            *moveTo(13,2),
            *moveTo(12,3),
            *moveTo(11,3),
            *moveTo(10,3),
            *moveTo(9,3),
            *moveTo(8,3),
            *moveTo(7,3),
            *moveTo(6,3),
            *moveTo(5,3),
            *moveTo(4,3),
            *moveTo(3,3),
            *moveTo(2,3),
            *moveTo(1,3),
            *moveTo(0,3),
            1.d6, // Fail Rush
            NoRerollSelected(),
            DiceRollResults(1.d6, 1.d6),
            2.d8, // Bounce
        )
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(0, state.awayScore)
        assertEquals(BallState.ON_GROUND, state.field[0, 2].balls.single().state)
    }

    @Test
    fun failDodgeInOpponentEndZone() {
        controller.startTestMode(FullGame)
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
            *moveTo(13,2),
            *moveTo(12,3),
            *moveTo(11,3),
            *moveTo(10,3),
            *moveTo(9,3),
            *moveTo(8,3),
            *moveTo(7,3),
            *moveTo(6,3),
            *moveTo(5,3),
            *moveTo(4,3),
            *moveTo(3,3),
            *moveTo(2,3),
            *moveTo(1,3),
            *moveTo(0,3),
            1.d6, // Fail Dodge
            NoRerollSelected(),
            DiceRollResults(1.d6, 1.d6),
            2.d8, // Bounce
        )
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(0, state.awayScore)
        assertEquals(BallState.ON_GROUND, state.field[0, 2].balls.single().state)
    }

    @Test
    @Ignore
    fun jumpIntoOpponentEndZone() {
        TODO("Implement Jump first")
    }

    @Test
    @Ignore
    fun leapIntoOpponentEndZone() {
        TODO("Should be moved into LeapTests")
    }

    @Test
    fun catchPassInOpponentEndZone() {
        controller.startTestMode(FullGame)
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
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            Confirm, // Start Pass
            FieldSquareSelected(0, 3),
            6.d6, // Throw
            NoRerollSelected(),
            6.d6, // Catch + Score
            NoRerollSelected(),
        )
        assertEquals(1, state.awayScore)
    }

    @Test
    fun catchHandOffInOpponentEndZone() {
        controller.startTestMode(FullGame)
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
        assertEquals(1, state.awayScore)
    }

    @Test
    fun catchBounceInOpponentEndZone() {
        controller.startTestMode(FullGame)
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
        assertEquals(1, state.awayScore)
    }

    @Test
    fun catchThrowInInInOpponentEndZone() {
        controller.startTestMode(FullGame)
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
        assertEquals(1, state.awayScore)
    }

    @Test
    fun pickupBallInOpponentEndZone() {
        controller.startTestMode(FullGame)
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
            *moveTo(13,2),
            *moveTo(12,3),
            *moveTo(11,3),
            *moveTo(10,3),
            *moveTo(9,3),
            *moveTo(8,3),
            *moveTo(7,3),
            *moveTo(6,3),
            *moveTo(5,3),
            *moveTo(4,3),
            *moveTo(3,3),
            *moveTo(2,3),
            *moveTo(1,3),
            *moveTo(0,3),
            6.d6, // Pickup and Score
            NoRerollSelected(),
        )
        assertEquals(1, state.awayScore)
    }

    @Test
    @Ignore
    fun pickupBallDuringPushInOpponentEndZone() {
        TODO()
    }

    @Test
    @Ignore
    fun ballCarrierPushedIntoOpponentEndZone() {
        TODO()
    }
}
