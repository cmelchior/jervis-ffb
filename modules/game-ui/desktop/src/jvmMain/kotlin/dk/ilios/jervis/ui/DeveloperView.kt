package dk.ilios.jervis.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val windowState = rememberWindowState()
    Window(onCloseRequest = ::exitApplication, state = windowState) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Content()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Content() {

}

