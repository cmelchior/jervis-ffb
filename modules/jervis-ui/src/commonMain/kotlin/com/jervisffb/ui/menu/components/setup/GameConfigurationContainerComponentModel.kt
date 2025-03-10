package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.rules.Rules
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * This component is the main responsible for coordinating all aspects of configuring the rules
 * for a game.
 */
class GameConfigurationContainerComponentModel(rulesBuilder: Rules.Builder, private val menuViewModel: MenuViewModel) : ScreenModel {

    // TODO Changing between Fumbbl and Strict should toggle this
    var rulesBuilder: Rules.Builder = rulesBuilder

    val rulesModel = RulesSetupComponentModel(this@GameConfigurationContainerComponentModel.rulesBuilder, menuViewModel)
    val timersModel = SetupTimersComponentModel(this@GameConfigurationContainerComponentModel.rulesBuilder, menuViewModel)
    val inducementsModel = InducementsSetupComponentModel(this@GameConfigurationContainerComponentModel.rulesBuilder, menuViewModel)
    val customizationsModel = CustomizationSetupComponentModel(this@GameConfigurationContainerComponentModel.rulesBuilder, menuViewModel)

    val isSetupValid: Flow<Boolean> = combine(
        rulesModel.isSetupValid,
        timersModel.isSetupValid,
        inducementsModel.isSetupValid,
        customizationsModel.isSetupValid,
    ) { isSetupValid, timersValid, inducementsValid, customizationsValid ->
        isSetupValid && timersValid && inducementsValid && customizationsValid
    }

    /**
     * Returns the ruleset used for this game
     */
    fun createRules(): Rules {
        return this@GameConfigurationContainerComponentModel.rulesBuilder.build()
    }

    fun updateRulesPreset(rules: Rules.Builder) {
        this@GameConfigurationContainerComponentModel.rulesBuilder = rules // Update to new
        rulesModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
        timersModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
        inducementsModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
        customizationsModel.updateRulesBuilder(this@GameConfigurationContainerComponentModel.rulesBuilder)
    }
}
