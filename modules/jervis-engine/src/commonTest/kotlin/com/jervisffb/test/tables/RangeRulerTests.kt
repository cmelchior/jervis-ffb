package com.jervisffb.test.tables

import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.tables.Range
import com.jervisffb.engine.rules.bb2020.tables.RangeRuler
import kotlin.test.Test
import kotlin.test.assertEquals

class RangeRulerTests {

    private val ruler = RangeRuler

    @Test
    fun passingPlayer() {
        assertEquals(Range.PASSING_PLAYER, ruler.measure(FieldCoordinate(1, 1), FieldCoordinate(1,1)))
    }

    @Test
    fun quickPass() {
        assertEquals(Range.QUICK_PASS, ruler.measure(FieldCoordinate(1, 1), FieldCoordinate(0,0)))
        assertEquals(Range.QUICK_PASS, ruler.measure(FieldCoordinate(0, 0), FieldCoordinate(2,2)))
    }

    @Test
    fun shortPass() {
        assertEquals(Range.SHORT_PASS, ruler.measure(FieldCoordinate(0, 0), FieldCoordinate(3,2)))
    }

    @Test
    fun longPass() {
        assertEquals(Range.LONG_PASS, ruler.measure(FieldCoordinate(0, 0), FieldCoordinate(4,6)))
    }

    @Test
    fun longBomb() {
        assertEquals(Range.LONG_BOMB, ruler.measure(FieldCoordinate(0, 0), FieldCoordinate(13,0)))
    }

    @Test
    fun outOfRange() {
        assertEquals(Range.OUT_OF_RANGE, ruler.measure(FieldCoordinate(0, 0), FieldCoordinate(14,0)))
        assertEquals(Range.OUT_OF_RANGE, ruler.measure(FieldCoordinate(0, 0), FieldCoordinate(0,14)))
        assertEquals(Range.OUT_OF_RANGE, ruler.measure(FieldCoordinate(0, 0), FieldCoordinate(11,9)))
    }

    @Test
    fun coveredByRuler_originAndTargetNotIncluded() {
        TODO()
    }

    @Test
    fun coveredByRuler() {
        // Which corner cases to consider here?
        TODO()
    }
}
