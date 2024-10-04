package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import dk.ilios.jervis.ui.viewmodel.MenuViewModel

@Composable
fun App(menuViewModel: MenuViewModel) {
    Navigator(IntroScreen(menuViewModel))
}
