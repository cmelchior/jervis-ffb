package dk.ilios.jervis.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.utils.createRandomAction

@Preview
@Composable
fun AppPreview() {
    val rules = BB2020Rules
    val p1 = Player()
    val p2 = Player()
    val state = Game(p1, p1)
    val actionProvider = { state: Game, availableActions: List<ActionDescriptor> ->
        createRandomAction(state, availableActions)
    }
    val controller = GameController(rules, state, actionProvider)
    App(controller)
}