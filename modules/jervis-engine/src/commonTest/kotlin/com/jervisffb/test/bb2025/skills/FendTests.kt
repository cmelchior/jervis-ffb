package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Fend
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Fend] skill.
 */
class FendTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun preventFollowUpFromPushback() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.FEND)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock), // Pushed Back
            DirectionSelected(Direction.UP_LEFT),
            Confirm, // Use Fend
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(13, 5), attacker.location)
    }

    @Test
    fun preventFollowUpFromStumble() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.FEND)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 5.dblock), // Stumble
            DirectionSelected(Direction.UP_LEFT),
            Confirm, // Use Fend
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(13, 5), attacker.location)
    }

    @Test
    fun preventFollowUpFromPow() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.FEND)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock), // POW
            DirectionSelected(Direction.UP_LEFT),
            Confirm, // Use Fend
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(13, 5), attacker.location)
    }



    @Test
    fun preventFollowUpFromFrenzy() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        attacker.addSkill(SkillType.FRENZY)
        defender.addSkill(SkillType.FEND)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock), // Pushed Back
            DirectionSelected(Direction.UP_LEFT),
            Confirm, // Use Fend
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(13, 5), attacker.location)
    }

    @Test
    fun doesNotWorkOnBothDown() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.apply {
            addSkill(SkillType.FEND)
            addSkill(SkillType.BLOCK)
        }
        attacker.addSkill(SkillType.BLOCK)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 2.dblock),
            Confirm, // Use Block (Defender)
            Confirm, // Use Block (Attacker)
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(12, 5), defender.location)
        assertTrue(rules.isStanding(defender))
    }

    @Ignore
    @Test
    fun doesNotWorkAgainstBallChain() {
        // TODO
    }

    @Ignore
    @Test
    fun doesNotWorkAgainstJuggernaut() {
        // TODO
    }
}
