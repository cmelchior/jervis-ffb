package dk.ilios.jervis

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.utils.createRandomAction

// Testing

fun main(args: Array<String>) {
    val rules = BB2020Rules
    val p1 = Player()
    val p2 = Player()
    val state = Game(p1, p2)
    val actionProvider = { state: Game, availableActions: List<ActionDescriptor> ->
        createRandomAction(state, availableActions)
    }
    val controller = GameController(rules, state, actionProvider)
    controller.start()
}