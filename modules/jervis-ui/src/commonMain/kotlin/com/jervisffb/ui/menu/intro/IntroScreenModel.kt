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
            val viewModel = FumbblScreenModel(menuViewModel)
            viewModel.initialize()
            navigator.push(FumbblScreen(menuViewModel, viewModel))
        }
    }

    fun gotoStandAloneScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val viewModel = StandAloneScreenModel(menuViewModel)
            navigator.push(StandAloneScreen(menuViewModel, viewModel))
        }

    }

    fun gotoDevModeScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val viewModel = DevScreenModel(menuViewModel)
            navigator.push(DevScreen(menuViewModel, viewModel))
        }
    }

    val clientVersion: String = BuildConfig.releaseVersion
}
