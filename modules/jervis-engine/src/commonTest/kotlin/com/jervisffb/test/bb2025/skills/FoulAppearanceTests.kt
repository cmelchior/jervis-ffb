package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.bb2025.skills.FoulAppearance
import com.jervisffb.engine.rules.common.actions.ActionType
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.foulAppearanceRoll
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertStanding
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [FoulAppearance] skill
 */
class FoulAppearanceTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun checkListOfActions() {
        // We use this test to ensure that if new special actions are added,
        // we remember to create a Foul Appearance test for them (or mark them
        // as not applicable)
        val affectedActions = mutableSetOf<ActionType>()
        val unaffectedActions = mutableSetOf<ActionType>()
        PlayerStandardActionType.entries.forEach {
            when (it) {
                PlayerStandardActionType.BLITZ -> affectedActions.add(it)
                PlayerStandardActionType.BLOCK -> affectedActions.add(it)
                PlayerStandardActionType.FOUL -> unaffectedActions.add(it)
                PlayerStandardActionType.HAND_OFF -> unaffectedActions.add(it)
                PlayerStandardActionType.MOVE -> unaffectedActions.add(it)
                PlayerStandardActionType.PASS -> unaffectedActions.add(it)
                PlayerStandardActionType.SECURE_THE_BALL -> unaffectedActions.add(it)
                PlayerStandardActionType.SPECIAL -> unaffectedActions.add(it)
                PlayerStandardActionType.THROW_TEAM_MATE -> unaffectedActions.add(it)
            }
        }
        PlayerSpecialActionType.entries.forEach {
            when (it) {
                PlayerSpecialActionType.BALL_AND_CHAIN -> unaffectedActions.add(it)
                PlayerSpecialActionType.BOMBARDIER -> unaffectedActions.add(it)
                PlayerSpecialActionType.BREATHE_FIRE -> affectedActions.add(it)
                PlayerSpecialActionType.CHAINSAW -> affectedActions.add(it)
                PlayerSpecialActionType.HYPNOTIC_GAZE -> affectedActions.add(it)
                PlayerSpecialActionType.KICK_TEAM_MATE -> unaffectedActions.add(it)
                PlayerSpecialActionType.MULTIPLE_BLOCK -> affectedActions.add(it)
                PlayerSpecialActionType.PROJECTILE_VOMIT -> affectedActions.add(it)
                PlayerSpecialActionType.STAB -> affectedActions.add(it)
            }
        }
        val totalActions = PlayerStandardActionType.entries.size + PlayerSpecialActionType.entries.size
        assertEquals(totalActions, affectedActions.size + unaffectedActions.size)
    }

    @Test
    fun requires2PlusToBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.FOUL_APPEARANCE)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            1.d6, // Fail Foul Appearance
            TeamRerollSelected<RegularTeamReroll>(),
            2.d6, // Succeed Foul Appearance
            4.dblock,
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
        )
        assertNull(state.activePlayer)
        attacker.assertCoordinates(13, 5)
        attacker.assertStanding()
        defender.assertCoordinates(11, 5)
        defender.assertStanding()
    }

    @Test
    fun workOnBlock() {
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.FOUL_APPEARANCE)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            *foulAppearanceRoll(1.d6),
        )
        assertNull(state.activePlayer)
    }

    @Test
    fun workOnBlitz() {
        val attacker = state.getPlayerById("A7".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.FOUL_APPEARANCE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            SmartMoveTo(13, 4),
            PlayerSelected(defender), // Start block
            BlockTypeSelected(BlockType.STANDARD),
            *foulAppearanceRoll(1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(0, awayTeam.turnData.blitzActions)
    }

    @Test
    fun workOnBreatheFire() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.FOUL_APPEARANCE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender),
            *foulAppearanceRoll(1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(Availability.HAS_ACTIVATED, attacker.available)
        defender.assertStanding()
    }

    @Ignore
    @Test
    fun workOnChainsaw() {
        TODO("Waiting for Chainsaw support")
    }

    @Ignore
    @Test
    fun workOnHypnoticGaze() {
        TODO("Waiting for Hypnotic Gaze support")
    }

    @Ignore
    @Test
    fun workOnMultipleBlock() {
        TODO("Waiting for Multiple Block support")
    }

    @Ignore
    @Test
    fun onlyCancelOneBlockDuringMultipleBlock() {
        TODO("Waiting for Multiple Block support")
    }

    @Test
    fun workOnProjectileVomit() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.PROJECTILE_VOMIT)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.FOUL_APPEARANCE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.PROJECTILE_VOMIT),
            PlayerSelected(defender),
            *foulAppearanceRoll(1.d6),
        )
        assertNull(state.activePlayer)
        assertNull(state.activePlayer)
        assertEquals(Availability.HAS_ACTIVATED, attacker.available)
        defender.assertStanding()
    }

    @Test
    fun workOnStab() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.STAB)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.FOUL_APPEARANCE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.STAB),
            PlayerSelected(defender),
            *foulAppearanceRoll(1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(Availability.HAS_ACTIVATED, attacker.available)
        defender.assertStanding()
    }
}
