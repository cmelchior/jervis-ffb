package com.jervisffb.test

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class responsible for testing the concepts of "Open" and "Marked" players
 * as defined on page 26 in the rulebook.
 */
class MarkedTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun isOpen_standing() {
        val player = awayTeam[10.playerNo]
        assertTrue(player.coordinates.getSurroundingCoordinates(rules, 1, false).none {
            state.field[it].isOccupied()
        })
        assertTrue(rules.isOpen(player))
    }

    @Test
    fun isOpen_prone() {
        val player = awayTeam[10.playerNo]
        player.state = PlayerState.PRONE
        assertTrue(player.coordinates.getSurroundingCoordinates(rules, 1, false).none {
            state.field[it].isOccupied()
        })
        assertTrue(rules.isOpen(player))
        assertFalse(rules.isMarked(player))
    }

    @Test
    fun isMarked_standing() {
        val player = awayTeam[1.playerNo]
        assertTrue(player.coordinates.getSurroundingCoordinates(rules, 1, false).any {
            state.field[it].let { it.isOccupied() && it.player!!.team != player.team }
        })
        assertTrue(rules.isMarked(player))
        assertFalse(rules.isOpen(player))
    }

    @Test
    fun isMarked_prone() {
        val player = awayTeam[1.playerNo]
        player.state = PlayerState.PRONE
        assertTrue(player.coordinates.getSurroundingCoordinates(rules, 1, false).any {
            state.field[it].let { it.isOccupied() && it.player!!.team != player.team }
        })
        assertTrue(rules.isMarked(player))
        assertFalse(rules.isOpen(player))
    }

    @Test
    fun isNotMarkedByTeamPlayers() {
        val player = awayTeam[6.playerNo]
        assertTrue(player.coordinates.getSurroundingCoordinates(rules, 1, false).any {
            state.field[it].let { it.isOccupied() && it.player!!.team == player.team }
        })
        assertFalse(rules.isMarked(player))
        assertTrue(rules.isOpen(player))
    }

    @Test
    fun countMarksOnPlayer() {
        val marks1 = rules.calculateMarks(state, awayTeam, awayTeam["A1".playerId].coordinates)
        assertEquals(2, marks1)
        val marks2 = rules.calculateMarks(state, awayTeam, awayTeam["A3".playerId].coordinates)
        assertEquals(3, marks2)
        val marks3 = rules.calculateMarks(state, awayTeam, awayTeam["A10".playerId].coordinates)
        assertEquals(0, marks3)
    }

    @Test
    fun noTackleZone_cannotMarkPlayers() {
        homeTeam["H1".playerId].hasTackleZones = false
        homeTeam["H2".playerId].hasTackleZones = false
        val player = awayTeam["A1".playerId]
        val marks = rules.calculateMarks(state, awayTeam, player.coordinates)
        assertEquals(0, marks)
        assertFalse(rules.isMarked(player))
        assertTrue(rules.isOpen(player))
    }

    @Test
    fun noTackleZone_cannotDeflectPass() {
        homeTeam["H1".playerId].hasTackleZones = false // In [12, 5]
        homeTeam["H2".playerId].state = PlayerState.PRONE // In [12, 6]
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 6),
            *throwBall(6.d6),
            *catch(6.d6) // Catch, because H1 and H2 cannot intercept
        )
        assertTrue(awayTeam["A2".playerId].hasBall())
    }

    @Test
    fun noTackleZone_cannotCatchBall() {
        awayTeam["A7".playerId].hasTackleZones = false
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(16, 4),
            Confirm, // Start pass
            FieldSquareSelected(15, 1),
            *throwBall(4.d6),
            7.d8 // Bounce because player cannot catch
        )
        assertEquals(FieldCoordinate(15, 2), state.singleBall().location)
    }
}
