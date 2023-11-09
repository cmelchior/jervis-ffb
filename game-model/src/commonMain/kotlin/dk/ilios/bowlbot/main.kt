package dk.ilios.bowlbot

import dk.ilios.bowlbot.actions.Action
import dk.ilios.bowlbot.actions.ActionDescriptor
import dk.ilios.bowlbot.actions.Continue
import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.model.Player
import dk.ilios.bowlbot.rules.BB2020Rules

// Testing

fun main(args: Array<String>) {
    val rules = BB2020Rules
    val p1 = Player()
    val p2 = Player()
    val state = Game(p1, p2)
    val actionProvider = { state: Game, availableActions: List<ActionDescriptor> ->
        Continue
    }
    val controller = GameController(rules, state, actionProvider)
    controller.start()
}