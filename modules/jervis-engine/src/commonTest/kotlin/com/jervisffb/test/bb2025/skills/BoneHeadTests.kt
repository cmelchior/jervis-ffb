package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.ForegoActivationSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.BoneHead
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.boneHead
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [BoneHead] skill.
 */
class BoneHeadTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
        awayTeam["A1".playerId].addSkill(SkillType.BONE_HEAD)
    }

    @Test
    fun getDistractedIfFail() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *boneHead(1.d6),
        )
        val player = awayTeam["A1".playerId]
        assertNull(state.activePlayer)
        assertTrue(rules.isDistracted(player))
    }

    @Test
    fun rollAfterSelectingAction() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *boneHead(2.d6),
            *moveTo(14, 5),
            *dodge(1.d6)
        )
        val player = awayTeam["A1".playerId]
        assertEquals(player, state.activePlayer)
        assertFalse(rules.isDistracted(player))
    }

    @Test
    fun useActionIfFailRoll() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLITZ),
            *boneHead(1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(0, state.awayTeam.turnData.blitzActions)
    }

    @Test
    fun doesNotClearDistractedIfForegoActivation() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *boneHead(1.d6),
            EndTurn,
        )
        val player = awayTeam["A1".playerId]
        assertTrue(rules.isDistracted(player))
        controller.rollForward(
            EndTurn,
            ForegoActivationSelected(player)
        )
        assertTrue(rules.isDistracted(player))
    }

    @Test
    fun clearWhenActivatingAgain() {
        val player = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *boneHead(1.d6),
        )
        assertNull(state.activePlayer)
        assertTrue(rules.isDistracted(player))
        controller.rollForward(
            EndTurn,
            EndTurn,
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *boneHead(2.d6),
        )
        assertEquals(player, state.activePlayer)
        assertFalse(rules.isDistracted(player))
    }

    @Ignore
    @Test
    fun canUseProToRerollBoneHeadRoll() {
        // TODO
    }
}
