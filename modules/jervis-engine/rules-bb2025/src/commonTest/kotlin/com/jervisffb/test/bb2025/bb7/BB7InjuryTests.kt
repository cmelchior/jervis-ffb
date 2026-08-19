package com.jervisffb.test.bb2025.bb7

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB72025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertBadlyHurt
import com.jervisffb.test.utils.assertDead
import com.jervisffb.test.utils.assertNoActivePlayer
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * This class test differences in rolling for injuries and casualties compared
 * to normal standard BB2025.
 */
class BB7InjuryTests: JervisGameBB72025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun rollDirectlyForCasualty() {
        val mover = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            SmartMoveTo(7, 2),
            *moveTo(6, 1),
            *dodge(1.d6),
            DiceRollResults(6.d6, 6.d6),
            DiceRollResults(6.d6, 6.d6),
        )
        mover.assertDead()
        homeTeam.assertActive()
        state.assertNoActivePlayer()
    }

    @Test
    fun rollDirectlyForStuntyCasualty() {
        val mover = awayTeam["A1".playerId].also {
            it.addSkill(SkillType.STUNTY)
        }
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            SmartMoveTo(7, 2),
            *moveTo(6, 1),
            *dodge(1.d6),
            DiceRollResults(6.d6, 6.d6),
            DiceRollResults(6.d6, 3.d6),
        )
        mover.assertBadlyHurt()
        homeTeam.assertActive()
        state.assertNoActivePlayer()
    }
}
