package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.rules.bb2025.skills.RightStuff
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [RightStuff] skill.
 */
class RightStuffTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        setupAndStartThrowTeamMateGame()
    }

    @Test
    fun enableThrowTeamMate() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
        )
        assertTrue(awayTeam["A13".playerId].hasSkill(SkillType.RIGHT_STUFF))
        assertTrue(controller.getAvailableActions().get<SelectPlayer>().players.contains("A13".playerId))
    }

    @Test
    fun enableThrowTeamMateWhenProne() {
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.putProne()
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
        )
        assertTrue(thrownPlayer.hasSkill(SkillType.RIGHT_STUFF))
        assertTrue(controller.getAvailableActions().get<SelectPlayer>().players.contains("A13".playerId))
    }

    @Test
    fun cannotThrowPlayerWithoutRightStuff() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 6),
            *dodge(),
            *moveTo(14, 7)
        )
        val adjacentPlayer = awayTeam["A2".playerId]
        assertFalse(adjacentPlayer.hasSkill(SkillType.RIGHT_STUFF))
        assertTrue(awayTeam["A1".playerId].coordinates.isAdjacent(rules, adjacentPlayer.coordinates))
        assertTrue(controller.getAvailableActions().actions.none { it is SelectPlayer })
    }
}
