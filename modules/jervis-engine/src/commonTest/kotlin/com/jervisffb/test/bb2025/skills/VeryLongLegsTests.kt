package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.context.JumpRollContext
import com.jervisffb.engine.model.context.LeapRollContext
import com.jervisffb.engine.model.context.PogoRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.InterceptionModifier
import com.jervisffb.engine.model.modifiers.LeapModifier
import com.jervisffb.engine.rules.bb2025.procedures.actions.pass.InterceptionRollContext
import com.jervisffb.engine.rules.bb2025.skills.VeryLongLegs
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.skills.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.jumpTo
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.SelectTeamReroll
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [VeryLongLegs] skill.
 */
class VeryLongLegsTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun ignoresCloudBurster() {
        awayTeam["A10".playerId].addSkill(SkillType.CLOUD_BURSTER)
        homeTeam["H2".playerId].apply {
            agility = 1 // Make interceptor superhuman
            addSkill(SkillType.VERY_LONG_LEGS)
        }
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 9),
            *throwBall(6.d6),
            PlayerSelected("H2".playerId), // Select Interceptor
            4.d6, // Intercept (with "-6 + 2" modifier) - Will fail
        )
        assertFalse(state.getContext<PassContext>().intercept!!.didIntercept)
        controller.rollForward(
            Undo,
            5.d6, // Intercept - Will succeed
        )
        assertEquals(homeTeam, state.activeTeam)
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun isOptional() {
        homeTeam["H1".playerId].putProne()
        assertEquals(3, awayTeam["A1".playerId].agility)
        awayTeam["A1".playerId].addSkill(SkillType.VERY_LONG_LEGS)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *jumpTo(11, 4),
            Cancel, // Do not use Very Long Legs
            3.d6,
        )
        assertFalse(state.getContext<JumpRollContext>().isSuccess)
        controller.rollForward(
            SelectTeamReroll<RegularTeamReroll>(),
            4.d6
        )
        val player = awayTeam["A1".playerId]
        player.assertStanding()
        player.assertCoordinates(11, 4)
    }

    @Test
    fun improvesInterception() {
        homeTeam["H2".playerId].apply {
            agility = 1 // Make interceptor superhuman
            addSkill(SkillType.VERY_LONG_LEGS)
        }
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(13, 9),
            *throwBall(6.d6),
            PlayerSelected("H2".playerId), // Select Interceptor
        )
        assertContains(state.getContext<InterceptionRollContext>().modifiers, InterceptionModifier.VERY_LONG_LEGS)
        controller.rollForward(
            5.d6, // Intercept - Will succeed
        )
        assertEquals(homeTeam, state.activeTeam)
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun worksOnLeap() {
        awayTeam["A1".playerId].apply {
            addSkill(SkillType.VERY_LONG_LEGS)
            addSkill(SkillType.LEAP)
        }
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 4),
            Confirm, // Use Very Long Legs
            3.d6, // -2 Marked, +1 Leap
        )
        assertFalse(state.getContext<LeapRollContext>().isSuccess)
        assertTrue(state.getContext<LeapRollContext>().modifiers.contains(LeapModifier.VERY_LONG_LEGS))
        controller.rollForward(
            SelectTeamReroll<RegularTeamReroll>(),
            4.d6
        )
        val player = awayTeam["A1".playerId]
        player.assertStanding()
        player.assertCoordinates(11, 4)
    }

    @Test
    fun doesNotWorkOnPogo() {
        awayTeam["A1".playerId].apply {
            addSkill(SkillType.VERY_LONG_LEGS)
            addSkill(SkillType.POGO_STICK)
        }
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.POGO),
            FieldSquareSelected(11, 4),
            Confirm, // Use Very Long Legs
            2.d6, // -2 Marked (ignored)
        )
        assertFalse(state.getContext<PogoRollContext>().isSuccess)
        assertFalse(state.getContext<PogoRollContext>().modifiers.any { it.description == "Leap" })
        controller.rollForward(
            SelectTeamReroll<RegularTeamReroll>(),
            3.d6
        )
        val player = awayTeam["A1".playerId]
        player.assertStanding()
        player.assertCoordinates(11, 4)
    }

    @Test
    fun worksOnJump() {
        homeTeam["H1".playerId].putProne()
        assertEquals(3, awayTeam["A1".playerId].agility)
        awayTeam["A1".playerId].addSkill(SkillType.VERY_LONG_LEGS)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *jumpTo(11, 4),
            Confirm, // Use Very long legs
            3.d6, // Modifiers: -1 Mark +1 Very Long Leg
            NoRerollSelected(),
            EndAction
        )
        val player = awayTeam["A1".playerId]
        player.assertStanding()
        player.assertCoordinates(11, 4)
    }
}
