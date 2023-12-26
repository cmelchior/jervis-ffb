package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.ui.model.ActionSelectorViewModel
import dk.ilios.jervis.ui.model.FieldViewModel
import dk.ilios.jervis.ui.model.GameStatusViewModel
import dk.ilios.jervis.ui.model.LogViewModel
import dk.ilios.jervis.ui.model.ReplayViewModel
import dk.ilios.jervis.ui.model.SidebarViewModel
import dk.ilios.jervis.controller.GameController
import kotlinx.coroutines.channels.Channel

@Composable
fun App(
    controller: GameController,
    actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
    actionSelectedChannel: Channel<Action>
) {
    Screen(
        FieldViewModel(controller.state.field),
        SidebarViewModel(controller.state.homeTeam),
        SidebarViewModel(controller.state.awayTeam),
        GameStatusViewModel(controller),
        ReplayViewModel(controller),
        ActionSelectorViewModel(controller, actionRequestChannel, actionSelectedChannel),
        LogViewModel(controller),
    )
}
