package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.model.BallType
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.model.StadiumType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.BB2020ArgueTheCallTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020CasualtyTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020LastingInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardKickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardPrayersToNuffleTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StuntyInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB7KickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.BB7PrayersToNuffleTable
import com.jervisffb.engine.rules.bb2020.tables.BB7StandardInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB7StuntyInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.SpringWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.SummerWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.WinterWeatherTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025ArgueTheCallTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025CasualtyTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025LastingInjuryTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardInjuryTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardKickOffEventTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardPrayersToNuffleTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StandardWeatherTable
import com.jervisffb.engine.rules.bb2025.tables.BB2025StuntyInjuryTable
import com.jervisffb.engine.rules.builder.BallSelectorRule
import com.jervisffb.engine.rules.builder.NoStadium
import com.jervisffb.engine.rules.builder.RollForStadiumUsed
import com.jervisffb.engine.rules.builder.RollOnUnusualBallTable
import com.jervisffb.engine.rules.builder.SpecificStadium
import com.jervisffb.engine.rules.builder.SpecificUnusualBall
import com.jervisffb.engine.rules.builder.StadiumRule
import com.jervisffb.engine.rules.builder.StandardBall
import com.jervisffb.engine.rules.common.tables.ArgueTheCallTable
import com.jervisffb.engine.rules.common.tables.CasualtyTable
import com.jervisffb.engine.rules.common.tables.InjuryTable
import com.jervisffb.engine.rules.common.tables.KickOffTable
import com.jervisffb.engine.rules.common.tables.LastingInjuryTable
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.rules.common.tables.WeatherTable
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.utils.DropdownEntryWithValue
import com.jervisffb.ui.menu.utils.findEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * This component is responsible for all the UI control needed to configure the rules of a game.
 */
