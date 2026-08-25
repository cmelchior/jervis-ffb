package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.RulesParameterBuilder
import com.jervisffb.engine.rules.builder.BallSelectorRule
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.builder.StadiumRule
import com.jervisffb.engine.rules.common.tables.ArgueTheCallTable
import com.jervisffb.engine.rules.common.tables.CasualtyTable
import com.jervisffb.engine.rules.common.tables.InjuryTable
import com.jervisffb.engine.rules.common.tables.KickOffTable
import com.jervisffb.engine.rules.common.tables.LastingInjuryTable
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.rules.common.tables.WeatherTable
import com.jervisffb.ui.menu.utils.DropdownEntryWithValue
import com.jervisffb.ui.menu.utils.findEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Should the rules component be on the left or right side of the screen?
enum class RulesSetupColumn {
    LEFT,
    RIGHT,
}

// All configurable rules setting. Needed so the shared UI knows how to render
// it. They are not required to be used by a ruleset. Omitting it just leaves
// the value unmodifiable, so the default value from Rulees is always used.
enum class RulesSetupControl {
    WEATHER_TABLE,
    PRAYERS_TO_NUFFLE_TABLE,
    KICK_OFF_TABLE,
    INJURY_TABLE,
    STUNTY_INJURY_TABLE,
    CASUALTY_TABLE,
    LASTING_INJURY_TABLE,
    ARGUE_THE_CALL_TABLE,
    PITCH,
    BALL,
    STADIUM,
    PRAYERS_TO_NUFFLE_TOGGLE,
    MATCH_EVENTS_TOGGLE,
    EXTRA_TIME_TOGGLE,
}

// A rules section with a header and position
data class RulesSetupSection(
    val title: String,
    val column: RulesSetupColumn,
    val controls: List<RulesSetupControl>,
)

data class RulesSetupConfiguration(
    // Which "main" version of the ruleset to use
    val gameVersion: GameVersion,

    // Ordering controls section ordering within each column.
    // Omit a section or leave it empty to hide it.
    val sections: List<RulesSetupSection>,

    // Values displayed in each available dropdown
    val weatherTables: List<Pair<String, List<DropdownEntryWithValue<WeatherTable>>>>,
    val prayersToNuffleTables: List<Pair<String, List<DropdownEntryWithValue<PrayersToNuffleTable>>>>,
    val kickOffTables: List<Pair<String, List<DropdownEntryWithValue<KickOffTable>>>>,
    val injuryTables: List<Pair<String, List<DropdownEntryWithValue<InjuryTable>>>>,
    val stuntyInjuryTables: List<Pair<String, List<DropdownEntryWithValue<InjuryTable>>>>,
    val casualtyTables: List<Pair<String, List<DropdownEntryWithValue<CasualtyTable>>>>,
    val lastingInjuryTables: List<Pair<String, List<DropdownEntryWithValue<LastingInjuryTable>>>>,
    val argueTheCallTables: List<Pair<String, List<DropdownEntryWithValue<ArgueTheCallTable>>>>,
    val unusualBallList: List<Pair<String, List<DropdownEntryWithValue<BallSelectorRule>>>>,
    val pitches: List<Pair<String, List<DropdownEntryWithValue<PitchType>>>>,
    val stadia: List<Pair<String, List<DropdownEntryWithValue<StadiumRule>>>>,
) {
    init {
        val controls = sections.flatMap { it.controls }
        require(controls.size == controls.toSet().size) {
            "A rules setup control can only appear in one section"
        }
    }
}

// This component is responsible for the shared UI control needed to configure
// the rules of a game. Subclasses  supply ruleset-specific entries and section
// placement.
abstract class RulesSetupComponentModel protected constructor(
    initialRulesBuilder: RulesParameterBuilder,
    private val parent: GameConfigurationContainerComponentModel,
    configuration: RulesSetupConfiguration,
) : ScreenModel {

    var rulesBuilder = initialRulesBuilder

    val gameVersion = configuration.gameVersion
    val sections = configuration.sections
    val weatherTables = configuration.weatherTables
    val prayersToNuffleTables = configuration.prayersToNuffleTables
    val kickOffTables = configuration.kickOffTables
    val injuryTables = configuration.injuryTables
    val stuntyInjuryTables = configuration.stuntyInjuryTables
    val casualtyTables = configuration.casualtyTables
    val lastingInjuryTables = configuration.lastingInjuryTables
    val argueTheCallTables = configuration.argueTheCallTables
    val unusualBallList = configuration.unusualBallList
    val pitches = configuration.pitches
    val stadia = configuration.stadia

    // Currently it isn't possible to put the rules section in an invalid state
    // but keep it here to keep it future-proof.
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
    val selectedUnusualBall = MutableStateFlow<DropdownEntryWithValue<BallSelectorRule>?>(null)
    val selectedPitch = MutableStateFlow<DropdownEntryWithValue<PitchType>?>(null)
    val selectedStadium = MutableStateFlow<DropdownEntryWithValue<StadiumRule>?>(null)
    val prayersToNuffle = MutableStateFlow(true)
    val matchEvents = MutableStateFlow(false)
    val extraTime = MutableStateFlow(false)

    init {
        require(initialRulesBuilder.gameVersion == gameVersion) {
            "Expected a $gameVersion rules builder, but got ${initialRulesBuilder.gameVersion}"
        }
        updateRulesBuilder(rulesBuilder)
    }

    fun updateRulesBase(entry: DropdownEntryWithValue<Rules>) {
        // GameConfigurationContainerComponentModel will create the correct ruleset-specific model.
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
    fun updateRulesBuilder(ruleBuilder: RulesParameterBuilder) {
        require(ruleBuilder.gameVersion == gameVersion) {
            "Cannot load ${ruleBuilder.gameVersion} rules into a $gameVersion setup model"
        }
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

internal fun createRulesSetupComponentModel(
    rulesBuilder: RulesParameterBuilder,
    parent: GameConfigurationContainerComponentModel,
): RulesSetupComponentModel = when (rulesBuilder.gameVersion) {
    GameVersion.BB2020 -> RulesSetupComponentModel2020(rulesBuilder, parent)
    GameVersion.BB2025 -> RulesSetupComponentModel2025(rulesBuilder, parent)
}
