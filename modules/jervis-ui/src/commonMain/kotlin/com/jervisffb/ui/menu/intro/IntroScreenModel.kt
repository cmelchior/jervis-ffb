package com.jervisffb.ui.menu.intro

import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.ui.BuildConfig
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.DevScreen
import com.jervisffb.ui.menu.DevScreenModel
import com.jervisffb.ui.menu.JervisScreenModel
import com.jervisffb.ui.menu.StandAloneScreen
import com.jervisffb.ui.menu.StandAloneScreenModel
import com.jervisffb.ui.menu.fumbbl.FumbblScreen
import com.jervisffb.ui.menu.fumbbl.FumbblScreenModel
import kotlinx.coroutines.launch

/**
 * ViewModel class for the Main starting screen.
 */
class IntroScreenModel(private val menuViewModel: MenuViewModel) : JervisScreenModel {

    fun gotoFumbblScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = FumbblScreenModel(menuViewModel)
            screenModel.initialize()
            navigator.push(FumbblScreen(menuViewModel, screenModel))
        }
    }

    fun gotoStandAloneScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = StandAloneScreenModel(menuViewModel)
            navigator.push(StandAloneScreen(menuViewModel, screenModel))
        }

    }

    fun gotoDevModeScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = DevScreenModel(menuViewModel)
            navigator.push(DevScreen(menuViewModel, screenModel))
        }
    }

    val clientVersion: String = BuildConfig.releaseVersion
}
