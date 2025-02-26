package com.jervisffb.ui.menu.hotseat

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.setup.SetupGameComponentModel
import com.jervisffb.ui.menu.components.setup.SetupTimersComponentModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * View model for controlling the "Setup Game" screen, that is the 1st step in the "Hotseat Game" flow.
 */
class SetupHotseatGameScreenModel(private val menuViewModel: MenuViewModel, private val parentModel: HotseatScreenModel) : ScreenModel {
    val setupGameModel = SetupGameComponentModel(menuViewModel)
    val setupTimersModel = SetupTimersComponentModel(menuViewModel)
    val isSetupValid: Flow<Boolean> = setupGameModel.isSetupValid.combine(setupTimersModel.isSetupValid) {
        isSetupValid, timersValid -> isSetupValid && timersValid
    }

    fun gameSetupDone() {
        parentModel.gameSetupDone()
    }





}

