package com.jervisffb.ui.menu.components.setup

import com.jervisffb.engine.bb2020.tables.AutumnWeatherTable
import com.jervisffb.engine.bb2020.tables.BB2020ArgueTheCallTable
import com.jervisffb.engine.bb2020.tables.BB2020CasualtyTable
import com.jervisffb.engine.bb2020.tables.BB2020LastingInjuryTable
import com.jervisffb.engine.bb2020.tables.BB2020StandardInjuryTable
import com.jervisffb.engine.bb2020.tables.BB2020StandardKickOffEventTable
import com.jervisffb.engine.bb2020.tables.BB2020StandardPrayersToNuffleTable
import com.jervisffb.engine.bb2020.tables.BB2020StandardWeatherTable
import com.jervisffb.engine.bb2020.tables.BB2020StuntyInjuryTable
import com.jervisffb.engine.bb2020.tables.BB7KickOffEventTable
import com.jervisffb.engine.bb2020.tables.BB7PrayersToNuffleTable
import com.jervisffb.engine.bb2020.tables.BB7StandardInjuryTable
import com.jervisffb.engine.bb2020.tables.BB7StuntyInjuryTable
import com.jervisffb.engine.bb2020.tables.SpringWeatherTable
import com.jervisffb.engine.bb2020.tables.SummerWeatherTable
import com.jervisffb.engine.bb2020.tables.WinterWeatherTable
import com.jervisffb.engine.common.tables.DisabledCasualtyTable
import com.jervisffb.engine.common.tables.DisabledLastingInjuryTable
import com.jervisffb.engine.model.BallType
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.model.StadiumType
import com.jervisffb.engine.rules.RulesParameterBuilder
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.builder.NoStadium
import com.jervisffb.engine.rules.builder.RollForStadiumUsed
import com.jervisffb.engine.rules.builder.RollOnUnusualBallTable
import com.jervisffb.engine.rules.builder.SpecificStadium
import com.jervisffb.engine.rules.builder.SpecificUnusualBall
import com.jervisffb.engine.rules.builder.StandardBall
import com.jervisffb.ui.menu.utils.DropdownEntryWithValue

/**
 * This file contains the setup for the "Rules" tab when a BB2020 game has
 * been selected.
 */

class BB2020RulesSetupComponentModel(
    initialRulesBuilder: RulesParameterBuilder,
    parent: GameConfigurationContainerComponentModel,
) : RulesSetupComponentModel(initialRulesBuilder, parent, BB2020_RULES_SETUP_CONFIGURATION)

