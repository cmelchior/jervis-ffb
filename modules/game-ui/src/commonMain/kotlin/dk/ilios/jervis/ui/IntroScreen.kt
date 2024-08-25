package dk.ilios.jervis.ui

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
import dk.ilios.jervis.serialize.JervisSerialization
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import dk.ilios.jervis.utils.isRegularFile
import dk.ilios.jervis.utils.platformFileSystem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toPath

sealed interface GameMode

data object Random : GameMode

data object Manual : GameMode

data class Replay(val file: Path) : GameMode

class IntroScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {
    fun start(
        navigator: Navigator,
        mode: GameMode,
    ) {
        GlobalScope.launch {
            val screenModel = GameScreenModel(mode, menuViewModel)
            screenModel.initialize()
            navigator.push(GameScreen(screenModel, emptyList()))
        }
    }

    fun loadGame(
        navigator: Navigator,
        file: Path,
    ) {
        GlobalScope.launch {
            val (controller, actions) = JervisSerialization.loadFromFile(file)
            val screenModel = GameScreenModel(Manual, menuViewModel, controller)
            screenModel.initialize()
            navigator.push(GameScreen(screenModel, actions))
        }
    }

    val availableReplayFiles: List<Path>
        get() {
            val dir = "/Users/christian.melchior/Private/jervis-bloodbowl-agent/replays-fumbbl".toPath()
            return if (!platformFileSystem.exists(dir)) {
                emptyList()
            } else {
                platformFileSystem.list(dir)
                    .filter { it.isRegularFile }
                    .filter { it.name.endsWith(".json") }
            }
        }
}

class IntroScreen(private val menuViewModel: MenuViewModel) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { IntroScreenModel(menuViewModel) }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                screenModel.start(navigator, Manual)
            }) {
                Text(
                    text = "Start game with manual actions",
                    modifier = Modifier.padding(16.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                screenModel.start(navigator, Random)
            }) {
                Text(
                    text = "Start game with random actions",
                    modifier = Modifier.padding(16.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                filePicker(
                    "Load Jervis game file",
                    null,
                    "Jervis game file (*.jrvs)",
                    "jrvs",
                ) { filePath ->
                    screenModel.loadGame(navigator, filePath)
                }
            }) {
                Text(
                    text = "Load game",
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
