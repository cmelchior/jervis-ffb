@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.CatchRollContext
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.JumpRollContext
import com.jervisffb.engine.model.context.LandingRollContext
import com.jervisffb.engine.model.context.LeapRollContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.PogoRollContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.SecureTheBallRollContext
import com.jervisffb.engine.model.context.ShadowingRollContext
import com.jervisffb.engine.model.context.SteadyFootingRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.AccuracyRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.LeapRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.PogoRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.pass.InterceptionContext
import com.jervisffb.engine.rules.bb2025.procedures.actions.pass.InterceptionRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.pass.InterceptionRollContext
import com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball.SecureTheBallRoll
import com.jervisffb.engine.rules.bb2025.procedures.skills.ShadowingRoll
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.BoneHeadRoll
import com.jervisffb.engine.rules.common.procedures.BoneHeadRollContext
import com.jervisffb.engine.rules.common.procedures.CatchRoll
import com.jervisffb.engine.rules.common.procedures.PickupRoll
import com.jervisffb.engine.rules.common.procedures.ReallyStupidRoll
import com.jervisffb.engine.rules.common.procedures.ReallyStupidRollContext
import com.jervisffb.engine.rules.common.procedures.SteadyFootingRoll
import com.jervisffb.engine.rules.common.procedures.UnchannelledFuryRoll
import com.jervisffb.engine.rules.common.procedures.UnchannelledFuryRollContext
import com.jervisffb.engine.rules.common.procedures.actions.block.BreatheFireContext
import com.jervisffb.engine.rules.common.procedures.actions.block.BreatheFireRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.DauntlessRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.DauntlessRollContext
import com.jervisffb.engine.rules.common.procedures.actions.block.ProjectileVomitContext
import com.jervisffb.engine.rules.common.procedures.actions.block.ProjectileVomitRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.JumpRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.RushRoll
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.LandingRoll
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.ActionButtonData
import com.jervisffb.ui.game.dialogs.ButtonId
import com.jervisffb.ui.game.dialogs.DieButtonData
import com.jervisffb.ui.game.dialogs.RollAnimationData
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlin.time.ExperimentalTime


/**
 * Abstract class for handling all single D6 with a potential reroll like:
 *
 * - Accuracy
 * - BoneHead
 * - Breathe Fire
 * - Catch
 * - Dauntless
 * - Dodge
 * - Interception
 * - Jump
 * - Landing
 * - Leap
 * - Pickup
 * - Pogo
 * - Projectile Vomit
 * - Really Stupid
 * - Rush
 * - Shadowing
 * - Steady Footing
 * - Unchannelled Fury
 */
abstract class D6WithRerollWheelController() : ActionWheelDialogController() {

