package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.rules.bb2025.skills.PileDriver
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.blitzBlock
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.followUp
import com.jervisffb.test.standardBlock
import com.jervisffb.test.useApothecary
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.assertKnockedOut
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.assertStunned
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [PileDriver] skill.
 */
class PileDriverTests: JervisGameBB2025Test() {

    @Test
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun putFoulerProne() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.PILE_DRIVER)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            Confirm, // Follow up
            DiceRollResults(1.d6, 3.d6), // AV roll
            Confirm, // Use Pile Driver
            DiceRollResults(5.d6, 6.d6), // AV Roll
            DiceRollResults(1.d6, 2.d6), // Injury Roll
        )
        assertNull(state.activePlayer)
        attacker.assertProne()
        defender.assertStunned()
    }

    @Test
    fun endsBlitz() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.PILE_DRIVER)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *blitzBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(true),
            DiceRollResults(1.d6, 3.d6), // AV Roll
            Confirm, // Use Pile Driver
            DiceRollResults(2.d6, 3.d6), // AV Roll
        )
        assertNull(state.activePlayer)
        attacker.assertProne()
        defender.assertProne()
    }

    @Test
    fun caughtByRef() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.PILE_DRIVER)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(true),
            DiceRollResults(1.d6, 3.d6), // AV Roll
            Confirm, // Use Pile Driver
            DiceRollResults(2.d6, 2.d6), // AV Roll
            Cancel, // Do not argue the call
        )
        assertNull(state.activePlayer)
        assertEquals(PlayerDogoutState.BANNED, attacker.state)
        defender.assertProne()
    }

    @Test
    fun quickFoulNotApplied() {
        val attacker = awayTeam["A1".playerId]
        attacker.apply {
            addSkill(SkillType.PILE_DRIVER)
            addSkill(SkillType.QUICK_FOUL)
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(true),
            DiceRollResults(1.d6, 3.d6), // AV Roll
            Confirm, // Use Pile Driver
            DiceRollResults(2.d6, 3.d6), // AV Roll
        )
        // Action ends without offering Quick Foul movement
        assertNull(state.activePlayer)
        attacker.assertProne()
        defender.assertProne()
    }

    // Clarified to work in Designer's Commentary May 2026
    @Test
    fun loneFoulerWorks() {
        val attacker = awayTeam["A1".playerId]
        attacker.apply {
            addSkill(SkillType.PILE_DRIVER)
            addSkill(SkillType.LONE_FOULER)
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(true),
            DiceRollResults(1.d6, 3.d6), // AV Roll
            Confirm, // Use Pile Driver
            DiceRollResults(2.d6, 3.d6), // AV Roll
            Confirm, // Use Lone Fouler
            DiceRollResults(5.d6, 6.d6), // AV Roll
            DiceRollResults(2.d6, 3.d6), // Injury Roll
        )
        assertNull(state.activePlayer)
        attacker.assertProne()
        defender.assertStunned()
    }

    @Test
    fun putTheBootInWorks() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.PILE_DRIVER)
        val assistant = awayTeam["A6".playerId]
        assistant.addSkill(SkillType.PUT_THE_BOOT_IN)
        val defender = homeTeam["H1".playerId]
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(assistant, PlayerStandardActionType.MOVE),
            SmartMoveTo(12, 4),
            EndAction,
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            DiceRollResults(1.dblock, 6.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(index = 1),
            DirectionSelected(Direction.LEFT),
            followUp(true),
            DiceRollResults(4.d6, 6.d6), // AV Roll
            DiceRollResults(1.d6, 2.d6), // Injury Roll -> Stunned
            Confirm, // Use Pile Driver
            PlayersSelected(listOf(assistant.id)),
            DiceRollResults(3.d6, 5.d6), // AV Roll: 8 + 1 breaks!
            DiceRollResults(5.d6, 4.d6), // Injury Roll -> KO,
            useApothecary(false),
        )
        assertNull(state.activePlayer)
        attacker.assertProne()
        defender.assertKnockedOut()
    }

    // Clarified to work in Designer's Commentary May 2026
    @Test
    fun sneakyGitWorks() {
        val attacker = awayTeam["A1".playerId]
        attacker.apply {
            addSkill(SkillType.PILE_DRIVER)
            addSkill(SkillType.SNEAKY_GIT)
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(true),
            DiceRollResults(1.d6, 3.d6), // AV Roll
            Confirm, // Use Pile Driver
            DiceRollResults(2.d6, 2.d6), // AV Roll, trigger Referee
            Confirm, // Use Sneaky Git to avoid sent-off
        )
        assertNull(state.activePlayer)
        attacker.assertProne()
        defender.assertProne()
    }

    @Test
    fun dirtyPlayerWorks() {
        val attacker = awayTeam["A1".playerId]
        attacker.apply {
            addSkill(SkillType.PILE_DRIVER)
            addSkill(SkillType.DIRTY_PLAYER)
        }
        val defender = homeTeam["H1".playerId]
        assertEquals(9, defender.armorValue)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(true),
            DiceRollResults(3.d6, 2.d6), // AV Roll
            Confirm, // Use Pile Driver
            DiceRollResults(3.d6, 5.d6), // AV Roll: 8
            Confirm, // Use Dirty Player -> AV: 8 + 1 breaks
            DiceRollResults(2.d6, 4.d6), // Injury Roll -> KO,
        )
        assertNull(state.activePlayer)
        attacker.assertProne()
        defender.assertStunned()
    }

    @Test
    fun doesNotWorkIfNotAdjacent() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.PILE_DRIVER)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(1.d6, 3.d6), // AV Roll
        )
        assertNull(state.activePlayer)
        attacker.assertStanding()
        defender.assertProne()
    }

    @Test
    fun doesNotWorkIfNotStanding() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.PILE_DRIVER)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 2.dblock),
            DiceRollResults(1.d6, 3.d6), // Defender AV Roll
            DiceRollResults(1.d6, 3.d6), // Attacker AV Roll
        )
        assertNull(state.activePlayer)
        attacker.assertProne()
        defender.assertProne()
    }

    @Test
    fun doesNotWorkOnSpecialActions() {
        val attacker = awayTeam["A1".playerId]
        attacker.apply {
            addSkill(SkillType.STAB)
            addSkill(SkillType.PILE_DRIVER)
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.STAB),
            PlayerSelected(defender),
            DiceRollResults(1.d6, 3.d6), // AV Roll - Stab
        )
        assertNull(state.activePlayer)
        attacker.assertStanding()
    }

    // Clarified to work in Designer's Commentary May 2026
    @Test
    fun doesNotPreventFoulAction() {
        val attacker = awayTeam["A1".playerId].also {
            it.addSkill(SkillType.PILE_DRIVER)
            it.addSkill(SkillType.BLOCK)
        }
        val fouler = awayTeam["A2".playerId]
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 2.dblock),
            Confirm, // Use Block
            DiceRollResults(1.d6, 3.d6), // AV roll
            Confirm, // Use Pile Driver
            DiceRollResults(5.d6, 6.d6), // AV Roll
            DiceRollResults(1.d6, 2.d6), // Injury Roll
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            PlayerSelected(defender),
            DiceRollResults(5.d6, 6.d6), // AV Roll
            DiceRollResults(6.d6, 3.d6), // Injury Roll
            useApothecary(false),
        )
        assertNull(state.activePlayer)
        defender.assertKnockedOut()
    }
}
