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
import dk.ilios.jervis.rules.skills.Duration

enum class KickOffEvent(override val description: String, override val procedure: Procedure, override val duration: Duration): TableResult {
    GET_THE_REF("Get the Ref", GetTheRef, Duration.END_OF_GAME),
    TIME_OUT("Time Out", TimeOut, Duration.END_OF_GAME),
    SOLID_DEFENSE("Solid Defense", SolidDefense, Duration.END_OF_GAME),
    HIGH_KICK("High Kick", HighKick, Duration.END_OF_GAME),
    CHEERING_FANS("Cheering Fans", CheeringFans, Duration.END_OF_GAME),
    BRILLIANT_COACHING("Brilliant Coaching", BrilliantCoaching, Duration.END_OF_GAME),
    CHANGING_WEATHER("Changing Weather", ChangingWeather, Duration.END_OF_GAME),
    QUICK_SNAP("Quick Snap", QuickSnap, Duration.END_OF_GAME),
    BLITZ("Blitz", Blitz, Duration.END_OF_GAME),
    OFFICIOUS_REF("Officious Ref", OfficiousRef, Duration.END_OF_GAME),
    PITCH_INVASION("Pitch Invasion", PitchInvasion, Duration.END_OF_GAME),
}
