package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.bb2025.skills.QuickFoul
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [QuickFoul] skill.
 */
class QuickFoulTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            awayTeam["A1".playerId].addSkill(SkillType.QUICK_FOUL.id())
        }
        startDefaultGame()
    }

    @Test
    fun canMoveAfterFoul() {
        homeTeam["H1".playerId].putProne()
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(1.d6, 2.d6),
            *moveTo(14, 5),
            *dodge(6.d6),
            EndAction
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        homeTeam["H1".playerId].assertProne()
    }

    @Test
    fun cannotFoulTwice() {
        homeTeam["H1".playerId].putProne()
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(1.d6, 2.d6),
            *moveTo(14, 5),
            *dodge(6.d6),
            *moveTo(13, 5)
        )
        assertTrue(homeTeam["H1".playerId].location.isAdjacent(rules, PitchCoordinate(13, 5)))
        assertFalse(controller.getAvailableActions().contains<SelectPlayer>())
    }

    @Test
    fun cannotMoveAfterTurnOver() {
        homeTeam["H1".playerId].putProne()
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(1.d6, 1.d6),
            Confirm,
            6.d6, // Successfully argue the call, but it will be a turnover
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }
}
