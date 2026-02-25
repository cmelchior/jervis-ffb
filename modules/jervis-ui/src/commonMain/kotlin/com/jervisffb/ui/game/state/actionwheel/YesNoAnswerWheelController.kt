@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.BothDownContext
import com.jervisffb.engine.model.context.FoulContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.context.SecureTheBallContext
import com.jervisffb.engine.model.context.StumbleContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BB2020PushStepInitialMoveSequence
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BothDown
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.Stumble
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.CreatePushChainStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.FollowUpStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.LeapStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball.SecureTheBallStep
import com.jervisffb.engine.rules.common.procedures.Pickup
import com.jervisffb.engine.rules.common.procedures.TheKickOff
import com.jervisffb.engine.rules.common.procedures.actions.foul.FoulStep
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.ArmourRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.InjuryRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.UseBB11Apothecary
import com.jervisffb.engine.rules.common.procedures.tables.injury.UseBB7Apothecary
import com.jervisffb.engine.rules.common.skills.SkillType
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
 * - Fend (skill usage)
 * - Leap (skill usage)
 * - Safe Pass (skill usage)
 * - Sidestep (skill usage)
 * - Tackle (skill usage)
 * - Taunt (skill usage)
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

abstract class UseSkillWheelController(skill: SkillType) : YesNoAnswerWheelController() {
    override val yesLabel: String = "Use ${skill.description}?"
    override val noLabel: String = "Do not use ${skill.description}"
}

object FollowUpWheelController: YesNoAnswerWheelController() {
    override val nodes: Set<Node> = setOf(
        BB2020PushStepInitialMoveSequence.DecideToFollowUp,
        FollowUpStep.ChooseToFollowUp,
    )

    override val yesLabel: String = "Follow Up"
    override val noLabel: String = "Stay"

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<BlockContext>()
        return context.attacker.coordinates
    }
}

object UseBigHandWheelController: UseSkillWheelController(SkillType.BIG_HAND) {
    override val nodes: Set<Node> = setOf(
        Pickup.ChooseToUseBigHand,
        SecureTheBallStep.ChooseToUseBigHand,
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val player = when (state.stack.currentNode()) {
            Pickup.ChooseToUseBigHand -> state.getContext<PickupRollContext>().player
            SecureTheBallStep.ChooseToUseBigHand -> state.getContext<SecureTheBallContext>().player
            else -> error("Unsupported node: ${state.stack.currentNode()}")
        }
        return player.coordinates
    }
}

object UseBlockWheelController: UseSkillWheelController(SkillType.BLOCK) {
    override val nodes: Set<Node> = setOf(
        BothDown.AttackerChooseToUseBlock,
        BothDown.DefenderChooseToUseBlock,
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<BothDownContext>()
        return when (val currentNode = state.stack.currentNode()) {
            BothDown.AttackerChooseToUseBlock -> context.attacker.coordinates
            BothDown.DefenderChooseToUseBlock -> context.defender.coordinates
            else -> error("Unsupported node: $currentNode")
        }
    }
}

object UseWrestleWheelController: UseSkillWheelController(SkillType.WRESTLE) {
    override val nodes: Set<Node> = setOf(
        BothDown.AttackerChooseToUseWrestle,
        BothDown.DefenderChooseToUseWrestle,
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<BothDownContext>()
        return when (val currentNode = state.stack.currentNode()) {
            BothDown.AttackerChooseToUseWrestle -> context.attacker.coordinates
            BothDown.DefenderChooseToUseWrestle -> context.defender.coordinates
            else -> error("Unsupported node: $currentNode")
        }
    }
}

object UseSafePassWheelController: UseSkillWheelController(SkillType.SAFE_PASS) {
    override val nodes: Set<Node> = setOf(
        com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassStep.ChooseToUseSafePass
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val player = state.getContext<PassContext>().thrower
        return player.coordinates
    }
}

object UseSidestepWheelController: UseSkillWheelController(SkillType.SIDESTEP) {
    override val nodes: Set<Node> = setOf(
        BB2020PushStepInitialMoveSequence.DecideToUseSidestep,
        CreatePushChainStep.DecideToUseSidestep,
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val player = state.getContext<PushContext>().pushee()
        return player.coordinates
    }
}

object UseDodgeWheelController: UseSkillWheelController(SkillType.DODGE) {
    override val nodes: Set<Node> = setOf(
        Stumble.ChooseToUseDodge
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val defender = state.getContext<StumbleContext>().defender
        return defender.coordinates
    }
}

object UseTackleWheelController: UseSkillWheelController(SkillType.TACKLE) {
    override val nodes: Set<Node> = setOf(
        Stumble.ChooseToUseTackle
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val attacker = state.getContext<StumbleContext>().attacker
        return attacker.coordinates
    }
}

object UseVeryLongLegsWheelController: UseSkillWheelController(SkillType.VERY_LONG_LEGS) {
    override val nodes: Set<Node> = setOf(
        com.jervisffb.engine.rules.bb2020.procedures.actions.move.JumpStep.ChooseToUseVeryLongLegs,
        com.jervisffb.engine.rules.bb2025.procedures.actions.move.JumpStep.ChooseToUseVeryLongLegs
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val player = state.getContext<MoveContext>().player
        return player.coordinates
    }
}

object UseKickWheelController: UseSkillWheelController(SkillType.KICK) {
    override val nodes: Set<Node> = setOf(
        TheKickOff.ChooseToUseKick
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val player = state.kickingPlayer ?: error("Missing kicking player: $state")
        return player.coordinates
    }
}

object UseDirtyPlayerWheelController: UseSkillWheelController(SkillType.DIRTY_PLAYER) {
    override val nodes: Set<Node> = setOf(
        ArmourRoll.ChooseToUseDirtyPlayer,
        InjuryRoll.ChooseToUseDirtyPlayer
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val player = state.getContext<FoulContext>().fouler
        return player.coordinates
    }
}

object UseFendWheelController: UseSkillWheelController(SkillType.FEND) {
    override val nodes: Set<Node> = setOf(
        FollowUpStep.ChooseToUseFend
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val player = when (state.stack.currentNode()) {
            FollowUpStep.ChooseToUseFend -> state.getContext<PushContext>().firstPushee
            else -> error("Unsupported node: ${state.stack.currentNode()}")
        }
        return player.coordinates
    }
}

object UseTauntWheelController: UseSkillWheelController(SkillType.TAUNT) {
    override val nodes: Set<Node> = setOf(
        FollowUpStep.ChooseToUseTaunt
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val player = when (state.stack.currentNode()) {
            FollowUpStep.ChooseToUseTaunt -> state.getContext<PushContext>().firstPushee
            else -> error("Unsupported node: ${state.stack.currentNode()}")
        }
        return player.coordinates
    }
}

object UseThickSkullWheelController: UseSkillWheelController(SkillType.THICK_SKULL) {
    override val nodes: Set<Node> = setOf(
        InjuryRoll.ChooseToUseThickSkull
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<RiskingInjuryContext>()
        val player = context.player
        return player.coordinates
    }
}

object UseLeapWheelController: UseSkillWheelController(SkillType.LEAP) {
    override val nodes: Set<Node> = setOf(
        LeapStep.ChooseToUseLeapModifier
    )
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<MoveContext>()
        val player = context.player
        return player.coordinates
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
        return when (attacker.location) {
            DogOut -> {
                state.getContext<PushContext>().pushChain.last().from
            }
            is FieldCoordinate -> {
                attacker.coordinates
            }
            is GiantLocation -> TODO("Not supported")
        }
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
