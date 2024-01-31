package dk.ilios.jervis.procedures

import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.RerollSource

data class RerollContext(val roll: DiceRollType, val source: RerollSource)

data class RerollResultContext(val roll: DiceRollType, val source: RerollSource, val rerollAllowed: Boolean)