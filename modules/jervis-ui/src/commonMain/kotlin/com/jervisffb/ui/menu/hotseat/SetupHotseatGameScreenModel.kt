package com.jervisffb.ui.menu.hotseat

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.setup.GameConfigurationContainerComponentModel

/**
 * View model for controlling the "Setup Game" screen, that is the 1st step in the "Hotseat Game" flow.
 */
class SetupHotseatGameScreenModel(private val menuViewModel: MenuViewModel, private val parentModel: HotseatScreenModel) : ScreenModel {
    val rules = StandardBB2020Rules().toBuilder()
    val gameConfigModel = GameConfigurationContainerComponentModel(rules, menuViewModel)

    fun createRules(): Rules {
        return gameConfigModel.createRules()
    }

    fun gameSetupDone() {
        parentModel.gameSetupDone()
    }
}

