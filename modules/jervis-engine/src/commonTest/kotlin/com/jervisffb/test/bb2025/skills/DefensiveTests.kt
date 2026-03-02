package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.rules.bb2025.skills.Defensive
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Defensive] skill.
 */
class DefensiveTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun preventGuard_defensive() {
        awayTeam["A2".playerId].addSkill(SkillType.GUARD)
        homeTeam["H2".playerId].addSkill(SkillType.DEFENSIVE)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
            BlockTypeSelected(BlockType.STANDARD)
        )
        assertEquals(1, controller.getAvailableActions().get<RollDice>().dice.size)
    }

    @Test
    fun preventGuard_offensive() {
        awayTeam["A2".playerId].apply {
            addSkill(SkillType.GUARD)
            addSkill(SkillType.DEFENSIVE)
        }
        homeTeam["H2".playerId].addSkill(SkillType.GUARD)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
            BlockTypeSelected(BlockType.STANDARD)
        )
        assertEquals(2, controller.getAvailableActions().get<RollDice>().dice.size)
    }

    @Test
    fun preventGuardFromAllOpponents() {
        awayTeam["A2".playerId].addSkill(SkillType.DEFENSIVE)
        homeTeam["H1".playerId].addSkill(SkillType.GUARD)
        homeTeam["H3".playerId].addSkill(SkillType.GUARD)
        controller.rollForward(
            *activatePlayer("A2", PlayerStandardActionType.BLOCK),
            PlayerSelected("H2".playerId),
            BlockTypeSelected(BlockType.STANDARD)
        )
        assertEquals(1, controller.getAvailableActions().get<RollDice>().dice.size)
    }

    @Test
    fun doesNotPreventGuardWhenDistracted() {
        awayTeam["A2".playerId].addSkill(SkillType.GUARD)
        val assister = homeTeam["H2".playerId]
        assister.apply {
            addSkill(SkillType.DEFENSIVE)
            hasTackleZones = false
        }
        assertTrue(rules.isDistracted(assister))
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
            BlockTypeSelected(BlockType.STANDARD)
        )
        assertEquals(2, controller.getAvailableActions().get<RollDice>().dice.size)
    }

    @Test
    fun preventPutTheBootIn() {
        val assister = awayTeam["A2".playerId]
        assister.addSkill(SkillType.PUT_THE_BOOT_IN)

        val foulTarget = homeTeam["H1".playerId]
        foulTarget.state = PlayerState.PRONE

        val fouler = awayTeam["A1".playerId]
        fouler.addSkill(SkillType.DEFENSIVE)

        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected(foulTarget),
            DiceRollResults(2.d6, 6.d6), // With +1 from Boot it should break armour
            DiceRollResults(1.d6, 2.d6),
        )
        assertEquals(PlayerState.STUNNED, foulTarget.state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun doesNotPreventPutTheBootInWhenDistracted() {
        val assister = awayTeam["A2".playerId]
        assister.apply {
            addSkill(SkillType.PUT_THE_BOOT_IN)
            hasTackleZones = false
        }
        assertTrue(rules.isDistracted(assister))

        val foulTarget = homeTeam["H1".playerId]
        foulTarget.state = PlayerState.PRONE

        val fouler = awayTeam["A1".playerId]
        fouler.addSkill(SkillType.DEFENSIVE)

        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected(foulTarget),
            DiceRollResults(2.d6, 6.d6), // With +1 from Boot it should break armour, without it shouldn't
        )
        assertEquals(PlayerState.PRONE, foulTarget.state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }
}
