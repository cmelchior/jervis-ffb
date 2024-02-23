package dk.ilios.jervis.ui

import androidx.compose.foundation.layout.*
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
import java.io.File

sealed interface GameMode
data object Random: GameMode
data object Manual: GameMode
data class Replay(val file: File): GameMode

class IntroScreenModel: ScreenModel {

    fun start(navigator: Navigator, mode: GameMode) {
        navigator.push(GameScreen(GameScreenModel(mode)))
    }

    val availableReplayFiles: List<File>
        get() {
            val dir = File("/Users/christian.melchior/Private/jervis-bloodbowl-agent/replays-fumbbl")
            return dir.listFiles()!!.toList().filter { it.isFile }.filter { it.name.endsWith(".json") }
        }
}

class IntroScreen: Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { IntroScreenModel() }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                screenModel.start(navigator, Manual)
            }) {
                Text(
                    text = "Start game with manual actions",
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                screenModel.start(navigator, Random)
            }) {
                Text(
                    text = "Start game with random actions",
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            screenModel.availableReplayFiles.forEach { file ->
                Button(onClick = {
                    screenModel.start(navigator, Replay(file))
                }) {
                    Text(
                        text = "Start replay: ${file.name}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}