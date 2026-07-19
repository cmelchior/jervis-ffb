package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.getSkill
import com.jervisffb.engine.rules.bb2025.skills.Sprint
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.blitzBlock
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.jump
import com.jervisffb.test.leap
import com.jervisffb.test.moveTo
import com.jervisffb.test.rushRoll
import com.jervisffb.test.rushTo
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Sprint] skill.
 */
class SprintTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun workOnMove() {
        val movingPlayer = awayTeam["A8".playerId]
        movingPlayer.addSkill(SkillType.SPRINT)
        controller.rollForward(
            *activatePlayer(movingPlayer, PlayerStandardActionType.MOVE),
            *moveTo(16, 13),
            *moveTo(17, 13),
            *moveTo(18, 13),
            *moveTo(19, 13),
            *moveTo(20, 13),
            *moveTo(21, 13),
            *moveTo(22, 13),
            *moveTo(23, 13), // 1st Rush
            *rushRoll(2.d6),
            *moveTo(24, 13), // 2nd Rush
            *rushRoll(2.d6),
            *moveTo(25, 13), // 3rd Rush
        )
        assertTrue(movingPlayer.getSkill<Sprint>().used)
        controller.rollForward(
            *rushRoll(2.d6),
            EndAction
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        movingPlayer.assertCoordinates(25, 13)
        assertFalse(movingPlayer.getSkill<Sprint>().used)
    }

    @Test
    fun workOnBlitzToThrowBlock() {
        val attacker = state.getPlayerById("A10".playerId)
        attacker.addSkill(SkillType.SPRINT)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender.id),
            *moveTo(15, 7),
            *moveTo(14, 7),
            *moveTo(14, 6),
            *moveTo(14, 5),
            *moveTo(14, 4),
            *moveTo(14, 3),
            *moveTo(13, 3),
            *rushTo(12, 3),
            *rushTo(12, 4),
            PlayerSelected(defender.id), // Start block
            *rushRoll(2.d6), // Needs to Sprint Rush to make the block
            BlockTypeSelected(BlockType.STANDARD),
            4.dblock, // Block roll
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.DOWN_LEFT),
            Cancel, // Do not follow up
        )
        assertTrue(attacker.getSkill<Sprint>().used)
        controller.rollForward(
            EndAction
        )
        assertFalse(attacker.getSkill<Sprint>().used)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun workOnMoveAfterBlitz() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.SPRINT)
        val defender = state.getPlayerById("H1".playerId)
        assertEquals(6, attacker.move)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender.id), // Select target of blitz
            *blitzBlock(defender, 4.dblock),
            DirectionSelected(Direction.DOWN_LEFT),
            Cancel,
            *moveTo(13, 4),
            *dodge(6.d6),
            *moveTo(14,4),
            *moveTo(15,4),
            *moveTo(16,4),
            *moveTo(17,4),
            *rushTo(18,4),
            *rushTo(19,4),
            *moveTo(20, 4),
        )
        assertTrue(attacker.getSkill<Sprint>().used)
        controller.rollForward(
            *rushRoll(6.d6), // Use Sprint to Rush
            EndAction
        )
        assertFalse(attacker.getSkill<Sprint>().used)
    }

    @Test
    fun workOnJump() {
        homeTeam["H1".playerId].putProne()
        val jumpingPlayer = awayTeam["A1".playerId]
        jumpingPlayer.addSkill(SkillType.SPRINT)

        // Adjust move so jumping player needs 1 rush + Sprint to jump
        jumpingPlayer.movesLeft = 3
        jumpingPlayer.rushesLeft = 2

        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(6.d6),
            *moveTo(15, 5),
            *moveTo(14,5),
            *moveTo(13,5),
            *rushRoll(2.d6),
            MoveTypeSelected(MoveType.JUMP),
            PitchSquareSelected(11, 5),
            *rushRoll(2.d6),
            *rushRoll(2.d6), // Sprint
        )
        assertTrue(jumpingPlayer.getSkill<Sprint>().used)
        controller.rollForward(
            *jump(4.d6),
            EndAction
        )
        assertFalse(jumpingPlayer.getSkill<Sprint>().used)
        jumpingPlayer.assertStanding()
    }

    @Test
    fun workOnLeap() {
        homeTeam["H1".playerId].putProne()
        val jumpingPlayer = awayTeam["A1".playerId]
        jumpingPlayer.apply {
            addSkill(SkillType.SPRINT)
            addSkill(SkillType.LEAP)
        }

        // Adjust move so jumping player needs 1 rush + Sprint to jump
        jumpingPlayer.movesLeft = 3
        jumpingPlayer.rushesLeft = 2

        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(6.d6),
            *moveTo(15, 5),
            *moveTo(14,5),
            *moveTo(13,5),
            *rushRoll(2.d6),
            MoveTypeSelected(MoveType.LEAP),
            PitchSquareSelected(11, 5),
            *rushRoll(2.d6),
            *rushRoll(2.d6), // Sprint
        )
        assertTrue(jumpingPlayer.getSkill<Sprint>().used)
        controller.rollForward(
            *leap(4.d6),
            EndAction
        )
        assertFalse(jumpingPlayer.getSkill<Sprint>().used)
        jumpingPlayer.assertStanding()
    }

    @Test
    fun workOnPogo() {
        homeTeam["H1".playerId].putProne()
        val jumpingPlayer = awayTeam["A1".playerId]
        jumpingPlayer.apply {
            addSkill(SkillType.SPRINT)
            addSkill(SkillType.POGO_STICK)
        }

        // Adjust move so jumping player needs 1 rush + Sprint to jump
        jumpingPlayer.movesLeft = 3
        jumpingPlayer.rushesLeft = 2

        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(6.d6),
            *moveTo(15, 5),
            *moveTo(14,5),
            *moveTo(13,5),
            *rushRoll(2.d6),
            MoveTypeSelected(MoveType.POGO),
            PitchSquareSelected(11, 5),
            *rushRoll(2.d6),
            *rushRoll(2.d6), // Sprint
        )
        assertTrue(jumpingPlayer.getSkill<Sprint>().used)
        controller.rollForward(
            *leap(4.d6),
            EndAction
        )
        assertFalse(jumpingPlayer.getSkill<Sprint>().used)
        jumpingPlayer.assertStanding()
    }
}
