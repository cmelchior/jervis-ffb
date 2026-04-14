package com.jervisffb.test.bb2025.actions

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.utils.singleInstanceOf
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.rushTo
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class responsible for testing the Pass action as described on page 48-51 in the rulebook.
 */
class PassActionTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun cancelBeforeMoveOrHandOffDoesNotUseAction() {
        assertEquals(1, state.awayTeam.turnData.passActions)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            EndAction
        )
        assertEquals(1, state.awayTeam.turnData.passActions)
        assertEquals(Availability.AVAILABLE, awayTeam["A10".playerId].available)
    }

    @Test
    fun moveWithoutBallUsesAction() {
        assertEquals(1, state.awayTeam.turnData.passActions)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(16, 8),
            EndAction
        )
        assertEquals(0, state.awayTeam.turnData.passActions)
    }

    @Test
    fun canStartActionWithoutBall() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            *throwBall(6.d6),
            *catch(5.d6),
        )
        assertFalse(awayTeam["A10".playerId].hasBall())
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun actionEndsAfterPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            *throwBall(6.d6),
            *catch(5.d6),
        )
        assertEquals(Availability.HAS_ACTIVATED,  awayTeam["A10".playerId].available)
        assertNull(state.activePlayer)
        assertEquals(0, awayTeam.turnData.passActions)
    }

    @Test
    fun canCancelThrow() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            Cancel, // Cancel pass action
            PassTypeSelected(PassType.STANDARD), // Restart pass
            PitchSquareSelected(15, 1),
            *throwBall(6.d6),
            *catch(5.d6),
        )
        assertEquals(Availability.HAS_ACTIVATED,  awayTeam["A10".playerId].available)
        assertNull(state.activePlayer)
        assertEquals(0, awayTeam.turnData.passActions)
    }

    @Test
    fun canPassAfterAllMovesAreUsed() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(23, 7),
            *rushTo(24, 7),
            *rushTo(25, 7),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            *throwBall(6.d6),
            *catch(5.d6),
        )
        assertEquals(Availability.HAS_ACTIVATED,  awayTeam["A10".playerId].available)
        assertNull(state.activePlayer)
        assertEquals(0, awayTeam.turnData.passActions)
    }

    @Test
    fun throwWithoutMoving() {
        // Fake state, by giving the ball to the player
        SetBallState.carried(state.singleBall(), awayTeam["A10".playerId]).execute(state)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            *throwBall(6.d6),
            *catch(5.d6),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun bounceFromTargetIfCatchFailed() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            *throwBall(6.d6),
            *catch(1.d6), // Fail catch
            7.d8
        )
        assertEquals(PitchCoordinate(15, 2), state.singleBall().coordinates)
    }

    @Test
    fun quickPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(16, 4),
        )
        assertEquals(
            Range.QUICK_PASS,
            rules.rangeRuler.measure(awayTeam["A10".playerId], PitchCoordinate(15, 1))
        )
        controller.rollForward(
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            *throwBall(4.d6),
            *catch(6.d6),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun shortPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(16, 5),
        )
        assertEquals(
            Range.SHORT_PASS,
            rules.rangeRuler.measure(awayTeam["A10".playerId], PitchCoordinate(15, 1))
        )
        controller.rollForward(
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            *throwBall(5.d6),
            *catch(6.d6),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun longPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(16, 8),
        )
        assertEquals(
            Range.LONG_PASS,
            rules.rangeRuler.measure(awayTeam["A10".playerId], PitchCoordinate(15, 1))
        )
        controller.rollForward(
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            *throwBall(6.d6),
            *catch(6.d6),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun longBomb() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(13, 13),
        )
        assertEquals(
            Range.LONG_BOMB,
            rules.rangeRuler.measure(awayTeam["A10".playerId], PitchCoordinate(15, 1))
        )
        controller.rollForward(
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            *throwBall(6.d6),
            *catch(6.d6),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun canTargetAllSquaresInRange() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            PassTypeSelected(PassType.STANDARD),
        )
        val targets = controller.getAvailableActions().singleInstanceOf<SelectPitchLocation>()
        assertTrue(targets.squares.all { it.type == TargetSquare.Type.THROW_TARGET })
        assertEquals(309, targets.squares.map { it.coordinate }.toSet().size)
    }

    @Test
    fun markedModifiersToThrower() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(13, 4)
        )
        assertTrue(rules.isMarked(awayTeam["A10".playerId]))
        assertEquals(
            Range.QUICK_PASS,
            rules.rangeRuler.measure(awayTeam["A10".playerId], PitchCoordinate(14, 1))
        )
        controller.rollForward(
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(14, 1),
            *throwBall(4.d6), // Throw Quick pass (-1 marked modifier), needs 5+
        )
        assertEquals(BallState.SCATTERED, state.singleBall().state)
    }

    @Test
    fun accuratePass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 4),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(14, 1),
            *throwBall(4.d6), // Throw Quick pass, needs 4+
        )
        assertEquals(PassingType.ACCURATE, state.getContext<PassContext>().passingResult)
        controller.rollForward(
            *catch(4.d6)
        )
        assertTrue(awayTeam["A6".playerId].hasBall())
    }

    @Test
    fun inaccuratePass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 4),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(14, 1),
            *throwBall(3.d6), // Throw Quick pass, needs 4+
        )
        assertEquals(PassingType.INACCURATE, state.getContext<PassContext>().passingResult)
        controller.rollForward(
            DiceRollResults(2.d8, 8.d8, 4.d8), // Scatter
            *catch(4.d6),
        )
        assertTrue(awayTeam["A6".playerId].hasBall())
    }

    @Test
    fun fumbledPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 5),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(14, 1),
            *throwBall(1.d6), // Fumbbl pass
        )
        assertEquals(PassingType.FUMBLED, state.getContext<PassContext>().passingResult)
        controller.rollForward(
            8.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        state.singleBall().assertCoordinates(15, 6)
    }

    @Test
    fun opponentMustSelectOneToIntercept() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(13, 9),
            *throwBall(6.d6),
        )
        val interceptors = controller.getAvailableActions().singleInstanceOf<SelectPlayer>().players
        assertEquals(5, interceptors.size)
        controller.rollForward(
            PlayerSelected("H2".playerId),
            6.d6 // Intercept
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun runningInterference_requiresStandingWithTackleZone() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(13, 9),
            *throwBall(6.d6),
        )
        val interceptors = controller.getAvailableActions().singleInstanceOf<SelectPlayer>().players
        assertEquals(5, interceptors.size)

        // Make some players invalid for selection
        homeTeam["H2".playerId].state = PlayerState.PRONE
        homeTeam["H3".playerId].state = PlayerState.STUNNED
        homeTeam["H4".playerId].makeDistracted()

        val modifiedInterceptors = controller.getAvailableActions().singleInstanceOf<SelectPlayer>().players
        assertEquals(2, modifiedInterceptors.size)
    }

    @Test
    fun failingToDoPassingInterferenceContinuesThrow() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(13, 9),
            *throwBall(6.d6),
            PlayerSelected("H2".playerId), // Select Interceptor
            2.d6, // Fail to deflect
            *catch(6.d6),
        )
        assertTrue(awayTeam["A5".playerId].hasBall())
    }

    @Test
    fun chooseNotToDeflect() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(13, 9),
            *throwBall(6.d6),
            Cancel, // Do not deflect
            *catch(6.d6),
        )
        assertTrue(awayTeam["A5".playerId].hasBall())
    }

    @Test
    fun intercept_accuratePass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(13, 9),
            *throwBall(6.d6),
        )
        assertEquals(PassingType.ACCURATE, state.getContext<PassContext>().passingResult)
        homeTeam["H2".playerId].agility = 1 // Make interceptor super human
        awayTeam["A1".playerId].makeDistracted() // Remove mark
        awayTeam["A2".playerId].makeDistracted()  // Remove mark
        awayTeam["A3".playerId].makeDistracted()  // Remove mark
        controller.rollForward(
            PlayerSelected("H2".playerId), // Select Interceptor
            3.d6, // Intercept (with -3 modifier) - Will fail
        )
        assertFalse(state.getContext<PassContext>().intercept!!.didIntercept)
        controller.rollForward(
            Undo,
            4.d6, // Intercept - Will succeed
        )
        assertEquals(homeTeam, state.activeTeam)
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun intercept_inaccuratePass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(13, 10),
            *throwBall(4.d6), // Inaccurate Pass
            DiceRollResults(4.d8, 2.d8, 5.d8) // Scatter back to the original target
        )
        assertEquals(PassingType.INACCURATE, state.getContext<PassContext>().passingResult)
        homeTeam["H2".playerId].agility = 1 // Make interceptor super human
        awayTeam["A1".playerId].makeDistracted()  // Remove mark
        awayTeam["A2".playerId].makeDistracted()  // Remove mark
        awayTeam["A3".playerId].makeDistracted()  // Remove mark
        controller.rollForward(
            PlayerSelected("H2".playerId), // Select Interceptor
            2.d6, // Intecept (with -2 modifier) - Will fail
        )
        assertFalse(state.getContext<PassContext>().intercept!!.didIntercept)
        controller.rollForward(
            Undo,
            3.d6, // Intercept - Will succeed
        )
        assertEquals(homeTeam, state.activeTeam)
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun intercept_markedModifiers() {
        awayTeam["A1".playerId].state = PlayerState.PRONE
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(9, 9),
            *throwBall(3.d6), // Pass - Inaccurate
            DiceRollResults(5.d8, 5.d8, 5.d8) // Scatter to [12, 9]
        )
        assertEquals(PassingType.INACCURATE, state.getContext<PassContext>().passingResult)
        assertEquals(2, rules.calculateMarks(state, homeTeam, homeTeam["H2".playerId].coordinates))
        homeTeam["H2".playerId].agility = 1 // Make interceptor superhuman
        controller.rollForward(
            PlayerSelected("H2".playerId), // Select Interceptor
            4.d6, // Intercept (with -4 modifier) - Will fail
        )
        assertFalse(state.getContext<PassContext>().intercept!!.didIntercept)
        controller.rollForward(
            Undo,
            5.d6, // Intercept - Will succeed
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun throwContinuesIfFailingToIntercept() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(14, 9),
            *throwBall(6.d6),
        )
        controller.rollForward(
            PlayerSelected("H2".playerId), // Select Interceptor
            2.d6, // Fail Intercept
            2.d8 // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(PitchCoordinate(14, 8), state.singleBall().coordinates)
    }

    @Test
    fun successfulInterception() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(13, 9),
            *throwBall(6.d6),
            PlayerSelected("H2".playerId), // Select Interceptor
            6.d6, // Intercept
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
        assertFalse(awayTeam["A10".playerId].hasBall())
        assertEquals(homeTeam, state.activeTeam)
        assertNull(state.activePlayer)
    }
}
