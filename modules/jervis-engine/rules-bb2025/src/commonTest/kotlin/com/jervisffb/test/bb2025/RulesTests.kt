package com.jervisffb.test.bb2025

import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.StandardBB2020Rules
import kotlin.test.Test
import kotlin.test.assertEquals

class RulesTests {

    val rules = StandardBB2020Rules()

    @Test
    fun getSurroundingCoordinates_topLeftCorner_insidePitch() {
        val topLeft = PitchCoordinate(0, 0)
        val expectedCoordinates = setOf(
            PitchCoordinate(1, 0),
            PitchCoordinate(1, 1),
            PitchCoordinate(0, 1),
        )
        val coords = topLeft.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
        assertEquals(3, coords.size, coords.toString())
        assertEquals(3, coords.intersect(expectedCoordinates).size, coords.toString())
    }

    @Test
    fun getSurroundingCoordinates_bottomLeftCorner_insidePitch() {
        val bottomLeft = PitchCoordinate(0, rules.pitchHeight - 1)
        val expectedCoordinates = setOf(
            PitchCoordinate(0, rules.pitchHeight - 2),
            PitchCoordinate(1, rules.pitchHeight - 2),
            PitchCoordinate(1, rules.pitchHeight - 1),
        )
        val coords = bottomLeft.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
        assertEquals(3, coords.size, coords.toString())
        assertEquals(3, coords.intersect(expectedCoordinates).size, coords.toString())
    }

    @Test
    fun getSurroundingCoordinates_topRightCorner_insidePitch() {
        val topRight = PitchCoordinate(rules.pitchWidth - 1, 0)
        val expectedCoordinates = setOf(
            PitchCoordinate(rules.pitchWidth - 2, 0),
            PitchCoordinate(rules.pitchWidth - 2, 1),
            PitchCoordinate(rules.pitchWidth - 1, 1),
        )
        val coords = topRight.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
        assertEquals(3, coords.size, coords.toString())
        assertEquals(3, coords.intersect(expectedCoordinates).size, coords.toString())
    }

    @Test
    fun getSurroundingCoordinates_bottomRightCorner_insidePitch() {
        val bottomRight = PitchCoordinate(rules.pitchWidth - 1, rules.pitchHeight - 1)
        val expectedCoordinates = setOf(
            PitchCoordinate(rules.pitchWidth - 2, rules.pitchHeight - 1),
            PitchCoordinate(rules.pitchWidth - 2, rules.pitchHeight - 2),
            PitchCoordinate(rules.pitchWidth - 1, rules.pitchHeight - 2),
        )
        val coords = bottomRight.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
        assertEquals(3, coords.size, coords.toString())
        assertEquals(3, coords.intersect(expectedCoordinates).size, coords.toString())
    }

    @Test
    fun getSurroundingCoordinates_middleOfField() {
        val bottomRight = PitchCoordinate(1, 1)
        val expectedCoordinates = setOf(
            PitchCoordinate(0, 0),
            PitchCoordinate(1, 0),
            PitchCoordinate(2, 0),
            PitchCoordinate(0, 1),
            PitchCoordinate(2, 1),
            PitchCoordinate(0, 2),
            PitchCoordinate(1, 2),
            PitchCoordinate(2, 2),
        )
        val coords = bottomRight.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
        assertEquals(8, coords.size, coords.toString())
        assertEquals(8, coords.intersect(expectedCoordinates).size, coords.toString())
    }

    @Test
    fun canOfferAssistAgainst() {

    }
}
