package com.jervisffb.fumbbl.net.adapter

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules

sealed interface JervisActionHolder {
    val expectedNode: Node
}

data class JervisAction(
    val action: GameAction,
    override val expectedNode: Node,
) : JervisActionHolder

// Only run this action if the expectedNode matches,
// otherwise just ignore it.
data class OptionalJervisAction(
    val action: GameAction,
    override val expectedNode: Node,
): JervisActionHolder

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

fun MutableList<JervisActionHolder>.addOptional(
    action: GameAction,
    expectedNode: Node,
) {
    this.add(OptionalJervisAction(action, expectedNode))
}

