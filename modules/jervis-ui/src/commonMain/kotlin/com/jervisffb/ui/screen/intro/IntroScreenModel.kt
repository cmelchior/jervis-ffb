package com.jervisffb.ui.screen.intro

import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.ui.BuildConfig
import com.jervisffb.ui.screen.fumbbl.FumbblScreen
import com.jervisffb.ui.screen.fumbbl.FumbblScreenModel
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.launch

/**
 * ViewModel class for the Main starting screen.
 */
class IntroScreenModel(private val menuViewModel: MenuViewModel) : com.jervisffb.ui.screen.JervisScreenModel {

    fun gotoFumbblScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = FumbblScreenModel(menuViewModel)
            screenModel.initialize()
            navigator.push(FumbblScreen(menuViewModel, screenModel))
        }
    }

    fun gotoStandAloneScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = _root_ide_package_.com.jervisffb.ui.screen.StandAloneScreenModel(menuViewModel)
            navigator.push(_root_ide_package_.com.jervisffb.ui.screen.StandAloneScreen(menuViewModel, screenModel))
        }

    }

    fun gotoDevModeScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = _root_ide_package_.com.jervisffb.ui.screen.DevScreenModel(menuViewModel)
            navigator.push(_root_ide_package_.com.jervisffb.ui.screen.DevScreen(menuViewModel, screenModel))
        }
    }

    val clientVersion: String = BuildConfig.releaseVersion
}
