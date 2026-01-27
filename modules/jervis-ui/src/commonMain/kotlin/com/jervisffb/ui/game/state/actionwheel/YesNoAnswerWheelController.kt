@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.FoulContext
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.SecureTheBallContext
import com.jervisffb.engine.model.context.StumbleContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball.SecureTheBallStep
import com.jervisffb.engine.rules.common.procedures.Pickup
import com.jervisffb.engine.rules.common.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.common.procedures.actions.block.BothDown
import com.jervisffb.engine.rules.common.procedures.actions.block.BothDownContext
import com.jervisffb.engine.rules.common.procedures.actions.block.PushContext
import com.jervisffb.engine.rules.common.procedures.actions.block.PushStepInitialMoveSequence
import com.jervisffb.engine.rules.common.procedures.actions.block.Stumble
import com.jervisffb.engine.rules.common.procedures.actions.foul.FoulStep
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.UseBB11Apothecary
import com.jervisffb.engine.rules.common.procedures.tables.injury.UseBB7Apothecary
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.ActionButtonData
import com.jervisffb.ui.game.dialogs.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlin.time.ExperimentalTime


/**
 * Abstract class controlling all instances of using skills or "next step" that
 * can be boiled down to a Yes/No answer.
 *
 * During Block action:
 * - Block (skill usage)
 * - Dodge (skill usage)
 * - Sidestep (skill usage)
 * - Tackle (skill usage)
 * - Follow Up
 */
abstract class YesNoAnswerWheelController : ActionWheelDialogController() {

    open val yesLabel: String = "Yes"
    open val noLabel: String = "No"
    abstract fun getActionWheelCenter(state: Game): FieldCoordinate?
    abstract override val nodes: Set<Node>

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper,
    ) {
        val buttons = listOf(
            ActionButtonData(
                id = ButtonId("accept-yes"),
                label = { yesLabel },
                icon = ActionIcon.CONFIRM,
                action = { provider.userActionSelected(Confirm) },
            ),
            ActionButtonData(
                id = ButtonId("accept-no"),
                label = { noLabel },
                icon = ActionIcon.CANCEL,
                action = { provider.userActionSelected(Cancel) },
            ),
        )
        val wheelState = ActionWheelUiStateData(
            center = getActionWheelCenter(acc.game),
            topAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
            bottomItems = buttons,
            bottomExpandMode = MenuExpandMode.TwoWay,
            bottomAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            onDismiss = null,
            animationOnly = false
        )
        acc.addActionWheelEvent(wheelState)
    }
}

object FollowUpWheelController: YesNoAnswerWheelController() {
    override val nodes: Set<Node> = setOf(
        PushStepInitialMoveSequence.DecideToFollowUp
    )
    override val yesLabel: String = "Follow Up"
    override val noLabel: String = "Stay"

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<BlockContext>()
        return context.attacker.coordinates
    }
}

object UseBigHandWheelController: YesNoAnswerWheelController() {
    override val nodes: Set<Node> = setOf(
        Pickup.ChooseToUseBigHand,
        SecureTheBallStep.ChooseToUseBigHand,
    )
    override val yesLabel: String = "Use Big Hand"
    override val noLabel: String = "Do not use Big Hand"

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val player = when (state.stack.currentNode()) {
            Pickup.ChooseToUseBigHand -> state.getContext<PickupRollContext>().player
            SecureTheBallStep.ChooseToUseBigHand -> state.getContext<SecureTheBallContext>().player
            else -> error("Unsupported node: ${state.stack.currentNode()}")
        }
        return player.coordinates
    }
}

object UseBlockWheelController: YesNoAnswerWheelController() {

    override val nodes: Set<Node> = setOf(
        BothDown.AttackerChooseToUseBlock,
        BothDown.DefenderChooseToUseBlock,
    )

    override val yesLabel: String = "Use Block"
    override val noLabel: String = "Do not use Block"

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<BothDownContext>()
        return when (val currentNode = state.stack.currentNode()) {
            BothDown.AttackerChooseToUseBlock -> context.attacker.coordinates
            BothDown.DefenderChooseToUseBlock -> context.defender.coordinates
            else -> error("Unsupported node: $currentNode")
        }
    }
}

object UseWrestleWheelController: YesNoAnswerWheelController() {
    override val nodes: Set<Node> = setOf(
        BothDown.AttackerChooseToUseWrestle,
        BothDown.DefenderChooseToUseWrestle,
    )
    override val yesLabel: String = "Use Wrestle"
    override val noLabel: String = "Do not use Wrestle"

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<BothDownContext>()
        return when (val currentNode = state.stack.currentNode()) {
            BothDown.AttackerChooseToUseWrestle -> context.attacker.coordinates
            BothDown.DefenderChooseToUseWrestle -> context.defender.coordinates
            else -> error("Unsupported node: $currentNode")
        }
    }
}

object UseSidestepWheelController: YesNoAnswerWheelController() {
    override val nodes: Set<Node> = setOf(
        PushStepInitialMoveSequence.DecideToUseSidestep
    )
    override val yesLabel: String = "Use Sidestep"
    override val noLabel: String = "Do not use Sidestep"

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val player = state.getContext<PushContext>().pushee()
        return player.coordinates
    }
}

object UseDodgeWheelController: YesNoAnswerWheelController() {
    override val nodes: Set<Node> = setOf(
        Stumble.ChooseToUseDodge
    )
    override val yesLabel: String = "Use Dodge"
    override val noLabel: String = "Do not use Dodge"

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val defender = state.getContext<StumbleContext>().defender
        return defender.coordinates
    }
}

object UseTackleWheelController: YesNoAnswerWheelController() {
    override val nodes: Set<Node> = setOf(
        Stumble.ChooseToUseTackle
    )
    override val yesLabel: String = "Use Tackle"
    override val noLabel: String = "Do not use Tackle"

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val attacker = state.getContext<StumbleContext>().attacker
        return attacker.coordinates
    }
}

object UseApothecaryWheelController: YesNoAnswerWheelController() {
    override val nodes: Set<Node> = setOf(
        UseBB11Apothecary.ChooseToUseApothecary,
        UseBB7Apothecary.ChooseToUseApothecary,
    )
    override val yesLabel: String = "Use Apothecary"
    override val noLabel: String = "Do not use Apothecary"

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val attacker = state.getContext<RiskingInjuryContext>().player
        return attacker.coordinates
    }
}

object ArgueTheCallWheelController: YesNoAnswerWheelController() {
    override val nodes: Set<Node> = setOf(
        FoulStep.DecideToArgueTheCall,
    )
    override val yesLabel: String = "Argue The Call"
    override val noLabel: String = "Stay Silent"

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<FoulContext>()
        val player = context.fouler
        return player.coordinates
    }
}
