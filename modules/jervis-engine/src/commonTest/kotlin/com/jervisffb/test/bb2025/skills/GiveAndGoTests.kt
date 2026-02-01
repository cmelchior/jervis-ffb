package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.GiveAndGo
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [GiveAndGo] skill.
 */
class GiveAndGoTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            awayTeam["A10".playerId].apply {
                addSkill(SkillType.CANNONEER.id())
                passing = 2
            }
        }
        startDefaultGame()
    }

    @Test
    fun worksOnSuccessfulQuickPass() {
        awayTeam["A10".playerId].addSkill(SkillType.GIVE_AND_GO)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(16, 4),
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            *throwBall(4.d6),
            *catch(6.d6),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
        assertFalse(state.isTurnOver())
        controller.rollForward(
            *moveTo(16,5),
            EndAction
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(16, 5), awayTeam["A10".playerId].location)
    }

    @Test
    fun doNotWorkOnRangeLongerThanQuickPass() {
        awayTeam["A10".playerId].addSkill(SkillType.GIVE_AND_GO)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(6.d6),
            SmartMoveTo(16, 5),
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            *throwBall(5.d6),
            *catch(6.d6),
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
        assertNull(state.activePlayer)
    }

    @Test
    fun doNotWorkAfterTurnoverDuringPass() {
        awayTeam["A10".playerId].addSkill(SkillType.GIVE_AND_GO)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(16, 4),
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            *throwBall(4.d6),
            *catch(1.d6),
            2.d8 // Bounce
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun workOnSuccessfulHandoff() {
        awayTeam["A10".playerId].addSkill(SkillType.GIVE_AND_GO)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 7),
            PlayerSelected("A3".playerId), // Target of hand-off
            *catch(6.d6),
        )
        assertTrue(awayTeam["A3".playerId].hasBall())
        assertFalse(state.isTurnOver())
        controller.rollForward(
            *moveTo(14, 8),
            EndAction
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(14, 8), awayTeam["A10".playerId].location)
    }

    @Test
    fun doNotWorkAfterTurnoverDuringHandoff() {
        awayTeam["A10".playerId].addSkill(SkillType.GIVE_AND_GO)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.HAND_OFF),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(14, 7),
            PlayerSelected("A3".playerId), // Target of hand-off
            *catch(1.d6),
            3.d8 // Bounce
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
    }
}
