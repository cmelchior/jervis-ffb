package com.jervisffb.engine.rules

import com.jervisffb.engine.rules.bb2025.BB2025TeamActions
import com.jervisffb.engine.rules.bb2025.tables.BB2025ArgueTheCallTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025CasualtyTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025LastingInjuryTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025RangeRuler
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardInjuryTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardKickOffEventTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardPrayersToNuffleTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardWeatherTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StuntyInjuryTable
import com.jervisffb.engine.rules.builder.GameType
import kotlinx.serialization.Serializable

/**
 * Top-level class for all variants of the 2025 Blood Bowl rules.
 */
abstract class BB2025Rules : Rules(
    name = "Blood Bowl 2025 Rules",
    gameType = GameType.STANDARD,
    teamActions = BB2025TeamActions(),
    kickOffEventTable = BB2025StandardKickOffEventTable,
    prayersToNuffleTable = BB2025StandardPrayersToNuffleTable,
    weatherTable = BB2025StandardWeatherTable,
    injuryTable = BB2025StandardInjuryTable,
    stuntyInjuryTable = BB2025StuntyInjuryTable,
    casualtyTable = BB2025CasualtyTable,
    lastingInjuryTable = BB2025LastingInjuryTable,
    argueTheCallTable = BB2025ArgueTheCallTable,
    rangeRuler = BB2025RangeRuler,
)

@Serializable
class StandardBB2025Rules : BB2025Rules() {
    override val name: String = "Blood Bowl 2025 Rules (Strict)"
}

