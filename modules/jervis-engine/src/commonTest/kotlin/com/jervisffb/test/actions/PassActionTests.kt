package com.jervisffb.test.actions

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.rushTo
import com.jervisffb.test.utils.firstInstanceOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class responsible for testing the Pass action as described on page 48-51 in the rulebook.
 */
class PassActionTests: JervisGameTest() {

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
            4.d6, // Pickup
            NoRerollSelected(),
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            6.d6, // Throw
            NoRerollSelected(),
            5.d6, // Catch
            NoRerollSelected(),
        )
        assertFalse(awayTeam["A10".playerId].hasBall())
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun actionEndsAfterPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            6.d6, // Throw
            NoRerollSelected(),
            5.d6, // Catch
            NoRerollSelected(),
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
            4.d6, // Pickup
            NoRerollSelected(),
            Confirm, // Start pass
            Cancel, // Cancel pass action
            Confirm, // Restart pass
            FieldSquareSelected(15, 1),
            6.d6, // Throw
            NoRerollSelected(),
            5.d6, // Catch
            NoRerollSelected(),
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
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(23, 7),
            *rushTo(24, 7),
            *rushTo(25, 7),
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            6.d6, // Throw
            NoRerollSelected(),
            5.d6, // Catch
            NoRerollSelected(),
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
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            6.d6, // Throw
            NoRerollSelected(),
            5.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun bounceFromTargetIfCatchFailed() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            6.d6, // Throw
            NoRerollSelected(),
            1.d6, // Fail catch
            NoRerollSelected(),
            7.d8
        )
        assertEquals(FieldCoordinate(15, 2), state.singleBall().location)
    }

    @Test
    fun quickPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(16, 4),
        )
        assertEquals(
            Range.QUICK_PASS,
            rules.rangeRuler.measure(awayTeam["A10".playerId], FieldCoordinate(15, 1))
        )
        controller.rollForward(
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            4.d6, // Throw
            NoRerollSelected(),
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun shortPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(16, 5),
        )
        assertEquals(
            Range.SHORT_PASS,
            rules.rangeRuler.measure(awayTeam["A10".playerId], FieldCoordinate(15, 1))
        )
        controller.rollForward(
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            5.d6, // Throw
            NoRerollSelected(),
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun longPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(16, 8),
        )
        assertEquals(
            Range.LONG_PASS,
            rules.rangeRuler.measure(awayTeam["A10".playerId], FieldCoordinate(15, 1))
        )
        controller.rollForward(
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            6.d6, // Throw
            NoRerollSelected(),
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun longBomb() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(13, 13),
        )
        assertEquals(
            Range.LONG_BOMB,
            rules.rangeRuler.measure(awayTeam["A10".playerId], FieldCoordinate(15, 1))
        )
        controller.rollForward(
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            6.d6, // Throw
            NoRerollSelected(),
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun canTargetAllSquaresInRange() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            Confirm
        )
        val targets = controller.getAvailableActions().firstInstanceOf<SelectFieldLocation>()
        assertTrue(targets.squares.all { it.type == TargetSquare.Type.THROW_TARGET })
        assertEquals(309, targets.squares.map { it.coordinate }.toSet().size)
    }

    @Test
    fun markedModifiersToThrower() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(13, 4)
        )
        assertTrue(rules.isMarked(awayTeam["A10".playerId]))
        assertEquals(
            Range.QUICK_PASS,
            rules.rangeRuler.measure(awayTeam["A10".playerId], FieldCoordinate(14, 1))
        )
        controller.rollForward(
            Confirm, // Start pass
            FieldSquareSelected(14, 1),
            4.d6, // Throw Quick pass (-1 marked modifier), needs 5+
            NoRerollSelected(),
        )
        assertEquals(BallState.SCATTERED, state.singleBall().state)
    }

    @Test
    fun accuratePass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(14, 4),
            Confirm, // Start pass
            FieldSquareSelected(14, 1),
            4.d6, // Throw Quick pass, needs 4+
            NoRerollSelected(),
        )
        assertEquals(PassingType.ACCURATE, state.getContext<PassContext>().passingResult)
        controller.rollForward(
            4.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A6".playerId].hasBall())
    }

    @Test
    fun inaccuratePass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(14, 4),
            Confirm, // Start pass
            FieldSquareSelected(14, 1),
            3.d6, // Throw Quick pass, needs 4+
            NoRerollSelected(),
        )
        assertEquals(PassingType.INACCURATE, state.getContext<PassContext>().passingResult)
        controller.rollForward(
            DiceRollResults(2.d8, 8.d8, 4.d8), // Scatter
            4.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A6".playerId].hasBall())
    }

    @Test
    fun wildlyInaccuratePass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(14, 5),
            Confirm, // Start pass
            FieldSquareSelected(14, 1),
            2.d6, // Throw Short pass, needs 5+ (-1 modifier)
            NoRerollSelected(),
        )
        assertEquals(PassingType.WILDLY_INACCURATE, state.getContext<PassContext>().passingResult)
        controller.rollForward(
            DiceRollResults(2.d8, 2.d6), // Deviate
            8.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(15,4), state.singleBall().location)
    }

    @Test
    fun fumbledPass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(14, 5),
            Confirm, // Start pass
            FieldSquareSelected(14, 1),
            1.d6, // Fumbbl pass
            NoRerollSelected(),
        )
        assertEquals(PassingType.FUMBLED, state.getContext<PassContext>().passingResult)
        controller.rollForward(
            8.d8, // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(15,6), state.singleBall().location)
    }

    @Test
    fun opponentMustSelectOneToRunInterference() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
        )
        val interceptors = controller.getAvailableActions().firstInstanceOf<SelectPlayer>().players
        assertEquals(5, interceptors.size)
        controller.rollForward(
            PlayerSelected("H2".playerId),
            6.d6, // Deflect,
            6.d6 // Intercept
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun runningInterference_requiresStandingWithTackleZone() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
        )
        val interceptors = controller.getAvailableActions().firstInstanceOf<SelectPlayer>().players
        assertEquals(5, interceptors.size)

        // Make some players invalid for selection
        homeTeam["H2".playerId].state = PlayerState.PRONE
        homeTeam["H3".playerId].state = PlayerState.STUNNED
        homeTeam["H4".playerId].hasTackleZones = false

        val modifiedInterceptors = controller.getAvailableActions().firstInstanceOf<SelectPlayer>().players
        assertEquals(2, modifiedInterceptors.size)
    }

    @Test
    fun failingToDoPassingInterferenceContinuesThrow() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
            PlayerSelected("H2".playerId), // Select Interceptor
            2.d6, // Fail to deflect
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A5".playerId].hasBall())
    }

    @Test
    fun chooseNotToDeflect() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
            Cancel, // Do not deflect
            6.d6, // Catch
            NoRerollSelected(),
        )
        assertTrue(awayTeam["A5".playerId].hasBall())
    }

    @Test
    fun runningInterference_accuratePass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
        )
        assertEquals(PassingType.ACCURATE, state.getContext<PassContext>().passingResult)
        homeTeam["H2".playerId].agility = 1 // Make interceptor super human
        awayTeam["A1".playerId].hasTackleZones = false // Remove mark
        awayTeam["A2".playerId].hasTackleZones = false // Remove mark
        awayTeam["A3".playerId].hasTackleZones = false // Remove mark
        controller.rollForward(
            PlayerSelected("H2".playerId), // Select Interceptor
            3.d6, // Deflect (with -3 modifier) - Will fail
        )
        assertFalse(state.getContext<PassContext>().passingInterference!!.didDeflect)
        controller.rollForward(
            Undo,
            4.d6, // Deflect - Will succeed
            2.d6, // Catch (with -1 modifier)
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun runningInterference_inaccuratePass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 10),
            4.d6, // Pass - Inaccurate
            NoRerollSelected(),
            DiceRollResults(4.d8, 2.d8, 5.d8) // Scatter back to the original target
        )
        assertEquals(PassingType.INACCURATE, state.getContext<PassContext>().passingResult)
        homeTeam["H2".playerId].agility = 1 // Make interceptor super human
        awayTeam["A1".playerId].hasTackleZones = false // Remove mark
        awayTeam["A2".playerId].hasTackleZones = false // Remove mark
        awayTeam["A3".playerId].hasTackleZones = false // Remove mark
        controller.rollForward(
            PlayerSelected("H2".playerId), // Select Interceptor
            2.d6, // Deflect (with -2 modifier) - Will fail
        )
        assertFalse(state.getContext<PassContext>().passingInterference!!.didDeflect)
        controller.rollForward(
            Undo,
            3.d6, // Deflect - Will succeed
            2.d6, // Catch (with -1 modifier)
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun runningInterference_wildlyInaccuratePass() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 10),
            2.d6, // Pass - Wildly inaccurate
            NoRerollSelected(),
            DiceRollResults(7.d8, 6.d6) // Deviate to [12, 9]
        )
        assertEquals(PassingType.WILDLY_INACCURATE, state.getContext<PassContext>().passingResult)
        homeTeam["H2".playerId].agility = 2 // Make interceptor super human
        awayTeam["A1".playerId].hasTackleZones = false // Remove mark
        awayTeam["A2".playerId].hasTackleZones = false // Remove mark
        awayTeam["A3".playerId].hasTackleZones = false // Remove mark
        awayTeam["A4".playerId].hasTackleZones = false // Remove mark
        awayTeam["A5".playerId].hasTackleZones = false // Remove mark
        controller.rollForward(
            PlayerSelected("H2".playerId), // Select Interceptor
            2.d6, // Deflect (with -1 modifier) - Will fail
        )
        assertFalse(state.getContext<PassContext>().passingInterference!!.didDeflect)
        controller.rollForward(
            Undo,
            3.d6, // Deflect - Will succeed
            3.d6, // Catch (with -1 modifier)
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun runningInterference_markedModifiers() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 10),
            2.d6, // Pass - Wildly inaccurate
            NoRerollSelected(),
            DiceRollResults(7.d8, 6.d6) // Deviate to [12, 9]
        )
        assertEquals(PassingType.WILDLY_INACCURATE, state.getContext<PassContext>().passingResult)
        assertEquals(3, rules.calculateMarks(state, homeTeam, homeTeam["H2".playerId].coordinates))
        homeTeam["H2".playerId].agility = 1 // Make interceptor super human
        controller.rollForward(
            PlayerSelected("H2".playerId), // Select Interceptor
            4.d6, // Deflect (with -4 modifier) - Will fail
        )
        assertFalse(state.getContext<PassContext>().passingInterference!!.didDeflect)
        controller.rollForward(
            Undo,
            5.d6, // Deflect - Will succeed
            5.d6, // Catch (with -4 modifier)
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun scatterIfFailedToIntercept() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
        )
        controller.rollForward(
            PlayerSelected("H2".playerId), // Select Interceptor
            6.d6, // Deflect
            2.d6, // Fail Intercept
            DiceRollResults(1.d8, 1.d8, 1.d8), // Scatter
            2.d8 // Bounce
        )
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(9, 2), state.singleBall().location)
    }

    @Test
    fun successfulInterception() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
            PlayerSelected("H2".playerId), // Select Interceptor
            6.d6, // Deflect
            6.d6, // Intercept
        )
        assertTrue(homeTeam["H2".playerId].hasBall())
        assertFalse(awayTeam["A10".playerId].hasBall())
        assertEquals(homeTeam, state.activeTeam)
        assertNull(state.activePlayer)
    }

    @Test
    fun passAction_deflectEndsUpOnOpponentTeam() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
            PlayerSelected("H2".playerId), // Select Interceptor
            6.d6, // Deflect
            2.d6, // Fail Intercept
            DiceRollResults(2.d8, 7.d8, 2.d8), // Scatter
            6.d6 // Catch
        )
        // Results in a turnover
        assertEquals(BallState.CARRIED, state.singleBall().state)
        assertTrue(homeTeam["H1".playerId].hasBall())
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun deflectBallGoingOutOfBounds() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(10, 0), // Empty square
            3.d6, // Inaccurate pass
            NoRerollSelected(),
            DiceRollResults(2.d8, 2.d8, 2.d8),
        )
        assertEquals(FieldCoordinate.OUT_OF_BOUNDS, state.getContext<PassContext>().target)
        assertEquals(FieldCoordinate.OUT_OF_BOUNDS, state.singleBall().location)
        controller.rollForward(
            PlayerSelected("H7".playerId), // Select Interceptor
            6.d6, // Deflect
        )
        controller.rollForward(
            2.d6, // Fail intercept
            NoRerollSelected(), // Do not use Catch
            DiceRollResults(4.d8, 4.d8, 4.d8), // Scatter from interceptor
            4.d8 // Bounce
        )
        // Turnover
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(6, 1), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun deflectCausesBallToScatterOutOfBounds() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(10, 0), // Empty square
            3.d6, // Inaccurate pass
            NoRerollSelected(),
            DiceRollResults(2.d8, 2.d8, 2.d8),
        )
        assertEquals(FieldCoordinate.OUT_OF_BOUNDS, state.getContext<PassContext>().target)
        assertEquals(FieldCoordinate.OUT_OF_BOUNDS, state.singleBall().location)
        controller.rollForward(
            PlayerSelected("H7".playerId), // Select Interceptor
            6.d6, // Deflect
        )
        controller.rollForward(
            2.d6, // Fail intercept
            NoRerollSelected(), // Do not use Catch
            DiceRollResults(1.d8, 1.d8, 1.d8), // Scatter from interceptor, goes out of bounds at [9, 0]
            2.d3, // Throw-in direction
            DiceRollResults(1.d6, 2.d6), // Throw-in distance
            4.d8 // Bounce
        )
        // Turnover
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(8, 3), state.singleBall().location)
        assertEquals(homeTeam, state.activeTeam)
    }
}
