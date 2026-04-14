package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseReroll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock.SingleStandardBlockChooseReroll
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock.SingleStandardBlockChooseResult
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
 * Control selecting a block result or a reroll option.
 */
object StandardBlockChooseResultOrRerollWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(
        StandardBlockChooseResult.SelectBlockResult,
        StandardBlockChooseReroll.ReRollSourceOrAcceptRoll,
        SingleStandardBlockChooseResult.SelectBlockResult,
        SingleStandardBlockChooseReroll.ChooseRerollSourceOrAcceptRoll,
    )

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.getContext<BlockContext>().defender.coordinates
    }

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper,
    ) {
        val currentNode = acc.gameController.currentNode()

        // If it is the blocking player that also chooses the final result, we shortcut
        // the action sequence a bit, by folding "no-reroll" into pressing one of the
        // dice.
        // If not, we add a "Confirm" roll to the action buttons, which will transfer
        // control to the other coach for selecting the final result.
        val context = acc.game.getContext<BlockContext>()
        val selectResultTeam = context.getTeamSelectingResult()
        val attackerChooseResult = (context.attacker.team == selectResultTeam)
        val diceButtonEnabled = attackerChooseResult
            || (currentNode == SingleStandardBlockChooseResult.SelectBlockResult)
            || (currentNode == StandardBlockChooseResult.SelectBlockResult)

        val diceButtons = context.roll.mapIndexed { index, die ->
            DieButtonData(
                id = ButtonId("block-$index"),
                label = { null },
                diceRollType = DiceRollType.BLOCK,
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
                enabled = diceButtonEnabled,
                preferLtr = (index == 0),
            )
        }

        val actionButtons = mutableListOf<ActionButtonData>()
        if (
            currentNode == StandardBlockChooseReroll.ReRollSourceOrAcceptRoll
            || currentNode == SingleStandardBlockChooseReroll.ChooseRerollSourceOrAcceptRoll
        ) {
            val rerollButtons = actions.getOrNull<SelectRerollOption>()?.let { rerollOption ->
                rerollOption.options.map { option ->
                    val rerollSource = option.getRerollSource(acc.game)
                    ActionButtonData(
                        id = ButtonId("Reroll-${rerollSource.rerollDescription}"),
                        label = { rerollSource.rerollDescription },
                        icon = ActionIcon.TEAM_REROLL,
                        enabled = true,
                        action = { provider.userActionSelected(RerollOptionSelected(option)) }
                    )
                }
            }
            actionButtons.addAll(rerollButtons ?: emptyList())
            if (!attackerChooseResult) {
                actionButtons.add(
                    ActionButtonData(
                        id = ButtonId("confirm"),
                        label = { "Confirm Roll" },
                        icon = ActionIcon.CONFIRM,
                        action = {
                            provider.userActionSelected(NoRerollSelected())
                        },
                    )
                )
            }
        }

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

    // Animate rolling the die, but only for clients
    override fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
//        val dice = selectedAction.safeCast<DiceRollResults>().rolls.map { it as DBlockResult }
//        val serverRoll = (acc.gameController.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER)
//        if (serverRoll) {
//            val diceButtons = dice.mapIndexed { index, die ->
//                DieButtonData(
//                    id = ButtonId("block-$index"),
//                    label = null,
//                    diceValue = die,
//                    action = { /* Do nothing */ },
//                    options = DBlockResult.allOptions(),
//                    expandable = false,
//                    animateRoll = RollAnimationData(
//                        endValue = die,
//                    )
//                )
//            }
//            val context = acc.game.getContext<BlockContext>()
//            val wheelState = ActionWheelUiState(
//                center = context.defender.coordinates,
//                topItems = diceButtons,
//                topExpandMode = MenuExpandMode.Compact(),
//                topAnimationType = ButtonLayoutMode.ANIMATING_ROLL,
//                bottomItems = emptyList(),
//                bottomAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
//                onDismiss = null,
//                animationOnly = true
//            )
//            acc.addActionWheelEvent(wheelState)
//            return true
//        }
        return false
    }
}
