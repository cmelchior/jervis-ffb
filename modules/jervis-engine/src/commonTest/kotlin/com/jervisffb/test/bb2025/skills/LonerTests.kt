package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillValue
import com.jervisffb.engine.rules.bb2025.skills.Loner
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.loner
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Loner] skill
 */
class LonerTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun mustRollXToUseReroll() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillId(type = SkillType.LONER, SkillValue.Int(4)))
        assertTrue(awayTeam.rerolls.none { it.rerollUsed })
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            TeamRerollSelected<RegularTeamReroll>(),
            *loner(4.d6), // Roll successfully for Loner
            3.d6, // Succeed dodge,
            EndAction
        )
        assertEquals(1, awayTeam.rerolls.count { it.rerollUsed })
    }

    @Test
    fun lonerTriggerOnAllRerollsDuringActivation() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillId(type = SkillType.LONER, SkillValue.Int(4)))
        assertTrue(awayTeam.rerolls.none { it.rerollUsed })
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            TeamRerollSelected<RegularTeamReroll>(),
            *loner(4.d6), // Roll successfully for Loner
            3.d6, // Succeed dodge,
            *moveTo(13, 5),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            TeamRerollSelected<RegularTeamReroll>(),
            *loner(4.d6), // Roll successfully for Loner
            3.d6, // Succeed dodge,
            EndAction
        )
        assertEquals(2, awayTeam.rerolls.count { it.rerollUsed })
    }

    @Test
    fun failedRollStillUsesReroll() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillId(type = SkillType.LONER, SkillValue.Int(4)))
        assertTrue(awayTeam.rerolls.none { it.rerollUsed })
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            TeamRerollSelected<RegularTeamReroll>(),
            *loner(3.d6), // Roll successfully for Loner
            DiceRollResults(1.d6, 1.d6),
        )
        assertEquals(1, awayTeam.rerolls.count { it.rerollUsed })
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        player.assertProne()
    }

    @Test
    fun teamRerollsWorkOnFailedLoner() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillId(type = SkillType.LONER, SkillValue.Int(3)))
        assertTrue(awayTeam.rerolls.none { it.rerollUsed })
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            TeamRerollSelected<RegularTeamReroll>(),
            2.d6, // Fail Loner Roll
        )
        assertEquals(1, awayTeam.rerolls.count { it.rerollUsed })
        controller.rollForward(
            TeamRerollSelected<RegularTeamReroll>()
        )
        assertEquals(2, awayTeam.rerolls.count { it.rerollUsed })
        controller.rollForward(
            *loner(4.d6), // Succeed Loner on 2nd reroll
            3.d6, // This allows re-rolling Loner on 1st reroll
            3.d6, // Succeed dodge
            EndAction
        )
        assertEquals(2, awayTeam.rerolls.count { it.rerollUsed })
    }

    @Test
    fun proRerollWorksOnFailedLoner() {

    }
}
