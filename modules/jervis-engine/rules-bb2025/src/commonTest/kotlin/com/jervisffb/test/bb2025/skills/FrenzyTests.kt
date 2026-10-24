package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.bb2025.skills.Brawler
import com.jervisffb.engine.bb2025.skills.Frenzy
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.common.modifiers.rooted
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.blitzBlock
import com.jervisffb.test.dauntlessRoll
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.foulAppearanceRoll
import com.jervisffb.test.rushRoll
import com.jervisffb.test.standardBlock
import com.jervisffb.test.takeRoot
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.makeChomped
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Frenzy] skill.
 */
class FrenzyTests: JervisGameBB2025Test() {

    private val attacker: Player get() = awayTeam["A1".playerId]
    private val defender: Player get() = homeTeam["H1".playerId]

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
        attacker.addSkill(SkillType.FRENZY)
    }

    @Test
    fun mustFollowUpWhenPushingBack() {
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
        )
        defender.assertCoordinates(11, 5)
        defender.assertStanding()
        attacker.assertCoordinates(12, 5)
        attacker.assertStanding()
    }

    @Test
    fun secondBlockCannotBeSkipped() {
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
            BlockTypeSelected(BlockType.STANDARD),
            3.dblock,
            NoRerollSelected(),
            SelectSingleBlockDieResult(index = 0),
            DirectionSelected(Direction.LEFT),
        )
        defender.assertCoordinates(10, 5)
        defender.assertStanding()
        attacker.assertCoordinates(11, 5)
        attacker.assertStanding()
    }

    @Test
    fun secondBlockEndsTheFrenzySequence() {
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
            BlockTypeSelected(BlockType.STANDARD),
            DiceRollResults(3.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
        )
        assertNull(state.activePlayer)
        defender.assertCoordinates(10, 5)
        defender.assertStanding()
        attacker.assertCoordinates(11, 5)
        attacker.assertStanding()
    }

    @Test
    fun mustBlockTwiceDuringBlitz() {
        assertEquals(6, attacker.move)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *blitzBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
        )
        // Move is used just before throwing the block
        // During a Blitz, we cannot capture this, so just ensure we used 2 moves.
        assertEquals(4, attacker.movesLeft)
        controller.rollForward(
            *secondBlock(3.dblock),
            DirectionSelected(Direction.LEFT),
        )
        assertEquals(4, attacker.movesLeft)
        controller.rollForward(
            EndAction
        )
        attacker.assertCoordinates(11, 5)
        defender.assertCoordinates(10, 5)
    }

    @Test
    fun hornsAppliesToBothBlitzBlocksWithoutStacking() {
        attacker.addSkill(SkillType.HORNS)
        assertEquals(3, attacker.strength)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.STANDARD),
        )
        assertEquals(4, attacker.strength)
        controller.rollForward(
            DiceRollResults(3.dblock, 3.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            BlockTypeSelected(BlockType.STANDARD),
        )
        assertEquals(4, attacker.strength)
        controller.rollForward(
            DiceRollResults(3.dblock, 3.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            EndAction,
        )
        awayTeam.assertActive()
        state.assertNoActivePlayer()
        assertEquals(3, attacker.strength)
    }

    @Test
    fun mustRushForSecondBlitzBlock() {
        startBlitzWithMovement(moves = 1, rushes = 2)
        controller.rollForward(
            *blitzBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            *rushRoll(2.d6),
            *secondBlock(3.dblock),
            DirectionSelected(Direction.LEFT),
        )
        assertEquals(0, attacker.movesLeft)
        assertEquals(1, attacker.rushesLeft)
        attacker.assertCoordinates(11, 5)
        controller.rollForward(EndAction)
        awayTeam.assertActive()
    }

    @Test
    fun failedRushPreventsSecondBlitzBlock() {
        startBlitzWithMovement(moves = 1, rushes = 2)
        controller.rollForward(
            *blitzBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            *rushRoll(1.d6),
            DiceRollResults(1.d6, 1.d6), // Attacker armour.
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        attacker.assertProne()
        attacker.assertCoordinates(12, 5)
        defender.assertStanding()
        defender.assertCoordinates(11, 5)
    }

    @Test
    fun cannotBlockAgainWithoutMovementOrRushes() {
        startBlitzWithMovement(moves = 1, rushes = 0)
        controller.rollForward(
            *blitzBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
        )
        controller.rollForward(EndAction)
        attacker.assertCoordinates(12, 5)
        defender.assertCoordinates(11, 5)
        awayTeam.assertActive()
    }

    @Test
    fun cannotBlockAgainAfterUsingLastRushForFirstBlock() {
        startBlitzWithMovement(moves = 0, rushes = 1)
        controller.rollForward(
            PlayerSelected(defender),
            *rushRoll(2.d6),
            BlockTypeSelected(BlockType.STANDARD),
            3.dblock,
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            EndAction,
        )
        defender.assertCoordinates(11, 5)
        state.assertNoActivePlayer()
        awayTeam.assertActive()
    }

    @Test
    fun sprintCanBeDeclinedToAvoidSecondBlock() {
        attacker.addSkill(SkillType.SPRINT)
        startBlitzWithMovement(moves = 1, rushes = 0)
        controller.rollForward(
            *blitzBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not use Sprint.
            EndAction,
        )
        attacker.assertCoordinates(12, 5)
        defender.assertCoordinates(11, 5)
        state.assertNoActivePlayer()
        awayTeam.assertActive()
    }

    @Test
    fun sprintCanBeUsedForSecondBlock() {
        attacker.addSkill(SkillType.SPRINT)
        startBlitzWithMovement(moves = 1, rushes = 0)
        controller.rollForward(
            *blitzBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            Confirm, // Use Sprint.
            *rushRoll(2.d6),
            *secondBlock(3.dblock),
            DirectionSelected(Direction.LEFT),
            EndAction,
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        attacker.assertCoordinates(11, 5)
        defender.assertCoordinates(10, 5)
    }

    @Test
    fun blockActionDoesNotRequireMovementOrRushes() {
        attacker.movesLeft = 0
        attacker.rushesLeft = 0
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            *secondBlock(3.dblock),
            DirectionSelected(Direction.LEFT),
        )
        attacker.assertCoordinates(11, 5)
        state.assertNoActivePlayer()
        awayTeam.assertActive()
    }

    @Test
    fun bothDownWithBlockDoesNotTriggerSecondBlock() {
        attacker.addSkill(SkillType.BLOCK)
        defender.addSkill(SkillType.BLOCK)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
            Confirm, // Defender uses Block.
            Confirm, // Attacker uses Block.
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        attacker.assertStanding()
        defender.assertStanding()
    }

    @Test
    fun bothDownWithBlockDoesNotTriggerSecondBlitzBlock() {
        attacker.addSkill(SkillType.BLOCK)
        defender.addSkill(SkillType.BLOCK)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *blitzBlock(defender, 2.dblock),
            Confirm,
            Confirm,
        )
        assertEquals(5, attacker.movesLeft)
        controller.rollForward(EndAction)
        state.assertNoActivePlayer()
        awayTeam.assertActive()
    }

    @Test
    fun knockedDownDefenderPreventsSecondBlockButStillForcesFollowUp() {
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            DiceRollResults(1.d6, 1.d6),
        )
        attacker.assertCoordinates(12, 5)
        defender.assertProne()
        state.assertNoActivePlayer()
    }

    @Test
    fun dodgeAgainstStumbleStillRequiresSecondBlock() {
        defender.addSkill(SkillType.DODGE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 5.dblock),
            Confirm, // Use Dodge.
            DirectionSelected(Direction.LEFT),
            *secondBlock(3.dblock),
            DirectionSelected(Direction.LEFT),
        )
        state.assertNoActivePlayer()
        defender.assertStanding()
        defender.assertCoordinates(10, 5)
    }

    @Test
    fun rootedDefenderCanBeBlockedTwice() {
        defender.addSkill(SkillType.TAKE_ROOT)
        defender.addStatusEffect(PlayerStatusEffect.rooted())
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            *secondBlock(3.dblock),
        )
        state.assertNoActivePlayer()
        attacker.assertCoordinates(13, 5)
        defender.assertCoordinates(12, 5)
        defender.assertStanding()
    }

    @Test
    fun rootedAttackerCannotFollowUpForSecondBlock() {
        attacker.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *takeRoot(1.d6),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
        )
        state.assertNoActivePlayer()
        attacker.assertCoordinates(13, 5)
        defender.assertCoordinates(11, 5)
    }

    @Test
    fun distractedAttackerDoesNotUseFrenzy() {
        controller.rollForward(*activatePlayer(attacker, PlayerStandardActionType.BLOCK))
        attacker.makeDistracted()
        controller.rollForward(
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Following up is optional without Frenzy.
        )
        state.assertNoActivePlayer()
        attacker.assertCoordinates(13, 5)
        defender.assertCoordinates(11, 5)
    }

    @Test
    fun standFirmDefenderCanBeBlockedTwiceDuringBlitz() {
        defender.addSkill(SkillType.STAND_FIRM)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *blitzBlock(defender, 3.dblock),
            Confirm, // Stand Firm.
            *secondBlock(3.dblock),
            Confirm, // Stand Firm again.
            EndAction,
        )
        state.assertNoActivePlayer()
        attacker.assertCoordinates(13, 5)
        defender.assertCoordinates(12, 5)
        awayTeam.assertActive()
    }

    @Test
    fun chompedDefenderCanBeBlockedTwice() {
        defender.makeChomped(attacker)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            *secondBlock(3.dblock),
        )
        state.assertNoActivePlayer()
        attacker.assertCoordinates(13, 5)
        defender.assertCoordinates(12, 5)
        awayTeam.assertActive()
    }

    @Test
    fun fendPreventsSecondBlitzBlock() {
        defender.addSkill(SkillType.FEND)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *blitzBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            Confirm, // Fend.
            EndAction,
        )
        state.assertNoActivePlayer()
        attacker.assertCoordinates(13, 5)
        defender.assertCoordinates(11, 5)
    }

    @Test
    fun secondBlockCanBeReplacedWithStab() {
        attacker.addSkill(SkillType.STAB)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
        )
        assertTrue(BlockType.STAB in controller.getAvailableActions().get<SelectBlockType>().types)
        controller.rollForward(
            BlockTypeSelected(BlockType.STAB),
            DiceRollResults(1.d6, 1.d6), // Stab fails to break armour.
        )
        state.assertNoActivePlayer()
        defender.assertStanding()
        attacker.assertCoordinates(12, 5)
    }

    @Test
    fun secondBlockOffersAllEligibleSpecialActions() {
        attacker.addSkill(SkillType.BREATHE_FIRE)
        attacker.addSkill(SkillType.CHAINSAW)
        attacker.addSkill(SkillType.MONSTROUS_MOUTH)
        attacker.addSkill(SkillType.PROJECTILE_VOMIT)
        attacker.addSkill(SkillType.STAB)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
        )
        assertEquals(
            setOf(
                BlockType.STANDARD,
                BlockType.BREATHE_FIRE,
                BlockType.CHAINSAW,
                BlockType.CHOMP,
                BlockType.PROJECTILE_VOMIT,
                BlockType.STAB
            ),
            controller.getAvailableActions().get<SelectBlockType>().types.toSet(),
        )
    }

    @Test
    fun secondBlitzBlockCanBeReplacedWithStab() {
        attacker.addSkill(SkillType.STAB)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *blitzBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            BlockTypeSelected(BlockType.STAB),
            DiceRollResults(1.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        defender.assertCoordinates(11, 5)
        defender.assertStanding()
    }

    @Test
    fun failedStabAsFirstBlitzBlockDoesNotTriggerFrenzy() {
        attacker.addSkill(SkillType.STAB)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.STAB),
            DiceRollResults(1.d6, 1.d6),
        )
        assertEquals(5, attacker.movesLeft)
        state.assertNoActivePlayer()
        defender.assertStanding()
    }

    @Test
    fun foulAppearanceIsRolledForBothBlocks() {
        defender.addSkill(SkillType.FOUL_APPEARANCE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            *foulAppearanceRoll(2.d6),
            3.dblock,
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            BlockTypeSelected(BlockType.STANDARD),
            *foulAppearanceRoll(2.d6),
            3.dblock,
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
        )
        state.assertNoActivePlayer()
        defender.assertCoordinates(10, 5)
    }

    @Test
    fun failingFirstFoulAppearancePreventsSecondBlock() {
        defender.addSkill(SkillType.FOUL_APPEARANCE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            *foulAppearanceRoll(1.d6),
        )
        state.assertNoActivePlayer()
        attacker.assertCoordinates(13, 5)
        defender.assertCoordinates(12, 5)
    }

    @Test
    fun failingFirstFoulAppearancePreventsSecondBlitzBlock() {
        defender.addSkill(SkillType.FOUL_APPEARANCE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.STANDARD),
            *foulAppearanceRoll(1.d6),
        )
        state.assertNoActivePlayer()
        defender.assertCoordinates(12, 5)
    }

    @Test
    fun failingSecondFoulAppearanceEndsBlitz() {
        defender.addSkill(SkillType.FOUL_APPEARANCE)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.STANDARD),
            *foulAppearanceRoll(2.d6),
            3.dblock,
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            BlockTypeSelected(BlockType.STANDARD),
            *foulAppearanceRoll(1.d6),
        )
        state.assertNoActivePlayer()
        defender.assertCoordinates(11, 5)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun brawlerCannotBeUsedOnBothBlocks() {
        attacker.addSkill(SkillType.BRAWLER)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            2.dblock,
        )
        val brawler = controller.getAvailableActions().get<SelectRerollOption>().options.single {
            it.getRerollSource(state) is Brawler
        }
        controller.rollForward(
            RerollOptionSelected(brawler),
            3.dblock,
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            BlockTypeSelected(BlockType.STANDARD),
            2.dblock,
        )
        val options = controller.getAvailableActions().get<SelectRerollOption>().options
        assertTrue(options.none { it.getRerollSource(state) is Brawler })
        controller.rollForward(
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DiceRollResults(1.d6, 1.d6),
            DiceRollResults(1.d6, 1.d6)
        )
        attacker.assertProne()
        defender.assertProne()
        state.assertNoActivePlayer()
        homeTeam.assertActive()
    }

    @Test
    fun brawlerCanBeSavedForSecondBlock() {
        attacker.addSkill(SkillType.BRAWLER)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            BlockTypeSelected(BlockType.STANDARD),
            2.dblock,
        )
        val brawler = controller.getAvailableActions().get<SelectRerollOption>().options.single {
            it.getRerollSource(state) is Brawler
        }
        controller.rollForward(
            RerollOptionSelected(brawler),
            3.dblock,
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
        )
        state.assertNoActivePlayer()
        defender.assertCoordinates(10, 5)
    }

    @Test
    fun dauntlessIsRolledAgainForSecondBlock() {
        attacker.addSkill(SkillType.DAUNTLESS)
        defender.baseStrength = 4
        defender.strength = 4
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            PlayerSelected(defender),
            *dauntlessRoll(2.d6),
            3.dblock,
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
            BlockTypeSelected(BlockType.STANDARD),
            *dauntlessRoll(1.d6), // Failure must not retain the first block's bonus.
        )
        assertEquals(3, attacker.strength)
        controller.rollForward(
            DiceRollResults(3.dblock, 3.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.LEFT),
        )
        state.assertNoActivePlayer()
        assertEquals(3, attacker.strength)
    }

    @Test
    fun touchdownDuringFollowUpPreventsSecondBlock() {
        defender.addSkill(SkillType.SIDESTEP)
        SetPlayerLocation(attacker, PitchCoordinate(1, 3)).execute(state)
        SetPlayerLocation(defender, PitchCoordinate(0, 3)).execute(state)
        SetBallState.carried(state.singleBall(), attacker).execute(state)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            Confirm, // Sidestep keeps the defender on the pitch.
            DirectionSelected(Direction.UP), // Defender remains adjacent and on the pitch.
            Confirm, // Accept touchdown.
        )
        assertEquals(1, state.awayScore)
        state.assertNoActivePlayer()
    }

    @Test
    fun touchdownDuringBlitzFollowUpPreventsSecondBlock() {
        defender.addSkill(SkillType.SIDESTEP)
        SetPlayerLocation(attacker, PitchCoordinate(1, 3)).execute(state)
        SetPlayerLocation(defender, PitchCoordinate(0, 3)).execute(state)
        SetBallState.carried(state.singleBall(), attacker).execute(state)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            *blitzBlock(defender, 3.dblock),
            Confirm, // Sidestep.
            DirectionSelected(Direction.UP),
            Confirm, // Accept touchdown.
        )
        assertEquals(1, state.awayScore)
        state.assertNoActivePlayer()
    }

    private fun startBlitzWithMovement(moves: Int, rushes: Int) {
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
        )
        attacker.movesLeft = moves
        attacker.rushesLeft = rushes
    }

    private fun secondBlock(vararg dice: DBlockResult): Array<GameAction> = arrayOf(
        BlockTypeSelected(BlockType.STANDARD),
        DiceRollResults(*dice),
        NoRerollSelected(),
        SelectSingleBlockDieResult(),
    )
}
