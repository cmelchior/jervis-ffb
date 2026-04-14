package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.ForegoActivationSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.rules.bb2025.skills.EyeGouge
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultKickOffAwayTeam
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.skipTurns
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.putProne
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [EyeGouge] skill.
 */
class EyeGougeTests: JervisGameBB2025Test() {

    @Test
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun applyOnPushback() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.EYE_GOUGE)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            Confirm, // Use Eye Gouge
            DirectionSelected(Direction.LEFT),
            Cancel,
        )
        assertNull(state.activePlayer)
        assertTrue(defender.statusEffects.any { it.type == PlayerStatusEffectType.EYE_GOUGE })
    }

    @Test
    fun applyOnPushbackIntoCrowd() {
        SetPlayerLocation(homeTeam[8.playerNo], PitchCoordinate(1, 14)).execute(state)
        SetPlayerLocation(awayTeam[1.playerNo], PitchCoordinate(1, 13)).execute(state)
        awayTeam["A1".playerId].apply {
            addSkill(SkillType.EYE_GOUGE)
        }
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H8", 4.dblock),
            Confirm, // Use Eye Gouge
            DirectionSelected(Direction.DOWN),
            Confirm, // Follow up
            DiceRollResults(1.d6, 1.d6), // Injury roll
        )
        assertNull(state.activePlayer)
        homeTeam["H8".playerId].let {
            assertEquals(DogOut, it.location)
            assertEquals(PlayerState.RESERVE, it.state)
            assertTrue(it.statusEffects.any { it.type == PlayerStatusEffectType.EYE_GOUGE })
        }
    }

    @Test
    fun applyOnStumble() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.EYE_GOUGE)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 5.dblock),
            Confirm, // Use Eye Gouge
            DirectionSelected(Direction.UP_LEFT),
            Cancel,
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        assertTrue(defender.statusEffects.any { it.type == PlayerStatusEffectType.EYE_GOUGE })
    }

    @Test
    fun applyOnPow() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.EYE_GOUGE)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            Confirm, // Use Eye Gouge
            DirectionSelected(Direction.DOWN_LEFT),
            Cancel,
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        assertTrue(defender.statusEffects.any { it.type == PlayerStatusEffectType.EYE_GOUGE })
    }

    @Test
    fun doesNotApplyWhenUsingStandFirm() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.EYE_GOUGE)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.STAND_FIRM)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            Confirm, // Use Stand Firm
        )
        assertNull(state.activePlayer)
        assertTrue(defender.statusEffects.none { it.type == PlayerStatusEffectType.EYE_GOUGE })
    }

    @Test
    fun workOnFoulAssists() {
        homeTeam["H1".playerId].state = PlayerState.PRONE
        homeTeam["H2".playerId].state = PlayerState.STUNNED
        awayTeam["A1".playerId].addStatusEffect(PlayerStatusEffect.eyeGouge())
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul, A1 can assist if not having Eye Gouge
            DiceRollResults(5.d6, 3.d6)
        )
        assertNull(state.activePlayer)
        homeTeam["H1".playerId].assertProne()
    }

    @Test
    fun workOnBlockAssists() {
        // Allow A2 to assist on H1, it it didn't have Eye Gouge
        listOf("H2", "H3").forEach {
            state.getPlayerById(it.playerId).putProne()
        }
        val player = state.getPlayerById("A1".playerId)
        val assister = state.getPlayerById("A2".playerId)
        assister.addStatusEffect(PlayerStatusEffect.eyeGouge())
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
        )
        val context = state.getContext<BlockContext>()
        assertEquals(0, context.defensiveAssists)
        assertEquals(0, context.offensiveAssists)
    }

    @Test
    fun doesNotWorkOnChainPushes() {
        SetPlayerLocation(homeTeam[4.playerNo], PitchCoordinate(11, 4)).execute(state)
        SetPlayerLocation(homeTeam[10.playerNo], PitchCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[11.playerNo], PitchCoordinate(11, 6)).execute(state)
        homeTeam["H1".playerId].addStatusEffect(PlayerStatusEffect.eyeGouge())
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock), // H1 has Eye Gouge, but does not trigger it on a Chain Push
            DirectionSelected(Direction.LEFT), // First push
            DirectionSelected(Direction.UP_LEFT), // 2nd push
            Confirm // Follow up
        )
        assertEquals(PitchCoordinate(12, 5), awayTeam["A1".playerId].coordinates)
        assertEquals(PitchCoordinate(11, 5), homeTeam["H1".playerId].coordinates)
        assertEquals(PitchCoordinate(10, 4), homeTeam["H10".playerId].coordinates)
    }

    @Test
    fun nextActivationClearStatus() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.EYE_GOUGE)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            Confirm, // Use Eye Gouge
            DirectionSelected(Direction.LEFT),
            Cancel,
        )
        assertNull(state.activePlayer)
        assertTrue(defender.statusEffects.any { it.type == PlayerStatusEffectType.EYE_GOUGE })
        controller.rollForward(
            EndTurn,
            *activatePlayer(defender, PlayerStandardActionType.MOVE),
        )
        assertTrue(defender.statusEffects.none { it.type == PlayerStatusEffectType.EYE_GOUGE })
    }

    @Test
    fun foregoActivationDoesNotClearStatus() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.EYE_GOUGE)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            Confirm, // Use Eye Gouge
            DirectionSelected(Direction.LEFT),
            Cancel,
        )
        assertNull(state.activePlayer)
        assertTrue(defender.statusEffects.any { it.type == PlayerStatusEffectType.EYE_GOUGE })
        controller.rollForward(
            EndTurn,
            ForegoActivationSelected(defender),
        )
        assertTrue(defender.statusEffects.any { it.type == PlayerStatusEffectType.EYE_GOUGE })
    }

    @Test
    fun endOfHalfDoesNotClearStatus() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.EYE_GOUGE)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            Confirm, // Use Eye Gouge
            DirectionSelected(Direction.LEFT),
            Cancel,
        )
        assertNull(state.activePlayer)
        assertTrue(defender.statusEffects.any { it.type == PlayerStatusEffectType.EYE_GOUGE })
        controller.rollForward(
            EndTurn,
            *skipTurns(15),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam()
        )
        assertEquals(2, state.halfNo)
        assertEquals(homeTeam, state.activeTeam)
        assertTrue(defender.statusEffects.any { it.type == PlayerStatusEffectType.EYE_GOUGE })
    }
}
