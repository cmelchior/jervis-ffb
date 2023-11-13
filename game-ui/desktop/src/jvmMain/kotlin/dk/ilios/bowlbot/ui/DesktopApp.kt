package dk.ilios.bowlbot.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import dk.ilios.bowlbot.actions.ActionDescriptor
import dk.ilios.bowlbot.actions.Continue
import dk.ilios.bowlbot.actions.ContinueWhenReady
import dk.ilios.bowlbot.actions.D2Result
import dk.ilios.bowlbot.actions.EndTurn
import dk.ilios.bowlbot.actions.EndTurnWhenReady
import dk.ilios.bowlbot.actions.RollD2
import dk.ilios.bowlbot.actions.SelectAvailableSpace
import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.model.Player
import dk.ilios.bowlbot.rules.BB2020Rules
import dk.ilios.bowlbot.utils.createRandomAction
import kotlin.random.Random

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