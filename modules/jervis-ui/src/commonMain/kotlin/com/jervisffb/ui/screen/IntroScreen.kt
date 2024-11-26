@file:OptIn(
    org.jetbrains.compose.resources.InternalResourceApi::class,
    org.jetbrains.compose.resources.ExperimentalResourceApi::class,
)
package com.jervisffb.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.ui.BuildConfig
import com.jervisffb.ui.view.MenuBox
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.launch

class IntroScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    fun gotoFumbblScreen(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = FumbblScreenModel(menuViewModel)
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

class IntroScreen(private val menuViewModel: MenuViewModel) : Screen {

    override val key: ScreenKey = "IntroScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { IntroScreenModel(menuViewModel) }
            MenuScreen {
                Row(
                    modifier = Modifier.fillMaxHeight(0.5f).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MenuBox(
                        label = "FUMBBL",
                        onClick = { screenModel.gotoFumbblScreen(navigator) },
                    )
                    MenuBox(
                        label = "Standalone",
                        onClick = { screenModel.gotoStandAloneScreen(navigator) },
                    )
                    MenuBox(
                        label = "Dev Mode",
                        onClick = { screenModel.gotoDevModeScreen(navigator) },
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(8.dp)
                        .align(Alignment.BottomStart),
                ) {
                    Text(
                        text = screenModel.clientVersion,
                        color = Color.White,
                        style = LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(2f, 2f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
            }
    }


}
