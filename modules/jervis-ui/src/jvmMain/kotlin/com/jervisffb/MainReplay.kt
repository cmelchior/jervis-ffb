package com.jervisffb

import androidx.compose.ui.window.application
import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.GameController
import com.jervisffb.engine.rules.StandardBB2020Rules
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

fun main() =
    application {
        val actionRequestChannel =
            Channel<Pair<GameController, List<ActionDescriptor>>>(
                capacity = 2,
                onBufferOverflow = BufferOverflow.SUSPEND,
            )
        val actionSelectedChannel = Channel<GameAction>(capacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)
        val rules = StandardBB2020Rules
//    val fumbbl = FumbblReplay(actionRequestChannel, actionSelectedChannel)
//    runBlocking {
//        fumbbl.loadCommands()
//    }
//    val game = fumbbl.getGame()
//    val controller = GameController(rules, game, fumbbl.getActionProvider())
//    Window(onCloseRequest = ::exitApplication) {
//        App(controller, actionRequestChannel, actionSelectedChannel)
//    }
    }
