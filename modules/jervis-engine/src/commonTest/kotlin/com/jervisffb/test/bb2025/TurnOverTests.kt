package com.jervisffb.test.bb2025

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
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
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.standardBlock
import com.jervisffb.test.throwBall
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Turnovers are always associated with other actions and should normally
 * explicitly on their own page, we also compile them together here.
 * be covered by tests for those actions, but since turnovers are described
 *
 * See page 35 in the rulebook.
 */
class TurnOverTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun fallingOverInOwnActivation() {
        controller.rollForward(
            PlayerSelected("A8".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(22, 13),
            *moveTo(23, 13), // Rush
            1.d6, // Fail Rush
            NoRerollSelected(),
        )
        assertEquals(PlayerState.FALLEN_OVER, state.getPlayerById("A8".playerId).state)
        assertEquals(TurnOver.STANDARD, state.turnOver)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Armour roll
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun knockedDown_duringOwnActivation() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 1.dblock),
        )
        assertEquals(PlayerState.KNOCKED_DOWN, attacker.state)
        assertEquals(TurnOver.STANDARD, state.turnOver)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Armour roll
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun knockedDown_duringOtherTeamTurn_noTurnOver() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT), // Push back
            Cancel, // Do not follow up
        )
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
        assertNull(state.turnOver)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Armour roll
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun ownPlayerWithBallPushedIntoCrowd() {
        SetPlayerLocation(awayTeam[1.playerNo], FieldCoordinate(23, 0)).execute(state)
        SetPlayerLocation(awayTeam[2.playerNo], FieldCoordinate(23, 1)).execute(state)
        SetPlayerLocation(awayTeam[3.playerNo], FieldCoordinate(23, 2)).execute(state)
        SetPlayerLocation(homeTeam[1.playerNo], FieldCoordinate(24, 0)).execute(state)
        SetPlayerLocation(homeTeam[2.playerNo], FieldCoordinate(24, 1)).execute(state)
        SetPlayerLocation(homeTeam[3.playerNo], FieldCoordinate(24, 2)).execute(state)
        SetPlayerLocation(awayTeam[4.playerNo], FieldCoordinate(25, 0)).execute(state)
        SetPlayerLocation(awayTeam[5.playerNo], FieldCoordinate(25, 1)).execute(state)
        SetPlayerLocation(awayTeam[7.playerNo], FieldCoordinate(25, 2)).execute(state)
        SetBallState.carried(state.singleBall(), awayTeam[5.playerNo]).execute(state)
        controller.rollForward(
            *activatePlayer("A2", PlayerStandardActionType.BLOCK),
            *standardBlock("H2", 4.dblock),
            DirectionSelected(Direction.RIGHT), // First push
            DirectionSelected(Direction.RIGHT), // 2nd push (into the crowd) -> TurnOver
        )
        assertEquals(TurnOver.STANDARD, state.turnOver)
        controller.rollForward(
            Cancel, // Do not follow up
            DiceRollResults(1.d6, 1.d6), // Crowd Injury roll, causes turnover
            2.d3, // Throw-in direction
            DiceRollResults(1.d6, 2.d6), // Throw-in distance
            *catch(6.d6)
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun playerPushedIntoCrowd_noBall_noTurnOver() {
        SetPlayerLocation(awayTeam[1.playerNo], FieldCoordinate(23, 0)).execute(state)
        SetPlayerLocation(awayTeam[2.playerNo], FieldCoordinate(23, 1)).execute(state)
        SetPlayerLocation(awayTeam[3.playerNo], FieldCoordinate(23, 2)).execute(state)
        SetPlayerLocation(homeTeam[1.playerNo], FieldCoordinate(24, 0)).execute(state)
        SetPlayerLocation(homeTeam[2.playerNo], FieldCoordinate(24, 1)).execute(state)
        SetPlayerLocation(homeTeam[3.playerNo], FieldCoordinate(24, 2)).execute(state)
        SetPlayerLocation(awayTeam[4.playerNo], FieldCoordinate(25, 0)).execute(state)
        SetPlayerLocation(awayTeam[5.playerNo], FieldCoordinate(25, 1)).execute(state)
        SetPlayerLocation(awayTeam[7.playerNo], FieldCoordinate(25, 2)).execute(state)
        controller.rollForward(
            *activatePlayer("A2", PlayerStandardActionType.BLOCK),
            *standardBlock("H2", 4.dblock),
            DirectionSelected(Direction.RIGHT), // First push
            DirectionSelected(Direction.RIGHT), // 2nd push (into the crowd)
        )
        assertNull(state.turnOver)
        controller.rollForward(
            Cancel, // Do not follow up
            DiceRollResults(1.d6, 1.d6), // Crowd Injury roll, do not cause turnover
        )
        assertNull(state.turnOver)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun playerWithBallPushedIntoCrowd_opponentTurn_noTurnOver() {
        SetPlayerLocation(homeTeam[8.playerNo], FieldCoordinate(1, 14)).execute(state)
        SetPlayerLocation(awayTeam[1.playerNo], FieldCoordinate(1, 13)).execute(state)
        SetBallState.carried(state.singleBall(), homeTeam[8.playerNo]).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H8", 4.dblock),
            DirectionSelected(Direction.DOWN), // Pushed into crowd, no turnover
        )
        assertNull(state.turnOver)
        controller.rollForward(
            Confirm, // Follow up
            DiceRollResults(1.d6, 1.d6), // Injury roll
            2.d3, // Throw-in direction
            DiceRollResults(1.d6, 1.d6), // Throw-in distance
            4.d8 // Bounce
        )
        assertNull(state.turnOver)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun failToPickupBall_landsOnEmptySquare() {
        controller.rollForward(
            *activatePlayer("A11", PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            2.d6, // Fail pickup
            NoRerollSelected(),
        )
        assertEquals(TurnOver.STANDARD, state.turnOver)
        controller.rollForward(
            2.d8 // Bounce
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun failToPickupBall_caughtByActiveTeam() {
        controller.rollForward(
            *activatePlayer("A11", PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            2.d6, // Fail pickup
            NoRerollSelected(),
        )
        assertEquals(TurnOver.STANDARD, state.turnOver)
        controller.rollForward(
            4.d8, // Bounce
            *catch(6.d6)
        )
        assertTrue(awayTeam["A10".playerId].hasBall())
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun failToPickupBall_caughtByOpponentTeam() {
        SetPlayerLocation(awayTeam[10.playerNo], FieldCoordinate(15, 7)).execute(state)
        SetPlayerLocation(homeTeam[1.playerNo], FieldCoordinate(16, 7)).execute(state)
        controller.rollForward(
            *activatePlayer("A11", PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            2.d6, // Fail pickup
            NoRerollSelected(),
        )
        assertEquals(TurnOver.STANDARD, state.turnOver)
        controller.rollForward(
            4.d8, // Bounce
            6.d6 // Catch
        )
        assertTrue(homeTeam["H1".playerId].hasBall())
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun fumblesPass_caughtByOwnTeam() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 5),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 5),
            *throwBall(1.d6), // Fumbled pass
        )
        assertEquals(PassingType.FUMBLED, state.getContext<PassContext>().passingResult)
        assertEquals(TurnOver.STANDARD, state.turnOver)
        controller.rollForward(
            4.d8, // Bounce
            *catch(6.d6)
        )
        assertTrue(awayTeam["A1".playerId].hasBall())
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun fumblesPass_caughtByOpponentTeam() {
        SetPlayerLocation(homeTeam[1.playerNo], FieldCoordinate(16, 7)).execute(state)
        SetPlayerLocation(awayTeam[10.playerNo], FieldCoordinate(23, 7)).execute(state)
        controller.rollForward(
            *activatePlayer("A11", PlayerStandardActionType.PASS),
            SmartMoveTo(17, 7),
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(23, 7),
            *throwBall(1.d6), //Fumbbl pass
        )
        assertEquals(PassingType.FUMBLED, state.getContext<PassContext>().passingResult)
        assertEquals(TurnOver.STANDARD, state.turnOver)
        controller.rollForward(
            4.d8, // Bounce
            6.d6 // Opponent catches it
        )
        assertTrue(homeTeam[1.playerNo].hasBall())
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun fumblesPass_landsOnGround() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 5),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(14, 1),
            *throwBall(1.d6), // Fumbled pass
        )
        assertEquals(PassingType.FUMBLED, state.getContext<PassContext>().passingResult)
        assertEquals(TurnOver.STANDARD, state.turnOver)
        controller.rollForward(
            8.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(15,6), state.singleBall().location)
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun failedToCatchPass_ground() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(15, 1),
            *throwBall(6.d6),
            *catch(1.d6),
            2.d8, // Bounce
        )
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun failedToCatchPass_teamCatchesBall_noTurnOver() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(16, 4),
        )
        assertEquals(
            Range.QUICK_PASS,
            rules.rangeRuler.measure(awayTeam["A10".playerId], FieldCoordinate(15, 1))
        )
        controller.rollForward(
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(15, 1),
            *throwBall(4.d6),
            *catch(1.d6),
            4.d8, // Bounce
            *catch(6.d6) // Other team player catches ball
        )
        assertTrue(awayTeam["A6".playerId].hasBall())
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun failedToCatchPass_opponent() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 5),
            *throwBall(6.d6),
            *catch(1.d6), // Fail catch
            4.d8, // Bounce
            6.d6, // Caught by opponent
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun failedToCatchHandOff_ground() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 5),
            PlayerSelected("A1".playerId),
            1.d6, // Fail catch
            NoRerollSelected(),
            3.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(14, 4), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun failedToCatchHandOff_teamCatchesBall_noTurnOver() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(15, 2),
            PlayerSelected("A7".playerId),
            *catch(1.d6), // Fail to catch
            4.d8, // Bounce to other team player
            *catch(6.d6)
        )
        assertTrue(awayTeam["A6".playerId].hasBall())
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun failedToCatchHandOff_opponent() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 5),
            PlayerSelected("A1".playerId),
            1.d6, // Fail catch
            NoRerollSelected(),
            4.d8, // Bounce
            6.d6 // Caught by opponent
        )
        assertTrue(homeTeam["H1".playerId].hasBall())
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun successfulInterception() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 9),
            *throwBall(6.d6),
            PlayerSelected("H2".playerId), // Select Interceptor
            6.d6, // Intercept
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun sentOffByRef() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(1, awayTeam.turnMarker)
        assertEquals(0, homeTeam.turnMarker)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Cancel // Do not argue the call
        )
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(1, awayTeam.turnMarker)
        assertEquals(1, awayTeam.turnMarker)
    }

    @Test
    fun touchdownInOwnTurn() {
        // Give ball to player and enough move to reach the End Zone in one turn.
        val player = awayTeam[6.playerNo]
        SetBallState.carried(state.singleBall(), player).execute(state)
        player.movesLeft = 20
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            SmartMoveTo(0, 3) // Score
        )
        // We cannot check if TurnOver is set because there is no "stop" between reaching
        // the end zone the activation ending (Could we add a skill that triggers at end of Activation?)
        assertNull(state.activePlayer)
        assertNull(state.activeTeam)
        assertEquals(1, state.awayScore)
    }

    @Test
    fun touchdownInOpponentsTurn() {
        // Give ball to opponent player an set them up to be pushed into the away team end zone
        val defender = homeTeam[1.playerNo]
        SetPlayerLocation(defender, FieldCoordinate(24, 1)).execute(state)
        SetBallState.carried(state.singleBall(), defender).execute(state)
        val attacker = awayTeam[1.playerNo]
        SetPlayerLocation(attacker, FieldCoordinate(23, 1)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.RIGHT), // Push into end zone
            Cancel // Do not follow up
        )
        // We cannot check if TurnOver is set because there is no "stop" between check for touchdown
        // during push and the activation ending
        assertNull(state.activePlayer)
        assertNull(state.activeTeam)
        assertEquals(1, state.homeScore)
    }
}
