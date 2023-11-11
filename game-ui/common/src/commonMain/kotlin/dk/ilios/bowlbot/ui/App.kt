package dk.ilios.bowlbot.ui

import androidx.compose.runtime.Composable
import dk.ilios.bloodbowl.ui.model.ActionSelectorViewModel
import dk.ilios.bloodbowl.ui.model.FieldViewModel
import dk.ilios.bloodbowl.ui.model.GameStatusViewModel
import dk.ilios.bloodbowl.ui.model.LogViewModel
import dk.ilios.bloodbowl.ui.model.ReplayViewModel
import dk.ilios.bloodbowl.ui.model.SidebarViewModel
import dk.ilios.bowlbot.controller.GameController

@Composable
fun App(controller: GameController) {
    Screen(
        FieldViewModel(),
        SidebarViewModel(),
        SidebarViewModel(),
        GameStatusViewModel(controller),
        ReplayViewModel(controller),
        ActionSelectorViewModel(controller),
        LogViewModel(controller),
    )
}
