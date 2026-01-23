package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.getSkill
import com.jervisffb.engine.rules.bb2025.skills.PutTheBootIn
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [PutTheBootIn] skill.
 * Technically, you could reroll a shadowing roll, but since both Team Rerolls
 * and Pro don't work during the opponent turn, these cannot be used, so no
 * known reroll type exists.
 */
class PutTheBootInTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            awayTeam["A2".playerId].addSkill(SkillType.PUT_THE_BOOT_IN.id())
        }
        startDefaultGame()
    }

    @Test
    fun automaticallyUsePutTheBootIn() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(2.d6, 6.d6), // With +1 from Boot, should break armour
            DiceRollResults(1.d6, 2.d6),
        )
        assertEquals(PlayerState.STUNNED, homeTeam["H1".playerId].state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun doesNotDoubleCountIfPlayerIsFree() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        homeTeam["H2".playerId].state = PlayerState.PRONE
        homeTeam["H3".playerId].state = PlayerState.PRONE
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(2.d6, 5.d6), // With +1 from Assist, should only reach 8, thus not breaking armour
        )
        assertEquals(PlayerState.PRONE, homeTeam["H1".playerId].state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun allAdjacentPlayersCanUseIt() {
        awayTeam["A1".playerId].addSkill(SkillType.PUT_THE_BOOT_IN.id())
        awayTeam["A2".playerId].removeSkill(awayTeam["A2".playerId].getSkill<PutTheBootIn>())
        awayTeam["A3".playerId].addSkill(SkillType.PUT_THE_BOOT_IN.id())
        homeTeam["H2".playerId].state = PlayerState.PRONE
        controller.rollForward(
            *activatePlayer("A2", PlayerStandardActionType.FOUL),
            PlayerSelected("H2".playerId),
            DiceRollResults(2.d6, 5.d6), // With +2 from Boot, should breach armour
            DiceRollResults(1.d6, 2.d6),
        )
        assertEquals(PlayerState.STUNNED, homeTeam["H2".playerId].state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun skillOnFoulerDoesNotCount() {
        homeTeam["H2".playerId].state = PlayerState.PRONE
        controller.rollForward(
            *activatePlayer("A2", PlayerStandardActionType.FOUL),
            PlayerSelected("H2".playerId),
            DiceRollResults(2.d6, 6.d6), // No assists
        )
        assertEquals(PlayerState.PRONE, homeTeam["H2".playerId].state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun cannotUseItIfDistracted() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        awayTeam["A2".playerId].hasTackleZones = false
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(2.d6, 6.d6), // Boot is Distracted, so cannot help
        )
        assertEquals(PlayerState.PRONE, homeTeam["H1".playerId].state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun cannotUseItIfProne() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        awayTeam["A2".playerId].state = PlayerState.PRONE
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(2.d6, 6.d6), // Boot is Prone, so cannot help
        )
        assertEquals(PlayerState.PRONE, homeTeam["H1".playerId].state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun onlyWorksOnOffensiveAssists() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        homeTeam["H2".playerId].addSkill(SkillType.PUT_THE_BOOT_IN)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(2.d6, 6.d6), // With +1 from Boot, should break armour
            DiceRollResults(1.d6, 2.d6),
        )
        assertEquals(PlayerState.STUNNED, homeTeam["H1".playerId].state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }
}
