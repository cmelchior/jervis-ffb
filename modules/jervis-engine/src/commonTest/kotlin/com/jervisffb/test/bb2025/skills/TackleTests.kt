package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.rules.bb2025.skills.Dodge
import com.jervisffb.engine.rules.bb2025.skills.Tackle
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.SelectTeamReroll
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertStanding
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Tackle] skill.
 */
class TackleTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun useTackleOnDodgingAway() {
        homeTeam["H1".playerId].addSkill(SkillType.TACKLE)
        awayTeam["A1".playerId].addSkill(SkillType.DODGE)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail Dodge
            PlayerSelected("H1".playerId), // Use Tackle to prevent Dodge reroll
        )
        assertTrue(controller.getAvailableActions().get<SelectRerollOption>().options.none { it.getRerollSource(state) is Dodge })
        controller.rollForward(
            SelectTeamReroll<RegularTeamReroll>(),
            6.d6,
            EndAction,
        )
        awayTeam["A1".playerId].assertStanding()
    }

    @Test
    fun doNotUseTackleIfNotNeededOnDodge() {
        homeTeam["H1".playerId].addSkill(SkillType.TACKLE)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail Dodge
        )
        // Dodge reroll is not available
        assertFalse(controller.getAvailableActions().get<SelectRerollOption>().options.any { it.getRerollSource(state) is Dodge })
        controller.rollForward(
            SelectTeamReroll<RegularTeamReroll>(),
            6.d6,
            EndAction,
        )
        awayTeam["A1".playerId].assertStanding()
    }

    @Test
    fun useTackleOnBlock() {
        homeTeam["H1".playerId].addSkill(SkillType.DODGE)
        awayTeam["A1".playerId].addSkill(SkillType.TACKLE)
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 5.dblock), // Stumble
            Confirm, // Use Tackle
            DirectionSelected(Direction.UP_LEFT),
            Cancel // Do not follow up
        )
        attacker.assertCoordinates(13, 5)
        attacker.assertStanding()
        defender.assertCoordinates(11, 4)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
    }

    @Test
    fun doNotUseTackleIfNotNeededOnBlock() {
        awayTeam["A1".playerId].addSkill(SkillType.TACKLE)
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            PlayerSelected(attacker.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected(defender.id),
            5.dblock, // Stumble result
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.UP_LEFT),
            Cancel // Do not follow up
        )
        attacker.assertCoordinates(13, 5)
        attacker.assertStanding()
        defender.assertCoordinates(11, 4)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
    }
}
