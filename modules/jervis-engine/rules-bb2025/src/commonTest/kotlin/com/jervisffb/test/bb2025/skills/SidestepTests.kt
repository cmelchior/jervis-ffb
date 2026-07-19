package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Sidestep
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.makeDistracted
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Sidestep] skill.
 */
class SidestepTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun allEmptyAdjacentSquaresAreAvailable() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.SIDESTEP)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
        )
        assertEquals(homeTeam, controller.getAvailableActions().team)
        controller.rollForward(
            Confirm // Use Sidestep
        )
        val actions = controller.getAvailableActions()
        assertEquals(5, actions.get<SelectDirection>().directions.size)
        assertEquals(homeTeam, actions.team)
        controller.rollForward(
            DirectionSelected(Direction.UP_RIGHT),
            Cancel // Do not follow up
        )
        defender.assertCoordinates(13, 4)
        defender.assertStanding()
        assertNull(state.activePlayer)
    }

    @Test
    fun notAvailableIfNoEmptyAdjacentSquares() {
        SetPlayerLocation(homeTeam[5.playerNo], PitchCoordinate(11, 4)).execute(state)
        SetPlayerLocation(homeTeam[4.playerNo], PitchCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[6.playerNo], PitchCoordinate(11, 6)).execute(state)
        SetPlayerLocation(homeTeam[7.playerNo], PitchCoordinate(12, 4)).execute(state)
        SetPlayerLocation(homeTeam[8.playerNo], PitchCoordinate(13, 4)).execute(state)
        homeTeam[7.playerNo].putProne()
        homeTeam[8.playerNo].putProne()
        val defender = homeTeam[1.playerNo]
        defender.addSkill(SkillType.SIDESTEP)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
        )
        val actions = controller.getAvailableActions()
        assertEquals(awayTeam, actions.team)
        assertEquals(3, actions.get<SelectDirection>().directions.size)
        controller.rollForward(
            DirectionSelected(Direction.LEFT),
            DirectionSelected(Direction.LEFT),
            Cancel
        )
        assertNull(state.activePlayer)
        defender.assertStanding()
        defender.assertCoordinates(11, 5)
    }

    @Test
    fun notAvailableIfProne() {
        val player = homeTeam[4.playerNo]
        SetPlayerLocation(player, PitchCoordinate(11, 4)).execute(state)
        SetPlayerLocation(homeTeam[5.playerNo], PitchCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[6.playerNo], PitchCoordinate(11, 6)).execute(state)
        player.apply {
            addSkill(SkillType.SIDESTEP)
            putProne()
        }
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.UP_LEFT),
        )
        // Player chain-pushed is prone, so cannot use sidestep
        val actions = controller.getAvailableActions()
        assertEquals(awayTeam, actions.team)
        assertEquals(3, actions.get<SelectDirection>().directions.size)
        controller.rollForward(
            DirectionSelected(Direction.LEFT),
            Cancel // Do not follow up
        )
        player.assertProne()
        player.assertCoordinates(10, 4)
        assertNull(state.activePlayer)
    }

    @Test
    fun workDuringChainPush() {
        val player = homeTeam[4.playerNo]
        SetPlayerLocation(player, PitchCoordinate(11, 4)).execute(state)
        SetPlayerLocation(homeTeam[5.playerNo], PitchCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[6.playerNo], PitchCoordinate(11, 6)).execute(state)
        player.addSkill(SkillType.SIDESTEP)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.UP_LEFT),
        )
        assertEquals(homeTeam, controller.getAvailableActions().team)
        controller.rollForward(
            Confirm // Use Sidestep
        )
        assertEquals(6, controller.getAvailableActions().get<SelectDirection>().directions.size)
        controller.rollForward(
            DirectionSelected(Direction.RIGHT),
            Cancel // Do not follow up
        )
        player.assertStanding()
        player.assertCoordinates(12, 4)
        assertNull(state.activePlayer)
    }

    @Test
    fun doesNotWorkIfDistracted() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.apply {
            addSkill(SkillType.SIDESTEP)
            makeDistracted()
        }
        assertTrue(rules.isDistracted(defender))
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
        )
        val actions = controller.getAvailableActions()
        assertEquals(3, actions.get<SelectDirection>().directions.size)
        assertEquals(awayTeam, actions.team)
        controller.rollForward(
            DirectionSelected(Direction.LEFT),
            Cancel // Do not follow up
        )
        defender.assertCoordinates(11, 5)
        assertEquals(PlayerPitchState.STANDING, defender.state)
        assertNull(state.activePlayer)
    }
}
