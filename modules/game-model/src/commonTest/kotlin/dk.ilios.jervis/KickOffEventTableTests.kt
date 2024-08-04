package dk.ilios.jervis

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.procedures.bb2020.kickoff.Blitz
import dk.ilios.jervis.procedures.bb2020.kickoff.BrilliantCoaching
import dk.ilios.jervis.procedures.bb2020.kickoff.ChangingWeather
import dk.ilios.jervis.procedures.bb2020.kickoff.CheeringFans
import dk.ilios.jervis.procedures.bb2020.kickoff.GetTheRef
import dk.ilios.jervis.procedures.bb2020.kickoff.HighKick
import dk.ilios.jervis.procedures.bb2020.kickoff.OfficiousRef
import dk.ilios.jervis.procedures.bb2020.kickoff.PitchInvasion
import dk.ilios.jervis.procedures.bb2020.kickoff.QuickSnap
import dk.ilios.jervis.procedures.bb2020.kickoff.SolidDefense
import dk.ilios.jervis.procedures.bb2020.kickoff.TimeOut
import dk.ilios.jervis.rules.KickOffEventTable
import dk.ilios.jervis.rules.tables.TableResult
import kotlin.test.Test
import kotlin.test.assertEquals

class KickOffEventTableTests {
    private val table = KickOffEventTable

    @Test
    fun testValues() {
        assertEquals(TableResult("Get the Ref", GetTheRef), table.roll(D6Result(1), D6Result(1)))
        assertEquals(TableResult("Time Out", TimeOut), table.roll(D6Result(2), D6Result(1)))
        assertEquals(TableResult("Solid Defense", SolidDefense), table.roll(D6Result(2), D6Result(2)))
        assertEquals(TableResult("High Kick", HighKick), table.roll(D6Result(3), D6Result(2)))
        assertEquals(TableResult("Cheering Fans", CheeringFans), table.roll(D6Result(3), D6Result(3)))
        assertEquals(TableResult("Brilliant Coaching", BrilliantCoaching), table.roll(D6Result(4), D6Result(3)))
        assertEquals(TableResult("Changing Weather", ChangingWeather), table.roll(D6Result(4), D6Result(4)))
        assertEquals(TableResult("Quick Snap", QuickSnap), table.roll(D6Result(5), D6Result(4)))
        assertEquals(TableResult("Blitz", Blitz), table.roll(D6Result(5), D6Result(5)))
        assertEquals(TableResult("Officious Ref", OfficiousRef), table.roll(D6Result(6), D6Result(5)))
        assertEquals(TableResult("Pitch Invasion", PitchInvasion), table.roll(D6Result(6), D6Result(6)))
    }
}
