package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.BB2025Rules
import com.jervisffb.engine.rules.StandardBB2025Rules
import com.jervisffb.engine.rules.bb2025.skills.Leader
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.rerolls.LeaderTeamReroll
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.blitzBlock
import com.jervisffb.test.defaultAwaySetup
import com.jervisffb.test.defaultDetermineKickingTeam
import com.jervisffb.test.defaultHomeSetup
import com.jervisffb.test.defaultKickOffAwayTeam
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.followUp
import com.jervisffb.test.moveTo
import com.jervisffb.test.skipTurns
import com.jervisffb.test.standardBlock
import com.jervisffb.test.teamSetup
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.putInKnockedOut
import com.jervisffb.test.utils.putInReserves
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Leader] skill
 */
class LeaderTests: JervisGameBB2025Test() {

    override val rules: BB2025Rules = StandardBB2025Rules().update {
        hasExtraTime = true
        turnsInExtraTime = 8
    }

    @BeforeTest
    override fun setUp() {
        super.setUp()
        awayTeam["A10".playerId].addSkill(SkillType.LEADER)
        awayTeam["A1".playerId].addSkill(SkillType.LEADER)
        startDefaultGame()
    }

    @Test
    fun stateIsVisibleDuringSetup() {
        super.setUp()
        val leader = awayTeam["A10".playerId]
        leader.addSkill(SkillType.LEADER)
        controller.rollForward(
            *defaultPregame(),
            *defaultHomeSetup(),
        )
        assertFalse(awayTeam.rerolls.any { it is LeaderTeamReroll })
        controller.rollForward(
            PlayerSelected(leader),
            FieldSquareSelected(23, 7)
        )
        assertEquals(1, awayTeam.rerolls.count { it is LeaderTeamReroll })
        controller.rollForward(
            PlayerSelected(leader),
            DogoutSelected
        )
        assertFalse(awayTeam.rerolls.any { it is LeaderTeamReroll })
        controller.rollForward(
            *defaultAwaySetup()
        )
        assertEquals(1, awayTeam.rerolls.count { it is LeaderTeamReroll })
    }

    @Test
    fun onlyOneLeaderReroll() {
        assertEquals(2, awayTeam.count { it.hasSkill(SkillType.LEADER) })
        assertEquals(1, awayTeam.rerolls.count { it is LeaderTeamReroll })
    }

