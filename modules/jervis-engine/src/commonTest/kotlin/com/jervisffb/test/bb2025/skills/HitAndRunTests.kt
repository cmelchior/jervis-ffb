package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.blitzBlock
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Defensive] skill.
 */
class HitAndRunTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun worksOnBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.HIT_AND_RUN)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            Confirm, // Use Hit and Run
            FieldSquareSelected(14, 6)
        )
        assertNull(state.activePlayer)
        assertEquals(FieldCoordinate(14, 6), attacker.location)
    }

    @Test
    fun worksOnBlitz() {
        val attacker = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId]
        attacker.addSkill(SkillType.HIT_AND_RUN)
        assertEquals(6, attacker.move)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *blitzBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            Confirm, // Use Hit and Run
            FieldSquareSelected(14, 6)
        )
        assertEquals(attacker, state.activePlayer)
        assertEquals(FieldCoordinate(14, 6), attacker.location)
        assertEquals(5, attacker.movesLeft)
    }

    @Test
    fun worksOnStabBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            addSkill(SkillType.STAB)
            addSkill(SkillType.HIT_AND_RUN)
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.STAB),
            PlayerSelected(homeTeam["H1".playerId]),
            DiceRollResults(1.d6, 2.d6),
            Confirm, // Use Hit and Run
            FieldSquareSelected(14, 5)
        )
        assertNull(state.activePlayer)
        assertEquals(FieldCoordinate(14, 5), attacker.location)
    }

    @Test
    fun worksOnStabBlitz() {
        val attacker = awayTeam["A1".playerId]
        attacker.apply {
            addSkill(SkillType.STAB)
            addSkill(SkillType.HIT_AND_RUN)
        }
        val defender = homeTeam["H1".playerId]
        assertEquals(6, attacker.move)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.STAB),
            DiceRollResults(1.d6, 2.d6),
            Confirm, // Use Hit and Run
            FieldSquareSelected(14, 4)
        )
        assertNull(state.activePlayer)
        assertEquals(FieldCoordinate(14, 4), attacker.location)
    }

    @Test
    fun canMoveIntoOpponentsPlaceIfRemoved() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.HIT_AND_RUN)
        val defender = homeTeam["H1".playerId]
        SetPlayerLocation(defender, FieldCoordinate(12, 4)).execute(state)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(6.d6, 6.d6),
            DiceRollResults(4.d6, 6.d6),
            1.d16, // Badly Hurt
            Cancel, // Do not use Apothecary
            Confirm, // Use Hit and Run
            FieldSquareSelected(12, 4)
        )
        assertNull(state.activePlayer)
        assertEquals(FieldCoordinate(12, 4), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
    }

    @Test
    fun cannotMoveToSquaresBeingMarkedByOpponent() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.HIT_AND_RUN)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            Confirm, // Use Hit and Run
        )
        val targetSquares = controller.getAvailableActions().get<SelectFieldLocation>().squares.map { it.coordinate }.toSet()
        val expectedSquares = setOf(
            FieldCoordinate(13, 4),
            FieldCoordinate(14, 4),
            FieldCoordinate(14, 5),
            FieldCoordinate(14, 6),
        )
        assertEquals(targetSquares.size, expectedSquares.size)
        targetSquares.forEach {
            assertTrue(expectedSquares.contains(it), "Failed to find coordinate $it")
        }
    }

    @Test
    fun cannotMoveToSquaresWherePlayerIsMarkingOpponent() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.HIT_AND_RUN)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(1.d6, 1.d6), // Make Player Prone
            Confirm, // Use Hit and Run
        )
        val targetSquares = controller.getAvailableActions().get<SelectFieldLocation>().squares.map { it.coordinate }.toSet()
        val expectedSquares = setOf(
            FieldCoordinate(13, 4),
            FieldCoordinate(14, 4),
            FieldCoordinate(14, 5),
            FieldCoordinate(14, 6),
        )
        assertEquals(targetSquares.size, expectedSquares.size)
        targetSquares.forEach {
            assertTrue(expectedSquares.contains(it), "Failed to find coordinate $it")
        }
    }

    @Test
    fun cannotBeUsedIfNotStanding() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.HIT_AND_RUN)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 2.dblock),
            DiceRollResults(1.d6, 1.d6), // Defender armour roll
            DiceRollResults(1.d6, 1.d6), // Attacker armour roll
        )
        assertNull(state.activePlayer)
    }

    @Test
    fun cannotBeUsedIfNoTargetSquaresAvailable() {
        val attacker = state.getPlayerById("A2".playerId)
        attacker.addSkill(SkillType.HIT_AND_RUN)
        val defender = homeTeam["H2".playerId]
        val blockingPlayer = homeTeam["H10".playerId]
        SetPlayerLocation(blockingPlayer, FieldCoordinate(14, 6)).execute(state)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            Confirm, // Follow Up
        )
        assertNull(state.activePlayer)
        assertEquals(FieldCoordinate(12, 6), attacker.location)
    }

    // Edge case for detecting available squares that was discovered during testing
    @Test
    fun cannotMoveOnTopOfPlayerStandingAlone() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.HIT_AND_RUN)
        val alonePlayer = awayTeam["A10".playerId]
        alonePlayer.makeDistracted()
        SetPlayerLocation(alonePlayer, FieldCoordinate(12, 4)).execute(state)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.DOWN_LEFT),
            Confirm, // Follow up
            Confirm, // Use Hit and Run
        )
        val targetSquares = controller.getAvailableActions().get<SelectFieldLocation>().squares.map { it.coordinate }.toSet()
        val expectedSquares = setOf(
            FieldCoordinate(11, 4),
            FieldCoordinate(13, 4),
        )
        assertEquals(targetSquares.size, expectedSquares.size)
        targetSquares.forEach {
            assertTrue(expectedSquares.contains(it), "Failed to find coordinate $it")
        }
    }
}
