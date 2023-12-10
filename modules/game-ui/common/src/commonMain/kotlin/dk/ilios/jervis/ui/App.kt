package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import dk.ilios.jervis.ui.model.ActionSelectorViewModel
import dk.ilios.jervis.ui.model.FieldViewModel
import dk.ilios.jervis.ui.model.GameStatusViewModel
import dk.ilios.jervis.ui.model.LogViewModel
import dk.ilios.jervis.ui.model.ReplayViewModel
import dk.ilios.jervis.ui.model.SidebarViewModel
import dk.ilios.jervis.controller.GameController

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
