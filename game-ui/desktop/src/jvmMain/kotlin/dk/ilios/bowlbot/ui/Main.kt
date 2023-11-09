package dk.ilios.bowlbot.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dk.ilios.bowlbot.actions.ActionDescriptor
import dk.ilios.bowlbot.actions.Continue
import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.model.Player
import dk.ilios.bowlbot.rules.BB2020Rules

fun main() = application {
    val rules = BB2020Rules
    val p1 = Player()
    val p2 = Player()
    val state = Game(p1, p1)
    val actionProvider = { state: Game, availableActions: List<ActionDescriptor> ->
        Continue
    }
    val controller = GameController(rules, state, actionProvider)
    Window(onCloseRequest = ::exitApplication) {
        App(controller)
    }
}
