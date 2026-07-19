package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.AlwaysHungry
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.accuracy
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.alwaysHungry
import com.jervisffb.test.bounce
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.giveBallToPlayer
import com.jervisffb.test.landingRoll
import com.jervisffb.test.moveTo
import com.jervisffb.test.squirmFree
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertDead
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertStanding
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Class testing usage of the [AlwaysHungry] skill.
 */
class AlwaysHungryTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        setupAndStartThrowTeamMateGame()
    }

    @Test
    fun need2PlusToThrow() {
        val thrower = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ALWAYS_HUNGRY)
        }
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(6.d6),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(11, 4),
            *alwaysHungry(2.d6),
            *accuracy(6.d6),
            DiceRollResults(4.d8, 4.d8, 4.d8), // Always scatter
            *landingRoll(6.d6)
        )
        state.assertNoActivePlayer()
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(8, 4)
    }

    @Test
    fun squirmFreeIsAutomaticFumbledThrow() {
        val thrower = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ALWAYS_HUNGRY)
        }
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(6.d6),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(11, 4),
            *alwaysHungry(1.d6),
            *squirmFree(2.d6),
            bounce(5.d8),
            *landingRoll(6.d6)
        )
        state.assertNoActivePlayer()
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(15, 4)
    }

    @Test
    fun getEaten() {
        val thrower = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ALWAYS_HUNGRY)
        }
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(6.d6),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(11, 4),
            *alwaysHungry(1.d6),
            *squirmFree(1.d6),
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        thrownPlayer.assertDead()
    }

    @Test
    fun apothecaryDoNotWork() {
        assertEquals(1, awayTeam.teamApothecaries.size)
        val thrower = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ALWAYS_HUNGRY)
        }
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(6.d6),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(11, 4),
            *alwaysHungry(1.d6),
            *squirmFree(1.d6),
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        thrownPlayer.assertDead()
        assertFalse(awayTeam.teamApothecaries.single().used)
    }

    @Test
    fun regenDoNotWork() {
        assertEquals(1, awayTeam.teamApothecaries.size)
        val thrower = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ALWAYS_HUNGRY)
        }
        val thrownPlayer = awayTeam["A13".playerId].apply {
            addSkill(SkillType.REGENERATION)
        }
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(6.d6),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(11, 4),
            *alwaysHungry(1.d6),
            *squirmFree(1.d6),
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        thrownPlayer.assertDead()
    }

    @Test
    fun bounceBallAndTurnover() {
        val thrower = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ALWAYS_HUNGRY)
        }
        val thrownPlayer = awayTeam["A13".playerId].apply {
            giveBallToPlayer(this)
        }
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(6.d6),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(11, 4),
            *alwaysHungry(1.d6),
            *squirmFree(1.d6),
            bounce(5.d8)
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        thrownPlayer.assertDead()
        state.singleBall().assertCoordinates(15, 5)
    }
}
