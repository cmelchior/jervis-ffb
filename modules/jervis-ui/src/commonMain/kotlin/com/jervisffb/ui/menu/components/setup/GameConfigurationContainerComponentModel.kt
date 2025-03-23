package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.rules.FumbblBB2020Rules
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
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

private val defaultRulesBaseList = listOf<DropdownEntryWithValue<Rules>>(
    DropdownEntryWithValue("Blood Bowl 2020 Rules (Strict)", StandardBB2020Rules()),
    DropdownEntryWithValue("Fumbbl Compatible BB2020 Rules", FumbblBB2020Rules()),
)

/**
 * This component is the main responsible for coordinating all aspects of configuring the rules
 * for a game. This includes swi
 */
class GameConfigurationContainerComponentModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    val selectedGameTab: MutableStateFlow<Int> = MutableStateFlow(1)

    // Configure the tab layout
    val tabs = listOf(
        GameTab(
            tabName = "Continue From File",
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
            enabled = false,
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
    // all the other components also depends on this information, so keep the toggle here as well.
    // We need to initialize the default values here to avoid some annoying lifecycle issues getting
    // the rulesBuilder to all sub models.
    val availableRulesBase = MutableStateFlow(defaultRulesBaseList)
    val selectedRulesBase = MutableStateFlow(availableRulesBase.value.first())
    var rulesBuilder: Rules.Builder = selectedRulesBase.value!!.value.toBuilder()

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
        timersModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
        inducementsModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
        customizationsModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
    }


    fun updateSelectGameType(tabIndex: Int) {
        selectedGameTab.value = tabIndex
    }

    /**
     * Returns the ruleset used for this game
     */
    fun createRules(): Rules {
        return this@GameConfigurationContainerComponentModel.rulesBuilder.build()
    }
}
