package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.common.procedures.rerolls.UseProReroll
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ActionButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.DieButtonData
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalPitchDataWrapper

/**
 * Controls selecting the die when using the Pro skill. Including aborting using
 * it.
 */
object SelectProDieWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(
        UseProReroll.SelectDieToReroll
    )

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        // Using Pro on Block Dice should keep the block dice over the defender
        // position.
        // TODO This probably needs more checks once Multiple Block is introduced
        return state.getContextOrNull<BlockContext>()
            ?.defender?.coordinates
            ?:state.getRerollContext().player?.coordinates
            ?: error("Missing player")
    }

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper,
    ) {
        val currentNode = acc.gameController.currentNode()

        // Pro is currently only user-facing during blocks, so we can hard code the
        // dice ids. This isn't ideal, though, and we might need to find a way
        // to forward exactly which wheel controller triggered this.
        val context = acc.game.getRerollContext()
        val dice = actions.get<SelectDicePoolResult>().pools.first().dice

        val diceButtons = dice.orEmpty().mapIndexed { index, die ->
            // "block" mirrors the naming convention used in `StandardBlockChooseResultOrRerollWheelController`
            val prefix = if (die.result is DBlockResult) "block" else "unknown"
            DieButtonData(
                id = ButtonId("$prefix-$index"),
                label = { null },
                diceRollType = context.type,
                diceValue = die.result,
                action = {
                    val action = if (actions.contains<SelectNoReroll>()) {
                        CompositeGameAction(
                            NoRerollSelected(0),
                            DicePoolResultsSelected.fromSingleDice(die)
                        )
                    } else {
                        DicePoolResultsSelected.fromSingleDice(die)
                    }
                    provider.userActionSelected(action)
                },
                options = DBlockResult.allOptions(),
                expandable = false,
                enabled = true,
                preferLtr = (index == 0),
            )
        }

        // The buttonId's here must be kept in sync with `StandardBlockWheelControllers` to make sure the buttons
        // animate correctly.
        val rerollSource = context.source ?: error("No reroll source")
        val actionButtons = listOf(ActionButtonData(
            id = ButtonId("Reroll-${rerollSource.rerollDescription}"),
            label = { "Cancel using Pro" },
            icon = ActionIcon.CANCEL,
            enabled = true,
            action = { provider.userActionSelected(Cancel) }
        ))

        val wheelState = ActionWheelUiStateData(
            center = getActionWheelCenter(acc.game),
            topItems = diceButtons,
            topExpandMode = MenuExpandMode.Compact(),
            topAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            bottomItems = actionButtons,
            bottomExpandMode = MenuExpandMode.Compact(),
            bottomAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
        )
        acc.addActionWheelEvent(wheelState)
    }
}
