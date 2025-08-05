@file:OptIn(
    InternalResourceApi::class,
    ExperimentalResourceApi::class,
)

package com.jervisffb.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_frontpage_krox
import com.jervisffb.ui.game.view.MenuBox
import com.jervisffb.ui.game.view.SplitMenuBox
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.hotseat.HotseatScreen
import com.jervisffb.ui.menu.hotseat.HotseatScreenModel
import com.jervisffb.ui.menu.p2p.client.P2PClientScreen
import com.jervisffb.ui.menu.p2p.client.P2PClientScreenModel
import com.jervisffb.ui.menu.p2p.host.P2PHostScreen
import com.jervisffb.ui.menu.p2p.host.P2PHostScreenModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.imageResource

class StandAloneScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    fun startHotSeatGame(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val viewModel = HotseatScreenModel(navigator, menuViewModel)
            navigator.push(HotseatScreen(menuViewModel, viewModel))
        }
    }

    fun startP2PServer(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val viewModel = P2PHostScreenModel(navigator, menuViewModel)
            navigator.push(P2PHostScreen(menuViewModel, viewModel))
        }
    }

    fun startP2PClient(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val viewModel = P2PClientScreenModel(navigator, menuViewModel)
            navigator.push(P2PClientScreen(menuViewModel, viewModel))
        }
    }
}

class StandAloneScreen(private val menuViewModel: MenuViewModel, viewModel: StandAloneScreenModel) : Screen {
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
    val viewModel = rememberScreenModel { StandAloneScreenModel(menuViewModel) }
    MenuScreenWithTitle(
        menuViewModel,
        title = "Standalone Games",
        pageImage = {
            Image(
                modifier = Modifier.align(Alignment.BottomEnd).fillMaxWidth(0.35f).offset(x = -50.dp, y = 0.dp).scale(1f,1f),
                bitmap = imageResource(Res.drawable.jervis_frontpage_krox),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
            )
        }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Row(
                modifier = Modifier.fillMaxWidth(0.62f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MenuBox(
                    label = "Hotseat",
                    onClick = { viewModel.startHotSeatGame(navigator) },
                    frontPage = true
                )
                Spacer(modifier = Modifier.width(32.dp))
                SplitMenuBox(
                    labelTop = "P2P\nClient",
                    onClickTop = { viewModel.startP2PClient(navigator) },
                    labelMiddle = "P2P\nHost",
                    onClickMiddle = { viewModel.startP2PServer(navigator) },
                    labelBottom = "Replay",
                    onClickBottom = null,
                    menuViewModel.p2pHostAvaiable,
                )
            }
            Box(modifier = Modifier.fillMaxWidth(0.70f)) {

            }
        }
    }
}
