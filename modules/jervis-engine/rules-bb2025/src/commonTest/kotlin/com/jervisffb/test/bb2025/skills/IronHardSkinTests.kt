package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.bb2025.skills.IronHardSkin
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.chainsawRoll
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.followUp
import com.jervisffb.test.moveTo
import com.jervisffb.test.projectileVomitRoll
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.assertStunned
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [IronHardSkin] skill.
 *
 * See page 129 in the BB2025 rulebook.
 */
class IronHardSkinTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun workOnKnockedDown() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.MIGHTY_BLOW)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.IRON_HARD_SKIN)
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(3.d6, 5.d6),
            Confirm, // Use Iron Hard Skin
        )
        state.assertNoActivePlayer()
        defender.assertProne()
    }

    @Test
    fun doNotAskIfNoOpponentRelevantSkills() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.IRON_HARD_SKIN)
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(3.d6, 5.d6),
        )
        state.assertNoActivePlayer()
        defender.assertProne()
    }

    @Test
    fun workOnFoul() {
        val fouler = awayTeam["A1".playerId].apply {
            addSkill(SkillType.DIRTY_PLAYER)
        }
        val victim = homeTeam["H1".playerId].apply {
            addSkill(SkillType.IRON_HARD_SKIN)
            putProne()
        }
        assertEquals(9, victim.armorValue)
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            PlayerSelected(victim),
            DiceRollResults(2.d6, 6.d6),
            Confirm, // Use Iron Hard Skin
        )
        victim.assertProne()
        state.assertNoActivePlayer()
        awayTeam.assertActive()
    }

    // While technically working on Stab, there are no modifiers to apply, so just skip it
    @Test
    fun doNotAskOnStab() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            addSkill(SkillType.STAB)
            addSkill(SkillType.MIGHTY_BLOW.idAdjustment(1))
            addSkill(SkillType.DIRTY_PLAYER)
        }
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.IRON_HARD_SKIN)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.STAB),
            PlayerSelected(defender),
            DiceRollResults(2.d6, 6.d6)
        )
        assertNull(state.activePlayer)
        defender.assertStanding()
    }

    // While technically working on Projectile Vomit, there are no modifiers to apply, so just skip it
    @Test
    fun doNotAskOnProjectileVomit() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.apply {
            addSkill(SkillType.PROJECTILE_VOMIT)
            addSkill(SkillType.MIGHTY_BLOW.idAdjustment(1))
        }
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.IRON_HARD_SKIN)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.PROJECTILE_VOMIT),
            PlayerSelected(defender),
            *projectileVomitRoll(2.d6),
            DiceRollResults(2.d6, 6.d6),
        )
        state.assertNoActivePlayer()
        defender.assertStanding()
    }

    @Test
    fun workAgainstClaws() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.CLAWS)
        val defender = state.getPlayerById("H1".playerId).apply {
            addSkill(SkillType.IRON_HARD_SKIN)
            assertEquals(9, armorValue)
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.DOWN_LEFT),
            followUp(false),
            DiceRollResults(6.d6, 2.d6),
            Confirm, // Use Iron Hard Skin
        )
        state.assertNoActivePlayer()
        defender.assertProne()
    }

    @Test
    fun workAgainstLethalFlight() {
        setupAndStartThrowTeamMateGame()
        val thrownPlayer = awayTeam["A13".playerId].also {
            it.addSkill(SkillType.LETHAL_FLIGHT)
        }
        val hitPlayer = homeTeam["H1".playerId].apply {
            addSkill(SkillType.IRON_HARD_SKIN)
            assertEquals(10, armorValue)
        }
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(12, 5),
            *qualityRoll(6.d6),
            DiceRollResults(1.d8, 7.d8, 5.d8), // Hit target square
            DiceRollResults(3.d6, 6.d6), // Armour roll
            Confirm, // Use Iron Hard Skin
            2.d8, // Bounce to empty square
            DiceRollResults(1.d6, 1.d6), // Thrown Player Prone
        )
        thrownPlayer.assertProne()
        hitPlayer.assertProne()
    }

    @Test
    fun workAgainstArmBar() {
        val dodger = awayTeam["A1".playerId]
        dodger.addSkill(SkillType.IRON_HARD_SKIN)
        val opponent = homeTeam["H1".playerId]
        opponent.addSkill(SkillType.ARM_BAR)
        assertEquals(9, dodger.armorValue)
        controller.rollForward(
            *activatePlayer(dodger, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(1.d6),
            DiceRollResults(3.d6, 5.d6),
            Confirm, // Use Iron Hard Skin
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        dodger.assertProne()
    }

    @Test
    fun workAgainstMightyBlow() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.MIGHTY_BLOW)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.IRON_HARD_SKIN)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(2.d6, 5.d6),
            Confirm // Use Iron Hard Skin
        )
        state.assertNoActivePlayer()
        defender.assertProne()
    }

    @Test
    fun workAgainstDirtyPlayer() {
        val fouler = awayTeam["A1".playerId]
        fouler.addSkill(SkillType.DIRTY_PLAYER)
        val victim = homeTeam["H1".playerId]
        victim.addSkill(SkillType.IRON_HARD_SKIN)
        victim.putProne()
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            PlayerSelected(victim),
            DiceRollResults(2.d6, 6.d6),
            Confirm, // Use Iron Hard Skin
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        victim.assertProne()
    }

    @Test
    fun doesNotWorkAgainstChainsawKickback() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
            addSkill(SkillType.IRON_HARD_SKIN)
        }
        val defender = homeTeam["H1".playerId]
        assertEquals(9, attacker.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHAINSAW),
            PlayerSelected(defender),
            *chainsawRoll(1.d6),
            DiceRollResults(5.d6, 2.d6),
            DiceRollResults(1.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        defender.assertStanding()
        attacker.assertStunned()
    }

    @Test
    fun workAgainstChainsawSpecialAction() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
            addSkill(SkillType.IRON_HARD_SKIN)
        }
        val defender = homeTeam["H1".playerId]
        assertEquals(9, attacker.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHAINSAW),
            PlayerSelected(defender),
            *chainsawRoll(1.d6),
            DiceRollResults(5.d6, 2.d6),
            DiceRollResults(1.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        defender.assertStanding()
        attacker.assertStunned()
    }

    @Test
    fun workAgainstChainsawFoul() {
        val fouler = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        val victim = homeTeam["H1".playerId].apply {
            addSkill(SkillType.IRON_HARD_SKIN)
            putProne()
        }
        assertEquals(9, victim.armorValue)
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            PlayerSelected(victim),
            DiceRollResults(2.d6, 5.d6),
            Confirm, // Use Iron hard Skin
        )
        state.assertNoActivePlayer()
        victim.assertProne()
        fouler.assertStanding()
    }

    // First roll -> Lone Fouler -> 2nd Roll -> Iron Hard Skin prevents Dirty Player
    @Test
    fun workAfterArmourReroll() {
        val fouler = awayTeam["A6".playerId]
        fouler.apply {
            addSkill(SkillType.DIRTY_PLAYER)
            addSkill(SkillType.LONE_FOULER)
        }
        listOf("A1", "A2", "A3", "H1").forEach {
            state.getPlayerById(it.playerId).putProne()
        }
        val target = homeTeam["H1".playerId].apply {
            addSkill(SkillType.IRON_HARD_SKIN)
            assertEquals(9, armorValue)
        }
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6),
            Confirm, // Use Lone Fouler
            DiceRollResults(3.d6, 5.d6),
            Confirm, // Use Iron Hard Skin
        )
        state.assertNoActivePlayer()
        fouler.assertStanding()
        target.assertProne()
    }

    // First roll -> Use Iron Hard Skin -> Lone Fouler -> 2nd Roll -> Iron Hard Skin prevents Dirty Player
    @Test
    fun doNotAskAgainAfterReroll() {
        val fouler = awayTeam["A6".playerId]
        fouler.apply {
            addSkill(SkillType.DIRTY_PLAYER)
            addSkill(SkillType.LONE_FOULER)
        }
        listOf("A1", "A2", "A3", "H1").forEach {
            state.getPlayerById(it.playerId).putProne()
        }
        val target = homeTeam["H1".playerId].apply {
            addSkill(SkillType.IRON_HARD_SKIN)
            assertEquals(9, armorValue)
        }
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target), // Start foul
            DiceRollResults(3.d6, 5.d6),
            Cancel, // Do not use Iron Hard Skin
            Cancel, // Do not use Dirty Player
            Confirm, // Use Lone Fouler
            DiceRollResults(3.d6, 5.d6),
            Confirm, // Use Iron Hard Skin
        )
        state.assertNoActivePlayer()
        fouler.assertStanding()
        target.assertProne()
    }

}
