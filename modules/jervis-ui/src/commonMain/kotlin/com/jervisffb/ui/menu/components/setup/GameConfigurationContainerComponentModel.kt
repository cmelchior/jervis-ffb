package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * This component is the main responsible for coordinating all aspects of configuring the rules
 * for a game.
 */
class GameConfigurationContainerComponentModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    // TODO Changing between Fumbbl and Strict should toggle this
    var ruleBuilder: Rules.Builder = StandardBB2020Rules().toBuilder()

    val rulesModel = RulesSetupComponentModel(ruleBuilder, menuViewModel)
    val timersModel = SetupTimersComponentModel(ruleBuilder, menuViewModel)
    val inducementsModel = InducementsSetupComponentModel(ruleBuilder, menuViewModel)
    val customizationsModel = CustomizationSetupComponentModel(ruleBuilder, menuViewModel)

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
        return ruleBuilder.build()
    }

    fun updateRulesPreset(rules: Rules.Builder) {
        ruleBuilder = rules // Update to new
        rulesModel.updateRulesBuilder(ruleBuilder)
        timersModel.updateRulesBuilder(ruleBuilder)
        inducementsModel.updateRulesBuilder(ruleBuilder)
        customizationsModel.updateRulesBuilder(ruleBuilder)
    }
}
