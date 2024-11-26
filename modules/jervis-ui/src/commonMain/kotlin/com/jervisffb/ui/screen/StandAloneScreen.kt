@file:OptIn(
    org.jetbrains.compose.resources.InternalResourceApi::class,
    org.jetbrains.compose.resources.ExperimentalResourceApi::class,
)
package com.jervisffb.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StandAloneScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {
    fun startAiGame(navigator: Navigator, mode: GameMode) {
        GlobalScope.launch {
            val screenModel = GameScreenModel(mode, menuViewModel)
            screenModel.initialize()
            navigator.push(GameScreen(screenModel))
        }
    }

    fun startHotSeatGame(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = GameScreenModel(Manual, menuViewModel)
            screenModel.initialize()
            navigator.push(GameScreen(screenModel))
        }
    }

    fun startP2PServer(navigator: Navigator) {
        menuViewModel.navigatorContext.launch {
            val screenModel = P2PServerScreenModel(menuViewModel)
            navigator.push(P2PServerScreen(menuViewModel, screenModel))
        }
    }

    fun startP2PClient(navigator: Navigator) {


    }
}

class StandAloneScreen(private val menuViewModel: MenuViewModel, screenModel: StandAloneScreenModel) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { StandAloneScreenModel(menuViewModel) }
            MenuScreen {
                Row(
                    modifier = Modifier.fillMaxHeight(0.5f).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    MenuBox(
                        label = "P2P\nServer",
                        onClick = { screenModel.startP2PServer(navigator) },
                    )
                    MenuBox(
                        label = "P2P\nClient",
                        onClick = { screenModel.startP2PClient(navigator) },
                    )
                    MenuBox(
                        label = "Hotseat",
                        onClick = { screenModel.startHotSeatGame(navigator) },
                    )
                    MenuBox(
                        label = "vs. AI",
                        onClick = { /* */ },
                        enabled = false
                    )
                }
            }
    }

    @Composable
    private fun RowScope.MenuBox(label: String, onClick: () -> Unit, enabled: Boolean = true) {

        var modifier = Modifier
            .padding(24.dp)
            .fillMaxHeight()
            .weight(1f,  false)
            .aspectRatio(1f)

        if (enabled) {
            modifier = modifier.background(color = Color.Red).clickable { onClick() }
        } else {
            modifier = modifier.background(color = Color.Gray)
        }

        Box(
            modifier = modifier,
            contentAlignment = Alignment.BottomEnd,

        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = label,
                textAlign = TextAlign.End,
                maxLines = 2,
//                overflow = TextOverflow.Ellipsis,
                color = Color.White,
                fontSize = 48.sp,
            )
        }
    }
}
