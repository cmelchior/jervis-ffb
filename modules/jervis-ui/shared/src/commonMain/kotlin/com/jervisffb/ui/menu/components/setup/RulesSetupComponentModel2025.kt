package com.jervisffb.ui.menu.components.setup

import com.jervisffb.engine.bb2025.tables.ArgueTheCallTable2025
import com.jervisffb.engine.bb2025.tables.BB7ArgueTheCallTable
import com.jervisffb.engine.bb2025.tables.BB7KickOffEventTable
import com.jervisffb.engine.bb2025.tables.BB7PrayersToNuffleTable
import com.jervisffb.engine.bb2025.tables.BB7StandardInjuryTable
import com.jervisffb.engine.bb2025.tables.BB7StuntyInjuryTable
import com.jervisffb.engine.bb2025.tables.CasualtyTable2025
import com.jervisffb.engine.bb2025.tables.LastingInjuryTable2025
import com.jervisffb.engine.bb2025.tables.StandardInjuryTable2025
import com.jervisffb.engine.bb2025.tables.StandardKickOffEventTable2025
import com.jervisffb.engine.bb2025.tables.StandardPrayersToNuffleTable
import com.jervisffb.engine.bb2025.tables.StandardWeatherTable2025
import com.jervisffb.engine.bb2025.tables.StuntyInjuryTable2025
import com.jervisffb.engine.common.tables.DisabledCasualtyTable
import com.jervisffb.engine.common.tables.DisabledLastingInjuryTable
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.rules.RulesParameterBuilder
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.builder.NoStadium
import com.jervisffb.engine.rules.builder.StandardBall
import com.jervisffb.ui.menu.utils.DropdownEntryWithValue

/**
 * This file contains the setup for the "Rules" tab when a BB2025 game has
 * been selected.
 */

class RulesSetupComponentModel2025(
    initialRulesBuilder: RulesParameterBuilder,
    parent: GameConfigurationContainerComponentModel,
) : RulesSetupComponentModel(initialRulesBuilder, parent, BB2025_RULES_SETUP_CONFIGURATION)

private val BB2025_RULES_SETUP_CONFIGURATION = RulesSetupConfiguration(
    gameVersion = GameVersion.BB2025,
    sections = listOf(
        RulesSetupSection(
            title = "Pre-game Tables",
            column = RulesSetupColumn.LEFT,
            controls = listOf(
                RulesSetupControl.WEATHER_TABLE,
                RulesSetupControl.PRAYERS_TO_NUFFLE_TABLE,
            ),
        ),
        RulesSetupSection(
            title = "In-game Tables",
            column = RulesSetupColumn.RIGHT,
            controls = listOf(
                RulesSetupControl.KICK_OFF_TABLE,
                RulesSetupControl.INJURY_TABLE,
                RulesSetupControl.STUNTY_INJURY_TABLE,
                RulesSetupControl.CASUALTY_TABLE,
                RulesSetupControl.LASTING_INJURY_TABLE,
                RulesSetupControl.ARGUE_THE_CALL_TABLE,
            ),
        ),
        // No alternative pitches and balls are available in BB2025. So hide this for now.
        //    RulesSetupSection(
        //        title = "Pitch and Ball",
        //        column = RulesSetupColumn.RIGHT,
        //        controls = listOf(
        //            RulesSetupControl.PITCH,
        //            RulesSetupControl.BALL,
        //        ),
        //    ),
        // Stadium rules are not currently supported in BB2025, so just disable this section
        //    RulesSetupSection(
        //        title = "Stadia",
        //        column = RulesSetupColumn.RIGHT,
        //        controls = listOf(
        //            RulesSetupControl.STADIUM,
        //        ),
        //    ),
        RulesSetupSection(
            title = "Events",
            column = RulesSetupColumn.LEFT,
            controls = listOf(
                RulesSetupControl.EXTRA_TIME_TOGGLE,
            ),
        ),
    ),
    weatherTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", StandardWeatherTable2025, true),
        ),
    ),
    prayersToNuffleTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", StandardPrayersToNuffleTable, true),
        ),
        "Spike Magazine 22" to listOf(
            DropdownEntryWithValue("Sevens", BB7PrayersToNuffleTable, false),
        ),
    ),
    kickOffTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", StandardKickOffEventTable2025, true),
        ),
        "Spike Magazine 22" to listOf(
            DropdownEntryWithValue("Sevens", BB7KickOffEventTable, false),
        ),
    ),
    injuryTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", StandardInjuryTable2025, true),
        ),
        "Spike 22" to listOf(
            DropdownEntryWithValue("Sevens", BB7StandardInjuryTable, true),
        ),
    ),
    stuntyInjuryTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", StuntyInjuryTable2025, true),
        ),
        "Spike 22" to listOf(
            DropdownEntryWithValue("Sevens", BB7StuntyInjuryTable, true),
        ),
    ),
    casualtyTables = listOf(
        "" to listOf(
            DropdownEntryWithValue("Disabled", DisabledCasualtyTable, true),
        ),
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", CasualtyTable2025, true),
        ),
    ),
    lastingInjuryTables = listOf(
        "" to listOf(
            DropdownEntryWithValue("Disabled", DisabledLastingInjuryTable, true),
        ),
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", LastingInjuryTable2025, true),
        ),
    ),
    argueTheCallTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", ArgueTheCallTable2025, true),
        ),
        "Spike 22" to listOf(
            DropdownEntryWithValue("Sevens (Amateur Referees)", BB7ArgueTheCallTable, true),
        ),
    ),
    unusualBallList = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Normal Ball", StandardBall, true),
        ),
    ),
    pitches = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", PitchType.STANDARD, true),
        ),
    ),
    // Stadia are not shown for BB2025, but the current value is still needed to initialize shared state.
    stadia = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Disabled", NoStadium, true),
        ),
    ),
)
