package com.jervisffb

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.ui.App
import com.jervisffb.ui.viewmodel.MenuViewModel
import com.jervisffb.engine.utils.createDefaultGameState
import com.jervisffb.engine.utils.createRandomAction
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

@Composable
@Preview
fun AppPreview() {
    val rules = StandardBB2020Rules
    val state = createDefaultGameState(rules)
    val actionRequestChannel =
        Channel<Pair<GameController, List<ActionDescriptor>>>(capacity = 1, onBufferOverflow = BufferOverflow.SUSPEND)
    val actionSelectedChannel = Channel<GameAction>(1, onBufferOverflow = BufferOverflow.SUSPEND)
    val actionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
        createRandomAction(controller.state, availableActions)
    }
//    val controller = GameController(rules, state, actionProvider)
    val controller = GameController(rules, state)
    App(MenuViewModel()) // controller, actionRequestChannel, actionSelectedChannel)
}
