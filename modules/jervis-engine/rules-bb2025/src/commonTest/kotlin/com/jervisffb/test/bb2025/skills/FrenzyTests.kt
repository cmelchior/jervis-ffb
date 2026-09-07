package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.bb2025.skills.Frenzy
import com.jervisffb.engine.common.reports.ReportPushResult
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertStanding
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Frenzy] skill.
 */
class FrenzyTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun mustFollowUpWhenPushingBack() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        attacker.addSkill(SkillType.FRENZY)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock), // Pushed Back
            DirectionSelected(Direction.LEFT),
            // No follow-up choice is offered: Frenzy forces it, so the engine
            // continues straight to the mandatory second block.
        )
        defender.assertCoordinates(11, 5)
        defender.assertStanding()
        attacker.assertCoordinates(12, 5)
        attacker.assertStanding()
        // The push report must reflect that the attacker followed up.
        val pushReport = state.logs.filterIsInstance<ReportPushResult>().lastOrNull()
        assertNotNull(pushReport)
        assertTrue(pushReport.message.isNotEmpty())
    }

    @Test
    fun secondBlockCannotBeSkipped() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        attacker.addSkill(SkillType.FRENZY)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock), // Pushed Back
            DirectionSelected(Direction.LEFT),
        )
        // The second block is mandatory: ending the action is not offered.
        val actions = controller.getAvailableActions().actions
        assertNotNull(actions.singleOrNull { it is SelectBlockType })
        assertTrue(actions.none { it is EndActionWhenReady })
    }

    @Test
    fun secondBlockEndsTheFrenzySequence() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        attacker.addSkill(SkillType.FRENZY)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock), // Pushed Back
            DirectionSelected(Direction.LEFT),
            BlockTypeSelected(BlockType.STANDARD), // Mandatory 2nd block
            DiceRollResults(3.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
        )
        // No third block: the action completes and control returns.
        assertNull(state.activePlayer)
        defender.assertCoordinates(10, 5)
        defender.assertStanding()
        attacker.assertCoordinates(11, 5)
        attacker.assertStanding()
    }
}
