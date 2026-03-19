package com.jervisffb.test.bb2025.actions

import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.BlockActionContext
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock.SingleStandardBlockChooseResult
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.TeamReroll
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.bb2020.skills.BlockTests
import com.jervisffb.test.bb2020.skills.DodgeSkillTests
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class responsible for testing the actual Block action. Some aspects
 * might be delegated to other classes.
 *
 * - Pushbacks have a lot of corner cases so are tested in [PushBackTests]. This
 *   class just tests the basic pushback.
 * - Injury rolls are tested separately in [InjuryRollTests].
 * - All skills affecting block are tested in their own class, e.g. [BlockTests]
 *   and [DodgeSkillTests]
 */
class BlockActionTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    private fun activePlayerCanBlock(): Boolean {
        return controller.getAvailableActions()
            .filterIsInstance<SelectPlayerAction>()
            .first().actions
            .any { it.type == PlayerStandardActionType.BLOCK }
    }


    @Test
    fun cannotBlockWhileProne() {
        val player = state.getPlayerById("A4".playerId)
        player.state = PlayerState.PRONE
        controller.rollForward(
            PlayerSelected(player.id),
        )

        val canBlock = activePlayerCanBlock()
        assertFalse(canBlock)
    }

    @Test
    fun canOnlyBlockMarkedStandingPlayer() {
        val player = state.getPlayerById("A4".playerId)
        controller.rollForward(
            PlayerSelected(player.id),
        )

        // If blocking player doesn't have a tacklezone, they cannot block anyone
        player.makeDistracted()
        assertFalse(activePlayerCanBlock())

        // Non-standing players cannot be blocked
        player.hasTackleZones = true
        state.getPlayerById("H3".playerId).state = PlayerState.PRONE
        state.getPlayerById("H4".playerId).state = PlayerState.STUNNED
        state.getPlayerById("H5".playerId).state = PlayerState.STUNNED_OWN_TURN
        assertFalse(activePlayerCanBlock())

        state.getPlayerById("H3".playerId).state = PlayerState.STANDING
        state.getPlayerById("H4".playerId).state = PlayerState.STANDING
        state.getPlayerById("H5".playerId).state = PlayerState.STANDING
        assertTrue(activePlayerCanBlock())
    }


    @Test
    fun endActionBeforeSelectingTargetNotMarkPlayerAsActivated() {
        val player = state.getPlayerById("A4".playerId)
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            EndAction
        )
        assertEquals(Availability.AVAILABLE, player.available)
        assertEquals(Int.MAX_VALUE, state.awayTeam.turnData.blockActions)
    }

    @Test
    fun assistsFromOpenPlayers() {
        // Allow A2 to assist on H1
        state.getPlayerById("H2".playerId).state = PlayerState.PRONE
        state.getPlayerById("H3".playerId).state = PlayerState.PRONE
        val player = state.getPlayerById("A1".playerId)
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
        )
        val context = state.getContext<BlockContext>()
        assertEquals(0, context.defensiveAssists)
        assertEquals(1, context.offensiveAssists)
    }

    @Test
    fun markedPlayersCannotAssist() {
        // Everyone is on LoS, so no assists
        val player = state.getPlayerById("A1".playerId)
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
        )
        val context = state.getContext<BlockContext>()
        assertEquals(0, context.defensiveAssists)
        assertEquals(0, context.offensiveAssists)
    }

    @Test
    fun teamRerollRerollAllDice() {
        // Everyone is on LoS, so no assists
        val player = state.getPlayerById("A1".playerId)
        player.strength = 4 // Give player 2D block
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
            DiceRollResults(1.dblock, 2.dblock),
            CalculatedAction { _, _ ->
                controller.getAvailableActions().filterIsInstance<SelectRerollOption>()
                    .first().options
                    .first { it.getRerollSource(state) is TeamReroll }
                    .let { RerollOptionSelected(it) }
            },
            DiceRollResults(6.dblock, 6.dblock),
        )
        assertEquals(SingleStandardBlockChooseResult.SelectBlockResult, controller.currentNode())
        val context = state.getContext<BlockContext>()
        assertEquals(2, context.roll.size)
        context.roll.forEach {
            assertEquals(6, it.rerolledResult?.value)
            assertTrue(state.getRerollSourceById(it.rerollSource!!) is TeamReroll)
        }
    }

    @Test
    fun followUpIfDefenderIsPushedBack() {
        // Everyone is on LoS, so no assists
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
            Confirm
        )
        attacker.assertCoordinates(12, 5)
        attacker.assertStanding()
        defender.assertCoordinates(11, 5)
        defender.assertStanding()
    }

    @Test
    fun clearContextWhenDoneWithAction() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 1.dblock),
            DiceRollResults(1.d6, 1.d6)
        )
        assertNull(state.activePlayer)
        assertFalse(state.hasContext<BlockContext>())
        assertFalse(state.hasContext<BlockActionContext>())
    }

    @Test
    fun playerDown() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 1.dblock),
        )
        attacker.assertCoordinates(13, 5)
        assertEquals(PlayerState.KNOCKED_DOWN, attacker.state)
        defender.assertCoordinates(12, 5)
        defender.assertStanding()
    }

    @Test
    fun bothDown() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 2.dblock),
        )
        defender.assertCoordinates(12, 5)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
        attacker.assertCoordinates(13, 5)
        attacker.assertStanding()
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6),
        )
        defender.assertProne()
        assertEquals(PlayerState.KNOCKED_DOWN, attacker.state)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6),
        )
        defender.assertProne()
        attacker.assertProne()
        assertNull(state.activePlayer)
    }

    @Test
    fun pushBack() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel,
        )
        attacker.assertCoordinates(13, 5)
        attacker.assertStanding()
        defender.assertCoordinates(11, 5)
        defender.assertStanding()
    }

    @Test
    fun stumble() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 5.dblock),
            DirectionSelected(Direction.UP_LEFT),
            Cancel,
        )
        attacker.assertCoordinates(13, 5)
        attacker.assertStanding()
        defender.assertCoordinates(11, 4)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
    }

    @Test
    fun pow() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.DOWN_LEFT),
            Cancel,
        )
        attacker.assertCoordinates(13, 5)
        attacker.assertStanding()
        defender.assertCoordinates(11, 6)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
    }

    // Check that only the first player in the chain is knocked down
    @Test
    fun chainPushPowPlayer() {
        SetPlayerLocation(homeTeam[4.playerNo], FieldCoordinate(11, 4)).execute(state)
        SetPlayerLocation(homeTeam[10.playerNo], FieldCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[11.playerNo], FieldCoordinate(11, 6)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT), // First push
            DirectionSelected(Direction.UP_LEFT), // 2nd push
            Cancel // Do not follow up
        )
        assertEquals(FieldCoordinate(11, 5), homeTeam["H1".playerId].coordinates)
        assertEquals(PlayerState.KNOCKED_DOWN, homeTeam["H1".playerId].state)
        assertEquals(FieldCoordinate(10, 4), homeTeam["H10".playerId].coordinates)
        homeTeam["H10".playerId].assertStanding()
    }
}
