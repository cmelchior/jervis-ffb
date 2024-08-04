package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dk.ilios.jervis.fumbbl.FumbblReplayAdapter
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath

fun main() =
    application {
        val fumbbl = FumbblReplayAdapter("../../../replays/game-1624379.json".toPath())
        runBlocking {
            fumbbl.loadCommands()
        }
        val menuViewModel = MenuViewModel()

        val windowState =
            rememberWindowState(
                size = DpSize(pixelsToDp(145f + 782f + 145f), pixelsToDp(800f)),
            )
        Window(onCloseRequest = ::exitApplication, state = windowState) {
            WindowMenuBar(menuViewModel)
            App(menuViewModel)
        }
    }

@Composable
fun pixelsToDp(pixels: Float): Dp {
    val density = LocalDensity.current
    return with(density) { (this.density * pixels).toDp() }
}
