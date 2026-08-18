package com.jervisffb.ui.menu.components.setup

import com.jervisffb.engine.bb2025.tables.BB2025ArgueTheCallTable
import com.jervisffb.engine.bb2025.tables.BB2025CasualtyTable
import com.jervisffb.engine.bb2025.tables.BB2025LastingInjuryTable
import com.jervisffb.engine.bb2025.tables.BB2025StandardInjuryTable
import com.jervisffb.engine.bb2025.tables.BB2025StandardKickOffEventTable
import com.jervisffb.engine.bb2025.tables.BB2025StandardPrayersToNuffleTable
import com.jervisffb.engine.bb2025.tables.BB2025StandardWeatherTable
import com.jervisffb.engine.bb2025.tables.BB2025StuntyInjuryTable
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

class BB2025RulesSetupComponentModel(
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
            DropdownEntryWithValue("Standard", BB2025StandardWeatherTable, true),
        ),
    ),
    prayersToNuffleTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2025StandardPrayersToNuffleTable, true),
        ),
    ),
    kickOffTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2025StandardKickOffEventTable, true),
        ),
        //    "Spike Magazine 22" to listOf(
        //        DropdownEntryWithValue("Sevens", BB2025StandardKickOffEventTable, false),
        //    ),
    ),
    injuryTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2025StandardInjuryTable, true),
        ),
        //    "Spike 22" to listOf(
        //        DropdownEntryWithValue("Sevens", BB2025StandardInjuryTable, true),
        //    ),
    ),
    stuntyInjuryTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2025StuntyInjuryTable, true),
        ),
        //    "Spike 22" to listOf(
        //        DropdownEntryWithValue("Sevens", BB2025StuntyInjuryTable, true),
        //    ),
    ),
    casualtyTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2025CasualtyTable, true),
        ),
    ),
    lastingInjuryTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2025LastingInjuryTable, true),
        ),
    ),
    argueTheCallTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2025ArgueTheCallTable, true),
        ),
        //    "Spike 22" to listOf(
        //        DropdownEntryWithValue("Sevens", BB2025ArgueTheCallTable, true),
        //    ),
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
