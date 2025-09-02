package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.rules.BB72020Rules
import com.jervisffb.engine.rules.FumbblBB2020Rules
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.StandardBB2025Rules
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.utils.DropdownEntryWithValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

enum class ConfigType {
    FROM_FILE,
    STANDARD,
    BB7,
    DUNGEON_BOWL,
    GUTTER_BOWL,
}

enum class SetupTabType {
    LOAD_FILE,
    RULES,
    MAP,
    TIMERS,
    INDUCEMENTS,
    CUSTOMIZATIONS,
}

data class GameTab(
    val tabName: String,
    val type: ConfigType,
    val enabled: Boolean,
    val showSetupTabs: Boolean,
    val tabs: List<SetupTabDescription>,
)

data class SetupTabDescription(
    val name: String,
    val type: SetupTabType,
)

private val standardRulesBaseList = listOf<DropdownEntryWithValue<Rules>>(
    DropdownEntryWithValue("Blood Bowl 2020 Rules (Strict)", StandardBB2020Rules()),
    DropdownEntryWithValue("Blood Bowl 2020 Rules (FUMBBL Compatible)", FumbblBB2020Rules()),
    DropdownEntryWithValue("Blood Bowl 2020 Rules (Dev Settings)", StandardBB2020Rules().toBuilder().run {
        diceRollsOwner = DiceRollOwner.ROLL_ON_CLIENT
        undoActionBehavior = UndoActionBehavior.ALLOWED
        build()
    }),
    DropdownEntryWithValue("Blood Bowl 2025 Rules (Strict)", StandardBB2025Rules()),
)

private val bb7RulesBaseList = listOf<DropdownEntryWithValue<Rules>>(
    DropdownEntryWithValue("Blood Bowl Sevens 2020 Rules", BB72020Rules()),
    DropdownEntryWithValue("Blood Bowl Sevens 2020 Rules (Dev Settings)", BB72020Rules().toBuilder().run {
        diceRollsOwner = DiceRollOwner.ROLL_ON_CLIENT
        undoActionBehavior = UndoActionBehavior.ALLOWED
        build()
    }),
)

/**
 * This component is the main responsible for coordinating all aspects of configuring the rules
 * for a game.
 */
class GameConfigurationContainerComponentModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    val selectedGameTab: MutableStateFlow<Int> = MutableStateFlow(1)

    // Configure the tab layout
    val tabs = listOf(
        GameTab(
            tabName = "Save File",
            type = ConfigType.FROM_FILE,
            enabled = true,
            showSetupTabs = false,
            tabs = listOf(
                SetupTabDescription("Load File", SetupTabType.LOAD_FILE)
            )
        ),
        GameTab(
            tabName = "Standard",
            type = ConfigType.STANDARD,
            enabled = true,
            showSetupTabs = true,
            tabs = listOf(
                SetupTabDescription("Rules", SetupTabType.RULES),
                SetupTabDescription("Timers", SetupTabType.TIMERS),
                SetupTabDescription("Inducements", SetupTabType.INDUCEMENTS),
                SetupTabDescription("Customization", SetupTabType.CUSTOMIZATIONS),
            )
        ),
        GameTab(
            tabName = "BB7",
            type = ConfigType.BB7,
            enabled = true,
            showSetupTabs = true,
            tabs = listOf(
                SetupTabDescription("Rules", SetupTabType.RULES),
                SetupTabDescription("Timers", SetupTabType.TIMERS),
                SetupTabDescription("Inducements", SetupTabType.INDUCEMENTS),
                SetupTabDescription("Customization", SetupTabType.CUSTOMIZATIONS),
            )
        ),
        GameTab(
            tabName = "Dungeon Bowl",
            type = ConfigType.DUNGEON_BOWL,
            enabled = false,
            showSetupTabs = true,
            tabs = listOf(
                SetupTabDescription("Rules", SetupTabType.RULES),
                SetupTabDescription("Map", SetupTabType.MAP),
                SetupTabDescription("Timers", SetupTabType.TIMERS),
                SetupTabDescription("Inducements", SetupTabType.INDUCEMENTS),
                SetupTabDescription("Customization", SetupTabType.CUSTOMIZATIONS),
            )
        ),
        GameTab(
            tabName = "Gutter Bowl",
            type = ConfigType.GUTTER_BOWL,
            enabled = false,
            showSetupTabs = true,
            tabs = listOf(
                SetupTabDescription("Rules", SetupTabType.RULES),
                SetupTabDescription("Map", SetupTabType.MAP),
                SetupTabDescription("Timers", SetupTabType.TIMERS),
                SetupTabDescription("Inducements", SetupTabType.INDUCEMENTS),
                SetupTabDescription("Customization", SetupTabType.CUSTOMIZATIONS),
            )
        ),
    )


    // Expose which "Rules Base" is selected. While technically under the "Rules" component,
    // all the other components also depend on this information, so keep the toggle here as well.
    // We need to initialize the default values here to avoid some annoying lifecycle issues getting
    // the rulesBuilder to all submodels.
    val availableRulesBase = MutableStateFlow(standardRulesBaseList)
    val selectedRulesBase = MutableStateFlow(availableRulesBase.value.first())
    var rulesBuilder: Rules.Builder = selectedRulesBase.value.value.toBuilder()

    // Component models responsible for configuring a new game
    val rulesModel = RulesSetupComponentModel(this@GameConfigurationContainerComponentModel.rulesBuilder, this, menuViewModel)
    val timersModel = SetupTimersComponentModel(this@GameConfigurationContainerComponentModel.rulesBuilder, menuViewModel)
    val inducementsModel = InducementsSetupComponentModel(this@GameConfigurationContainerComponentModel.rulesBuilder, menuViewModel)
    val customizationsModel = CustomizationSetupComponentModel(this@GameConfigurationContainerComponentModel.rulesBuilder, menuViewModel)

    // Component models responsible for loading a previous game
    val loadFileModel = LoadFileComponentModel(this@GameConfigurationContainerComponentModel.rulesBuilder, menuViewModel)

    private val isManualSetupValid: Flow<Boolean> = combine(
        rulesModel.isSetupValid,
        timersModel.isSetupValid,
        inducementsModel.isSetupValid,
        customizationsModel.isSetupValid,
    ) { isSetupValid, timersValid, inducementsValid, customizationsValid ->
        isSetupValid && timersValid && inducementsValid && customizationsValid
    }
    private val isLoadSetupValid: StateFlow<Boolean> = loadFileModel.isSetupValid

    // Flow that determines whether the setup is valid, so
    val isSetupValid: Flow<Boolean> = combine(
        selectedGameTab,
        isManualSetupValid,
        isLoadSetupValid
    ) { selectedGameTab: Int, isManualSetupValid: Boolean, isLoadSetupValid: Boolean ->
        if (tabs[selectedGameTab].type == ConfigType.FROM_FILE) {
            isLoadSetupValid
        } else {
            isManualSetupValid
        }
    }

    init {
        // TODO Add support for loading (and saving) custom rule sets
    }

    fun updateRulesBase(entry: DropdownEntryWithValue<Rules>) {
        selectedRulesBase.value = entry
        rulesBuilder = entry.value.toBuilder()
        // LoadFileComponentModel does not care about preset updates, so is ignored here
        rulesModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
        timersModel.updateFromRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
        inducementsModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
        customizationsModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
    }


    fun updateSelectGameType(tabIndex: Int) {
        when (tabs[tabIndex].type) {
            ConfigType.FROM_FILE -> { /* Do nothing */ }
            ConfigType.STANDARD -> {
                availableRulesBase.value = standardRulesBaseList
                updateRulesBase(availableRulesBase.value.first())
            }
            ConfigType.BB7 -> {
                availableRulesBase.value = bb7RulesBaseList
                updateRulesBase(availableRulesBase.value.first())
            }
            ConfigType.DUNGEON_BOWL -> TODO("DUNGEON_BOWL not supported yet")
            ConfigType.GUTTER_BOWL -> TODO("GUTTER_BOWL not supported yet")
        }
        selectedGameTab.value = tabIndex
    }

    /**
     * Returns the ruleset used for this game
     */
    fun createRules(): Rules {
        return this@GameConfigurationContainerComponentModel.rulesBuilder.build()
    }
}
