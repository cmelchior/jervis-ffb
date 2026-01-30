package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.Guard
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class testing usage of the [Guard] skill.
 */
class GuardTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun worksOnOffensiveAssists() {
        awayTeam["A2".playerId].addSkill(SkillType.GUARD)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
            BlockTypeSelected(BlockType.STANDARD)
        )
        assertEquals(2, controller.getAvailableActions().get<RollDice>().dice.size)
    }

    @Test
    fun worksOnDefensiveAssists() {
        awayTeam["A2".playerId].addSkill(SkillType.GUARD)
        homeTeam["H2".playerId].addSkill(SkillType.GUARD)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
            BlockTypeSelected(BlockType.STANDARD)
        )
        assertEquals(1, controller.getAvailableActions().get<RollDice>().dice.size)
    }
}
