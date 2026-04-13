package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2025.skills.UnchannelledFury
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.UnchannelledFuryRollContext
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.proRoll
import com.jervisffb.test.unchannelledFury
import com.jervisffb.test.utils.SelectSkillReroll
import com.jervisffb.test.utils.TeamRerollSelected
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [UnchannelledFury] skill.
 */
class UnchannelledFuryTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
        awayTeam["A1".playerId].addSkill(SkillType.UNCHANNELLED_FURY)
    }

    @Test
    fun endActivationOnFailedRoll() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *unchannelledFury(3.d6),
        )
        val player = awayTeam["A1".playerId]
        assertNull(state.activePlayer)
        assertFalse(rules.isDistracted(player))
    }

    @Test
    fun useActionIfFailRoll() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLITZ),
            *unchannelledFury(1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(0, state.awayTeam.turnData.blitzActions)
    }

    @Test
    fun canUseProToRerollUnchannelledFury() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.PRO)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *unchannelledFury(1.d6, SelectSkillReroll(SkillType.PRO)),
            *proRoll(4.d6),
            4.d6, // Succeed Unchannelled roll
            *moveTo(14, 5),
            *dodge(6.d6)
        )
        assertEquals(player, state.activePlayer)
        assertFalse(rules.isDistracted(player))
    }

    @Test
    fun blockImprovesChance() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            3.d6,
        )
        assertTrue(state.getContext<UnchannelledFuryRollContext>().isSuccess)
        controller.rollForward(
            TeamRerollSelected<RegularTeamReroll>(),
            4.d6
        )
        val player = awayTeam["A1".playerId]
        assertEquals(player, state.activePlayer)
        assertFalse(rules.isDistracted(player))
    }

    @Test
    fun blitzImprovesChance() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLITZ),
            *unchannelledFury(2.d6),
            PlayerSelected("H1".playerId),
        )
        val player = awayTeam["A1".playerId]
        assertEquals(player, state.activePlayer)
        assertFalse(rules.isDistracted(player))
    }
}
