package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.SureFeet
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.SelectSkillReroll
import com.jervisffb.test.utils.firstInstanceOf
import com.jervisffb.test.utils.getSkill
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [SureFeet] skill
 */
class SureFeetTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        awayTeam["A6".playerId].addSkill(SkillType.SURE_FEET)
        startDefaultGame()
    }

    @Test
    fun useSureFeetOnFailedRush() {
        val player = awayTeam["A6".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            SmartMoveTo(20, 1), // Use all normal move
            *moveTo(21, 1),
            1.d6, // Fail rush
            SelectSkillReroll(SkillType.SURE_FEET),
            2.d6,
            EndAction
        )
        assertTrue(player.getSkill<SureFeet>().rerollUsed)
        assertEquals(FieldCoordinate(21, 1), player.location)
        assertEquals(PlayerState.STANDING, player.state)
    }

    @Test
    fun useSureFeetOnSuccessfulRush() {
        val player = awayTeam["A6".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            SmartMoveTo(20, 1), // Use all normal move
            *moveTo(21, 1),
            2.d6, // Fail rush
            SelectSkillReroll(SkillType.SURE_FEET),
            6.d6,
            EndAction
        )
        assertTrue(player.getSkill<SureFeet>().rerollUsed)
        assertEquals(FieldCoordinate(21, 1), player.location)
        assertEquals(PlayerState.STANDING, player.state)
    }

    @Test
    fun canOnlyUseRushOncePrTurn() {
        val player = awayTeam["A6".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            SmartMoveTo(20, 1), // Use all normal move
            *moveTo(21, 1),
            1.d6, // Fail rush
            SelectSkillReroll(SkillType.SURE_FEET),
            2.d6,
            *moveTo(22, 1),
            1.d6, // Fail 2nd rush
        )

        val availableRerolls = controller.getAvailableActions().firstInstanceOf<SelectRerollOption>()
        assertEquals(1, availableRerolls.options.size)
        assertTrue(availableRerolls.options.first().getRerollSource(state) is RegularTeamReroll)
    }
}
