package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.getSkill
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Dodge
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.singleInstanceOf
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.SelectSkillReroll
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Dodge] skill.
 */
class DodgeSkillTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun rerollFailedDodge() {
        val movingPlayer = awayTeam["A1".playerId]
        movingPlayer.addSkill(SkillType.DODGE)
        controller.rollForward(
            *activatePlayer(movingPlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail first Dodge
            SelectSkillReroll(SkillType.DODGE),
            3.d6
        )
        assertEquals(FieldCoordinate(14, 5), movingPlayer.coordinates)
        assertEquals(PlayerState.STANDING, movingPlayer.state)
    }

    @Test
    fun rerollSuccessfulDodge() {
        val movingPlayer = awayTeam["A1".playerId]
        movingPlayer.addSkill(SkillType.DODGE)
        controller.rollForward(
            *activatePlayer(movingPlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            6.d6, // Succeed first Dodge
            SelectSkillReroll(SkillType.DODGE),
            6.d6
        )
        assertEquals(FieldCoordinate(14, 5), movingPlayer.coordinates)
        assertEquals(PlayerState.STANDING, movingPlayer.state)
    }

    @Test
    fun worksOncePrTurn() {
        val movingPlayer = awayTeam["A1".playerId]
        movingPlayer.addSkill(SkillType.DODGE)
        controller.rollForward(
            *activatePlayer(movingPlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail first Dodge
            SelectSkillReroll(SkillType.DODGE),
            3.d6,
            *moveTo(13, 5), // Move back to trigger a 2nd dodge
            *moveTo(14, 5),
            1.d6 // Fail second dodge
        )
        assertTrue(movingPlayer.getSkill<Dodge>().rerollUsed)
        val selectRerolls = controller.getAvailableActions().singleInstanceOf<SelectRerollOption>()
        assertFalse(selectRerolls.options.any {it.getRerollSource(state) is Dodge })
    }

    @Test
    fun preventBeingKnockedDownOnStumble() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.DODGE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 5.dblock), // Stumble
            Confirm, // Use Dodge
            DirectionSelected(Direction.UP_LEFT),
            Cancel // Do not follow up
        )
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
        assertEquals(FieldCoordinate(11, 4), defender.location)
        assertEquals(PlayerState.STANDING, defender.state)
    }

    @Test
    fun doesNotWorkIfDistracted() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.apply {
            addSkill(SkillType.DODGE)
            makeDistracted()
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 5.dblock), // Stumble
            DirectionSelected(Direction.UP_LEFT),
            Cancel, // Do not follow up
        )
        assertEquals(FieldCoordinate(13, 5), attacker.location)
        assertEquals(PlayerState.STANDING, attacker.state)
        assertEquals(FieldCoordinate(11, 4), defender.location)
        assertEquals(PlayerState.KNOCKED_DOWN, defender.state)
    }
}
