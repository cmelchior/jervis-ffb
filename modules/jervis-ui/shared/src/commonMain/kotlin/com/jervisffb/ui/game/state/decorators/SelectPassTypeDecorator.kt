package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.SelectPassType
import com.jervisffb.engine.common.procedures.actions.pass.PassAction
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.procedures.actions.punt.PuntAction
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.model.UiAction
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.SimpleContextMenuOption

/**
 * This class is responsibible for rendering [SelectPassType] actions. These
 * transition between "action modes", e.g. during a Pass or Punt where you
 * shift from "Move" to "Pass". For this reason, they are also put into the
 * context menu as they are optional.
 */
object SelectPassTypeDecorator : PitchActionDecorator<SelectPassType> {

    private val nodesForContextMenu = setOf(
        PassAction.MoveOrPassOrEndAction,
        PuntAction.MoveOrPuntOrEndAction,
    )

    // We track these here until we get a better API. These are actually handled elsewhere,
    // but need to be swallowed here to avoid showing up as an unhandled action.
    private val swallowNodes: Set<Node> = setOf()

    override fun isApplicable(state: Game, request: ActionRequest): Boolean {
        val currentNode = state.stack.currentNode()
        return nodesForContextMenu.contains(currentNode) || swallowNodes.contains(currentNode)
    }

    override fun decorate(
        actionProvider: UiActionProvider,
        state: Game,
        descriptor: SelectPassType,
        owner: Team?,
        isEnabled: Boolean,
        acc: UiSnapshotAccumulator
    ) {
        // Some Cancel events are actually handled elsewhere, so just swallow them here
        if (state.stack.currentNode() in swallowNodes) {
            return
        }

        // We don't want to show Context Menu options to the inactive coach.
        if (!isEnabled) {
            return
        }

        val coordinates = state.activePlayer?.coordinates ?: error("Missing active player")
        descriptor.types.forEach { passType ->
            val (title, icon) = when (passType) {
                PassType.STANDARD -> "Throw Ball" to ActionIcon.PASS
                PassType.HAIL_MARY_PASS -> "Throw a Hail Mary" to ActionIcon.HAIL_MARY_PASS
                PassType.PUNT -> "Punt Ball" to ActionIcon.PUNT
            }
            val action = PassTypeSelected(passType)
            acc.updateSquare(coordinates) {
                it.copy(
                    contextMenuOptions = it.contextMenuOptions.add(
                        SimpleContextMenuOption(
                            title,
                            UiAction(action, GuardedAction(acc) { id -> actionProvider.userActionSelected(id, action) }),
                            icon
                        )
                    )
                )
            }
        }
    }
}
