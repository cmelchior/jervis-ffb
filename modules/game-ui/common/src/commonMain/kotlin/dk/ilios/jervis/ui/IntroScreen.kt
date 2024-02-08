package dk.ilios.jervis.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen

class IntroScreenModel: ScreenModel {

}

class IntroScreen: Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { IntroScreenModel() }
        Text("Hello")
    }
}