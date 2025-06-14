package com.jervisffb.test.actions

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.rushTo
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Class responsible for testing the Blitz action.
 * See page 46 and 59 in the rulebook.
 */
class BlitzActionTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun mustSelectTargetBeforeStartingBlitz() {
        val attacker = state.getPlayerById("A9".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLITZ),
        )
        assertEquals(2, controller.getAvailableActions().count())
        assertEquals(1, controller.getAvailableActions().actions.count { it is EndActionWhenReady })
        assertEquals(1, controller.getAvailableActions().actions.count { it is SelectPlayer })
        controller.rollForward(PlayerSelected(defender.id))
        assertEquals(2, controller.getAvailableActions().count())
        assertEquals(1, controller.getAvailableActions().actions.count { it is EndActionWhenReady })
        assertEquals(1, controller.getAvailableActions().actions.count { it is SelectMoveType })
    }

    // If declaring a blitz, the action is still used even if the block is
    // never performed
    @Test
    fun blitzWithoutBlockStillUsesAction() {
        val attacker = state.getPlayerById("A9".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLITZ),
            PlayerSelected(defender.id),
            *moveTo(13, 13),
            EndAction
        )
        assertEquals(0, attacker.team.turnData.blitzActions)
    }

    // The blitz is not used if action is ended before the player moved or did
    // the block.
    @Test
    fun blitzNotUsedIfNotMovedOrBlocked() {
        val attacker = state.getPlayerById("A9".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLITZ),
            PlayerSelected(defender.id),
            EndAction
        )
        assertEquals(1, attacker.team.turnData.blitzActions)
    }

    @Test
    fun declareBlitzFromProne() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.state = PlayerState.PRONE
        attacker.hasTackleZones = false
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLITZ),
            PlayerSelected(defender.id),
            MoveTypeSelected(MoveType.STAND_UP),
            PlayerSelected(defender.id),
            BlockTypeSelected(BlockType.STANDARD),
            6.dblock, // Block roll
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            Cancel,
            DiceRollResults(1.d6, 1.d6), // Armour roll
            EndAction
        )
        assertEquals(0, attacker.team.turnData.blitzActions)
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
        assertEquals(FieldCoordinate(11, 5), defender.location)
        assertEquals(PlayerState.PRONE, defender.state)
    }

    @Test
    fun cannotDeclareBlitzIfNoTargets() {
        val attacker = state.getPlayerById("A1".playerId)
        state.homeTeam
            .filter { it.location.isOnField(rules) }
            .forEach {
                it.state = PlayerState.PRONE
                it.hasTackleZones = false
            }
        controller.rollForward(PlayerSelected(attacker.id))

        val canBlitz = controller.getAvailableActions()
            .filterIsInstance<SelectPlayerAction>()
            .first().actions
            .any { it.type == PlayerStandardActionType.BLITZ }

        assertFalse(canBlitz)
    }

    @Test
    fun cannotBlockIfNotEnoughMove() {
        val attacker = state.getPlayerById("A10".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLITZ),
            PlayerSelected(defender.id),
            *moveTo(15, 7),
            *moveTo(14, 7),
            *moveTo(14, 6),
            *moveTo(14, 5),
            *moveTo(14, 4),
            *moveTo(14, 3),
            *moveTo(13, 3),
            *rushTo(12, 3, 2.d6),
            *rushTo(12, 4, 2.d6),
        )
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertEquals(1, controller.getAvailableActions().count())
    }

    @Test
    fun blitzUsesOneMove() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLITZ),
        )
        assertEquals(6, attacker.movesLeft)
        controller.rollForward(
            PlayerSelected(defender.id), // Start Blitz Action
            PlayerSelected(defender.id), // Start Block Sub action
            BlockTypeSelected(BlockType.STANDARD),
            6.dblock, // Block roll
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(1.d6, 1.d6), // Armour roll
        )
        assertEquals(5, attacker.movesLeft)
    }

    @Test
    fun rushToBlitz() {
        val attacker = state.getPlayerById("A10".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLITZ),
            PlayerSelected(defender.id),
            *moveTo(15, 7),
            *moveTo(14, 7),
            *moveTo(14, 6),
            *moveTo(14, 5),
            *moveTo(14, 4),
            *moveTo(14, 3),
            *moveTo(13, 3),
            *rushTo(12, 4),
            PlayerSelected(defender.id), // Start block
            BlockTypeSelected(BlockType.STANDARD),
            2.d6, // Needs to rush to make the block
            NoRerollSelected(),
            4.dblock, // Block roll
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.BOTTOM_LEFT),
            Cancel, // Do not follow up
            EndAction
        )
        assertEquals(0, attacker.team.turnData.blitzActions)
        assertEquals(FieldCoordinate(12, 4), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
        assertEquals(FieldCoordinate(11, 6), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
    }

    @Test
    fun failRushBeforeBlitz() {
        val attacker = state.getPlayerById("A10".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLITZ),
            PlayerSelected(defender.id),
            *moveTo(15, 7),
            *moveTo(14, 7),
            *moveTo(14, 6),
            *moveTo(14, 5),
            *moveTo(14, 4),
            *moveTo(14, 3),
            *moveTo(13, 3),
            *rushTo(12, 4, 2.d6),
            PlayerSelected(defender.id), // Start block
            BlockTypeSelected(BlockType.STANDARD),
            1.d6, // Fail last rush to make the block
            NoRerollSelected(),
            DiceRollResults(1.d6, 1.d6), // Armour roll
        )
        assertEquals(0, attacker.team.turnData.blitzActions)
        assertEquals(FieldCoordinate(12, 4), attacker.location)
        assertEquals(PlayerState.PRONE, attacker.state)
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
    }

    @Test
    fun moveAfterBlitz() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLITZ),
            PlayerSelected(defender.id), // Select target of blitz
            PlayerSelected(defender.id), // Start block
            BlockTypeSelected(BlockType.STANDARD),
            4.dblock, // Push back
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.BOTTOM_LEFT),
            Cancel,
            *moveTo(13, 4),
            6.d6, // Dodge roll
            NoRerollSelected(),
            EndAction
        )
        assertEquals(0, attacker.team.turnData.blitzActions)
        assertEquals(FieldCoordinate(13, 4), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
        assertEquals(FieldCoordinate(11, 6), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
    }
}
