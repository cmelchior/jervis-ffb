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
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Grab
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.blitzBlock
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.putProne
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
        defender.assertStanding()
        defender.assertCoordinates(11, 5)
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
        defender.assertCoordinates(13, 4)
        defender.assertStanding()
        assertNull(state.activePlayer)
    }

    // We cannot choose to use Grab if there are no adjacent squares. But having
    // squares would prevent a chain-push. So this just checks that we do not
    // ignore Sidestep due to the passive presence of Grab.
    // This behavior was also clarified in Designer's Commentary May 2026.
    @Test
    fun doesNotPreventSidestepOnChainPush() {
        SetPlayerLocation(homeTeam[5.playerNo], PitchCoordinate(11, 4)).execute(state)
        SetPlayerLocation(homeTeam[4.playerNo], PitchCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[6.playerNo], PitchCoordinate(11, 6)).execute(state)
        SetPlayerLocation(homeTeam[7.playerNo], PitchCoordinate(12, 4)).execute(state)
        SetPlayerLocation(homeTeam[8.playerNo], PitchCoordinate(13, 4)).execute(state)
        homeTeam[7.playerNo].putProne()
        homeTeam[8.playerNo].putProne()
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
        chainPushedPlayer.assertStanding()
        chainPushedPlayer.assertCoordinates(10, 5)
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
        // Grab cannot be used, so we only see the 3 default pushback options
        val actions = controller.getAvailableActions()
        assertEquals(3, actions.get<SelectDirection>().directions.size)
        assertEquals(awayTeam, actions.team)
        controller.rollForward(
            DirectionSelected(Direction.UP_LEFT),
            Cancel, // Do not follow up
            EndAction
        )
        defender.assertCoordinates(11, 4)
        defender.assertStanding()
        assertNull(state.activePlayer)
    }

    @Test
    fun doesNotCancelSidestepOnBlitz() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.GRAB)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.SIDESTEP)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *blitzBlock("H1", 3.dblock),
            Confirm, // Use Sidestep
        )
        val actions = controller.getAvailableActions()
        assertEquals(homeTeam, actions.team)
        assertEquals(5, actions.get<SelectDirection>().directions.size)

        controller.rollForward(
            DirectionSelected(Direction.UP_RIGHT),
            Cancel, // Do not follow up
            EndAction
        )
        defender.assertCoordinates(13, 4)
        defender.assertStanding()
        assertNull(state.activePlayer)
    }
}
