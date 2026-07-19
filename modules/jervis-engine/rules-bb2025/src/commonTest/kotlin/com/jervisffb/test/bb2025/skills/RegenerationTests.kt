package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.inducements.MortuaryAssistant
import com.jervisffb.engine.model.inducements.PlagueDoctor
import com.jervisffb.engine.rules.bb2025.skills.Regeneration
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.followUp
import com.jervisffb.test.regenerationRoll
import com.jervisffb.test.standardBlock
import com.jervisffb.test.useApothecary
import com.jervisffb.test.useMortuaryAssistant
import com.jervisffb.test.usePlagueDoctor
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertBadlyHurt
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertReserves
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Regeneration] trait, see page 135 in the BB2025 rulebook.
 */
class RegenerationTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun regenSucceedsSkipsCasualtyRoll() {
        val defender = homeTeam["H1".playerId].apply { addSkill(SkillType.REGENERATION) }
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(6.d6, 6.d6), // Armour broken
            DiceRollResults(4.d6, 6.d6), // Injury → Casualty
            regenerationRoll(4.d6),
        )
        state.assertNoActivePlayer()
        defender.assertReserves()
        assertTrue(homeTeam.teamApothecaries.none { it.used })
    }

    @Test
    fun regenFailsCasualtyRollProceeds() {
        val defender = homeTeam["H1".playerId].apply { addSkill(SkillType.REGENERATION) }
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(6.d6, 6.d6), // Armour broken
            DiceRollResults(4.d6, 6.d6), // Injury → Casualty
            regenerationRoll(3.d6),
            DiceRollResults(6.d16), // Badly Hurt
            useApothecary(false),
        )
        state.assertNoActivePlayer()
        defender.assertBadlyHurt()
    }

// TODO Move these to dedicated test files once inducements are properly added
//
//    @Test
//    fun regenSucceedsWithMortuaryAssistantReroll() {
//        val defender = homeTeam["H1".playerId].apply { addSkill(SkillType.REGENERATION) }
//        homeTeam.mortuaryAssistants.add(MortuaryAssistant())
//        controller.rollForward(
//            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
//            *standardBlock(defender, 6.dblock),
//            DirectionSelected(Direction.LEFT),
//            followUp(false),
//            DiceRollResults(6.d6, 6.d6), // Armour broken
//            DiceRollResults(4.d6, 6.d6), // Injury roll
//            1.d6, // Regeneration fails
//            Confirm, // Use Mortuary Assistant
//            5.d6, // Re-roll succeeds
//        )
//        assertEquals(PlayerDogoutState.RESERVE, defender.state)
//        assertEquals(Dogout, defender.location)
//        assertTrue(homeTeam.mortuaryAssistants.single().used)
//    }
//
//    @Test
//    fun regenFailsAfterMortuaryAssistantReroll() {
//        val defender = homeTeam["H1".playerId].apply { addSkill(SkillType.REGENERATION) }
//        homeTeam.mortuaryAssistants.add(MortuaryAssistant())
//        controller.rollForward(
//            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
//            *standardBlock(defender, 6.dblock),
//            DirectionSelected(Direction.LEFT),
//            Cancel, // Do not follow up
//            DiceRollResults(6.d6, 6.d6), // Armour broken
//            DiceRollResults(4.d6, 6.d6), // Injury → Casualty
//            1.d6, // Regeneration: fails
//            Confirm, // Use Mortuary Assistant
//            2.d6, // Re-roll also fails
//            DiceRollResults(6.d16), // Badly Hurt
//            Cancel, // Do not use Apothecary
//        )
//        assertEquals(PlayerDogoutState.BADLY_HURT, defender.state)
//        assertEquals(Dogout, defender.location)
//    }
//
//    @Test
//    fun regenSucceedsWithPlagueDoctorAfterDecliningMortuaryAssistant() {
//        val defender = homeTeam["H1".playerId].apply { addSkill(SkillType.REGENERATION) }
//        homeTeam.mortuaryAssistants.add(MortuaryAssistant())
//        homeTeam.tempApothecaries.add(Apothecary(used = false, type = ApothecaryType.PLAGUE_DOCTOR))
//        controller.rollForward(
//            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
//            *standardBlock(defender, 6.dblock),
//            DirectionSelected(Direction.LEFT),
//            Cancel, // Do not follow up
//            DiceRollResults(6.d6, 6.d6), // Armour broken
//            DiceRollResults(4.d6, 6.d6), // Injury → Casualty
//            1.d6, // Regeneration: fails
//            Cancel, // Decline Mortuary Assistant
//            Confirm, // Use Plague Doctor
//            6.d6, // Re-roll succeeds
//        )
//        assertEquals(PlayerDogoutState.RESERVE, defender.state)
//        assertEquals(Dogout, defender.location)
//        assertFalse(homeTeam.mortuaryAssistants.single().used)
//        assertTrue(homeTeam.tempApothecaries.single { it.type == ApothecaryType.PLAGUE_DOCTOR }.used)
//    }

    @Test
    fun regenFailsAfterAllRerollOptionsDeclined() {
        val defender = homeTeam["H1".playerId].apply { addSkill(SkillType.REGENERATION) }
        homeTeam.mortuaryAssistants.add(MortuaryAssistant())
        homeTeam.plagueDoctors.add(PlagueDoctor(used = false))
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(6.d6, 6.d6), // Armour broken
            DiceRollResults(4.d6, 6.d6), // Injury → Casualty
            regenerationRoll(2.d6),
            useMortuaryAssistant(false),
            usePlagueDoctor(false),
            DiceRollResults(6.d16), // Badly Hurt
            useApothecary(false),
        )
        state.assertNoActivePlayer()
        defender.assertBadlyHurt()
        assertFalse(homeTeam.mortuaryAssistants.single().used)
        assertFalse(homeTeam.plagueDoctors.single().used)
    }

    @Test
    fun teamRerollIsAvailableOnOwnTurn() {
        val attacker = awayTeam["A1".playerId].apply { addSkill(SkillType.REGENERATION) }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
            DiceRollResults(6.d6, 6.d6), // Defender Armour broken
            DiceRollResults(1.d6, 1.d6), // Defender Injury: Stunned
            DiceRollResults(6.d6, 6.d6), // Attacker Armour broken
            DiceRollResults(4.d6, 6.d6), // Attacker Injury → Casualty
            regenerationRoll(1.d6),
            TeamRerollSelected<StandardTeamReroll>(),
            regenerationRoll(5.d6),
        )
        state.assertNoActivePlayer()
        attacker.assertReserves()
    }

    @Ignore // Still waiting for SPP to be implemented
    @Test
    fun stillGetStarPlayerPointsIfSuccessful() {
        TODO()
    }
}
