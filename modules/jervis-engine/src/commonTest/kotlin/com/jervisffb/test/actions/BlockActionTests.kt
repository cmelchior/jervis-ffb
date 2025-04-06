package com.jervisffb.test.actions

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult
import com.jervisffb.engine.rules.bb2020.skills.TeamReroll
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.skills.BlockTests
import com.jervisffb.test.skills.DodgeTests
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class responsible for testing the actual Block action. Some aspects
 * might be delegated to other classes.
 *
 * - Pushbacks have a lot of corner cases so are tested in [PushBackTests]. This
 *   class just tests the basic pushback.
 * - Injury rolls are tested separately in [InjuryRollTests].
 * - All skills affecting block are tested in their own class, e.g. [BlockTests]
 *   and [DodgeTests]
 */
class BlockActionTests: JervisGameTest() {

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
        player.hasTackleZones = false
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
    fun endActionBeforeSelectingBlockTypeDoesNotMarkPlayerAsActivated() {
        val player = state.getPlayerById("A4".playerId)
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected("H4".playerId),
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
            BlockTypeSelected(BlockType.STANDARD)
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
            BlockTypeSelected(BlockType.STANDARD)
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
            BlockTypeSelected(BlockType.STANDARD),
            DiceRollResults(1.dblock, 2.dblock),
            CalculatedAction { _, _ ->
                controller.getAvailableActions().filterIsInstance<SelectRerollOption>()
                    .first().options
                    .first { it.getRerollSource(state) is TeamReroll }
                    .let { RerollOptionSelected(it) }
            },
            DiceRollResults(6.dblock, 6.dblock),
        )
        assertEquals(StandardBlockChooseResult.SelectBlockResult, controller.currentNode())
        val context = state.getContext<BlockContext>()
        assertEquals(2, context.roll.size)
        context.roll.forEach {
            assertEquals(6, it.rerolledResult?.value)
            assertTrue(it.rerollSource is TeamReroll)
        }
    }

    @Test
    fun followUpIfDefenderIsPushedBack() {
        // Everyone is on LoS, so no assists
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            3.dblock,
            NoRerollSelected(),
            CalculatedAction { _, _ -> selectSingleDieResult() },
            DirectionSelected(Direction.LEFT),
            Confirm
        )
        assertEquals(FieldCoordinate(12, 5), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
        assertEquals(FieldCoordinate(11, 5), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
    }

    @Test
    fun playerDown() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            1.dblock,
            NoRerollSelected(),
            CalculatedAction { _, _ -> selectSingleDieResult() },
        )
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.KNOCKED_DOWN, attacker.state)
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
    }

    @Test
    fun bothDown() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            2.dblock,
            NoRerollSelected(),
            CalculatedAction { _, _ -> selectSingleDieResult() },
        )
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.KNOCKED_DOWN, attacker.state)
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
    }

    @Test
    fun pushBack() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            3.dblock,
            NoRerollSelected(),
            CalculatedAction { _, _ -> selectSingleDieResult() },
            DirectionSelected(Direction.LEFT),
            Cancel,
        )
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
        assertEquals(FieldCoordinate(11, 5), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
    }

    @Test
    fun stumble() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            5.dblock,
            NoRerollSelected(),
            CalculatedAction { _, _ -> selectSingleDieResult() },
            DirectionSelected(Direction.UP_LEFT),
            Cancel,
        )
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
        assertEquals(FieldCoordinate(11, 4), defender.location)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
    }

    @Test
    fun pow() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            5.dblock,
            NoRerollSelected(),
            CalculatedAction { _, _ -> selectSingleDieResult() },
            DirectionSelected(Direction.BOTTOM_LEFT),
            Cancel,
        )
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
        assertEquals(FieldCoordinate(11, 6), defender.location)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
    }

    private fun selectSingleDieResult(): DicePoolResultsSelected {
        // TODO Need to rework these API's so this is easier
        return controller.getAvailableActions()
            .filterIsInstance<SelectDicePoolResult>()
            .first().pools.first()
            .let { DicePoolResultsSelected(listOf(DicePoolChoice(0, it.dice.map { it.result }))) }
    }
}