    // Parameters / Methods required to customize the behavior
    abstract val buttonIdPrefix: String
    abstract val rollDiceNode: Node
    abstract val chooseRerollSourceNode: Node
    abstract val rerollDiceNode: Node
    abstract fun getActionWheelCenter(state: Game): FieldCoordinate?
    abstract fun getOriginalRoll(state: Game): D6Result
    override val nodes: Set<Node> by lazy {
        setOf(
            rollDiceNode,
            chooseRerollSourceNode,
            rerollDiceNode,
        )
    }
    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper,
    ) {
        if (acc.stack.currentNode() == rollDiceNode || acc.stack.currentNode() == rerollDiceNode) {
            val buttons = D6Result.allOptions().map { d6Option ->
                DieButtonData(
                    id = ButtonId("$buttonIdPrefix-${d6Option.value}"),
                    label = { null }, // "Roll ${d6Option.value}",
                    diceValue = d6Option,
                    action = { provider.userActionSelected(d6Option) },
                    options = D6Result.allOptions(),
                    expandable = false,
                )
            }
            val wheelState = ActionWheelUiStateData(
                center = getActionWheelCenter(acc.game), //
                topItems = buttons,
                topExpandMode = MenuExpandMode.FanOut(spread = 360f),
                topAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
                bottomAnimationType = if (acc.stack.currentNode() == PickupRoll.ReRollDie) ButtonLayoutMode.CONTRACT_NEW_SUBMENU else ButtonLayoutMode.ANIMATING_ROLL,
                onDismiss = null,
                animationOnly = false
            )
            acc.addActionWheelEvent(wheelState)
        }

        if (acc.stack.currentNode() == chooseRerollSourceNode) {
            val roll = getOriginalRoll(acc.game)
            val rolledValue = DieButtonData(
                id = ButtonId("$buttonIdPrefix-${roll.value}"),
                label = { "Accept roll: ${roll.value}" },
                diceValue = roll,
                action = { provider.userActionSelected(NoRerollSelected()) },
                options = emptyList(),
                expandable = false,
            )
            val rerollOptions = actions.filterIsInstance<SelectRerollOption>().firstOrNull()?.let { rerollOption ->
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
            } ?: emptyList()
            val wheelState = ActionWheelUiStateData(
                center = getActionWheelCenter(acc.game),
                topItems = listOf(rolledValue),
                topExpandMode = MenuExpandMode.FanOut(spread = 360f),
                topAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
                bottomItems = rerollOptions,
                bottomExpandMode = MenuExpandMode.Compact(),
                bottomAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
                onDismiss = null,
                animationOnly = false,
            )
            acc.addActionWheelEvent(wheelState)
        }
    }

    // Animate rolling the die, but only for clients
    override fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        val serverRoll = (acc.gameController.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER)
        val currentNode = acc.stack.currentNode()
        if ((currentNode == rollDiceNode || currentNode == rerollDiceNode) && serverRoll) {
            val button = selectedAction.safeCast<DiceRollResults>().let { roll ->
                val d6Roll = roll.rolls.first() as D6Result
                val buttonId = when (currentNode) {
                    rollDiceNode -> ButtonId("$buttonIdPrefix-${d6Roll.value}")
                    rerollDiceNode -> {
                        val originalRole = getOriginalRoll(acc.game)
                        ButtonId("$buttonIdPrefix-${originalRole.value}")
                    }
                    else -> error("Unexpected node: $currentNode")
                }
                DieButtonData(
                    id = buttonId,
                    label = { "" },
                    diceValue = d6Roll,
                    action = { /* Do nothing */ },
                    options = emptyList(),
                    expandable = false,
                    enabled = false,
                    animateRoll = RollAnimationData(
                        endValue = d6Roll,
                    ),
                )
            }
            val wheelState = ActionWheelUiStateData(
                center = getActionWheelCenter(acc.game),
                topItems = listOf(button),
                topExpandMode = MenuExpandMode.Compact(),
                topAnimationType = ButtonLayoutMode.ANIMATING_ROLL,
                bottomItems = emptyList(),
                bottomAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
                onDismiss = null,
                animationOnly = true
            )
            acc.addActionWheelEvent(wheelState)
            return true
        }
        return false
    }
}

/**
 * Define the Action Wheel layout when rolling for Accuracy (passing) in BB2025.
 */
object AccuracyBB2020WheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "accuracy"
    override val rollDiceNode: Node = AccuracyRoll.RollDie
    override val chooseRerollSourceNode: Node = AccuracyRoll.ChooseReRollSource
    override val rerollDiceNode: Node = AccuracyRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<PassContext>().thrower.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<PassContext>()
        return context.passingRoll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Accuracy (passing) in BB2025.
 */
object AccuracyBB2025PassWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "accuracy"
    override val rollDiceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.RollDie
    override val chooseRerollSourceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.ChooseReRollSource
    override val rerollDiceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<PassContext>().thrower.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<PassContext>()
        return context.passingRoll!!.originalRoll
    }
}

object AccuracyBB2025ThrowTeamMateWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "quality"
    override val rollDiceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.ThrowTeammateAccuracyRoll.RollDie
    override val chooseRerollSourceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.ThrowTeammateAccuracyRoll.ChooseReRollSource
    override val rerollDiceNode: Node = com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate.ThrowTeammateAccuracyRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<ThrowTeamMateContext>().thrower.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ThrowTeamMateContext>()
        return context.qualityRoll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Catch.
 */
object CatchWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "catch"
    override val rollDiceNode: Node = CatchRoll.RollDie
    override val chooseRerollSourceNode: Node = CatchRoll.ChooseReRollSource
    override val rerollDiceNode: Node = CatchRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<CatchRollContext>().catchingPlayer.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<CatchRollContext>()
        return context.roll!!.originalRoll
    }
}


/**
 * Define the Action Wheel layout when rolling for Dodge.
 */
object DodgeWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "dodge"
    override val rollDiceNode: Node = DodgeRoll.RollDie
    override val chooseRerollSourceNode: Node = DodgeRoll.ChooseReRollSource
    override val rerollDiceNode: Node = DodgeRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.activePlayer?.coordinates ?: error("Missing active player: $state")
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<DodgeRollContext>()
        return context.roll!!.originalRoll
    }
}

object DauntlessWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "dauntless"
    override val rollDiceNode: Node = DauntlessRoll.RollDie
    override val chooseRerollSourceNode: Node = DauntlessRoll.ChooseReRollSource
    override val rerollDiceNode: Node = DauntlessRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<DauntlessRollContext>()
        return context.attacker.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<DauntlessRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Interception.
 */
object InterceptionWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "interception"
    override val rollDiceNode: Node = InterceptionRoll.RollDie
    override val chooseRerollSourceNode: Node = InterceptionRoll.ChooseReRollSource
    override val rerollDiceNode: Node = InterceptionRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<InterceptionContext>()
        return context.interceptingPlayer?.coordinates ?: error("Missing intercepting player: $state")
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<InterceptionRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Pickup.
 */
object PickupWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "pickup"
    override val rollDiceNode: Node = PickupRoll.RollDie
    override val chooseRerollSourceNode: Node = PickupRoll.ChooseReRollSource
    override val rerollDiceNode: Node = PickupRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate? {
        return state.activePlayer?.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<PickupRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Rush.
 */
object RushWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "rush"
    override val rollDiceNode: Node = RushRoll.RollDie
    override val chooseRerollSourceNode: Node = RushRoll.ChooseReRollSource
    override val rerollDiceNode: Node = RushRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.activePlayer?.coordinates ?: error("Missing active player: $state")
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<RushRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action Wheel layout when rolling for Securing the Ball.
 */
object SecureTheBallWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "secure"
    override val rollDiceNode: Node = SecureTheBallRoll.RollDie
    override val chooseRerollSourceNode: Node = SecureTheBallRoll.ChooseReRollSource
    override val rerollDiceNode: Node = SecureTheBallRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate? {
        return state.activePlayer?.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<SecureTheBallRollContext>()
        return context.roll!!.originalRoll
    }
}


/**
 * Define the Action Wheel layout when rolling for Shadowing.
 */
object ShadowingWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "shadowing"
    override val rollDiceNode: Node = ShadowingRoll.RollDie
    override val chooseRerollSourceNode: Node = ShadowingRoll.ChooseReRollSource
    override val rerollDiceNode: Node = ShadowingRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<ShadowingRollContext>().player.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ShadowingRollContext>()
        return context.roll!!.originalRoll
    }
}

object SteadyFootingWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "steady-footing"
    override val rollDiceNode: Node = SteadyFootingRoll.RollDie
    override val chooseRerollSourceNode: Node = SteadyFootingRoll.ChooseReRollSource
    override val rerollDiceNode: Node = SteadyFootingRoll.ReRollDie

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<SteadyFootingRollContext>().player.coordinates
    }

    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<SteadyFootingRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action-Wheel layout when rolling to Jump.
 */
object JumpWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "jump"
    override val rollDiceNode: Node = JumpRoll.RollDie
    override val chooseRerollSourceNode: Node = JumpRoll.ChooseReRollSource
    override val rerollDiceNode: Node = JumpRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<MoveContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<JumpRollContext>()
        return context.roll!!.originalRoll
    }
}

