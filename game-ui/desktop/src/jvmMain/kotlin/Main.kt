import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dk.ilios.bloodbowl.ui.common.App


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
