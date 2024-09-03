package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.fsm.Procedure
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
import dk.ilios.jervis.rules.skills.ResetPolicy

enum class KickOffEvent(override val description: String, override val procedure: Procedure, override val duration: ResetPolicy): TableResult {
    GET_THE_REF("Get the Ref", GetTheRef, ResetPolicy.END_OF_GAME),
    TIME_OUT("Time Out", TimeOut, ResetPolicy.END_OF_GAME),
    SOLID_DEFENSE("Solid Defense", SolidDefense, ResetPolicy.END_OF_GAME),
    HIGH_KICK("High Kick", HighKick, ResetPolicy.END_OF_GAME),
    CHEERING_FANS("Cheering Fans", CheeringFans, ResetPolicy.END_OF_GAME),
    BRILLIANT_COACHING("Brilliant Coaching", BrilliantCoaching, ResetPolicy.END_OF_GAME),
    CHANGING_WEATHER("Changing Weather", ChangingWeather, ResetPolicy.END_OF_GAME),
    QUICK_SNAP("Quick Snap", QuickSnap, ResetPolicy.END_OF_GAME),
    BLITZ("Blitz", Blitz, ResetPolicy.END_OF_GAME),
    OFFICIOUS_REF("Officious Ref", OfficiousRef, ResetPolicy.END_OF_GAME),
    PITCH_INVASION("Pitch Invasion", PitchInvasion, ResetPolicy.END_OF_GAME),
}
