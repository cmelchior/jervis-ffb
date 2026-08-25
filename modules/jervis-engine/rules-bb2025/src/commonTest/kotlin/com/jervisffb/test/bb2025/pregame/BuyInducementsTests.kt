package com.jervisffb.test.bb2020.pregame

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.InducementsSelected
import com.jervisffb.engine.actions.SelectInducements
import com.jervisffb.engine.bb2025.inducements.InducementType2025
import com.jervisffb.engine.bb2025.procedures.TeamTurn
import com.jervisffb.engine.bb2025.procedures.rerolls.ExtraTeamTrainingReroll
import com.jervisffb.engine.common.inducements.InducementSelectionCommon
import com.jervisffb.engine.common.inducements.InducementTypeCommon
import com.jervisffb.engine.common.procedures.DetermineKickingTeamStep
import com.jervisffb.engine.common.procedures.inducements.BuyInducements
import com.jervisffb.engine.utils.InvalidActionException
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.defaultFanFactor
import com.jervisffb.test.defaultJourneyMen
import com.jervisffb.test.defaultWeather
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class BuyInducementsTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        homeTeam.currentTeamValue = 1_000_000
        homeTeam.treasury = 100_000
        awayTeam.currentTeamValue = 1_000_000
        awayTeam.treasury = 100_000

    }

    private fun rollForwardToInducements() {
        controller.rollForward(
            *defaultFanFactor(),
            defaultWeather(),
            *defaultJourneyMen()
        )
    }

    @Test
    fun skipInducementsIfEqualCtv() {
        startDefaultGame()
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }

    @Test
    fun highCtvTeamBuyFirst() {
        homeTeam.currentTeamValue = 1_100_000
        rollForwardToInducements()
        assertEquals(BuyInducements.HigherCtvBuyPurchaseInducements, controller.currentNode())
        var actions = controller.getAvailableActions()
        actions.get<SelectInducements>().let {
            assertEquals(100_000, it.treasury)
            assertEquals(0, it.pettyCash)
        }
        controller.rollForward(
            Cancel
        )
        assertEquals(BuyInducements.LowerCtvBuyPurchaseInducements, controller.currentNode())
        actions = controller.getAvailableActions()
        actions.get<SelectInducements>().let {
            assertEquals(50_000, it.treasury)
            assertEquals(100_000, it.pettyCash)
        }
        controller.rollForward(
            Cancel
        )
        assertEquals(DetermineKickingTeamStep.SelectCoinSide, controller.currentNode())
    }


    @Test
    fun highCtvTeamCanUseTreasuryToBuyInducements() {
        homeTeam.currentTeamValue = 1_100_000
        homeTeam.treasury = 100_000
        rollForwardToInducements()
        controller.rollForward(
            InducementsSelected(
                listOf(InducementSelectionCommon.Simple(InducementType2025.BLITZERS_BEST_KEGS, 2))
            )
        )
        assertEquals(2, homeTeam.blitzersBestKegs)
        assertEquals(0, homeTeam.treasury)
        assertEquals(200_000, awayTeam.pettyCash)
    }

    @Test
    fun highCtv_invalidActionIfUsingTooMuchTreasury() {
        homeTeam.currentTeamValue = 1_100_000
        homeTeam.treasury = 50_000
        rollForwardToInducements()
        assertFailsWith<InvalidActionException> {
            controller.rollForward(
                InducementsSelected(
                    listOf(InducementSelectionCommon.Simple(InducementType2025.BLITZERS_BEST_KEGS, 2))
                )
            )
        }
    }

    @Test
    fun lowCtvTeamCanUseCtvDiff() {
        homeTeam.currentTeamValue = 1_100_000
        awayTeam.currentTeamValue = 1_000_000
        rollForwardToInducements()
        controller.rollForward(
            Cancel
        )
        assertEquals(100_000, awayTeam.pettyCash)
        controller.rollForward(
            InducementsSelected(
                listOf(InducementSelectionCommon.Simple(InducementType2025.BLITZERS_BEST_KEGS, 2))
            )
        )
        assertEquals(2, awayTeam.blitzersBestKegs)
        assertEquals(0, awayTeam.pettyCash)
        assertEquals(0, homeTeam.pettyCash)
    }

    @Test
    fun lowCtvTeamCanUseCtvDiffPlusTreasurySpend() {
        homeTeam.currentTeamValue = 1_100_000
        awayTeam.currentTeamValue = 1_000_000
        awayTeam.treasury = 100_000
        rollForwardToInducements()
        controller.rollForward(
            Cancel
        )
        controller.rollForward(
            InducementsSelected(
                listOf(
                    InducementSelectionCommon.Simple(InducementType2025.TEAM_MASCOT, 1),
                    InducementSelectionCommon.Simple(InducementTypeCommon.WEATHER_MAGE, 1),
                    InducementSelectionCommon.Simple(InducementTypeCommon.EXTRA_TEAM_TRAINING, 1),
                )
            )
        )
        assertEquals(1, awayTeam.mascots.size)
        assertEquals(1, awayTeam.weatherMages.size)
        assertNotNull(awayTeam.rerolls.single { it is ExtraTeamTrainingReroll })
        assertEquals(0, awayTeam.pettyCash)
        assertEquals(50_000, awayTeam.treasury)
        assertEquals(0, homeTeam.pettyCash)
    }

    @Test
    fun lowCtv_invalidActionIfUsingMoreThanAllowedTreasury() {
        homeTeam.currentTeamValue = 1_100_000
        awayTeam.currentTeamValue = 1_000_000
        awayTeam.treasury = 100_000
        rollForwardToInducements()
        controller.rollForward(
            Cancel
        )
        assertFailsWith<InvalidActionException> {
            controller.rollForward(
                InducementsSelected(
                    listOf(InducementSelectionCommon.Simple(InducementTypeCommon.EXTRA_TEAM_TRAINING, 2))
                )
            )
        }
    }

    @Test
    fun invalidActionIfBuyingMoreThanLimit() {
        homeTeam.currentTeamValue = 1_100_000
        awayTeam.currentTeamValue = 1_000_000
        awayTeam.treasury = 100_000
        rollForwardToInducements()
        controller.rollForward(
            Cancel
        )
        assertFailsWith<InvalidActionException> {
            controller.rollForward(
                InducementsSelected(
                    listOf(
                        InducementSelectionCommon.Simple(InducementType2025.BLITZERS_BEST_KEGS, 4),
                    )
                )
            )
        }
    }
}
