package com.jervisffb.test.tables

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.bb2020.tables.CornerThrowInPosition
import com.jervisffb.engine.rules.bb2020.tables.RandomDirectionTemplate
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
