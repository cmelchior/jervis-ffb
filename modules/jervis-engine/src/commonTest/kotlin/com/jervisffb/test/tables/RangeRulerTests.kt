package com.jervisffb.test.tables

import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.tables.Range
import com.jervisffb.engine.rules.bb2020.tables.RangeRuler
import com.jervisffb.test.JervisGameTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RangeRulerTests: JervisGameTest() {

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
        // Basic test, should probably also include some more advanced use cases
        startDefaultGame()
        val interceptors = rules.rangeRuler.opponentPlayersUnderRuler(awayTeam["A1".playerId], awayTeam["A5".playerId].coordinates)
        assertEquals(3, interceptors.size)
        assertFalse(interceptors.any { it.id == "A1".playerId || it.id == "A5".playerId})
    }

    @Test
    fun coveredByRuler() {
        // TODO See https://www.luccini.it/bloodbowl/downloads/Tabella_Intercetti.pdf, it is for the old pitch size, and since the ruler has been scaled
        //  differently, the current results might be slightly off.
    }
}
