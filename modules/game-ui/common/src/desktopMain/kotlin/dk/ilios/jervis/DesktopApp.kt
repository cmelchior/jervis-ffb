package dk.ilios.jervis

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.ui.App
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import dk.ilios.jervis.utils.createDefaultGameState
import dk.ilios.jervis.utils.createRandomAction
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

@Preview
@Composable
fun AppPreview() {
    val rules = BB2020Rules
    val state = createDefaultGameState(rules)
    val actionRequestChannel = Channel<Pair<GameController, List<ActionDescriptor>>>(capacity = 1, onBufferOverflow = BufferOverflow.SUSPEND)
    val actionSelectedChannel = Channel<GameAction>(1, onBufferOverflow = BufferOverflow.SUSPEND)
    val actionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
        createRandomAction(controller.state, availableActions)
    }
//    val controller = GameController(rules, state, actionProvider)
    val controller = GameController(rules, state)
    App(MenuViewModel()) //controller, actionRequestChannel, actionSelectedChannel)
}