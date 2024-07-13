package dk.ilios.jervis.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dk.ilios.jervis.fumbbl.FumbblReplayAdapter
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath

fun main() = application {
    val fumbbl = FumbblReplayAdapter("../../../replays/game-1624379.json".toPath())
    runBlocking {
        fumbbl.loadCommands()
    }
    val menuViewModel = MenuViewModel()
    val windowState = rememberWindowState()
    Window(onCloseRequest = ::exitApplication, state = windowState) {
        WindowMenuBar(menuViewModel)
        App(menuViewModel)
    }
}
