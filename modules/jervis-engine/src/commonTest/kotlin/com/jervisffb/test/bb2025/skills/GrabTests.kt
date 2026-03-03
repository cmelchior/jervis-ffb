package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Grab
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.blitzBlock
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [Grab] skill.
 */
class GrabTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun allEmptyAdjacentSquaresAreAvailable() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        attacker.addSkill(SkillType.GRAB)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
        )
        assertEquals(awayTeam, controller.getAvailableActions().team)
        controller.rollForward(
            Confirm // Use Grab
        )
        val actions = controller.getAvailableActions()
        assertEquals(5, actions.get<SelectDirection>().directions.size)
        assertEquals(awayTeam, actions.team)
        controller.rollForward(
            DirectionSelected(Direction.UP_RIGHT),
            Cancel // Do not follow up
        )
        assertEquals(FieldCoordinate(13, 4), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
        assertNull(state.activePlayer)
    }

    @Test
    fun notAvailableIfNoEmptyAdjacentSquares() {
        SetPlayerLocation(homeTeam[5.playerNo], FieldCoordinate(11, 4)).execute(state)
        SetPlayerLocation(homeTeam[4.playerNo], FieldCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[6.playerNo], FieldCoordinate(11, 6)).execute(state)
        SetPlayerLocation(homeTeam[7.playerNo], FieldCoordinate(12, 4)).execute(state)
        SetPlayerLocation(homeTeam[8.playerNo], FieldCoordinate(13, 4)).execute(state)
        homeTeam[7.playerNo].apply {
            state = PlayerState.PRONE
            hasTackleZones = false
        }
        homeTeam[8.playerNo].apply {
            state = PlayerState.PRONE
            hasTackleZones = false
        }
        val attacker = awayTeam[1.playerNo]
        val defender = homeTeam[1.playerNo]
        attacker.addSkill(SkillType.GRAB)
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
        assertEquals(PlayerState.STANDING, defender.state)
        assertEquals(FieldCoordinate(11, 5), defender.location)
    }

    @Test
    fun preventSidestepOnFirstBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        attacker.addSkill(SkillType.GRAB)
        defender.addSkill(SkillType.SIDESTEP)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
        )
        assertEquals(awayTeam, controller.getAvailableActions().team)
        controller.rollForward(
            Confirm // Use Grab
        )
        val actions = controller.getAvailableActions()
        assertEquals(5, actions.get<SelectDirection>().directions.size)
        assertEquals(awayTeam, actions.team)
        controller.rollForward(
            DirectionSelected(Direction.UP_RIGHT),
            Cancel // Do not follow up
        )
        assertEquals(FieldCoordinate(13, 4), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
        assertNull(state.activePlayer)
    }

    // We cannot choose to use Grab if there are no adjacent squares. But having
    // squares would prevent a chain-push. So this just checks that we do not
    // ignore Sidestep due to the passive presence of Grab.
    @Test
    fun doesNotPreventSidestepOnChainPush() {
        SetPlayerLocation(homeTeam[5.playerNo], FieldCoordinate(11, 4)).execute(state)
        SetPlayerLocation(homeTeam[4.playerNo], FieldCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[6.playerNo], FieldCoordinate(11, 6)).execute(state)
        SetPlayerLocation(homeTeam[7.playerNo], FieldCoordinate(12, 4)).execute(state)
        SetPlayerLocation(homeTeam[8.playerNo], FieldCoordinate(13, 4)).execute(state)
        homeTeam[7.playerNo].apply {
            state = PlayerState.PRONE
            hasTackleZones = false
        }
        homeTeam[8.playerNo].apply {
            state = PlayerState.PRONE
            hasTackleZones = false
        }
        val attacker = awayTeam[1.playerNo]
        val chainPushedPlayer = homeTeam[4.playerNo]
        attacker.addSkill(SkillType.GRAB)
        chainPushedPlayer.addSkill(SkillType.SIDESTEP)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
        )
        var actions = controller.getAvailableActions()
        assertEquals(awayTeam, actions.team)
        assertEquals(3, actions.get<SelectDirection>().directions.size)
        controller.rollForward(
            DirectionSelected(Direction.LEFT),
        )
        actions = controller.getAvailableActions()
        assertEquals(homeTeam, actions.team)
        controller.rollForward(
            Confirm // Use Sidestep
        )
        actions = controller.getAvailableActions()
        assertEquals(homeTeam, actions.team)
        assertEquals(3, actions.get<SelectDirection>().directions.size)
        controller.rollForward(
            DirectionSelected(Direction.LEFT),
            Cancel // Do not follow up
        )
        assertNull(state.activePlayer)
        assertEquals(PlayerState.STANDING, chainPushedPlayer.state)
        assertEquals(FieldCoordinate(10, 5), chainPushedPlayer.location)
    }

    @Test
    fun doesNotWorkOnBlitz() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        attacker.addSkill(SkillType.GRAB)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *blitzBlock("H1", 3.dblock),
        )
        val actions = controller.getAvailableActions()
        assertEquals(3, actions.get<SelectDirection>().directions.size)
        assertEquals(awayTeam, actions.team)
        // Grab cannot be used
        controller.rollForward(
            DirectionSelected(Direction.UP_LEFT),
            Cancel, // Do not follow up
            EndAction
        )
        assertEquals(FieldCoordinate(11, 4), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
        assertNull(state.activePlayer)
    }
}
