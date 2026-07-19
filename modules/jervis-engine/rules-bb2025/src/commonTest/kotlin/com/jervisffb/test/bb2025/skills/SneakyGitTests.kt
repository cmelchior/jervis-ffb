package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.rules.bb2025.skills.SneakyGit
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.assertStanding
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [SneakyGit] skill.
 */
class SneakyGitTests: JervisGameBB2025Test() {

    @Test
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun workOnDoubleArmour() {
        val fouler = awayTeam["A6".playerId]
        fouler.addSkill(SkillType.SNEAKY_GIT)
        homeTeam["H1".playerId].state = PlayerPitchState.PRONE
        assertEquals(1, awayTeam.turnData.foulActions)
        assertEquals(awayTeam, state.activeTeam)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Confirm, // Use Sneaky Git
        )
        assertNull(state.activePlayer)
        fouler.assertStanding()
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun doesNotWorkOnDoubleArmourIfBroken() {
        val fouler = awayTeam["A6".playerId]
        fouler.addSkill(SkillType.SNEAKY_GIT)
        homeTeam["H1".playerId].state = PlayerPitchState.PRONE
        assertEquals(1, awayTeam.turnData.foulActions)
        assertEquals(awayTeam, state.activeTeam)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(6.d6, 6.d6), // Roll double -> Sent off
            DiceRollResults(1.d6, 2.d6),
            Cancel // Do not argue the call
        )
        assertNull(state.activePlayer)
        assertEquals(PlayerDogoutState.BANNED, fouler.state)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun doesNotWorkOnDoubleInjury() {
        val fouler = awayTeam["A6".playerId]
        fouler.addSkill(SkillType.SNEAKY_GIT)
        homeTeam["H1".playerId].state = PlayerPitchState.PRONE
        assertEquals(1, awayTeam.turnData.foulActions)
        assertEquals(awayTeam, state.activeTeam)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(5.d6, 6.d6), // Roll double -> Sent off
            DiceRollResults(2.d6, 2.d6),
            Cancel // Do not argue the call
        )
        assertNull(state.activePlayer)
        assertEquals(PlayerDogoutState.BANNED, fouler.state)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Ignore // Wait for Lone Fouler
    @Test
    fun lookAtLastRollWhenUsingLoneFouler() {
        // TODO
    }
}
