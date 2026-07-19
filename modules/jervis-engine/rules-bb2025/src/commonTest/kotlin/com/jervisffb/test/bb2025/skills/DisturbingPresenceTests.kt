package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.context.CatchContext
import com.jervisffb.engine.model.context.InterceptionContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DisturbingPresenceModifier
import com.jervisffb.engine.rules.bb2025.skills.DisturbingPresence
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext
import com.jervisffb.engine.rules.common.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.sum
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.giveBallToPlayer
import com.jervisffb.test.landingRoll
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.makeDistracted
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [DisturbingPresence] skill.
 */
class DisturbingPresenceTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun workWhenDistracted() {
        val thrower = awayTeam["A1".playerId]
        thrower.passing = 3
        giveBallToPlayer(thrower)
        homeTeam["H1".playerId].putProne()
        homeTeam["H2".playerId].putProne()
        homeTeam["H3".playerId].apply {
            addSkill(SkillType.DISTURBING_PRESENCE)
            makeDistracted()
        }
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.PASS),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            4.d6,
        )
        val context = state.getContext<PassContext>()
        assertTrue(context.passingModifiers.any { it is DisturbingPresenceModifier })
        assertEquals(-2, context.passingModifiers.sum())
        controller.rollForward(
            TeamRerollSelected<StandardTeamReroll>(),
            5.d6,
            *catch(6.d6)
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun workWhenProne() {
        val thrower = awayTeam["A1".playerId]
        thrower.passing = 3
        giveBallToPlayer(thrower)
        homeTeam["H1".playerId].putProne()
        homeTeam["H2".playerId].putProne()
        homeTeam["H3".playerId].apply {
            addSkill(SkillType.DISTURBING_PRESENCE)
            putProne()
        }
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.PASS),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            4.d6,
        )
        val context = state.getContext<PassContext>()
        assertTrue(context.passingModifiers.any { it is DisturbingPresenceModifier })
        assertEquals(-2, context.passingModifiers.sum())
        controller.rollForward(
            TeamRerollSelected<StandardTeamReroll>(),
            5.d6,
            *catch(6.d6)
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun workOnPassAction() {
        val thrower = awayTeam["A1".playerId]
        thrower.passing = 3
        giveBallToPlayer(thrower)

        homeTeam["H1".playerId].putProne()
        homeTeam["H2".playerId].putProne()
        homeTeam["H3".playerId].addSkill(SkillType.DISTURBING_PRESENCE)

        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.PASS),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(15, 1),
            4.d6,
        )
        val context = state.getContext<PassContext>()
        assertTrue(context.passingModifiers.any { it is DisturbingPresenceModifier })
        assertEquals(-2, context.passingModifiers.sum())
        controller.rollForward(
            TeamRerollSelected<StandardTeamReroll>(),
            5.d6
        )
        assertEquals(PassingType.ACCURATE, state.getContext<PassContext>().passingResult)
        controller.rollForward(
            *catch(6.d6)
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun workOnThrowTeammateAction() {
        setupAndStartThrowTeamMateGame()
        val thrownPlayer = awayTeam["A13".playerId]
        homeTeam["H1".playerId].addSkill(SkillType.DISTURBING_PRESENCE)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(8, 4),
            *qualityRoll(1.d6),
        )
        val context = state.getContext<ThrowTeamMateContext>()
        assertTrue(context.qualityRollModifiers.any { it is DisturbingPresenceModifier })
        assertEquals(-4, context.qualityRollModifiers.sum())

        controller.rollForward(
            3.d8, // Bounce
            *landingRoll(4.d6)
        )
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(14, 4)
    }

    @Ignore
    @Test
    fun workOnBombSpecialAction() {
        // TODO Waiting for Bombardier support
    }

    @Test
    fun workOnInterceptRoll() {
        val thrower = awayTeam["A1".playerId]
        thrower.addSkill(SkillType.DISTURBING_PRESENCE)
        giveBallToPlayer(thrower)
        homeTeam["H1".playerId].putProne()
        homeTeam["H2".playerId].putProne()
        val interceptor = homeTeam["H3".playerId]
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.PASS),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(11, 8),
            *throwBall(4.d6), // Inaccurate Pass
            DiceRollResults(4.d8, 2.d8, 5.d8) // Scatter back to the original target
        )
        interceptor.agility = 1 // Make interceptor super human
        controller.rollForward(
            PlayerSelected(interceptor),
            5.d6, // Intercept (with -6 modifier) - Will fail
        )
        val context = state.getContext<PassContext>()
        val intercept = context.intercept as InterceptionContext
        assertFalse(intercept.didIntercept)
        assertTrue(intercept.interceptionRoll?.modifiers.orEmpty().any { it is DisturbingPresenceModifier })
        controller.rollForward(
            Undo,
            6.d6, // Intercept - Will succeed
        )
        assertEquals(homeTeam, state.activeTeam)
        assertTrue(interceptor.hasBall())
    }

    @Test
    fun workOnCatch() {
        val thrower = awayTeam["A6".playerId]
        val catcher = awayTeam["A1".playerId]
        catcher.agility = 2
        homeTeam["H1".playerId].addSkill(SkillType.DISTURBING_PRESENCE)
        giveBallToPlayer(thrower)

        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.PASS),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(catcher.coordinates),
            *throwBall(6.d6),
            1.d6, // Catch
        )
        val context = state.getContext<CatchContext>()
        assertTrue(context.modifiers.any { it is DisturbingPresenceModifier })
        assertEquals(-3, context.modifiers.sum())
        controller.rollForward(
            TeamRerollSelected<StandardTeamReroll>(),
            6.d6,
        )
        assertTrue(catcher.hasBall())
        state.assertNoActivePlayer()
    }

    @Test
    fun onlyOpponentPlayersCount() {
        listOf("A1", "A2", "A3", "A4", "A5").forEach {
            awayTeam[it.playerId].addSkill(SkillType.DISTURBING_PRESENCE)

        }
        listOf("H1", "H2", "H3", "H4", "H5").forEach {
            homeTeam[it.playerId].addSkill(SkillType.DISTURBING_PRESENCE)
        }

        val thrower = awayTeam["A2".playerId]
        giveBallToPlayer(thrower)
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.PASS),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(homeTeam["H2".playerId].coordinates),
            1.d6,
        )
        val context = state.getContext<PassContext>()
        assertTrue(context.passingModifiers.any { it is DisturbingPresenceModifier })
        assertEquals(-8, context.passingModifiers.sum())
    }

    @Ignore
    @Test
    fun doesNotWorkOnKickTeammate() {
        // TODO Waiting for Kick Team-mate support
    }

    @Test
    fun mustBeWithin3squares() {
        val thrower = awayTeam["A1".playerId]
        homeTeam["H4".playerId].addSkill(SkillType.DISTURBING_PRESENCE)
        homeTeam["H5".playerId].addSkill(SkillType.DISTURBING_PRESENCE)
        giveBallToPlayer(thrower)
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.PASS),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(homeTeam["H1".playerId].coordinates),
            1.d6,
        )
        val context = state.getContext<PassContext>()
        assertTrue(context.passingModifiers.any { it is DisturbingPresenceModifier })
        assertEquals(-1, context.passingModifiers.filterIsInstance<DisturbingPresenceModifier>().single().modifier)
    }
}