/**
 * Define the Action-Wheel layout when rolling for Bone Head.
 */
object BoneHeadWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "bonehead"
    override val rollDiceNode: Node = BoneHeadRoll.RollDie
    override val chooseRerollSourceNode: Node = BoneHeadRoll.ChooseReRollSource
    override val rerollDiceNode: Node = BoneHeadRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<ActivatePlayerContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<BoneHeadRollContext>()
        return context.roll.originalRoll
    }
}

/**
 * Define the Action-Wheel layout when rolling for Bone Head.
 */
object ReallyStupidWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "really-stupid"
    override val rollDiceNode: Node = ReallyStupidRoll.RollDie
    override val chooseRerollSourceNode: Node = ReallyStupidRoll.ChooseReRollSource
    override val rerollDiceNode: Node = ReallyStupidRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<ActivatePlayerContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ReallyStupidRollContext>()
        return context.roll.originalRoll
    }
}

/**
 * Define the Action-Wheel layout when rolling for Unchannelled Fury.
 */
object UnchannelledFuryWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "unchannelled-fury"
    override val rollDiceNode: Node = UnchannelledFuryRoll.RollDie
    override val chooseRerollSourceNode: Node = UnchannelledFuryRoll.ChooseReRollSource
    override val rerollDiceNode: Node = UnchannelledFuryRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<ActivatePlayerContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<UnchannelledFuryRollContext>()
        return context.roll.originalRoll
    }
}

/**
 * Define the Action-Wheel layout when rolling for Leap.
 */
object LeapWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "leap"
    override val rollDiceNode: Node = LeapRoll.RollDie
    override val chooseRerollSourceNode: Node = LeapRoll.ChooseReRollSource
    override val rerollDiceNode: Node = LeapRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<LeapRollContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<LeapRollContext>()
        return context.roll?.originalRoll!!
    }
}

/**
 * Define the Action-Wheel layout when rolling for Pogo.
 */
object PogoWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "pogo"
    override val rollDiceNode: Node = PogoRoll.RollDie
    override val chooseRerollSourceNode: Node = PogoRoll.ChooseReRollSource
    override val rerollDiceNode: Node = PogoRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<PogoRollContext>().player.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<PogoRollContext>()
        return context.roll?.originalRoll!!
    }
}

/**
 * Define the Action-Wheel layout when rolling for Landing after being thrown.
 */
object LandingWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "landing"
    override val rollDiceNode: Node = LandingRoll.RollDie
    override val chooseRerollSourceNode: Node = LandingRoll.ChooseReRollSource
    override val rerollDiceNode: Node = LandingRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<ThrowTeamMateContext>().thrownPlayer!!.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<LandingRollContext>()
        return context.roll?.originalRoll!!
    }
}

object ProjectileVomitWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "projectile-vomit"
    override val rollDiceNode: Node = ProjectileVomitRoll.RollDie
    override val chooseRerollSourceNode: Node = ProjectileVomitRoll.ChooseReRollSource
    override val rerollDiceNode: Node = ProjectileVomitRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<ProjectileVomitContext>().attacker.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<ProjectileVomitContext>()
        return context.vomitRoll?.originalRoll!!
    }
}

object BreatheFireWheelController : D6WithRerollWheelController() {
    override val buttonIdPrefix: String = "breathe-fire"
    override val rollDiceNode: Node = BreatheFireRoll.RollDie
    override val chooseRerollSourceNode: Node = BreatheFireRoll.ChooseReRollSource
    override val rerollDiceNode: Node = BreatheFireRoll.ReRollDie
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        return state.getContext<BreatheFireContext>().attacker.coordinates
    }
    override fun getOriginalRoll(state: Game): D6Result {
        val context = state.getContext<BreatheFireContext>()
        return context.breatheRoll?.originalRoll!!
    }
}



