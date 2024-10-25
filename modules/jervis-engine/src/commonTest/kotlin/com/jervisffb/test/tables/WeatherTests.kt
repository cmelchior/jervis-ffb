package com.jervisffb.test.tables

import com.jervisffb.test.JervisGameTest
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultFanFactor
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.CatchRollContext
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.AccuracyModifier
import com.jervisffb.engine.model.modifiers.CatchModifier
import com.jervisffb.engine.model.modifiers.PickupModifier
import com.jervisffb.engine.model.modifiers.RushModifier
import com.jervisffb.test.moveTo
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.tables.Range
import com.jervisffb.engine.rules.bb2020.tables.Weather
import com.jervisffb.test.skipTurns
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail

class WeatherTests: JervisGameTest() {

    @Test
    fun weatherRollChangesWeather() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultFanFactor(),
            DiceRollResults(6.d6, 6.d6), // Weather roll
        )
        assertEquals(Weather.BLIZZARD, state.weather)
    }

    @Test
    fun swelteringHeat() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                weatherRoll = DiceRollResults(1.d6, 1.d6), // Weather roll
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *skipTurns(16),
            2.d3, // Home Heat roll
            RandomPlayersSelected(listOf(
                homeTeam[PlayerNo(1)].id,
                homeTeam[PlayerNo(2)].id,
            )),
            1.d3, // Away Heat roll
            RandomPlayersSelected(listOf(awayTeam[PlayerNo(1)].id)),
        )
        assertEquals(Weather.SWELTERING_HEAT, state.weather)
        assertEquals(2, state.halfNo) // We are at the start of 2nd drive.
        listOf(
            homeTeam[PlayerNo(1)],
            homeTeam[PlayerNo(2)],
            awayTeam[PlayerNo(1)],
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
                weatherRoll = DiceRollResults(1.d6, 2.d6)
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup ball
            NoRerollSelected(),
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
                weatherRoll = DiceRollResults(5.d6, 6.d6)
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                deviate = DiceRollResults(4.d8, 3.d6), // Land on A10 at [16,7]
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
                weatherRoll = DiceRollResults(5.d6, 6.d6)
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
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
                weatherRoll = DiceRollResults(6.d6, 6.d6)
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *activatePlayer("A11", PlayerStandardActionType.MOVE),
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
                weatherRoll = DiceRollResults(6.d6, 6.d6)
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup ball
            NoRerollSelected(),
            Confirm,
        )

        // Check that no squares outside the valid range can be selected.
        controller.getAvailableActions().actions.filterIsInstance<SelectFieldLocation>().forEach {
            val range = rules.rangeRuler.measure(FieldCoordinate(17, 7), it.coordinate)
            if (range != Range.QUICK_PASS && range != Range.SHORT_PASS) {
                fail("Invalid range: $range for ${it.coordinate}")
            }
        }
    }
}
