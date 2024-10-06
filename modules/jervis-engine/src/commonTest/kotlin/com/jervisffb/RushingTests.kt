package com.jervisffb

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
import com.jervisffb.engine.rules.PlayerStandardActionType
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class responsible for testing Rushing. Note, this class is only testing
 * standard rushes. Skills and effects that could modify a Rush should be
 * tested in the test classes for those skills.
 */
class RushingTests: JervisGameTest() {

    @Test
    fun successfulRush() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            PlayerSelected("A8".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(16, 13),
            *moveTo(17, 13),
            *moveTo(18, 13),
            *moveTo(19, 13),
            *moveTo(20, 13),
            *moveTo(21, 13),
            *moveTo(22, 13),
            *moveTo(23, 13), // 1st Rush
            2.d6,
            NoRerollSelected(),
            *moveTo(24, 13), // 2nd Rush
            1.d6,
        )
        val reroll = RerollOptionSelected(
            option = (controller.getAvailableActions().actions.last() as SelectRerollOption).option
        )
        controller.rollForward(
            reroll,
            6.d6
        )
        val actions = controller.getAvailableActions().actions
        assertEquals(EndActionWhenReady, actions.single())
    }

    @Test
    fun failedRush() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            PlayerSelected("A8".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(16, 13),
            *moveTo(17, 13),
            *moveTo(18, 13),
            *moveTo(19, 13),
            *moveTo(20, 13),
            *moveTo(21, 13),
            *moveTo(22, 13),
            *moveTo(23, 13), // Rush
            1.d6, // Fail Rush
            NoRerollSelected(),
            DiceRollResults(1.d6, 1.d6), // Armour Roll
        )
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    @Ignore
    fun rushToBlitz() {
        TODO("Rush to blitz when out of moves. Test should probably be in BlitzTests")
    }
}
