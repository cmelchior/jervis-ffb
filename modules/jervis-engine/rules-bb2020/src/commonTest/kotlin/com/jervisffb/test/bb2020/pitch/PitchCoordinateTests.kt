package com.jervisffb.test.bb2020.pitch

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.test.bb2020.createDefaultGameStateBB2020
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class PitchCoordinateTests {
    private val rules = StandardBB2020Rules()
    private lateinit var state: Game

    @BeforeTest
    fun setUp() {
        state = createDefaultGameStateBB2020(rules)
    }

    @Test
    fun getSurroundingSquares() {
        val squares: List<PitchCoordinate> = PitchCoordinate(14, 7).getSurroundingCoordinates(rules)
        assertEquals(8, squares.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(13, 6),
                PitchCoordinate(14, 6),
                PitchCoordinate(15, 6),
                PitchCoordinate(14, 8),
                PitchCoordinate(15, 8),
                PitchCoordinate(14, 8),
                PitchCoordinate(13, 8),
                PitchCoordinate(14, 6),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getSurroundingSquares_topLeft() {
        val squares: List<PitchCoordinate> = PitchCoordinate(0, 0).getSurroundingCoordinates(rules)
        assertEquals(3, squares.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(1, 0),
                PitchCoordinate(1, 1),
                PitchCoordinate(0, 1),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getSurroundingSquares_topRight() {
        val squares: List<PitchCoordinate> =
            PitchCoordinate(
                rules.pitchWidth - 1,
                0,
            ).getSurroundingCoordinates(rules)
        assertEquals(3, squares.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(25, 1),
                PitchCoordinate(24, 1),
                PitchCoordinate(24, 0),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getSurroundingSquares_bottomLeft() {
        val squares: List<PitchCoordinate> =
            PitchCoordinate(
                0,
                rules.pitchHeight - 1,
            ).getSurroundingCoordinates(rules)
        assertEquals(3, squares.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(0, 13),
                PitchCoordinate(1, 13),
                PitchCoordinate(1, 14),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getSurroundingSquares_bottomRight() {
        val squares: List<PitchCoordinate> =
            PitchCoordinate(
                rules.pitchWidth - 1,
                rules.pitchHeight - 1,
            ).getSurroundingCoordinates(rules)
        assertEquals(3, squares.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(25, 13),
                PitchCoordinate(24, 14),
                PitchCoordinate(24, 13),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getCoordinatesAway_topLeft() {
        val squares = PitchCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, PitchCoordinate(15, 8))
        assertEquals(3, squares.size)

        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(13, 7),
                PitchCoordinate(13, 6),
                PitchCoordinate(14, 6),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getCoordinatesAway_topLeft_outOfBounds() {
        val squares1 = PitchCoordinate(0, 0).getCoordinatesAwayFromLocation(rules, PitchCoordinate(1, 1))
        assertTrue(squares1.isEmpty())

        val squares2 =
            PitchCoordinate(
                0,
                0,
            ).getCoordinatesAwayFromLocation(rules, PitchCoordinate(1, 1), includeOutOfBounds = true)
        assertEquals(3, squares2.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(-1, 0),
                PitchCoordinate(-1, -1),
                PitchCoordinate(0, -1),
            )
        assertSquaresContains(expectedSquares, expectedSquares)
    }

    @Test
    fun getCoordinatesAway_topCenter() {
        val squares = PitchCoordinate(14, 1).getCoordinatesAwayFromLocation(rules, PitchCoordinate(14, 2))
        assertEquals(3, squares.size)

        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(13, 0),
                PitchCoordinate(14, 0),
                PitchCoordinate(15, 0),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getCoordinatesAway_topCenter_outOfBounds() {
        val squares1 = PitchCoordinate(14, 0).getCoordinatesAwayFromLocation(rules, PitchCoordinate(14, 1))
        assertTrue(squares1.isEmpty())

        val squares2 =
            PitchCoordinate(
                14,
                0,
            ).getCoordinatesAwayFromLocation(rules, PitchCoordinate(14, 1), includeOutOfBounds = true)
        assertEquals(3, squares2.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(13, -1),
                PitchCoordinate(14, -1),
                PitchCoordinate(15, -1),
            )
        assertSquaresContains(expectedSquares, expectedSquares)
    }

    @Test
    fun getCoordinatesAway_topRight() {
        val squares = PitchCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, PitchCoordinate(13, 8))
        assertEquals(3, squares.size)

        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(14, 6),
                PitchCoordinate(15, 6),
                PitchCoordinate(15, 7),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getCoordinatesAway_topRight_outOfBounds() {
        val squares1 = PitchCoordinate(25, 0).getCoordinatesAwayFromLocation(rules, PitchCoordinate(24, 1))
        assertTrue(squares1.isEmpty())

        val squares2 =
            PitchCoordinate(
                25,
                0,
            ).getCoordinatesAwayFromLocation(rules, PitchCoordinate(24, 1), includeOutOfBounds = true)
        assertEquals(3, squares2.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(25, -1),
                PitchCoordinate(26, -1),
                PitchCoordinate(26, 0),
            )
        assertSquaresContains(expectedSquares, expectedSquares)
    }

    @Test
    fun getCoordinatesAway_right() {
        val squares = PitchCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, PitchCoordinate(13, 7))
        assertEquals(3, squares.size)

        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(15, 6),
                PitchCoordinate(15, 7),
                PitchCoordinate(15, 8),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getCoordinatesAway_right_outOfBounds() {
        val squares1 = PitchCoordinate(25, 7).getCoordinatesAwayFromLocation(rules, PitchCoordinate(24, 7))
        assertTrue(squares1.isEmpty())

        val squares2 =
            PitchCoordinate(
                25,
                7,
            ).getCoordinatesAwayFromLocation(rules, PitchCoordinate(24, 7), includeOutOfBounds = true)
        assertEquals(3, squares2.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(26, 6),
                PitchCoordinate(26, 7),
                PitchCoordinate(26, 8),
            )
        assertSquaresContains(expectedSquares, expectedSquares)
    }

    @Test
    fun getCoordinatesAway_bottomRight() {
        val squares = PitchCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, PitchCoordinate(13, 6))
        assertEquals(3, squares.size)

        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(15, 7),
                PitchCoordinate(15, 8),
                PitchCoordinate(14, 8),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getCoordinatesAway_bottomRight_outOfBounds() {
        val squares1 = PitchCoordinate(25, 14).getCoordinatesAwayFromLocation(rules, PitchCoordinate(24, 13))
        assertTrue(squares1.isEmpty())

        val squares2 =
            PitchCoordinate(
                25,
                14,
            ).getCoordinatesAwayFromLocation(rules, PitchCoordinate(24, 13), includeOutOfBounds = true)
        assertEquals(3, squares2.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(26, 14),
                PitchCoordinate(26, 15),
                PitchCoordinate(25, 15),
            )
        assertSquaresContains(expectedSquares, expectedSquares)
    }

    @Test
    fun getCoordinatesAway_bottomCenter() {
        val squares = PitchCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, PitchCoordinate(14, 6))
        assertEquals(3, squares.size)

        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(15, 8),
                PitchCoordinate(14, 8),
                PitchCoordinate(13, 8),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getCoordinatesAway_bottomCenter_outOfBounds() {
        val squares1 = PitchCoordinate(7, 14).getCoordinatesAwayFromLocation(rules, PitchCoordinate(7, 13))
        assertTrue(squares1.isEmpty())

        val squares2 =
            PitchCoordinate(
                7,
                14,
            ).getCoordinatesAwayFromLocation(rules, PitchCoordinate(7, 13), includeOutOfBounds = true)
        assertEquals(3, squares2.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(6, 15),
                PitchCoordinate(7, 15),
                PitchCoordinate(8, 15),
            )
        assertSquaresContains(expectedSquares, expectedSquares)
    }

    @Test
    fun getCoordinatesAway_bottomLeft() {
        val squares = PitchCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, PitchCoordinate(15, 6))
        assertEquals(3, squares.size)

        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(14, 8),
                PitchCoordinate(13, 8),
                PitchCoordinate(13, 7),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getCoordinatesAway_bottomLeft_outOfBounds() {
        val squares1 = PitchCoordinate(0, 14).getCoordinatesAwayFromLocation(rules, PitchCoordinate(1, 13))
        assertTrue(squares1.isEmpty())

        val squares2 =
            PitchCoordinate(
                0,
                14,
            ).getCoordinatesAwayFromLocation(rules, PitchCoordinate(0, 13), includeOutOfBounds = true)
        assertEquals(3, squares2.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(0, 15),
                PitchCoordinate(-1, 15),
                PitchCoordinate(-1, 0),
            )
        assertSquaresContains(expectedSquares, expectedSquares)
    }

    @Test
    fun getCoordinatesAway_left() {
        val squares = PitchCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, PitchCoordinate(15, 7))
        assertEquals(3, squares.size)

        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(13, 8),
                PitchCoordinate(13, 7),
                PitchCoordinate(13, 6),
            )
        assertSquaresContains(expectedSquares, squares)
    }

    @Test
    fun getCoordinatesAway_left_outOfBounds() {
        val squares1 = PitchCoordinate(0, 7).getCoordinatesAwayFromLocation(rules, PitchCoordinate(1, 7))
        assertTrue(squares1.isEmpty())

        val squares2 =
            PitchCoordinate(
                0,
                7,
            ).getCoordinatesAwayFromLocation(rules, PitchCoordinate(1, 7), includeOutOfBounds = true)
        assertEquals(3, squares2.size)
        // Clockwise (starting at top-left)
        val expectedSquares =
            listOf(
                PitchCoordinate(-1, 8),
                PitchCoordinate(-1, 7),
                PitchCoordinate(-1, 6),
            )
        assertSquaresContains(expectedSquares, expectedSquares)
    }

    @Test
    fun distanceTo() {
        val a = PitchCoordinate(0, 0)
        assertEquals(0, a.distanceTo(PitchCoordinate(0, 0)))
        assertEquals(1, a.distanceTo(PitchCoordinate(0, -1))) // Up
        assertEquals(2, a.distanceTo(PitchCoordinate(2, -1))) // Top-right
        assertEquals(3, a.distanceTo(PitchCoordinate(-3, 3))) // Bottom-left
    }

    /**
     * Assert that all [PitchCoordinate]s in one list is present in another list.
     */
    private fun assertSquaresContains(
        expectedSquares: List<PitchCoordinate>,
        squares: List<PitchCoordinate>,
    ) {
        expectedSquares.forEach {
            if (!squares.contains(it)) {
                fail("$it was not found in: ${squares.joinToString()}")
            }
        }
    }
}
