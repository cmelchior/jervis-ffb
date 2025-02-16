@file:OptIn(
    org.jetbrains.compose.resources.InternalResourceApi::class,
    org.jetbrains.compose.resources.ExperimentalResourceApi::class,
)
package com.jervisffb.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.frontpage_ball
import com.jervisffb.ui.screen.p2p.client.P2PClientScreen
import com.jervisffb.ui.screen.p2p.client.P2PClientScreenModel
import com.jervisffb.ui.screen.p2p.host.P2PServerScreen
import com.jervisffb.ui.screen.p2p.host.P2PHostScreenModel
import com.jervisffb.ui.view.MenuBox
import com.jervisffb.ui.view.SplitMenuBox
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StandAloneScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {
    fun startAiGame(navigator: Navigator, mode: GameMode) {
        GlobalScope.launch {
            val screenModel = GameScreenModel(null, null, mode, menuViewModel)
            screenModel.initialize()
            navigator.push(GameScreen(screenModel))
        }
    }

    fun startHotSeatGame(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = GameScreenModel(null, null, Manual, menuViewModel)
            screenModel.initialize()
            navigator.push(GameScreen(screenModel))
        }
    }

    fun startP2PServer(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = P2PHostScreenModel(menuViewModel)
            navigator.push(P2PServerScreen(menuViewModel, screenModel))
        }
    }

    fun startP2PClient(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = P2PClientScreenModel(navigator, menuViewModel)
            navigator.push(P2PClientScreen(menuViewModel, screenModel))
        }
    }
}

class StandAloneScreen(private val menuViewModel: MenuViewModel, screenModel: StandAloneScreenModel) : Screen {
    @Composable
    override fun Content() {
        JervisScreen(menuViewModel) {
            StandaloneScreen(menuViewModel)
        }
    }
}

@Composable
fun Screen.StandaloneScreen(menuViewModel: MenuViewModel) {
    val navigator = LocalNavigator.currentOrThrow
    val screenModel = rememberScreenModel { StandAloneScreenModel(menuViewModel) }
    MenuScreenWithTitle(menuViewModel, "Standalone Games", Res.drawable.frontpage_ball) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).offset(y = -40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SplitMenuBox(
                labelTop = "P2P\nJoin",
                onClickTop = { screenModel.startP2PClient(navigator) },
                labelBottom = "P2P\nHost",
                onClickBottom = { screenModel.startP2PServer(navigator) },
                menuViewModel.p2pHostAvaiable,
            )
            MenuBox(
                label = "Hotseat",
                onClick = { screenModel.startHotSeatGame(navigator) },
            )
            MenuBox(
                label = "vs AI",
                onClick = { /* */ },
            )
            MenuBox(
                label = "Replay",
                onClick = { /* */ },
                enabled = false
            )
        }
    }
}