class RulesSetupComponentModel(
    initialRulesBuilder: Rules.Builder,
    private val parent: GameConfigurationContainerComponentModel,
    private val menuViewModel: MenuViewModel
) : ScreenModel {

    var rulesBuilder = initialRulesBuilder

    val weatherTables = listOf<Pair<String, List<DropdownEntryWithValue<WeatherTable>>>>(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard (2025)", BB2025StandardWeatherTable, true),
            DropdownEntryWithValue("Standard (2020)", BB2020StandardWeatherTable, true),
        ),
        "Death Zone (2020)" to listOf(
            DropdownEntryWithValue("Spring", SpringWeatherTable, false),
            DropdownEntryWithValue("Summer", SummerWeatherTable, false),
            DropdownEntryWithValue("Autumn", SummerWeatherTable, false),
            DropdownEntryWithValue("Winter", WinterWeatherTable, false),
            DropdownEntryWithValue("Subterranean", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Primordial", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Graveyard", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Desolate Wasteland", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Mountainous", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Coastal", BB2020StandardWeatherTable, false),
            DropdownEntryWithValue("Desert", BB2020StandardWeatherTable, false),
        )
    )

    val prayersToNuffleTables = listOf<Pair<String, List<DropdownEntryWithValue<PrayersToNuffleTable>>>>(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard (2025)", BB2025StandardPrayersToNuffleTable, true),
            DropdownEntryWithValue("Standard (2020)", BB2020StandardPrayersToNuffleTable, true),
        ),
        "Death Zone (2020)" to listOf(
            DropdownEntryWithValue("Blood Bowl Sevens", BB7PrayersToNuffleTable, true),
        ),
    )

    val kickOffTables = listOf<Pair<String, List<DropdownEntryWithValue<KickOffTable>>>>(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard (2025)", BB2025StandardKickOffEventTable, true),
            DropdownEntryWithValue("Standard (2020)", BB2020StandardKickOffEventTable, true),
        ),
        "Death Zone (2020)" to listOf(
            DropdownEntryWithValue("Blood Bowl Sevens", BB7KickOffEventTable, true),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            DropdownEntryWithValue("Temple-City", BB2020StandardKickOffEventTable, false),
        )
    )

    val injuryTables = listOf<Pair<String, List<DropdownEntryWithValue<InjuryTable>>>>(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard (2025)", BB2025StandardInjuryTable, true),
            DropdownEntryWithValue("Standard (2020)", BB2020StandardInjuryTable, true),
        ),
        "Death Zone (2020)" to listOf(
            DropdownEntryWithValue("Blood Bowl Sevens", BB7StandardInjuryTable, true),
        ),
    )

    val stuntyInjuryTables = listOf<Pair<String, List<DropdownEntryWithValue<InjuryTable>>>>(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard (2025)", BB2025StuntyInjuryTable, true),
            DropdownEntryWithValue("Standard (2020)", BB2020StuntyInjuryTable, true),
        ),
        "Death Zone (2020)" to listOf(
            DropdownEntryWithValue("Blood Bowl Sevens", BB7StuntyInjuryTable, true),
        ),
    )

    val casualtyTables = listOf<Pair<String, List<DropdownEntryWithValue<CasualtyTable>>>>(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard (2025)", BB2025CasualtyTable, true),
            DropdownEntryWithValue("Standard (2020)", BB2020CasualtyTable, true),
        ),
    )

    val lastingInjuryTables = listOf<Pair<String, List<DropdownEntryWithValue<LastingInjuryTable>>>>(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard (2025)", BB2025LastingInjuryTable, true),
            DropdownEntryWithValue("Standard (2020)", BB2020LastingInjuryTable, true),
        ),
    )

    val argueTheCallTables = listOf<Pair<String, List<DropdownEntryWithValue<ArgueTheCallTable>>>>(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard (2025)", BB2025ArgueTheCallTable, true),
            DropdownEntryWithValue("Standard (2020)", BB2020ArgueTheCallTable, true),
        ),
    )

    val unusualBallList = listOf<Pair<String, List<DropdownEntryWithValue<BallSelectorRule>>>>(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Normal Ball", StandardBall, true),
        ),
        "Death Zone (2020)" to listOf(
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
    )

    val pitches = listOf<Pair<String, List<DropdownEntryWithValue<PitchType>>>>(
        "Rulebook" to listOf(
            DropdownEntryWithValue("Standard", PitchType.STANDARD, true),
        ),
        "Spike Magazine 14 (Norse)" to listOf(
            DropdownEntryWithValue("Frozen Lake", PitchType.FROZEN_LAKE, false),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            DropdownEntryWithValue("Overgrown Jungle", PitchType.OVERGROWN_JUNGLE, false),
        )
    )

    val stadia = listOf<Pair<String, List<DropdownEntryWithValue<StadiumRule>>>>(
        "Death Zone (2020)" to listOf(
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
    )

    // Currently it isn't possible to put the rules section in an invalid state, but keep it here to keep
    // it future-proof.
    val isSetupValid = MutableStateFlow(true)
    val availableRuleBases: StateFlow<List<DropdownEntryWithValue<Rules>>> = parent.availableRulesBase
    val selectedRuleBase: StateFlow<DropdownEntryWithValue<Rules>?> = parent.selectedRulesBase
    val selectedWeatherTable = MutableStateFlow<DropdownEntryWithValue<WeatherTable>?>(null)
    val selectedPrayersToNuffleTable = MutableStateFlow<DropdownEntryWithValue<PrayersToNuffleTable>?>(null)
    val selectedKickOffTable = MutableStateFlow<DropdownEntryWithValue<KickOffTable>?>(null)
    val selectedInjuryTable = MutableStateFlow<DropdownEntryWithValue<InjuryTable>?>(null)
    val selectedStuntyInjuryTable = MutableStateFlow<DropdownEntryWithValue<InjuryTable>?>(null)
    val selectedCasualtyTable = MutableStateFlow<DropdownEntryWithValue<CasualtyTable>?>(null)
    val selectedLastingInjuryTable = MutableStateFlow<DropdownEntryWithValue<LastingInjuryTable>?>(null)
    val selectedArgueTheCallTable = MutableStateFlow<DropdownEntryWithValue<ArgueTheCallTable>?>(null)
    val selectedUnusualBall = MutableStateFlow< DropdownEntryWithValue<BallSelectorRule>?>(null)
    val selectedPitch = MutableStateFlow<DropdownEntryWithValue<PitchType>?>(null)
    val selectedStadium = MutableStateFlow< DropdownEntryWithValue<StadiumRule>?>(null)
    val prayersToNuffle = MutableStateFlow(true)
    val matchEvents = MutableStateFlow(false)
    val extraTime = MutableStateFlow(false)

    init {
        updateRulesBuilder(rulesBuilder)
    }

    fun updateRulesBase(entry: DropdownEntryWithValue<Rules>) {
        // GameConfigurationContainerComponentModel will call back into this model and
        // update whatever is relevant
        parent.updateRulesBase(entry)
    }

    fun updateWeatherTable(entry: DropdownEntryWithValue<WeatherTable>) {
        selectedWeatherTable.value = entry
        rulesBuilder.weatherTable = entry.value
    }

    fun updatePrayersToNuffleTable(entry: DropdownEntryWithValue<PrayersToNuffleTable>) {
        selectedPrayersToNuffleTable.value = entry
        rulesBuilder.prayersToNuffleTable = entry.value
    }

    fun updateKickoffTable(entry: DropdownEntryWithValue<KickOffTable>) {
        selectedKickOffTable.value = entry
        rulesBuilder.kickOffEventTable = entry.value
    }

    fun updateInjuryTable(entry: DropdownEntryWithValue<InjuryTable>) {
        selectedInjuryTable.value = entry
        rulesBuilder.injuryTable = entry.value
    }

    fun updateStuntyInjuryTable(entry: DropdownEntryWithValue<InjuryTable>) {
        selectedStuntyInjuryTable.value = entry
        rulesBuilder.stuntyInjuryTable = entry.value
    }

    fun updateCasualtyTable(entry: DropdownEntryWithValue<CasualtyTable>) {
        selectedCasualtyTable.value = entry
        rulesBuilder.casualtyTable = entry.value
    }

    fun updateLastingInjuryTable(entry: DropdownEntryWithValue<LastingInjuryTable>) {
        selectedLastingInjuryTable.value = entry
        rulesBuilder.lastingInjuryTable = entry.value
    }

    fun updateArgueTheCallTable(entry: DropdownEntryWithValue<ArgueTheCallTable>) {
        selectedArgueTheCallTable.value = entry
        rulesBuilder.argueTheCallTable = entry.value
    }

    fun updateUnusualBall(entry: DropdownEntryWithValue<BallSelectorRule>) {
        selectedUnusualBall.value = entry
        rulesBuilder.ballSelectorRule = entry.value
    }

    fun updatePitch(entry: DropdownEntryWithValue<PitchType>) {
        selectedPitch.value = entry
        rulesBuilder.pitchType = entry.value
    }

    fun updateStadium(entry: DropdownEntryWithValue<StadiumRule>) {
        selectedStadium.value = entry
        rulesBuilder.stadium = entry.value
    }

    fun updatePrayersToNuffle(value: Boolean) {
        prayersToNuffle.value = value
        rulesBuilder.prayersToNuffleEnabled = value
    }

    fun updateMatchEvents(value: Boolean) {
        matchEvents.value = value
        rulesBuilder.matchEventsEnabled = value
    }

    fun updateExtraTime(value: Boolean) {
        extraTime.value = value
        rulesBuilder.hasExtraTime = value
    }

    // Rules Preset has been changed, reset all current configuration to match the new rules package
    fun updateRulesBuilder(ruleBuilder: Rules.Builder) {
        rulesBuilder = ruleBuilder
        updateWeatherTable(weatherTables.findEntry(rulesBuilder.weatherTable))
        updatePrayersToNuffleTable(prayersToNuffleTables.findEntry(rulesBuilder.prayersToNuffleTable))
        updateKickoffTable(kickOffTables.findEntry(rulesBuilder.kickOffEventTable))
        updateInjuryTable(injuryTables.findEntry(rulesBuilder.injuryTable))
        updateStuntyInjuryTable(stuntyInjuryTables.findEntry(rulesBuilder.stuntyInjuryTable))
        updateCasualtyTable(casualtyTables.findEntry(rulesBuilder.casualtyTable))
        updateLastingInjuryTable(lastingInjuryTables.findEntry(rulesBuilder.lastingInjuryTable))
        updateArgueTheCallTable(argueTheCallTables.findEntry(rulesBuilder.argueTheCallTable))
        updateUnusualBall(unusualBallList.findEntry(rulesBuilder.ballSelectorRule))
        updatePitch(pitches.findEntry(rulesBuilder.pitchType))
        updateStadium(stadia.findEntry(rulesBuilder.stadium))
        updatePrayersToNuffle(rulesBuilder.prayersToNuffleEnabled)
        updateMatchEvents(rulesBuilder.matchEventsEnabled)
        updateExtraTime(rulesBuilder.hasExtraTime)
    }
}
