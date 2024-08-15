package dk.ilios.jervis.fumbbl.adapter

import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

sealed interface JervisActionHolder {
    val expectedNode: Node
}

data class JervisAction(
    val action: GameAction,
    override val expectedNode: Node,
) : JervisActionHolder

data class CalculatedJervisAction(
    val actionFunc: (state: Game, rules: Rules) -> GameAction,
    override val expectedNode: Node,
) : JervisActionHolder

fun MutableList<JervisActionHolder>.add(
    action: GameAction,
    expectedNode: Node,
) {
    this.add(JervisAction(action, expectedNode))
}

fun MutableList<JervisActionHolder>.add(
    action: (state: Game, rules: Rules) -> GameAction,
    expectedNode: Node,
) {
    this.add(CalculatedJervisAction(action, expectedNode))
}
