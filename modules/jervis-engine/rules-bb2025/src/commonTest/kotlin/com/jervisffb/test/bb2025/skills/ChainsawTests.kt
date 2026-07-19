package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.Chainsaw
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.bounce
import com.jervisffb.test.chainsawRoll
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.giveBallToPlayer
import com.jervisffb.test.landingRoll
import com.jervisffb.test.moveTo
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.standardBlock
import com.jervisffb.test.steadyFootingRoll
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.assertStunned
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Chainsaw] skill
 */
class ChainsawTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun specialBlockAction() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            PlayerSelected(attacker)
        )
        val actions = controller.getAvailableActions().get<SelectPlayerAction>().actions
        assertTrue(actions.any { it.type == PlayerSpecialActionType.CHAINSAW })
    }

    @Test
    fun notRequiredWhenSelectingSpecialBlockActions() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
            addSkill(SkillType.PROJECTILE_VOMIT)
            addSkill(SkillType.STAB)
            addSkill(SkillType.BREATHE_FIRE)
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            PlayerSelected(attacker)
        )
        val actions = controller.getAvailableActions().get<SelectPlayerAction>().actions
        assertTrue(actions.any { it.type == PlayerSpecialActionType.CHAINSAW })
        assertTrue(actions.any { it.type == PlayerSpecialActionType.PROJECTILE_VOMIT })
        assertTrue(actions.any { it.type == PlayerSpecialActionType.STAB })
        assertTrue(actions.any { it.type == PlayerSpecialActionType.BREATHE_FIRE })
    }

    @Test
    fun canBeUsedDuringBlitz() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.CHAINSAW),
            *chainsawRoll(6.d6),
            DiceRollResults(1.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        defender.assertStanding()
    }

    @Test
    fun canBeUsedDuringFoul() {
        val fouler = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        val victim = homeTeam["H1".playerId].apply {
            putProne()
        }
        assertEquals(9, victim.armorValue)
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            PlayerSelected(victim),
            DiceRollResults(1.d6, 5.d6),
            Confirm, // Use Chainsaw
            *chainsawRoll(2.d6),
            DiceRollResults(1.d6, 2.d6),
        )
        state.assertNoActivePlayer()
        victim.assertStunned()
        fouler.assertStanding()
    }

    @Test
    fun worksOn2Plus() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHAINSAW),
            PlayerSelected(defender),
            *chainsawRoll(2.d6),
            DiceRollResults(1.d6, 2.d6),
        )
        state.assertNoActivePlayer()
        defender.assertStanding()
        attacker.assertStanding()
    }

    @Test
    fun kickbackOnBlock() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHAINSAW),
            PlayerSelected(defender),
            *chainsawRoll(1.d6),
            DiceRollResults(3.d6, 4.d6),
            DiceRollResults(1.d6, 2.d6),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        defender.assertStanding()
        attacker.assertStunned()
    }

    @Test
    fun kickbackOnFoul() {
        val fouler = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        val victim = homeTeam["H1".playerId].apply {
            putProne()
        }
        controller.run {
            rollForward(
                *activatePlayer(fouler, PlayerStandardActionType.FOUL),
                PlayerSelected(victim),
                DiceRollResults(1.d6, 5.d6),
                Confirm, // Use Chainsaw
                *chainsawRoll(1.d6),
                DiceRollResults(4.d6, 2.d6), // Fouler AV roll
                DiceRollResults(1.d6, 2.d6), // Fouler Injury roll
            )
        }
        state.assertNoActivePlayer()
        victim.assertProne()
        fouler.assertStunned()
    }

    @Test
    fun add3PlusToArmourDuringBlock() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        val defender = homeTeam["H1".playerId]
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHAINSAW),
            PlayerSelected(defender),
            *chainsawRoll(2.d6),
            DiceRollResults(5.d6, 1.d6),
            DiceRollResults(1.d6, 2.d6),
        )
        state.assertNoActivePlayer()
        defender.assertStunned()
        attacker.assertStanding()
    }

    @Test
    fun add3PlusToArmourDuringFoul() {
        val fouler = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        val victim = homeTeam["H1".playerId].apply {
            putProne()
        }
        assertEquals(9, victim.armorValue)
        controller.run {
            rollForward(
                *activatePlayer(fouler, PlayerStandardActionType.FOUL),
                PlayerSelected(victim),
                DiceRollResults(1.d6, 5.d6),
                Confirm,
                *chainsawRoll(2.d6),
                DiceRollResults(1.d6, 2.d6)
            )
        }
        state.assertNoActivePlayer()
        victim.assertStunned()
        fouler.assertStanding()
    }

    @Test
    fun usedOnFailedDodge() {
        val mover = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        controller.run {
            rollForward(
                *activatePlayer(mover, PlayerStandardActionType.MOVE),
                *moveTo(14, 5),
                *dodge(1.d6),
                DiceRollResults(1.d6, 5.d6),
                DiceRollResults(1.d6, 2.d6),
            )
        }
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        mover.assertStunned()
    }

    @Test
    fun usedOnFailedThrowTeammate() {
        setupAndStartThrowTeamMateGame()
        val thrownPlayer = awayTeam["A13".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        assertEquals(7, thrownPlayer.armorValue)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(10, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Always scatter
            *landingRoll(1.d6),
        )
        controller.rollForward(
            DiceRollResults(1.d6, 3.d6),
            DiceRollResults(1.d6, 2.d6),
        )
        thrownPlayer.assertStunned(ownTeamTurn = true)
    }

    @Test
    fun combineWithDirtyPlayer() {
        val fouler = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
            addSkill(SkillType.DIRTY_PLAYER)
        }
        val victim = homeTeam["H1".playerId].apply {
            putProne()
        }
        assertEquals(9, victim.armorValue)
        controller.run {
            rollForward(
                *activatePlayer(fouler, PlayerStandardActionType.FOUL),
                PlayerSelected(victim),
                DiceRollResults(1.d6, 4.d6),
                Confirm,
                *chainsawRoll(2.d6),
                Confirm, // Use Dirty Player
                DiceRollResults(1.d6, 2.d6)
            )
        }
        state.assertNoActivePlayer()
        victim.assertStunned()
        fouler.assertStanding()
    }

    @Test
    fun steadyFootingDuringFoulKickback() {
        val fouler = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
            addSkill(SkillType.STEADY_FOOTING)
        }
        val victim = homeTeam["H1".playerId].apply {
            putProne()
        }
        assertEquals(9, victim.armorValue)
        controller.run {
            rollForward(
                *activatePlayer(fouler, PlayerStandardActionType.FOUL),
                PlayerSelected(victim),
                DiceRollResults(3.d6, 5.d6),
                Confirm, // Use Chainsaw
                *chainsawRoll(1.d6),
                Confirm, // Use Steady Footing
                *steadyFootingRoll(6.d6), // Kickback averted, but +3 is still not used on Victim
            )
        }
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        victim.assertProne()
        fouler.assertStanding()
    }

    // Averting the kickback on the Special Action, still doesn't mean we add the +3
    @Test
    fun steadyFootingDuringBlockKickback() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
            addSkill(SkillType.STEADY_FOOTING)
        }
        val defender = homeTeam["H1".playerId]
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHAINSAW),
            PlayerSelected(defender),
            *chainsawRoll(1.d6),
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6),
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        defender.assertStanding()
        attacker.assertStanding()
    }

    @Test
    fun bounceBallIfHitByChainsaw() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        val defender = homeTeam["H1".playerId]
        giveBallToPlayer(defender)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHAINSAW),
            PlayerSelected(defender),
            *chainsawRoll(2.d6),
            DiceRollResults(5.d6, 2.d6),
            DiceRollResults(1.d6, 2.d6),
            bounce(4.d8)
        )
        state.assertNoActivePlayer()
        defender.assertStunned()
        attacker.assertStanding()
        state.singleBall().assertCoordinates(11, 5)
    }

    @Test
    fun bounceBallIfKickbackDuringBlock() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
            giveBallToPlayer(this)
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHAINSAW),
            PlayerSelected(defender),
            *chainsawRoll(1.d6),
            DiceRollResults(1.d6, 1.d6),
            bounce(5.d8)
        )
        state.assertNoActivePlayer()
        defender.assertStanding()
        attacker.assertProne()
        state.singleBall().assertCoordinates(14, 5)
    }

    @Test
    fun bounceBallIfKickbackDuringFoul() {
        val fouler = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
            giveBallToPlayer(this)
        }
        val victim = homeTeam["H1".playerId].apply {
            putProne()
        }
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            PlayerSelected(victim),
            DiceRollResults(1.d6, 5.d6),
            Confirm, // Use Chainsaw
            *chainsawRoll(1.d6),
            DiceRollResults(1.d6, 2.d6),
            bounce(5.d8)
        )
        state.assertNoActivePlayer()
        victim.assertProne()
        fouler.assertProne()
        state.singleBall().assertCoordinates(14, 5)
    }

    @Test
    fun doNotUseChainsawOnFoulIfNotNeeded() {
        val fouler = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        val victim = homeTeam["H1".playerId].apply {
            putProne()
        }
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            PlayerSelected(victim),
            DiceRollResults(1.d6, 4.d6),
        )
        state.assertNoActivePlayer()
        victim.assertProne()
        fouler.assertStanding()
    }

    @Test
    fun useChainsawIfDirtyPlayerCanBreakArmour() {
        val fouler = awayTeam["A1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
            addSkill(SkillType.DIRTY_PLAYER)
        }
        val victim = homeTeam["H1".playerId].apply {
            putProne()
        }
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            PlayerSelected(victim),
            DiceRollResults(1.d6, 4.d6),
            Confirm, // Use Chainsaw
            *chainsawRoll(2.d6),
            Confirm, // Use Dirty Player
            DiceRollResults(1.d6, 2.d6),
        )
        state.assertNoActivePlayer()
        victim.assertStunned()
        fouler.assertStanding()
    }

    @Test
    fun doesNotTriggerOnPlayerDownIfDefenderHasChainsaw() {
        val attacker = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId].apply {
            addSkill(SkillType.CHAINSAW)
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 1.dblock),
            DiceRollResults(4.d6, 4.d6),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        defender.assertStanding()
        attacker.assertProne()
    }
}
