package com.jervisffb.test.bb2025.actions

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.singleInstanceOf
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class responsible for testing the Secure the Ball action.
 * See page 56 in the BB2025 rulebook.
 */
class SecureTheBallActionTests : JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun notAllowedIfBigGuy() {
        val player = state.getPlayerById("A8".playerId)
        player.keywords.add(PlayerKeyword.BIG_GUY)
        controller.rollForward(PlayerSelected(player.id))
        val availableActions = controller.getAvailableActions()
            .singleInstanceOf<SelectPlayerAction>()
            .actions
            .map { it.type }

        assertFalse(availableActions.contains(PlayerStandardActionType.SECURE_THE_BALL))
    }

    // Securing the ball ends the players action and marks it as used.
    @Test
    fun successfulPickup() {
        val player = state.getPlayerById("A8".playerId)

        controller.rollForward(
            *activatePlayer("A8", PlayerStandardActionType.SECURE_THE_BALL),
            SmartMoveTo(17, 7),
            2.d6, // Minimum roll needed is 2+
            NoRerollSelected()
        )

        assertTrue(player.hasBall())
        assertNull(state.activePlayer)
        assertEquals(Availability.HAS_ACTIVATED, player.available)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun failedPickupIsTurnover() {
        val player = state.getPlayerById("A8".playerId)
        controller.rollForward(
            *activatePlayer("A8", PlayerStandardActionType.SECURE_THE_BALL),
            SmartMoveTo(17, 7),
            1.d6, // Failed pickup
            NoRerollSelected(),
            2.d8 // Bounce
        )

        assertFalse(player.hasBall())
        assertEquals(homeTeam, state.activeTeam)
    }

    // If the pickup fails and ends up in the hands of another team player, it is still a turnover
    @Test
    fun turnoverIfAnotherPlayerCatchesIt() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 6),
            EndAction,
            *activatePlayer("A8", PlayerStandardActionType.SECURE_THE_BALL),
            SmartMoveTo(17, 7),
            1.d6, // Failed roll
            NoRerollSelected(),
            2.d8, // Bounce
            *catch(6.d6)
        )
        assertTrue(awayTeam["A10".playerId].hasBall())
        assertEquals(state.homeTeam, state.activeTeam)
    }

    @Test
    fun turnoverIfPlayerDoesNotHaveBallWhenActionEnds() {
        controller.rollForward(
            *activatePlayer("A8", PlayerStandardActionType.SECURE_THE_BALL),
            *moveTo(15, 12), // We need to move; otherwise, the action is just treated as canceled.
            EndAction
        )
        assertNull(state.activePlayer)
        assertFalse(awayTeam["A8".playerId].hasBall())
        assertEquals(state.homeTeam, state.activeTeam)
    }

    @Ignore // Need support for Shadowing
    @Test
    fun markedModifierApplies() {
        // TODO
    }

    @Test
    fun pouringRainModifierApplies() {
        val player = state.getPlayerById("A8".playerId)
        state.weather = Weather.POURING_RAIN
        controller.rollForward(
            *activatePlayer("A8", PlayerStandardActionType.SECURE_THE_BALL),
            SmartMoveTo(17, 7),
            2.d6,
            TeamRerollSelected<RegularTeamReroll>(),
            3.d6
        )
        assertTrue(player.hasBall())
    }

    @Test
    fun notAllowedIfStandingPlayerWithTackleZonesWithinTwoSquares() {
        SetBallLocation(state.singleBall(), FieldCoordinate(14, 7)).execute(state)

        val player = state.getPlayerById("A8".playerId)
        val opponent = state.getPlayerById("H3".playerId)
        assertTrue(opponent.hasTackleZones)
        opponent.assertStanding()

        controller.rollForward(PlayerSelected(player.id))

        val availableActions = controller.getAvailableActions()
            .singleInstanceOf<SelectPlayerAction>()
            .actions
            .map { it.type }

        assertFalse(availableActions.contains(PlayerStandardActionType.SECURE_THE_BALL))
    }

    @Test
    fun actionAvailableIfDistractedOpponentWithinTwoSquares() {
        SetBallLocation(state.singleBall(), FieldCoordinate(14, 7)).execute(state)
        homeTeam.forEach { player ->
            if (player.location.isOnField(rules)) {
                player.makeDistracted()
                assertTrue(rules.isDistracted(player), "Player ${player.id} is not distracted")
            }
        }
        assertEquals(homeTeam, state.field[12, 7].player!!.team)

        val player = state.getPlayerById("A8".playerId)
        controller.rollForward(PlayerSelected(player.id))
        val availableActions = controller.getAvailableActions()
            .singleInstanceOf<SelectPlayerAction>()
            .actions
            .map { it.type }

        assertTrue(availableActions.contains(PlayerStandardActionType.SECURE_THE_BALL))
    }

    @Test
    fun cancelBeforeMoveDoesNotUseAction() {
        val player = state.getPlayerById("A8".playerId)
        assertEquals(1, state.awayTeam.turnData.secureTheBallActions)

        controller.rollForward(
            *activatePlayer("A8", PlayerStandardActionType.SECURE_THE_BALL),
            EndAction
        )

        assertEquals(1, state.awayTeam.turnData.secureTheBallActions)
        assertEquals(Availability.AVAILABLE, player.available)
        assertEquals(awayTeam, state.activeTeam)
    }
}
