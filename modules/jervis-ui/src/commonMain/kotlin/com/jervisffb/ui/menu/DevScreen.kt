@file:OptIn(
    InternalResourceApi::class,
    ExperimentalResourceApi::class,
)

package com.jervisffb.ui.menu

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
import com.jervisffb.ui.game.view.filePicker
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.utils.isRegularFile
import com.jervisffb.utils.platformFileSystem
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi

// Which teams should be allowed to create actions this client
enum class TeamActionMode {
    HOME_TEAM,
    AWAY_TEAM,
    ALL_TEAMS,
    NONE,
}

sealed interface GameMode
data object Random : GameMode
data class Manual(val actionMode: TeamActionMode) : GameMode
data class Replay(val file: Path) : GameMode

class DevScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {
    fun start(navigator: Navigator, mode: GameMode) {
        menuViewModel.navigatorContext.launch {
            val screenModel = GameScreenModel(null, null, mode, menuViewModel)
            screenModel.initialize()
            navigator.push(GameScreen(screenModel))
        }
    }

    fun loadGame(navigator: Navigator, file: Path) {
        menuViewModel.navigatorContext.launch {
            TODO()
//            val data = JervisSerialization.loadFromFile(file)
//            val runner = HotSeatGameRunner(data.game.rules, data.homeTeam, data.awayTeam)
//            val screenModel = GameScreenModel(
//                null,
//                null,
//                Manual,
//                menuViewModel,
//                runner,
//                data.actions
//            )
//            screenModel.initialize()
//            navigator.push(GameScreen(screenModel))
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


class DevScreen(private val menuViewModel: MenuViewModel, screenModel: DevScreenModel) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { DevScreenModel(menuViewModel) }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = {
                screenModel.start(navigator, Manual(TeamActionMode.ALL_TEAMS))
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