    // Only disable reroll if player can still come back
    @Test
    fun disableAfterKnockedDown() {
        val attacker1 = awayTeam["A10".playerId]
        val attacker2 = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId]
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            *activatePlayer(attacker1, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            SmartMoveTo(13, 4),
            *blitzBlock(defender, 1.dblock),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 6.d6), // Injury Roll, first Leader is a Casualty
            1.d16, // Badly Hurt
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            EndTurn,
            *activatePlayer(attacker2, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 1.dblock),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 2.d6), // Injury Roll, last Leader is Knocked Out
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
    }

    @Test
    fun removeAfterKnockedDown() {
        val attacker1 = awayTeam["A10".playerId]
        val attacker2 = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId]
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            *activatePlayer(attacker1, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            SmartMoveTo(13, 4),
            *blitzBlock(defender, 1.dblock),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 6.d6), // Injury Roll, first Leader is a Casualty
            1.d16, // Badly Hurt
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            EndTurn,
            *activatePlayer(attacker2, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 1.dblock),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 5.d6), // Injury Roll
            1.d16, // Last Leader is Badly Hurt -> re-roll removed
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.any { it is LeaderTeamReroll })
    }

    @Test
    fun disableAfterFallenOver() {
        val attacker1 = awayTeam["A10".playerId]
        val attacker2 = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId]
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            *activatePlayer(attacker1, PlayerStandardActionType.MOVE),
            SmartMoveTo(13, 4),
            *moveTo(14, 4),
            *dodge(1.d6),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 2.d6), // Injury Roll, first Leader is Knocked Out
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            EndTurn,
            *activatePlayer(attacker2, PlayerStandardActionType.MOVE),
            *moveTo(14, 4),
            *dodge(1.d6),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 2.d6), // Injury Roll, last Leader is Knocked Out
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
    }

    @Test
    fun removeAfterFallenOver() {
        val attacker1 = awayTeam["A10".playerId]
        val attacker2 = awayTeam["A1".playerId]
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            *activatePlayer(attacker1, PlayerStandardActionType.MOVE),
            SmartMoveTo(13, 4),
            *moveTo(14, 4),
            *dodge(1.d6),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 5.d6), // Injury Roll
            1.d16, // Last Leader is Badly Hurt -> re-roll removed
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            EndTurn,
            *activatePlayer(attacker2, PlayerStandardActionType.MOVE),
            *moveTo(14, 4),
            *dodge(1.d6),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 5.d6), // Injury Roll
            1.d16, // Last Leader is Badly Hurt -> re-roll removed
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.any { it is LeaderTeamReroll })
    }

    @Test
    fun disableAfterPushedIntoCrowd() {
        val player1 = awayTeam["A10".playerId]
        val player2 = awayTeam["A1".playerId]
        val attacker = homeTeam["H1".playerId]
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        // Remove first Leader
        controller.rollForward(
            *activatePlayer(player1, PlayerStandardActionType.MOVE),
            SmartMoveTo(13, 4),
            *moveTo(14, 4),
            *dodge(1.d6),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 2.d6), // Injury Roll, first Leader is Knocked Out
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            EndTurn,
            *activatePlayer(player2, PlayerStandardActionType.MOVE),
            *moveTo(14, 4),
            *dodge(6.d6),
            SmartMoveTo(12, 0),
            EndAction,
            EndTurn,
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(player2),
            *moveTo(11, 4),
            *dodge(6.d6),
            SmartMoveTo(12, 1),
            PlayerSelected(player2),
            BlockTypeSelected(BlockType.STANDARD),
            DiceRollResults(3.dblock, 4.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.UP),
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
    }

    @Test
    fun removeAfterPushedIntoCrowd() {
        val player1 = awayTeam["A10".playerId]
        val player2 = awayTeam["A1".playerId]
        val attacker = homeTeam["H1".playerId]
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        // Remove first Leader
        controller.rollForward(
            *activatePlayer(player1, PlayerStandardActionType.MOVE),
            SmartMoveTo(13, 4),
            *moveTo(14, 4),
            *dodge(1.d6),
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(6.d6, 5.d6), // Injury Roll
            1.d16, // Last Leader is Badly Hurt -> re-roll removed
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            EndTurn,
            *activatePlayer(player2, PlayerStandardActionType.MOVE),
            *moveTo(14, 4),
            *dodge(6.d6),
            SmartMoveTo(12, 0),
            EndAction,
            EndTurn,
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(player2),
            *moveTo(11, 4),
            *dodge(6.d6),
            SmartMoveTo(12, 1),
            PlayerSelected(player2),
            BlockTypeSelected(BlockType.STANDARD),
            DiceRollResults(3.dblock, 4.dblock),
            NoRerollSelected(),
            SelectSingleBlockDieResult(),
            DirectionSelected(Direction.UP),
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            followUp(false),
            DiceRollResults(6.d6, 5.d6), // Injury Roll
            1.d16, // Last Leader is Badly Hurt -> re-roll removed
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.any { it is LeaderTeamReroll })
    }

    // Re-roll is also removed during own turn if the teams own player is
    // chain-pushed into the crowd
    @Test
    fun removeAfterBeingPushedIntoCrowdInOwnTurn() {
        val player1 = awayTeam["A10".playerId]
        val player2 = awayTeam["A1".playerId]
        val defender = homeTeam["H8".playerId]

        // Remove first Leader
        player1.extraSkills.clear()

        // Manipulate board-state, so a chain push is easily available
        SetPlayerLocation(homeTeam["H6".playerId], FieldCoordinate(20, 0)).execute(state)
        SetPlayerLocation(homeTeam["H7".playerId], FieldCoordinate(22, 0)).execute(state)
        SetPlayerLocation(player2, FieldCoordinate(21, 0)).execute(state)
        SetPlayerLocation(homeTeam["H8".playerId], FieldCoordinate(21, 1)).execute(state)
        SetPlayerLocation(player1, FieldCoordinate(21, 2)).execute(state)

        assertTrue(awayTeam.rerolls.any { it is LeaderTeamReroll })
        controller.rollForward(
            *activatePlayer(player1, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.UP),
            DirectionSelected(Direction.UP),
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            followUp(false),
            DiceRollResults(6.d6, 5.d6), // Injury Roll
            1.d16, // Last Leader is Badly Hurt -> re-roll removed
            Cancel, // Do not use Apothecary
        )
        assertFalse(awayTeam.rerolls.any { it is LeaderTeamReroll })
    }

    @Test
    fun disableRerollIfBanned() {
        awayTeam["A1".playerId].extraSkills.clear() // Only one Leader on the Pitch
        awayTeam["A10".playerId].extraSkills.clear() // Only one Leader on the Pitch
        awayTeam["A6".playerId].addSkill(SkillType.LEADER) // Only one Leader on the Pitch
        awayTeam["A12".playerId].addSkill(SkillType.LEADER) // One Leader in the Dogout
        homeTeam["H1".playerId].state = PlayerState.PRONE

        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Confirm,
            1.d6, // Roll You're Outta Here
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
    }

    @Test
    fun removeRerollIfBanned() {
        awayTeam["A1".playerId].extraSkills.clear() // Only one Leader on the Pitch
        awayTeam["A10".playerId].extraSkills.clear() // Only one Leader on the Pitch
        awayTeam["A6".playerId].addSkill(SkillType.LEADER) // Only one Leader on the Pitch
        homeTeam["H1".playerId].state = PlayerState.PRONE

        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Confirm,
            1.d6, // Roll You're Outta Here
        )
        assertFalse(awayTeam.rerolls.any { it is LeaderTeamReroll })
    }

    @Test
    fun doesNotResetForExtraTime() {
        controller.rollForward(
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(1)
        )
        val player = awayTeam["A1".playerId]
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail dodge
            TeamRerollSelected<LeaderTeamReroll>(),
            4.d6, // Dodge works
            EndAction
        )
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        controller.rollForward(
            *skipTurns(15)
        )
        // Go into Extra Time
        controller.rollForward(
            *defaultDetermineKickingTeam(),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
    }

    @Test
    fun rerollAvailableDuringExtraTime() {
        controller.rollForward(
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(16),
            *defaultDetermineKickingTeam(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
        )
        assertEquals(3, state.halfNo)
        assertEquals(1, state.awayTeam.turnMarker)
        assertTrue(awayTeam.any { it.location.isOnField(rules) && it.hasSkill(SkillType.LEADER) })
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll}.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll}.enabled)
    }

    @Test
    fun reenableIfLeaderComesBackInNextDriveSameHalf() {
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)

        // First we remove the leaders from the field an disable the Leader re-roll
        // Other tests verify this is ok
        awayTeam["A1".playerId].putInKnockedOut()
        awayTeam["A10".playerId].putInKnockedOut()
        awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled = false

        // Then we make the Team score, triggering a new drive
        val scoringPlayer = awayTeam["A2".playerId]
        SetBallState.carried(state.singleBall(), scoringPlayer).execute(state)
        SetPlayerLocation(scoringPlayer, FieldCoordinate(1, 1)).execute(state)

        controller.rollForward(
            *activatePlayer(scoringPlayer, PlayerStandardActionType.MOVE),
            *moveTo(0, 0),
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            Confirm
        )
        awayTeam["A1".playerId].putInReserves()
        awayTeam["A10".playerId].putInReserves()
        controller.rollForward(
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
    }

    @Test
    fun disableStartingExtraTimeIfNotOnPitch() {
        controller.rollForward(
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(16),
            *defaultDetermineKickingTeam(),
            *defaultHomeSetup(),
        )
        // Fake Leaders not being on the Pitch starting Extra Time
        awayTeam["A1".playerId].putInKnockedOut()
        awayTeam["A10".playerId].putInKnockedOut()
        controller.rollForward(
            *teamSetup(
                listOf(
                    "A2".playerId to FieldCoordinate(13, 6),
                    "A3".playerId to FieldCoordinate(13, 7),
                    "A4".playerId to FieldCoordinate(13, 8),
                    "A5".playerId to FieldCoordinate(13, 9),
                    "A6".playerId to FieldCoordinate(14, 1),
                    "A7".playerId to FieldCoordinate(15, 1),
                    "A8".playerId to FieldCoordinate(15, 13),
                    "A9".playerId to FieldCoordinate(14, 13),
                    "A12".playerId to FieldCoordinate(16, 7),
                    "A11".playerId to FieldCoordinate(22, 7),
                )
            ),
            *defaultKickOffHomeTeam(),
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll}.rerollUsed)
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll}.enabled)
    }

    @Test
    fun reenableIfLeaderComesBackDuringExtraTime() {
        controller.rollForward(
            *skipTurns(16),
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
            *skipTurns(16),
            *defaultDetermineKickingTeam(),
            *defaultHomeSetup(),
        )
        // Fake Leaders not being on the Pitch starting Extra Time
        awayTeam["A1".playerId].putInKnockedOut()
        awayTeam["A10".playerId].putInKnockedOut()
        controller.rollForward(
            *teamSetup(
                listOf(
                    "A2".playerId to FieldCoordinate(13, 6),
                    "A3".playerId to FieldCoordinate(13, 7),
                    "A4".playerId to FieldCoordinate(13, 8),
                    "A5".playerId to FieldCoordinate(13, 9),
                    "A6".playerId to FieldCoordinate(14, 1),
                    "A7".playerId to FieldCoordinate(15, 1),
                    "A8".playerId to FieldCoordinate(15, 13),
                    "A9".playerId to FieldCoordinate(14, 13),
                    "A12".playerId to FieldCoordinate(16, 7),
                    "A11".playerId to FieldCoordinate(22, 7),
                )
            ),
            *defaultKickOffHomeTeam(),
        )

        // Manipulate state, to make it east to score
        val scoringPlayer = awayTeam["A2".playerId]
        SetBallState.carried(state.singleBall(), scoringPlayer).execute(state)
        SetPlayerLocation(scoringPlayer, FieldCoordinate(1, 1)).execute(state)
        controller.rollForward(
            *activatePlayer(scoringPlayer, PlayerStandardActionType.MOVE),
            *moveTo(0, 0),
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
        controller.rollForward(
            Confirm
        )

        // Setup for a new Drive during Extra Time
        awayTeam["A1".playerId].putInReserves()
        awayTeam["A10".playerId].putInReserves()
        controller.rollForward(
            *defaultSetup(homeFirst = false),
            *defaultKickOffAwayTeam(),
        )
        assertFalse(awayTeam.rerolls.single { it is LeaderTeamReroll }.rerollUsed)
        assertTrue(awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled)
    }

    @Test
    fun cannotUseDisabledLeaderReroll() {
        awayTeam.rerolls.single { it is LeaderTeamReroll }.enabled = false
        val player = awayTeam["A1".playerId]
        assertNotNull(awayTeam.rerolls.singleOrNull { it is LeaderTeamReroll })
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            1.d6, // Fail dodge
        )
        val rerollOptions = controller.getAvailableActions().get<SelectRerollOption>().options
        assertFalse(rerollOptions.any { it.getRerollSource(state) is LeaderTeamReroll })
        assertTrue(rerollOptions.any { it.getRerollSource(state) is RegularTeamReroll })
    }

    @Ignore
    @Test
    fun disableAfterGoingThroughTrapdoor() {
        TODO("Wait for Trapdoor support")
    }

    @Ignore
    @Test
    fun removeAfterGoingThroughTrapdoor() {
        TODO("Wait for Trapdoor support")
    }
}
