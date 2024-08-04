package dk.ilios.jervis

import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.rules.tables.CornerThrowInPosition
import dk.ilios.jervis.rules.tables.Direction
import dk.ilios.jervis.rules.tables.RandomDirectionTemplate
import kotlin.test.Test
import kotlin.test.assertEquals

class RandomDirectionTemplateTests {
    private val template = RandomDirectionTemplate

    @Test
    fun testDirections() {
        assertEquals(Direction(0, -1), template.roll(D8Result(2)))
        assertEquals(Direction(1, -1), template.roll(D8Result(3)))
        assertEquals(Direction(1, 0), template.roll(D8Result(5)))
        assertEquals(Direction(1, 1), template.roll(D8Result(8)))
        assertEquals(Direction(0, 1), template.roll(D8Result(7)))
        assertEquals(Direction(-1, 1), template.roll(D8Result(6)))
        assertEquals(Direction(-1, 0), template.roll(D8Result(4)))
        assertEquals(Direction(-1, -1), template.roll(D8Result(1)))
    }

    @Test
    fun testTopLeftCornerThrowIns() {
        assertEquals(Direction(1, 0), template.roll(CornerThrowInPosition.TOP_LEFT, D3Result(1)))
        assertEquals(Direction(1, 1), template.roll(CornerThrowInPosition.TOP_LEFT, D3Result(2)))
        assertEquals(Direction(0, 1), template.roll(CornerThrowInPosition.TOP_LEFT, D3Result(3)))
    }

    @Test
    fun testTopRightCornerThrowIns() {
        assertEquals(Direction(0, 1), template.roll(CornerThrowInPosition.TOP_RIGHT, D3Result(1)))
        assertEquals(Direction(-1, 1), template.roll(CornerThrowInPosition.TOP_RIGHT, D3Result(2)))
        assertEquals(Direction(-1, 0), template.roll(CornerThrowInPosition.TOP_RIGHT, D3Result(3)))
    }

    @Test
    fun testBottomLeftCornerThrowIns() {
        assertEquals(Direction(0, -1), template.roll(CornerThrowInPosition.BOTTOM_LEFT, D3Result(1)))
        assertEquals(Direction(1, -1), template.roll(CornerThrowInPosition.BOTTOM_LEFT, D3Result(2)))
        assertEquals(Direction(1, 0), template.roll(CornerThrowInPosition.BOTTOM_LEFT, D3Result(3)))
    }

    @Test
    fun testBottomRightCornerThrowIns() {
        assertEquals(Direction(-1, 0), template.roll(CornerThrowInPosition.BOTTOM_RIGHT, D3Result(1)))
        assertEquals(Direction(-1, -1), template.roll(CornerThrowInPosition.BOTTOM_RIGHT, D3Result(2)))
        assertEquals(Direction(0, -1), template.roll(CornerThrowInPosition.BOTTOM_RIGHT, D3Result(3)))
    }
}