private val BB2020_RULES_SETUP_CONFIGURATION = RulesSetupConfiguration(
    gameVersion = GameVersion.BB2020,
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
            column = RulesSetupColumn.LEFT,
            controls = listOf(
                RulesSetupControl.KICK_OFF_TABLE,
                RulesSetupControl.INJURY_TABLE,
                RulesSetupControl.STUNTY_INJURY_TABLE,
                RulesSetupControl.CASUALTY_TABLE,
                RulesSetupControl.LASTING_INJURY_TABLE,
                RulesSetupControl.ARGUE_THE_CALL_TABLE,
            ),
        ),
        RulesSetupSection(
            title = "Pitch and Ball",
            column = RulesSetupColumn.RIGHT,
            controls = listOf(
                RulesSetupControl.PITCH,
                RulesSetupControl.BALL,
            ),
        ),
        RulesSetupSection(
            title = "Stadia",
            column = RulesSetupColumn.RIGHT,
            controls = listOf(RulesSetupControl.STADIUM),
        ),
        RulesSetupSection(
            title = "Events",
            column = RulesSetupColumn.RIGHT,
            controls = listOf(
                RulesSetupControl.PRAYERS_TO_NUFFLE_TOGGLE,
                RulesSetupControl.MATCH_EVENTS_TOGGLE,
                RulesSetupControl.EXTRA_TIME_TOGGLE,
            ),
        ),
    ),
    weatherTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2020StandardWeatherTable, true),
        ),
        "Death Zone" to listOf(
            DropdownEntryWithValue("Spring", SpringWeatherTable, false),
            DropdownEntryWithValue("Summer", SummerWeatherTable, false),
            DropdownEntryWithValue("Autumn", AutumnWeatherTable, false),
            DropdownEntryWithValue("Winter", WinterWeatherTable, false),
            DropdownEntryWithValue("Subterranean", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Primordial", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Graveyard", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Desolate Wasteland", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Mountainous", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Coastal", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Desert", BB2020StandardWeatherTable, false),
        ),
    ),
    prayersToNuffleTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2020StandardPrayersToNuffleTable, true),
        ),
        "Death Zone" to listOf(
            DropdownEntryWithValue("Sevens", BB7PrayersToNuffleTable, true),
        ),
    ),
    kickOffTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2020StandardKickOffEventTable, true),
        ),
        "Death Zone" to listOf(
            DropdownEntryWithValue("Sevens", BB7KickOffEventTable, true),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            DropdownEntryWithValue("Temple-City", BB2020StandardKickOffEventTable, false),
        ),
    ),
    injuryTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2020StandardInjuryTable, true),
        ),
        "Death Zone" to listOf(
            DropdownEntryWithValue("Sevens", BB7StandardInjuryTable, true),
        ),
    ),
    stuntyInjuryTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2020StuntyInjuryTable, true),
        ),
        "Death Zone" to listOf(
            DropdownEntryWithValue("Sevens", BB7StuntyInjuryTable, true),
        ),
    ),
    casualtyTables = listOf(
        "" to listOf(
            DropdownEntryWithValue("Disabled", DisabledCasualtyTable, true),
        ),
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2020CasualtyTable, true),
        ),
    ),
    lastingInjuryTables = listOf(
        "" to listOf(
            DropdownEntryWithValue("Disabled", DisabledLastingInjuryTable, true),
        ),
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2020LastingInjuryTable, true),
        ),
    ),
    argueTheCallTables = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", BB2020ArgueTheCallTable, true),
        ),
    ),
    unusualBallList = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Normal Ball", StandardBall, true),
        ),
        "Death Zone" to listOf(
            DropdownEntryWithValue("Roll On Unusual Balls Table", RollOnUnusualBallTable, false),
            DropdownEntryWithValue("Explodin'", SpecificUnusualBall(BallType.EXPLODIN), false),
            DropdownEntryWithValue("Deamonic", SpecificUnusualBall(BallType.DEAMONIC), false),
            DropdownEntryWithValue("Stacked Lunch", SpecificUnusualBall(BallType.STACKED_LUNCH), false),
            DropdownEntryWithValue("Draconic", SpecificUnusualBall(BallType.DRACONIC), false),
            DropdownEntryWithValue("Spiteful Sprite", SpecificUnusualBall(BallType.SPITEFUL_SPRITE), false),
            DropdownEntryWithValue("Master-hewn", SpecificUnusualBall(BallType.MASTER_HEWN), false),
            DropdownEntryWithValue("Extra Spiky", SpecificUnusualBall(BallType.EXTRA_SPIKY), false),
            DropdownEntryWithValue("Greedy Nurgling", SpecificUnusualBall(BallType.GREEDY_NURGLING), false),
            DropdownEntryWithValue("Dark Majesty", SpecificUnusualBall(BallType.DARK_MAJESTY), false),
            DropdownEntryWithValue("Shady Special", SpecificUnusualBall(BallType.SHADY_SPECIAL), false),
            DropdownEntryWithValue("Soulstone", SpecificUnusualBall(BallType.SOULSTONE), false),
            DropdownEntryWithValue("Frozen", SpecificUnusualBall(BallType.FROZEN_BALL), false),
            DropdownEntryWithValue("Sacred Egg", SpecificUnusualBall(BallType.SACRED_EGG), false),
            DropdownEntryWithValue("Snotling Ball-suite", SpecificUnusualBall(BallType.SNOTLING_BALL_SUIT), false),
            DropdownEntryWithValue("Limpin' Squig", SpecificUnusualBall(BallType.LIMPIN_SQUIG), false),
            DropdownEntryWithValue("Warpstone Brazier", SpecificUnusualBall(BallType.WARPSTONE_BRAZIER), false),
        ),
        "Spike Magazine 14 (Norse)" to listOf(
            DropdownEntryWithValue("Hammer of Legend", SpecificUnusualBall(BallType.HAMMER_OF_LEGEND), false),
            DropdownEntryWithValue("The Runestone", SpecificUnusualBall(BallType.THE_RUNESTONE), false),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            DropdownEntryWithValue("Crystal Skull", SpecificUnusualBall(BallType.CRYSTAL_SKULL), false),
            DropdownEntryWithValue("Snake-swallowed", SpecificUnusualBall(BallType.SNAKE_SWALLOWED), false),
        ),
    ),
    pitches = listOf(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", PitchType.STANDARD, true),
        ),
        "Spike Magazine 14 (Norse)" to listOf(
            DropdownEntryWithValue("Frozen Lake", PitchType.FROZEN_LAKE, false),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            DropdownEntryWithValue("Overgrown Jungle", PitchType.OVERGROWN_JUNGLE, false),
        ),
    ),
    stadia = listOf(
        "Death Zone" to listOf(
            DropdownEntryWithValue("Disabled", NoStadium, true),
            DropdownEntryWithValue("Enabled", RollForStadiumUsed, false),
        ),
        "Unusual Playing Surface" to listOf(
            DropdownEntryWithValue("Ankle-Deep Water", SpecificStadium(StadiumType.ANKLE_DEEP_WATER), false),
            DropdownEntryWithValue("Sloping Pitch", SpecificStadium(StadiumType.SLOPING_PITCH), false),
            DropdownEntryWithValue("Ice", SpecificStadium(StadiumType.ICE), false),
            DropdownEntryWithValue("Astrogranite", SpecificStadium(StadiumType.ASTROGRANITE), false),
            DropdownEntryWithValue("Uneven Footing", SpecificStadium(StadiumType.UNEVEN_FOOTING), false),
            DropdownEntryWithValue("Solid Stone", SpecificStadium(StadiumType.SOLID_STONE), false),
        ),
    ),
)
