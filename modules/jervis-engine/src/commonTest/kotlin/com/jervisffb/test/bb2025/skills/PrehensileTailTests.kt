package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.DodgeRollModifier
import com.jervisffb.engine.rules.bb2025.skills.PrehensileTail
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [PrehensileTail] skill.
 */
class PrehensileTailTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun addNegativeModifierToDodge() {
        val movingPlayer = awayTeam["A1".playerId]
        val playerWithTail = homeTeam["H1".playerId]
        playerWithTail.addSkill(SkillType.PREHENSILE_TAIL)
        assertEquals(3, movingPlayer.agility)
        controller.rollForward(
            *activatePlayer(movingPlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            4.d6,
            PlayerSelected(playerWithTail), // Use Prehensile Tail
        )
        assertTrue(state.getContext<DodgeRollContext>().rollModifiers.any { it == DodgeRollModifier.PREHENSILE_TAIL })
        controller.rollForward(
            TeamRerollSelected<RegularTeamReroll>(),
            3.d6,
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        movingPlayer.assertProne()
        assertEquals(FieldCoordinate(14, 5), movingPlayer.coordinates)
    }

    @Test
    fun onlyOnePlayerCanUseIt() {
        val movingPlayer = awayTeam["A1".playerId]
        homeTeam["H1".playerId].addSkill(SkillType.PREHENSILE_TAIL)
        homeTeam["H2".playerId].addSkill(SkillType.PREHENSILE_TAIL)
        controller.rollForward(
            *activatePlayer(movingPlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            5.d6,
            PlayerSelected("H2".playerId), // Use Prehensile Tail
        )
        assertTrue(state.getContext<DodgeRollContext>().rollModifiers.any { it == DodgeRollModifier.PREHENSILE_TAIL })
        controller.rollForward(
            NoRerollSelected(),
            EndAction,
        )
        assertNull(state.activePlayer)
        movingPlayer.assertStanding()
        assertEquals(FieldCoordinate(14, 5), movingPlayer.coordinates)
    }
}
