package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.QualityModifier
import com.jervisffb.engine.rules.bb2025.skills.StrongArm
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.landingRoll
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [StrongArm] skill.
 */
class StrongArmTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        setupAndStartThrowTeamMateGame()
    }

    @Test
    fun useOnQuickPass() {
        awayTeam["A1".playerId].addSkill(SkillType.STRONG_ARM)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            PitchSquareSelected(11, 4), // Quick Pass - No modifiers
            Confirm, // Use Strong Arm
        )
        assertTrue(state.getContext<ThrowTeamMateContext>().qualityRollModifiers.contains(QualityModifier.STRONG_ARM))
        controller.rollForward(
            4.d6, // Should only succeed with +1 from Strong Arm
            NoRerollSelected(),
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            *landingRoll(6.d6)
        )
        awayTeam["A13".playerId].assertStanding()
        assertEquals(PitchCoordinate(8, 4), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun useOnShortPass() {
        awayTeam["A1".playerId].addSkill(SkillType.STRONG_ARM)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            PitchSquareSelected(9, 4), // Short Pass (-1)
            Confirm, // Use Strong Arm
        )
        assertTrue(state.getContext<ThrowTeamMateContext>().qualityRollModifiers.contains(QualityModifier.STRONG_ARM))
        controller.rollForward(
            5.d6, // Should only succeed with +1 from Strong Arm
            NoRerollSelected(),
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            *landingRoll(6.d6)
        )
        awayTeam["A13".playerId].assertStanding()
        assertEquals(PitchCoordinate(6, 4), awayTeam["A13".playerId].coordinates)
    }

    @Test
    fun doNotUse() {
        awayTeam["A1".playerId].addSkill(SkillType.STRONG_ARM)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected("A13".playerId),
            PitchSquareSelected(11, 4), // Quick Pass - No modifiers
            Cancel, // Do not use Strong Arm
            4.d6, // Should only succeed with +1 from Strong Arm
            NoRerollSelected(), // Subpar throw
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            *landingRoll(3.d6), // Fail landing
            DiceRollResults(1.d6, 1.d6),
        )
        awayTeam["A13".playerId].assertProne()
        assertEquals(PitchCoordinate(8, 4), awayTeam["A13".playerId].coordinates)
    }
}
