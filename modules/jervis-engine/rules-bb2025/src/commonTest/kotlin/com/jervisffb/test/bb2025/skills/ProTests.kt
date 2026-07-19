package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.getSkill
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.rules.bb2025.skills.Brawler
import com.jervisffb.engine.rules.bb2025.skills.Pro
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.followUp
import com.jervisffb.test.moveTo
import com.jervisffb.test.proRoll
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.SelectSkillReroll
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertActiveTeam
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertFallenOver
import com.jervisffb.test.utils.assertKnockedOut
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Pro] skill
 */
class ProTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun workOn3Plus() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.PRO)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            SelectSkillReroll(SkillType.PRO),
            *proRoll(3.d6),
            3.d6, // Succeed dodge,
        )
        assertTrue(player.getSkill<Pro>().rerollUsed)
        assertTrue(player.getSkill<Pro>().used)
        assertEquals(player, state.activePlayer)
        player.assertStanding()
        player.assertCoordinates(14, 5)
    }

    @Test
    fun failOn2OrLess() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.PRO)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            SelectSkillReroll(SkillType.PRO),
            *proRoll(2.d6),
        )
        assertTrue(player.getSkill<Pro>().rerollUsed)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6)
        )
        state.assertNoActivePlayer()
        state.assertActiveTeam(homeTeam)
        assertFalse(player.getSkill<Pro>().rerollUsed)
        player.assertProne()
        player.assertCoordinates(14, 5)
    }

    @Test
    fun onlyRerollOneDie() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.PRO)
            baseStrength = 4
            strength = 4
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            DiceRollResults(1.dblock, 2.dblock),
        )
        val rerollOptions = controller.getAvailableActions().get<SelectRerollOption>().options
        assertEquals(2, rerollOptions.size)
        assertTrue(rerollOptions.any { it.getRerollSource(state) is Pro })
        controller.rollForward(
            RerollOptionSelected(rerollOptions.first()),
            SelectSingleBlockDieResult(index = 0),
            *proRoll(3.d6),
            6.dblock
        )
        val block = state.getContext<BlockContext>()
        assertEquals(6.dblock, block.roll.first().result)
        assertEquals(2.dblock, block.roll.last().result)
        controller.rollForward(
            SelectSingleBlockDieResult(index = 0), // Select POW
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
        )
        assertTrue(attacker.getSkill<Pro>().rerollUsed)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6)
        )
        defender.assertProne()
        state.assertNoActivePlayer()
        state.assertActiveTeam(awayTeam)
    }

    @Test
    fun onlyOneRerollPrActivation() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.PRO)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            SelectSkillReroll(SkillType.PRO),
            *proRoll(3.d6),
            3.d6, // Succeed dodge,
            *moveTo(13, 5),
            *moveTo(14, 5),
            2.d6, // Fail dodge
        )
        assertTrue(player.getSkill<Pro>().rerollUsed)
        val rerollOptions = controller.getAvailableActions().get<SelectRerollOption>().options
        assertEquals(1, rerollOptions.size)
        assertFalse(rerollOptions.any { it.getRerollSource(state) is Pro })
        controller.rollForward(
            TeamRerollSelected<StandardTeamReroll>(),
            3.d6,
            EndAction
        )
    }

    @Test
    fun canRerollProRoll() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.PRO)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            SelectSkillReroll(SkillType.PRO),
            2.d6, // Fail Pro
            TeamRerollSelected<StandardTeamReroll>(),
            3.d6, // Succeed re-rolled Pro roll
            3.d6, // Succeed dodge,
        )
        assertTrue(player.getSkill<Pro>().rerollUsed)
        assertEquals(player, state.activePlayer)
        player.assertStanding()
        player.assertCoordinates(14, 5)
    }

    @Test
    fun doesNotWorkOutsideActivation() {
        setupAndStartThrowTeamMateGame()
        val thrownPlayer = awayTeam["A13".playerId].apply {
            addSkill(SkillType.PRO)
        }
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(10, 5),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 6.d8, 5.d8), // Always scatter
            1.d6, // Failed Landing
        )
        val rerollOptions = controller.getAvailableActions().get<SelectRerollOption>().options
        assertEquals(1, rerollOptions.size)
        assertFalse(rerollOptions.any { it.getRerollSource(state) is Pro })
        controller.rollForward(
            NoRerollSelected()
        )
        thrownPlayer.assertFallenOver()
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6),
        )
        thrownPlayer.assertProne()
    }

    @Test
    fun doesNotWorkOnArmourRoll() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.PRO)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(1.d6, 2.d6), // AV roll
        )
        assertFalse(attacker.getSkill<Pro>().rerollUsed)
        defender.assertProne()
        state.assertNoActivePlayer()
    }

    @Test
    fun doesNotWorkOnInjuryRoll() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.PRO)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 2.d6),
            Cancel, // Do not use Apothecary
        )
        assertFalse(attacker.getSkill<Pro>().rerollUsed)
        defender.assertKnockedOut()
        state.assertNoActivePlayer()
    }

    @Test
    fun doesNotWorkOnCasualtyRoll() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.PRO)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 6.d6),
            1.d16,
            Cancel, // Do not use Apothecary
        )
        assertFalse(attacker.getSkill<Pro>().rerollUsed)
        assertEquals(PlayerDogoutState.BADLY_HURT, defender.state)
        state.assertNoActivePlayer()
    }

    @Test
    fun doesNotWorkOnArgueTheCall() {
        val fouler = awayTeam["A6".playerId]
        fouler.addSkill(SkillType.PRO)
        val target = homeTeam["H1".playerId]
        target.putProne()
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Confirm, // Argue the call
            3.d6 // Roll "I Don't Care"
        )
        state.assertActiveTeam(homeTeam)
        assertFalse(fouler.getSkill<Pro>().rerollUsed)
        assertEquals(PlayerDogoutState.BANNED, fouler.state)
        assertEquals(Dogout, fouler.location)
    }

    @Test
    fun doesNotWorkOnTeamCaptain() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.PRO)
            extraSpecialRules.add(PlayerSpecialRule.TEAM_CAPTAIN)
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            2.d6, // Fail dodge
            TeamRerollSelected<StandardTeamReroll>(),
            5.d6, // Team Captain Roll
        )
        val rerollOptions = controller.getAvailableActions().get<SelectRerollOption>().options
        assertEquals(1, rerollOptions.size)
        assertFalse(rerollOptions.any { it.getRerollSource(state) is Pro })
        controller.rollForward(
            NoRerollSelected(),
            3.d6, // Succeed Dodge
        )
        assertEquals(1, awayTeam.rerolls.count { it.rerollUsed })
        assertFalse(player.getSkill<Pro>().rerollUsed)
        assertEquals(player, state.activePlayer)
        player.assertStanding()
    }

    @Test
    fun usingProPreventsOtherRerollsOnDicePool() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.BRAWLER)
            addSkill(SkillType.PRO)
            baseStrength = 4
            strength = 4
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            DiceRollResults(2.dblock, 2.dblock),
        )
        val rerollOptions = controller.getAvailableActions().get<SelectRerollOption>().options
        assertEquals(3, rerollOptions.size)
        assertTrue(rerollOptions.any { it.getRerollSource(state) is Brawler })
        assertTrue(rerollOptions.any { it.getRerollSource(state) is Pro })
        val rerollOption = rerollOptions[1] // Select Pro reroll
        assertTrue(rerollOption.getRerollSource(state) is Pro)
        controller.rollForward(
            RerollOptionSelected(rerollOption),
            SelectSingleBlockDieResult(index = 0),
            *proRoll(4.d6),
            6.dblock, // Reroll first die using Pro, we cannot reroll other dice
        )
        val actions = controller.getAvailableActions()
        assertIs<SelectDicePoolResult>(actions.single())
        controller.rollForward(
            SelectSingleBlockDieResult(index = 0), // Select POW
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
        )
        assertTrue(attacker.getSkill<Pro>().rerollUsed)
        controller.rollForward(
            DiceRollResults(1.d6, 1.d6)
        )
        defender.assertProne()
        state.assertNoActivePlayer()
        state.assertActiveTeam(awayTeam)
    }
}
