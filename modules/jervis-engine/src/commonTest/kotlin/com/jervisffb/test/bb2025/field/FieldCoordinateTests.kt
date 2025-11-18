package com.jervisffb.test.bb2025.field

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.StandardBB2025Rules
import com.jervisffb.test.bb2020.createDefaultGameStateBB2020
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class FieldCoordinateTests {
    private val rules = StandardBB2025Rules()
    private lateinit var state: Game

    @BeforeTest
    fun setUp() {
        state = createDefaultGameStateBB2020(rules)
    }

    @Test
    fun getSurroundingSquares() {
        val fields: List<FieldCoordinate> = FieldCoordinate(14, 7).getSurroundingCoordinates(rules)
        assertEquals(8, fields.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(13, 6),
                FieldCoordinate(14, 6),
                FieldCoordinate(15, 6),
                FieldCoordinate(14, 8),
                FieldCoordinate(15, 8),
                FieldCoordinate(14, 8),
                FieldCoordinate(13, 8),
                FieldCoordinate(14, 6),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getSurroundingSquares_topLeft() {
        val fields: List<FieldCoordinate> = FieldCoordinate(0, 0).getSurroundingCoordinates(rules)
        assertEquals(3, fields.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(1, 0),
                FieldCoordinate(1, 1),
                FieldCoordinate(0, 1),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getSurroundingSquares_topRight() {
        val fields: List<FieldCoordinate> =
            FieldCoordinate(
                rules.fieldWidth - 1,
                0,
            ).getSurroundingCoordinates(rules)
        assertEquals(3, fields.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(25, 1),
                FieldCoordinate(24, 1),
                FieldCoordinate(24, 0),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getSurroundingSquares_bottomLeft() {
        val fields: List<FieldCoordinate> =
            FieldCoordinate(
                0,
                rules.fieldHeight - 1,
            ).getSurroundingCoordinates(rules)
        assertEquals(3, fields.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(0, 13),
                FieldCoordinate(1, 13),
                FieldCoordinate(1, 14),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getSurroundingSquares_bottomRight() {
        val fields: List<FieldCoordinate> =
            FieldCoordinate(
                rules.fieldWidth - 1,
                rules.fieldHeight - 1,
            ).getSurroundingCoordinates(rules)
        assertEquals(3, fields.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(25, 13),
                FieldCoordinate(24, 14),
                FieldCoordinate(24, 13),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getCoordinatesAway_topLeft() {
        val fields = FieldCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, FieldCoordinate(15, 8))
        assertEquals(3, fields.size)

        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(13, 7),
                FieldCoordinate(13, 6),
                FieldCoordinate(14, 6),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getCoordinatesAway_topLeft_outOfBounds() {
        val fields1 = FieldCoordinate(0, 0).getCoordinatesAwayFromLocation(rules, FieldCoordinate(1, 1))
        assertTrue(fields1.isEmpty())

        val fields2 =
            FieldCoordinate(
                0,
                0,
            ).getCoordinatesAwayFromLocation(rules, FieldCoordinate(1, 1), includeOutOfBounds = true)
        assertEquals(3, fields2.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(-1, 0),
                FieldCoordinate(-1, -1),
                FieldCoordinate(0, -1),
            )
        assertFieldsContains(expectedFields, expectedFields)
    }

    @Test
    fun getCoordinatesAway_topCenter() {
        val fields = FieldCoordinate(14, 1).getCoordinatesAwayFromLocation(rules, FieldCoordinate(14, 2))
        assertEquals(3, fields.size)

        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(13, 0),
                FieldCoordinate(14, 0),
                FieldCoordinate(15, 0),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getCoordinatesAway_topCenter_outOfBounds() {
        val fields1 = FieldCoordinate(14, 0).getCoordinatesAwayFromLocation(rules, FieldCoordinate(14, 1))
        assertTrue(fields1.isEmpty())

        val fields2 =
            FieldCoordinate(
                14,
                0,
            ).getCoordinatesAwayFromLocation(rules, FieldCoordinate(14, 1), includeOutOfBounds = true)
        assertEquals(3, fields2.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(13, -1),
                FieldCoordinate(14, -1),
                FieldCoordinate(15, -1),
            )
        assertFieldsContains(expectedFields, expectedFields)
    }

    @Test
    fun getCoordinatesAway_topRight() {
        val fields = FieldCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, FieldCoordinate(13, 8))
        assertEquals(3, fields.size)

        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(14, 6),
                FieldCoordinate(15, 6),
                FieldCoordinate(15, 7),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getCoordinatesAway_topRight_outOfBounds() {
        val fields1 = FieldCoordinate(25, 0).getCoordinatesAwayFromLocation(rules, FieldCoordinate(24, 1))
        assertTrue(fields1.isEmpty())

        val fields2 =
            FieldCoordinate(
                25,
                0,
            ).getCoordinatesAwayFromLocation(rules, FieldCoordinate(24, 1), includeOutOfBounds = true)
        assertEquals(3, fields2.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(25, -1),
                FieldCoordinate(26, -1),
                FieldCoordinate(26, 0),
            )
        assertFieldsContains(expectedFields, expectedFields)
    }

    @Test
    fun getCoordinatesAway_right() {
        val fields = FieldCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, FieldCoordinate(13, 7))
        assertEquals(3, fields.size)

        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(15, 6),
                FieldCoordinate(15, 7),
                FieldCoordinate(15, 8),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getCoordinatesAway_right_outOfBounds() {
        val fields1 = FieldCoordinate(25, 7).getCoordinatesAwayFromLocation(rules, FieldCoordinate(24, 7))
        assertTrue(fields1.isEmpty())

        val fields2 =
            FieldCoordinate(
                25,
                7,
            ).getCoordinatesAwayFromLocation(rules, FieldCoordinate(24, 7), includeOutOfBounds = true)
        assertEquals(3, fields2.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(26, 6),
                FieldCoordinate(26, 7),
                FieldCoordinate(26, 8),
            )
        assertFieldsContains(expectedFields, expectedFields)
    }

    @Test
    fun getCoordinatesAway_bottomRight() {
        val fields = FieldCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, FieldCoordinate(13, 6))
        assertEquals(3, fields.size)

        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(15, 7),
                FieldCoordinate(15, 8),
                FieldCoordinate(14, 8),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getCoordinatesAway_bottomRight_outOfBounds() {
        val fields1 = FieldCoordinate(25, 14).getCoordinatesAwayFromLocation(rules, FieldCoordinate(24, 13))
        assertTrue(fields1.isEmpty())

        val fields2 =
            FieldCoordinate(
                25,
                14,
            ).getCoordinatesAwayFromLocation(rules, FieldCoordinate(24, 13), includeOutOfBounds = true)
        assertEquals(3, fields2.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(26, 14),
                FieldCoordinate(26, 15),
                FieldCoordinate(25, 15),
            )
        assertFieldsContains(expectedFields, expectedFields)
    }

    @Test
    fun getCoordinatesAway_bottomCenter() {
        val fields = FieldCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, FieldCoordinate(14, 6))
        assertEquals(3, fields.size)

        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(15, 8),
                FieldCoordinate(14, 8),
                FieldCoordinate(13, 8),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getCoordinatesAway_bottomCenter_outOfBounds() {
        val fields1 = FieldCoordinate(7, 14).getCoordinatesAwayFromLocation(rules, FieldCoordinate(7, 13))
        assertTrue(fields1.isEmpty())

        val fields2 =
            FieldCoordinate(
                7,
                14,
            ).getCoordinatesAwayFromLocation(rules, FieldCoordinate(7, 13), includeOutOfBounds = true)
        assertEquals(3, fields2.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(6, 15),
                FieldCoordinate(7, 15),
                FieldCoordinate(8, 15),
            )
        assertFieldsContains(expectedFields, expectedFields)
    }

    @Test
    fun getCoordinatesAway_bottomLeft() {
        val fields = FieldCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, FieldCoordinate(15, 6))
        assertEquals(3, fields.size)

        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(14, 8),
                FieldCoordinate(13, 8),
                FieldCoordinate(13, 7),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getCoordinatesAway_bottomLeft_outOfBounds() {
        val fields1 = FieldCoordinate(0, 14).getCoordinatesAwayFromLocation(rules, FieldCoordinate(1, 13))
        assertTrue(fields1.isEmpty())

        val fields2 =
            FieldCoordinate(
                0,
                14,
            ).getCoordinatesAwayFromLocation(rules, FieldCoordinate(0, 13), includeOutOfBounds = true)
        assertEquals(3, fields2.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(0, 15),
                FieldCoordinate(-1, 15),
                FieldCoordinate(-1, 0),
            )
        assertFieldsContains(expectedFields, expectedFields)
    }

    @Test
    fun getCoordinatesAway_left() {
        val fields = FieldCoordinate(14, 7).getCoordinatesAwayFromLocation(rules, FieldCoordinate(15, 7))
        assertEquals(3, fields.size)

        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(13, 8),
                FieldCoordinate(13, 7),
                FieldCoordinate(13, 6),
            )
        assertFieldsContains(expectedFields, fields)
    }

    @Test
    fun getCoordinatesAway_left_outOfBounds() {
        val fields1 = FieldCoordinate(0, 7).getCoordinatesAwayFromLocation(rules, FieldCoordinate(1, 7))
        assertTrue(fields1.isEmpty())

        val fields2 =
            FieldCoordinate(
                0,
                7,
            ).getCoordinatesAwayFromLocation(rules, FieldCoordinate(1, 7), includeOutOfBounds = true)
        assertEquals(3, fields2.size)
        // Clockwise (starting at top-left)
        val expectedFields =
            listOf(
                FieldCoordinate(-1, 8),
                FieldCoordinate(-1, 7),
                FieldCoordinate(-1, 6),
            )
        assertFieldsContains(expectedFields, expectedFields)
    }

    @Test
    fun distanceTo() {
        val a = FieldCoordinate(0, 0)
        assertEquals(0, a.distanceTo(FieldCoordinate(0, 0)))
        assertEquals(1, a.distanceTo(FieldCoordinate(0, -1))) // Up
        assertEquals(2, a.distanceTo(FieldCoordinate(2, -1))) // Top-right
        assertEquals(3, a.distanceTo(FieldCoordinate(-3, 3))) // Bottom-left
    }

    @Test
    fun validWideZoneCoordinates() {
        val validCoordinates = listOf(
            FieldCoordinate(0, 0),
            FieldCoordinate(0, 3),
            FieldCoordinate(12, 0),
            FieldCoordinate(12, 3),
            FieldCoordinate(25, 0),
            FieldCoordinate(25, 3),
            FieldCoordinate(13, 0),
            FieldCoordinate(13, 3),
            FieldCoordinate(25, 14),
            FieldCoordinate(25, 11),
            FieldCoordinate(0, 14),
            FieldCoordinate(0, 11),
        )
        validCoordinates.forEach {
            assertTrue(it.isInWideZone(rules), "Not in wide zone: $it")
        }
    }

    @Test
    fun invalidWideZoneCoordinates() {
        val invalidCoordinates = listOf(
            FieldCoordinate(0, 4),
            FieldCoordinate(12, 4),
            FieldCoordinate(13, 4),
            FieldCoordinate(25, 4),

            FieldCoordinate(0, 10),
            FieldCoordinate(12, 10),
            FieldCoordinate(13, 10),
            FieldCoordinate(25, 10),

            FieldCoordinate(-1, -1),
            FieldCoordinate(-1, 0),
            FieldCoordinate(0, -1),

            FieldCoordinate(25, -1),
            FieldCoordinate(26, -1),
            FieldCoordinate(26, 0),

            FieldCoordinate(26, 14),
            FieldCoordinate(27, 15),
            FieldCoordinate(26, 15),

            FieldCoordinate(0, 15),
            FieldCoordinate(-1, 15),
            FieldCoordinate(-1, 14),
        )
        invalidCoordinates.forEach {
            assertFalse(it.isInWideZone(rules), "Is in a wide zone: $it")
        }
    }

    @Test
    fun wideZoneAndEndZoneOverlap() {
        val validCoordinates = listOf(
            FieldCoordinate(0, 0),
            FieldCoordinate(25, 0),
            FieldCoordinate(25, 14),
            FieldCoordinate(0, 14),
        )
        validCoordinates.forEach {
            assertTrue(it.isInWideZone(rules), "Not in wide zone: $it")
            assertTrue(it.isInEndZone(rules), "Not in end zone: $it")
        }
    }

    /**
     * Assert that all [FieldCoordinate]s in one list is present in another list.
     */
    private fun assertFieldsContains(
        expectedFields: List<FieldCoordinate>,
        fields: List<FieldCoordinate>,
    ) {
        expectedFields.forEach {
            if (!fields.contains(it)) {
                fail("$it was not found in: ${fields.joinToString()}")
            }
        }
    }
}
