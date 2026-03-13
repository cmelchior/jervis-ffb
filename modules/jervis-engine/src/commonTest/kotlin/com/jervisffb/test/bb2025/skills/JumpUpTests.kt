package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.rules.bb2025.skills.JumpUp
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Class testing usage of the [JumpUp] skill
 */
class JumpUpTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
        val player = state.getPlayerById("A10".playerId)
        player.putProne()
    }

    @Test
    fun specialActionsNotAvailableWhenProne() {
        val attacker = awayTeam["A1".playerId]
        attacker.apply {
            addSkill(SkillType.JUMP_UP)
            addSkill(SkillType.PROJECTILE_VOMIT)
            addSkill(SkillType.STAB)
            addSkill(SkillType.BREATHE_FIRE)
            putProne()
        }
        listOf("H1", "H2").forEach {
            homeTeam[it.playerId].putProne()
        }
        controller.rollForward(
            PlayerSelected(attacker),
        )
        assertFalse(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerStandardActionType.BLOCK })
        assertFalse(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerSpecialActionType.PROJECTILE_VOMIT })
        assertFalse(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerSpecialActionType.BREATHE_FIRE })
        assertFalse(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerSpecialActionType.STAB })
    }

    @Test
    fun rollToBlock() {
        val attacker = awayTeam["A1".playerId]
        attacker.apply {
            addSkill(SkillType.JUMP_UP)
            putProne()
        }
        val defender = homeTeam["H1".playerId]
        assertEquals(3, attacker.agility)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            2.d6,
            NoRerollSelected(),
            *standardBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun failedRollToBlockLeavesPlayerProne() {
        val attacker = awayTeam["A1".playerId]
        attacker.apply {
            addSkill(SkillType.JUMP_UP)
            putProne()
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            1.d6,
            NoRerollSelected(),
        )
        assertNull(state.activePlayer)
        assertEquals(PlayerState.PRONE, attacker.state)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun cannotJumpUpToBlockIfNoOpponent() {
        val attacker = awayTeam["A1".playerId]
        attacker.apply {
            addSkill(SkillType.JUMP_UP)
            putProne()
        }
        listOf("H1", "H2").forEach {
            homeTeam[it.playerId].apply {
                putProne()
            }
        }
        controller.rollForward(
            PlayerSelected(attacker),
        )
        assertFalse(controller.getAvailableActions().get<SelectPlayerAction>().actions.any { it.type == PlayerStandardActionType.BLOCK })
    }

    @Test
    fun standUpForFreeDuringMoveAction() {
        val player = awayTeam["A1".playerId]
        player.apply {
            addSkill(SkillType.JUMP_UP)
            putProne()
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
        )
        assertEquals(6, player.movesLeft)
        controller.rollForward(
            MoveTypeSelected(MoveType.STAND_UP),
        )
        assertEquals(6, player.movesLeft)
    }

    @Test
    fun standUpForFreeDuringBlitzAction() {
        val player = awayTeam["A1".playerId]
        player.apply {
            addSkill(SkillType.JUMP_UP)
            putProne()
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.BLITZ),
        )
        assertEquals(6, player.movesLeft)
        controller.rollForward(
            PlayerSelected(homeTeam["H3".playerId]),
            MoveTypeSelected(MoveType.STAND_UP),
        )
        assertEquals(6, player.movesLeft)
    }

    @Test
    fun standUpForFreeDuringFoulAction() {
        homeTeam["H3".playerId].putProne()
        val player = awayTeam["A1".playerId]
        player.apply {
            addSkill(SkillType.JUMP_UP)
            putProne()
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.FOUL),
        )
        assertEquals(6, player.movesLeft)
        controller.rollForward(
            MoveTypeSelected(MoveType.STAND_UP),
        )
        assertEquals(6, player.movesLeft)
    }

    @Test
    fun standUpForFreeDuringPassAction() {
        val player = awayTeam["A1".playerId]
        player.apply {
            addSkill(SkillType.JUMP_UP)
            putProne()
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.PASS),
        )
        assertEquals(6, player.movesLeft)
        controller.rollForward(
            MoveTypeSelected(MoveType.STAND_UP),
        )
        assertEquals(6, player.movesLeft)
    }

    @Test
    fun standUpForFreeDuringHandOffAction() {
        val player = awayTeam["A1".playerId]
        player.apply {
            addSkill(SkillType.JUMP_UP)
            putProne()
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.HAND_OFF),
        )
        assertEquals(6, player.movesLeft)
        controller.rollForward(
            MoveTypeSelected(MoveType.STAND_UP),
        )
        assertEquals(6, player.movesLeft)
    }

    @Test
    fun standUpForFreeDuringThrowTeammateAction() {
        val player = awayTeam["A1".playerId]
        player.apply {
            addSkill(SkillType.THROW_TEAMMATE)
            addSkill(SkillType.JUMP_UP)
            putProne()
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.THROW_TEAM_MATE),
        )
        assertEquals(6, player.movesLeft)
        controller.rollForward(
            MoveTypeSelected(MoveType.STAND_UP),
        )
        assertEquals(6, player.movesLeft)
    }

    @Test
    fun standUpForFreeDuringSecureTheBallAction() {
        val player = awayTeam["A1".playerId]
        player.apply {
            addSkill(SkillType.THROW_TEAMMATE)
            addSkill(SkillType.JUMP_UP)
            putProne()
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.SECURE_THE_BALL),
        )
        assertEquals(6, player.movesLeft)
        controller.rollForward(
            MoveTypeSelected(MoveType.STAND_UP),
        )
        assertEquals(6, player.movesLeft)
    }
}
