package com.jervisffb.ui.screen.hotseat

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.ui.screen.components.setup.SetupGameComponentModel
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * View model for controlling the "Setup Game" screen, that is the 1st step in the "Hotseat Game" flow.
 */
class SetupHotseatGameScreenModel(private val menuViewModel: MenuViewModel, private val parentModel: HotseatScreenModel) : ScreenModel {
    val setupGameModel = SetupGameComponentModel(menuViewModel)
    val isSetupValid: StateFlow<Boolean> = setupGameModel.isSetupValid

    fun gameSetupDone() {
        parentModel.gameSetupDone()
    }





}
