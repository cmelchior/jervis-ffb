package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.Wrestle
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.blitzBlock
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [Wrestle] skill
 */
class WrestleTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun worksOnBlock() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.WRESTLE)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 2.dblock),
            Confirm, // Use Wrestle
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        attacker.assertProne()
        defender.assertProne()
    }

    @Test
    fun worksOnBlitz() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.WRESTLE)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *blitzBlock("H1", 2.dblock),
            Confirm, // Use Wrestle
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        attacker.assertProne()
        defender.assertProne()
    }

    @Test
    fun blockEndsIfDefenderChoosesFirst() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.WRESTLE)
        val defender = homeTeam["H1".playerId]
        defender.addSkill(SkillType.WRESTLE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 2.dblock),
            Confirm, // Defender uses Wrestle
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        attacker.assertProne()
        defender.assertProne()
    }

    @Test
    fun doesNotWorkWhenDistracted() {
        val attacker = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId]
        defender.apply {
            addSkill(SkillType.WRESTLE)
            makeDistracted()
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 2.dblock),
            DiceRollResults(1.d6, 1.d6),
            DiceRollResults(1.d6, 1.d6)
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        attacker.assertProne()
        defender.assertProne()
    }

    @Test
    fun overridesOpponentHavingBlock() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.BLOCK)
        val defender = homeTeam["H1".playerId]
        defender.addSkill(SkillType.WRESTLE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 2.dblock),
            Confirm // Defender uses Wrestle before Block can be used
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        attacker.assertProne()
        defender.assertProne()
    }
}
