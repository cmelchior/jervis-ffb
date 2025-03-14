package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.rules.Rules
import com.jervisffb.ui.game.viewmodel.MenuViewModel
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

/**
 * This component is the main responsible for coordinating all aspects of configuring the rules
 * for a game. This includes swi
 */
class GameConfigurationContainerComponentModel(rulesBuilder: Rules.Builder, private val menuViewModel: MenuViewModel) : ScreenModel {

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

    // TODO Changing between Fumbbl and Strict should toggle this
    var rulesBuilder: Rules.Builder = rulesBuilder

    // Component models responsible for configuring a new game
    val rulesModel = RulesSetupComponentModel(this@GameConfigurationContainerComponentModel.rulesBuilder, menuViewModel)
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

    fun updateSelectGameType(tabIndex: Int) {
        selectedGameTab.value = tabIndex
    }

    /**
     * Returns the ruleset used for this game
     */
    fun createRules(): Rules {
        return this@GameConfigurationContainerComponentModel.rulesBuilder.build()
    }

    fun updateRulesPreset(rules: Rules.Builder) {
        this@GameConfigurationContainerComponentModel.rulesBuilder = rules // Update to new
        // LoadFileComponentModel does not care about preset updates, so is ignored here
        rulesModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
        timersModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
        inducementsModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
        customizationsModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
    }
}
