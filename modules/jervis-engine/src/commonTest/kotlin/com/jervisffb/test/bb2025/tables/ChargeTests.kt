package com.jervisffb.test.bb2025.tables

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.ForegoActivationSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectPlayers
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.singleInstanceOf
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.SelectSkillReroll
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.assertStunned
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * This class tests the Charge! Kick-off Event result.
 */
class ChargeTests: JervisGameBB2025Test() {

    // Check that all selected players can move during a Charge!
    @Test
    fun charge_moveAction() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                ),
                bounce = null
            )
        )
        players.forEach {
            val startCoordinates = homeTeam[it].coordinates
            controller.rollForward(
                *activatePlayer(it.value, PlayerStandardActionType.MOVE),
                *moveTo(startCoordinates.x, startCoordinates.y - 1),
                EndAction
            )
            assertEquals(startCoordinates.move(Direction.UP, 1), homeTeam[it].coordinates)
        }
        controller.rollForward(
            EndTurn,
            2.d8 // Bounce
        )
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_blitzAction() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H6", PlayerStandardActionType.BLITZ),
                    PlayerSelected("A1".playerId),
                    SmartMoveTo(13, 4),
                    PlayerSelected("A1".playerId),
                    BlockTypeSelected(BlockType.STANDARD),
                    DiceRollResults(6.dblock, 6.dblock),
                    SelectSingleBlockDieResult(),
                    DirectionSelected(Direction.DOWN_RIGHT),
                    Cancel,
                    DiceRollResults(1.d6, 1.d6),
                    EndAction
                ),
                bounce = null
            )
        )
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun charge_throwTeamMate() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        homeTeam["H9".playerId].addSkill(SkillType.THROW_TEAMMATE)
        homeTeam["H10".playerId].let {
            it.strength = 2
            it.addSkill(SkillType.RIGHT_STUFF)
        }
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H9", PlayerStandardActionType.THROW_TEAM_MATE),
                    SmartMoveTo(10, 7),
                    PlayerSelected("H10".playerId),
                    FieldSquareSelected(11, 7),
                    6.d6, // Throw
                    DiceRollResults(2.d8, 2.d8, 2.d8),  // Scatter
                    6.d6 // Landing
                ),
                bounce = null
            )
        )
        assertEquals(FieldCoordinate(11, 4), homeTeam["H10".playerId].coordinates)
        homeTeam["H10".playerId].assertStanding()
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun charge_doNotSelectAnyPlayers() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    Cancel, // Do not select any players
                ),
            )
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(1, awayTeam.turnMarker)
    }

    @Test
    @Ignore
    fun charge_kickTeamMate() {
        // - [ ] 1 Kick Teammate
        TODO()
    }

    @Test
    fun charge_onlyListedActions() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        homeTeam["H9".playerId].addSkill(SkillType.THROW_TEAMMATE)
        // homeTeam["H9".playerId].addSkill(SkillType.KICK_TEAMMATE)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    PlayerSelected("H9".playerId),
                ),
                bounce = null
            )
        )
        controller.getAvailableActions().singleInstanceOf<SelectPlayerAction>().let {
            assertEquals(3, it.actions.size)
            assertTrue(it.actions.any { it.type == PlayerStandardActionType.MOVE })
            assertTrue(it.actions.any { it.type == PlayerStandardActionType.BLITZ })
            assertTrue(it.actions.any { it.type == PlayerStandardActionType.THROW_TEAM_MATE })
        }
    }

    @Test
    fun charge_endOnFallingOver() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H9", PlayerStandardActionType.MOVE),
                    SmartMoveTo(12, 10),
                    *moveTo(13, 10),
                    1.d6, // Fail Dodge -> Turnover
                    DiceRollResults(2.d6, 2.d6)
                ),
            )
        )
        assertEquals(1, state.awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_endOnKnockedDown() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H6", PlayerStandardActionType.BLITZ),
                    PlayerSelected("A1".playerId),
                    SmartMoveTo(13, 4),
                    PlayerSelected("A1".playerId),
                    BlockTypeSelected(BlockType.STANDARD),
                    DiceRollResults(1.dblock, 1.dblock),
                    SelectSingleBlockDieResult(), // Select Player Down!
                    DiceRollResults(1.d6, 1.d6),
                ),
            )
        )
        assertEquals(1, state.awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }

    // Dodge is listed as only working during a teams turn, but is expected to work during a Charge!
    // as they are listed as "activations work exactly like a team turn".
    @Test
    fun charge_canUseDodge() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        homeTeam["H9".playerId].addSkill(SkillType.DODGE)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H9", PlayerStandardActionType.MOVE),
                    SmartMoveTo(12, 10),
                    *moveTo(13, 10),
                    1.d6, // Fail Dodge
                    SelectSkillReroll(SkillType.DODGE),
                    6.d6,
                    EndAction,
                    EndTurn
                ),
            )
        )
        assertEquals(FieldCoordinate(13, 10), homeTeam["H9".playerId].coordinates)
        homeTeam["H9".playerId].assertStanding()
        assertEquals(1, state.awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_canForegoActivation() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                ),
                bounce = null
            )
        )
        players.forEach {
            controller.rollForward(
                ForegoActivationSelected(it),
            )
        }
        controller.rollForward(
            EndTurn,
            2.d8 // Bounce
        )
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_stunnedPlayersAreStillStunnedInFirstTurn() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H9", PlayerStandardActionType.MOVE),
                    SmartMoveTo(12, 10),
                    *moveTo(13, 10),
                    1.d6, // Fail Dodge -> Turnover
                    DiceRollResults(6.d6, 6.d6),
                    DiceRollResults(1.d6, 1.d6) // Stun
                ),
            )
        )
        homeTeam["H9".playerId].assertStunned()
        assertEquals(1, state.awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_onlyOpenPlayers() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                ),
                bounce = null
            )
        )
        controller.getAvailableActions().singleInstanceOf<SelectPlayers>().let {
            assertEquals(4, it.count)
            assertEquals(6, it.players.size)
            it.players.forEach {
                assertTrue(rules.isOpen(homeTeam[it]))
            }
        }
    }

    @Test
    fun charge_lessPlayersThanRolled() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )
        listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId).forEach {
            homeTeam[it].state = PlayerState.PRONE
        }
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    3.d3, // How many players to activate.
                ),
                bounce = null
            )
        )
        controller.getAvailableActions().singleInstanceOf<SelectPlayers>().let {
            assertEquals(2, it.count) // Rolled 6, but we only have 2 available
            assertEquals(2, it.players.size)
            it.players.forEach {
                assertTrue(rules.isOpen(homeTeam[it]))
            }
        }
    }

    @Test
    fun charge_skipIfNoAvailablePlayers() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup()
        )
        homeTeam.forEach {
            it.state = PlayerState.PRONE
        }
        controller.rollForward(
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                ),
                bounce = 2.d8
            )
        )
        assertEquals(1, awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_movePlayerUnderBallOnReceiverSide() {
        val players = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        homeTeam["H9".playerId].addSkill(SkillType.DODGE)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(14, 11),
                deviate = DiceRollResults(4.d8, 1.d6),
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate.
                    PlayersSelected(players),
                    *activatePlayer("H9", PlayerStandardActionType.MOVE),
                    SmartMoveTo(13, 11),
                    EndAction,
                    EndTurn,
                    6.d6, // Catch
                ),
                bounce = null
            )
        )
        assertEquals(FieldCoordinate(13, 11), homeTeam["H9".playerId].coordinates)
        assertTrue(homeTeam["H9".playerId].hasBall())
        assertEquals(1, state.awayTeam.turnMarker)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun charge_selectLessThanMaxPlayers() {
        val players = listOf("H9".playerId) //
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(6.d6, 4.d6), // Roll Charge
                    1.d3, // How many players to activate. (3 + 1)
                    PlayersSelected(players), // Only select 1 player
                    *activatePlayer("H9", PlayerStandardActionType.MOVE),
                    SmartMoveTo(12, 10),
                    *moveTo(13, 10),
                    *dodge(6.d6, reroll = null),
                    EndAction,
                    EndTurn,
                ),
            )
        )
        assertEquals(1, state.awayTeam.turnMarker)
        assertEquals(0, state.homeTeam.turnMarker)
    }
}
