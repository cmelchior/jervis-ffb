@file:OptIn(
    org.jetbrains.compose.resources.InternalResourceApi::class,
    org.jetbrains.compose.resources.ExperimentalResourceApi::class,
)
package com.jervisffb.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
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
import com.jervisffb.ui.viewmodel.MenuViewModel
import com.jervisffb.utils.isRegularFile
import com.jervisffb.utils.platformFileSystem
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toPath

class FumbblScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {
    fun start(navigator: Navigator, mode: GameMode) {
        menuViewModel.navigatorContext.launch {
            val screenModel = GameScreenModel(mode, menuViewModel)
            navigator.push(GameScreen(screenModel))
        }
    }

    val availableReplayFiles: List<Path>
        get() {
            val dir = "/Users/christian.melchior/Private/jervis-ffb/replays-fumbbl".toPath()
            return if (!platformFileSystem.exists(dir)) {
                emptyList()
            } else {
                platformFileSystem.list(dir)
                    .filter { it.isRegularFile }
                    .filter { it.name.endsWith(".json") }
            }
        }
}

class FumbblScreen(private val menuViewModel: MenuViewModel, screenModel: FumbblScreenModel) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { FumbblScreenModel(menuViewModel) }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            screenModel.availableReplayFiles.forEach { file ->
                Button(onClick = {
                    screenModel.start(navigator, Replay(file))
                }) {
                    Text(
                        text = "Start replay: ${file.name}",
                        modifier = Modifier.padding(16.dp),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
