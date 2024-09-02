package dk.ilios.jervis.tables

import dk.ilios.jervis.GameFlowTests
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.activatePlayer
import dk.ilios.jervis.defaultFanFactor
import dk.ilios.jervis.defaultKickOffHomeTeam
import dk.ilios.jervis.defaultPregame
import dk.ilios.jervis.defaultSetup
import dk.ilios.jervis.endTurns
import dk.ilios.jervis.ext.d3
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.ext.d8
import dk.ilios.jervis.ext.playerId
import dk.ilios.jervis.model.DogOut
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.context.CatchRollContext
import dk.ilios.jervis.model.context.PickupRollContext
import dk.ilios.jervis.model.context.RushRollContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.modifiers.AccuracyModifier
import dk.ilios.jervis.model.modifiers.CatchModifier
import dk.ilios.jervis.model.modifiers.PickupModifier
import dk.ilios.jervis.model.modifiers.RushModifier
import dk.ilios.jervis.moveTo
import dk.ilios.jervis.procedures.FullGame
import dk.ilios.jervis.procedures.actions.pass.PassContext
import dk.ilios.jervis.procedures.actions.pass.PassingType
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.tables.Range
import dk.ilios.jervis.rules.tables.Weather
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail

class WeatherTests: GameFlowTests() {

    @Test
    fun weatherRollChangesWeather() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultFanFactor(),
            DiceResults(6.d6, 6.d6), // Weather roll
        )
        assertEquals(Weather.BLIZZARD, state.weather)
    }

    @Test
    fun swelteringHeat() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                weatherRoll = DiceResults(1.d6, 1.d6), // Weather roll
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *endTurns(16),
            2.d3, // Home Heat roll
            RandomPlayersSelected(listOf(
                homeTeam[PlayerNo(1)]!!,
                homeTeam[PlayerNo(2)]!!,
            )),
            1.d3, // Away Heat roll
            RandomPlayersSelected(listOf(awayTeam[PlayerNo(1)]!!)),
        )
        assertEquals(Weather.SWELTERING_HEAT, state.weather)
        assertEquals(2, state.halfNo) // We are at the start of 2nd drive.
        listOf(
            homeTeam[PlayerNo(1)]!!,
            homeTeam[PlayerNo(2)]!!,
            awayTeam[PlayerNo(1)]!!,
        ).forEach { player ->
            assertEquals(PlayerState.FAINTED, player.state, "Player $player")
            assertEquals(DogOut, player.location, "Player $player")
        }
        assertEquals(2, homeTeam.filter { it.state == PlayerState.FAINTED }.size)
        assertEquals(1, awayTeam.filter { it.state == PlayerState.FAINTED }.size)
    }

    @Test
    fun verySunny_throwBall() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                weatherRoll = DiceResults(1.d6, 2.d6)
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup ball
            NoRerollSelected,
            Confirm,
            FieldSquareSelected(18, 7), // 1 Field away = Quick Pass
            4.d6 // Roll for Accuracy roll (should be 5+ to be accurate)
        )
        val context = state.getContext<PassContext>()
        assertContains(context.passingModifiers, AccuracyModifier.VERY_SUNNY)
        assertEquals(PassingType.INACCURATE, context.passingResult)
    }

    @Test
    @Ignore // Bomb not implemented yet
    fun verySunny_throwBomb() {
        TODO()
    }

    @Test
    fun pouringRain_catchRoll() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                weatherRoll = DiceResults(5.d6, 6.d6)
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                deviate = DiceResults(4.d8, 3.d6), // Land on A10 at [16,7]
                bounce = null
            ),
            4.d6 // Attempt to catch the ball. Should fail due to -2 to catch.
        )
        val context = state.getContext<CatchRollContext>()
        assertContains(context.modifiers, CatchModifier.POURING_RAIN)
        assertFalse(context.isSuccess)
    }

    @Test
    fun pouringRain_pickupRoll() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                weatherRoll = DiceResults(5.d6, 6.d6)
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerActionType.MOVE),
            *moveTo(17, 7),
            3.d6 // Attempt to pick up the ball. Should fail due to -1 to pickup.
        )
        val context = state.getContext<PickupRollContext>()
        assertContains(context.modifiers, PickupModifier.POURING_RAIN)
        assertFalse(context.isSuccess)
    }

    @Test
    fun blizzard_rushRoll() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                weatherRoll = DiceResults(6.d6, 6.d6)
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *activatePlayer("A11", PlayerActionType.MOVE),
            *moveTo(23, 7),
            *moveTo(24, 7),
            *moveTo(25, 7),
            *moveTo(25, 6),
            *moveTo(25, 5),
            *moveTo(25, 4),
            *moveTo(25, 3),
            *moveTo(25, 2), // Rush
            2.d6 // Rush roll
        )
        val context = state.getContext<RushRollContext>()
        assertContains(context.modifiers, RushModifier.BLIZZARD)
        assertFalse(context.isSuccess)
    }

    @Test
    fun blizzard_restrictPassRange() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                weatherRoll = DiceResults(6.d6, 6.d6)
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup ball
            NoRerollSelected,
            Confirm,
        )

        // Check that no squares outside the valid range can be selected.
        controller.getAvailableActions().filterIsInstance<SelectFieldLocation>().forEach {
            val range = rules.rangeRuler.measure(FieldCoordinate(17, 7), it.coordinate)
            if (range != Range.QUICK_PASS && range != Range.SHORT_PASS) {
                fail("Invalid range: $range for ${it.coordinate}")
            }
        }
    }
}
