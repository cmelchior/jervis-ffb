package dk.ilios.jervis

import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.rules.BB2020Rules
import kotlin.test.Test
import kotlin.test.assertEquals

class RulesTests {

    val rules = BB2020Rules

    @Test
    fun getSurroundingCoordinates_topLeftCorner_insideField() {
        val topLeft = FieldCoordinate(0, 0)
        val expectedCoordinates = setOf(
            FieldCoordinate(1, 0),
            FieldCoordinate(1, 1),
            FieldCoordinate(0, 1),
        )
        val coords = topLeft.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
        assertEquals(3, coords.size, coords.toString())
        assertEquals(3, coords.intersect(expectedCoordinates).size, coords.toString())
    }

    @Test
    fun getSurroundingCoordinates_bottomLeftCorner_insideField() {
        val bottomLeft = FieldCoordinate(0, rules.fieldHeight - 1)
        val expectedCoordinates = setOf(
            FieldCoordinate(0, rules.fieldHeight - 2),
            FieldCoordinate(1, rules.fieldHeight - 2),
            FieldCoordinate(1, rules.fieldHeight - 1),
        )
        val coords = bottomLeft.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
        assertEquals(3, coords.size, coords.toString())
        assertEquals(3, coords.intersect(expectedCoordinates).size, coords.toString())
    }

    @Test
    fun getSurroundingCoordinates_topRightCorner_insideField() {
        val topRight = FieldCoordinate(rules.fieldWidth - 1, 0)
        val expectedCoordinates = setOf(
            FieldCoordinate(rules.fieldWidth - 2, 0),
            FieldCoordinate(rules.fieldWidth - 2, 1),
            FieldCoordinate(rules.fieldWidth - 1, 1),
        )
        val coords = topRight.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
        assertEquals(3, coords.size, coords.toString())
        assertEquals(3, coords.intersect(expectedCoordinates).size, coords.toString())
    }

    @Test
    fun getSurroundingCoordinates_bottomRightCorner_insideField() {
        val bottomRight = FieldCoordinate(rules.fieldWidth - 1, rules.fieldHeight - 1)
        val expectedCoordinates = setOf(
            FieldCoordinate(rules.fieldWidth - 2, rules.fieldHeight - 1),
            FieldCoordinate(rules.fieldWidth - 2, rules.fieldHeight - 2),
            FieldCoordinate(rules.fieldWidth - 1, rules.fieldHeight - 2),
        )
        val coords = bottomRight.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
        assertEquals(3, coords.size, coords.toString())
        assertEquals(3, coords.intersect(expectedCoordinates).size, coords.toString())
    }

    @Test
    fun getSurroundingCoordinates_middleOfField() {
        val bottomRight = FieldCoordinate(1, 1)
        val expectedCoordinates = setOf(
            FieldCoordinate(0, 0),
            FieldCoordinate(1, 0),
            FieldCoordinate(2, 0),
            FieldCoordinate(0, 1),
            FieldCoordinate(2, 1),
            FieldCoordinate(0, 2),
            FieldCoordinate(1, 2),
            FieldCoordinate(2, 2),
        )
        val coords = bottomRight.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
        assertEquals(8, coords.size, coords.toString())
        assertEquals(8, coords.intersect(expectedCoordinates).size, coords.toString())
    }

}
