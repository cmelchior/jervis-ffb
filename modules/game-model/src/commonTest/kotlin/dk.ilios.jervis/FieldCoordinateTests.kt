package dk.ilios.jervis

import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.CoachId
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

private fun getDefaultTestSetup(rules: Rules): Game {
    val rules = BB2020Rules
    val team1: Team =
        teamBuilder(HumanTeam) {
            coach = Coach(CoachId("home-coach"), "HomeCoach")
            name = "HomeTeam"
            addPlayer(PlayerId("H1"), "Lineman-1", PlayerNo(1), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H2"), "Lineman-2", PlayerNo(2), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H3"), "Lineman-3", PlayerNo(3), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H4"), "Lineman-4", PlayerNo(4), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H5"), "Thrower-1", PlayerNo(5), HumanTeam.THROWER)
            addPlayer(PlayerId("H6"), "Catcher-1", PlayerNo(6), HumanTeam.CATCHER)
            addPlayer(PlayerId("H7"), "Catcher-2", PlayerNo(7), HumanTeam.CATCHER)
            addPlayer(PlayerId("H8"), "Blitzer-1", PlayerNo(8), HumanTeam.BLITZER)
            addPlayer(PlayerId("H9"), "Blitzer-2", PlayerNo(9), HumanTeam.BLITZER)
            addPlayer(PlayerId("H10"), "Blitzer-3", PlayerNo(10), HumanTeam.BLITZER)
            addPlayer(PlayerId("H11"), "Blitzer-4", PlayerNo(11), HumanTeam.BLITZER)
            reRolls = 4
            apothecaries = 1
        }
    val p1 = team1
    val p2 = team1
    val field = Field.createForRuleset(rules)
    return Game(p1, p2, field)
}

class FieldCoordinateTests {
    private val rules = BB2020Rules
    private lateinit var state: Game

    @BeforeTest
    fun setUp() {
        state = getDefaultTestSetup(rules)
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
                rules.fieldWidth.toInt() - 1,
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
                rules.fieldHeight.toInt() - 1,
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
                rules.fieldWidth.toInt() - 1,
                rules.fieldHeight.toInt() - 1,
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
        assertEquals(0u, a.distanceTo(FieldCoordinate(0, 0)))
        assertEquals(1u, a.distanceTo(FieldCoordinate(0, -1))) // Up
        assertEquals(2u, a.distanceTo(FieldCoordinate(2, -1))) // Top-right
        assertEquals(3u, a.distanceTo(FieldCoordinate(-3, 3))) // Bottom-left
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
