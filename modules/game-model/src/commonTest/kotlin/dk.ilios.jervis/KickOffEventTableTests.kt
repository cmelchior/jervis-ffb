package dk.ilios.jervis

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.rules.KickOffEvent
import dk.ilios.jervis.rules.KickOffEventTable
import kotlin.test.Test
import kotlin.test.assertEquals

class KickOffEventTableTests {

    private val table = KickOffEventTable

    @Test
    fun testValues() {
        assertEquals(KickOffEvent.GET_THE_REF, table.roll(D6Result(1), D6Result(1)))
        assertEquals(KickOffEvent.TIME_OUT, table.roll(D6Result(2), D6Result(1)))
        assertEquals(KickOffEvent.SOLID_DEFENSE, table.roll(D6Result(2), D6Result(2)))
        assertEquals(KickOffEvent.HIGH_KICK, table.roll(D6Result(3), D6Result(2)))
        assertEquals(KickOffEvent.CHEERING_FANS, table.roll(D6Result(3), D6Result(3)))
        assertEquals(KickOffEvent.BRILLIANT_COACHING, table.roll(D6Result(4), D6Result(3)))
        assertEquals(KickOffEvent.CHANGING_WEATHER, table.roll(D6Result(4), D6Result(4)))
        assertEquals(KickOffEvent.QUICK_SNAP, table.roll(D6Result(5), D6Result(4)))
        assertEquals(KickOffEvent.BLITZ, table.roll(D6Result(5), D6Result(5)))
        assertEquals(KickOffEvent.OFFICIOUS_REF, table.roll(D6Result(6), D6Result(5)))
        assertEquals(KickOffEvent.PITCH_INVASION, table.roll(D6Result(6), D6Result(6)))
    }
}