package dk.ilios

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import dk.ilios.bowlbot.actions.ActionDescriptor
import dk.ilios.bowlbot.actions.Continue
import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.model.Player
import dk.ilios.bowlbot.rules.BB2020Rules
import dk.ilios.bowlbot.ui.App

@Preview
@Composable
fun AppPreview() {
    val rules = BB2020Rules
    val p1 = Player()
    val p2 = Player()
    val state = Game(p1, p1)
    val actionProvider = { state: Game, availableActions: List<ActionDescriptor> ->
        Continue
    }
    val controller = GameController(rules, state, actionProvider)
    App(controller)
}