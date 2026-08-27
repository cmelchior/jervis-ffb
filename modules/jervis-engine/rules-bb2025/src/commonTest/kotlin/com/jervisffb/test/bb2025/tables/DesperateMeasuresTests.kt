package com.jervisffb.test.bb2025.tables

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.InducementEffectSelected
import com.jervisffb.engine.actions.InducementSelection
import com.jervisffb.engine.actions.InducementsSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.bb2025.inducements.effects.Hangover
import com.jervisffb.engine.bb2025.modifiers.PlayerStatusEffectType2025
import com.jervisffb.engine.bb2025.procedures.SetupTeam
import com.jervisffb.engine.common.inducements.InducementSelectionCommon
import com.jervisffb.engine.common.inducements.InducementTypeCommon
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.test.JervisGameBB72025Test
import com.jervisffb.test.defaultDetermineKickingTeam
import com.jervisffb.test.defaultFanFactor
import com.jervisffb.test.defaultWeather
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.skipTurns
import com.jervisffb.test.utils.assertReserves
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * This class is testing all the results on the Prayer to Nuffle Table.
 */
class DesperateMeasuresTests: JervisGameBB72025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        homeTeam.currentTeamValue = 1_050_000
        awayTeam.currentTeamValue = 1_000_000
    }

    // This assumes that it is the away team buying inducements
    private fun buyInducements(vararg inducements: InducementSelection<*>): Array<GameAction> {
        return buildList {
            addAll(defaultFanFactor())
            add(defaultWeather())
            add(InducementsSelected(inducements.toList()))
        }.toTypedArray()
    }

    private fun applyHangoverTo(player: Player) {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementTypeCommon.DESPERATE_MEASURES, 1)
            ),
            3.d8,
            *defaultDetermineKickingTeam(),
        )
        assertTrue(controller.getAvailableActions().contains<CancelWhenReady>())
        val hangover = assertIs<Hangover>(awayTeam.specialPlayCards.single())
        controller.rollForward(
            InducementEffectSelected(hangover.id),
            PlayerSelected(player.id),
        )
    }

    private fun startFirstDriveWithoutHungoverPlayer() {
        controller.rollForward(
            *defaultBB7HomeSetup().drop(2).toTypedArray(),
            *defaultBB7AwaySetup(),
            *defaultKickOffHomeTeam(selectKicker = PlayerSelected("H2".playerId)).filterNotNull().toTypedArray(),
        )
    }

    @Test
    fun hangover_onlyAtBeginningOfGame() {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementTypeCommon.DESPERATE_MEASURES, 1)
            ),
            3.d8,
        )

        val hangover = assertIs<Hangover>(awayTeam.specialPlayCards.single())
        assertEquals(listOf(Timing.BEFORE_FIRST_SETUP), hangover.triggers)
    }

    @Test
    fun hangover_missesDrive() {
        val player = homeTeam["H1".playerId]
        applyHangoverTo(player)

        player.assertReserves()
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType2025.HANGOVER))
        assertFalse(
            controller.getAvailableActions().get<SelectPlayer>().players.contains(player.id)
        )
        assertEquals(SetupTeam.SelectPlayerOrEndSetup, controller.currentNode())

        startFirstDriveWithoutHungoverPlayer()

        player.assertReserves()
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType2025.HANGOVER))
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun hangover_readyForNextDrive() {
        val player = homeTeam["H1".playerId]
        applyHangoverTo(player)
        startFirstDriveWithoutHungoverPlayer()

        controller.rollForward(
            *skipTurns(rules.turnsPrHalf * 2),
            *defaultBB7AwaySetup(),
        )

        player.assertReserves()
        assertFalse(player.hasStatusEffect(PlayerStatusEffectType2025.HANGOVER))
        assertTrue(
            controller.getAvailableActions().get<SelectPlayer>().players.contains(player.id)
        )
        assertEquals(SetupTeam.SelectPlayerOrEndSetup, controller.currentNode())
    }
}
