package com.jervisffb.engine.common.tables

import com.jervisffb.engine.common.procedures.tables.prayers.BlessedStatueOfNuffle
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.common.procedures.DummyProcedure
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.tables.PrayerToNuffleEvent

/**
 * List all possible outcomes, across all rule variants, when rolling on the Prayer to Nuffle table.
 *
 * This is the concrete catalog of prayers. It implements the `core`-resident
 * [com.jervisffb.engine.rules.common.tables.PrayerToNuffleEvent] abstraction and lives in `rules-common` together with
 * the procedures it references, so those procedures do not have to reside in
 * `core`.
 */
enum class CommonPrayerToNuffleTableResult(
    override val description: String,
    override val procedure: Procedure,
    override val duration: Duration
): PrayerToNuffleEvent {

    // BB2025 - All prayers now last the entire game
    DAZZLING_CATCHING("Dazzling Catching", DummyProcedure, Duration.END_OF_GAME),
    BLESSING_OF_NUFFLE("Blessing of Nuffle", BlessedStatueOfNuffle, Duration.END_OF_GAME),

    // BB2020 - Slight changes to duration and effect
}
