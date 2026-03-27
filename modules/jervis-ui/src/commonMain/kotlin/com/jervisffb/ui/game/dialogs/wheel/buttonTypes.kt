package com.jervisffb.ui.game.dialogs.wheel

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.actionwheel.ActionWheelDialogController
import kotlin.jvm.JvmInline
import kotlin.time.Duration

// These ID's identify a single button. They should be stable across
// action requests so we can animate changes correctly.
@JvmInline
value class ButtonId(val id: String)

@Suppress("UNCHECKED_CAST")
data class RollAnimationData<T: DieResult>(
    val endValue: T,
    val additionalDelayAfterRoll: Duration? = ActionWheelDialogController.DEFAULT_DELAY_AFTER_ROLL
) {
    val startingValue: T = endValue.allOptions(endValue).random() as T
    val intermediateValue: T = endValue.allOptions(endValue, startingValue).random() as T
}

sealed interface ButtonData {
    val id: ButtonId
    val action: () -> Unit
    val label: () -> String?
    var targetAngle: Float
    var defaultStartingAngle: Float
    val animateRoll: RollAnimationData<*>?
}

data class ActionButtonData(
    override val id: ButtonId,
    override val label: () -> String,
    val icon: ActionIcon,
    override val action: () -> Unit,
    val enabled: Boolean = true,
): ButtonData {
    override var targetAngle: Float = 0f
    override var defaultStartingAngle: Float = 0f
    override val animateRoll: RollAnimationData<*>? = null
}

data class DieButtonData<T: DieResult>(
    override val id: ButtonId,
    override val label: () -> String?,
    val diceRollType: DiceRollType,
    var diceValue: T,
    override val action: () -> Unit,
    val options: List<T>,
    val expandable: Boolean,
    val enabled: Boolean = true,
    override val animateRoll: RollAnimationData<T>? = null,
    val preferLtr: Boolean = true,
): ButtonData {
    override var targetAngle: Float = 0f
    override var defaultStartingAngle: Float = 0f
}

// Just Dummy for now
data class CoinMenuItem(
    override val id: ButtonId,
    val value: Coin,
    override val label: () -> String?,
    override val action: () -> Unit,
    override val animateRoll: RollAnimationData<*>? = null
): ButtonData {
    override var targetAngle: Float = 0f
    override var defaultStartingAngle: Float = 0f
}

///**
// * Class representing a "coin" menu item in the action wheel.
// */
//class CoinMenuItem(
//    override val id: ButtonId,
//    val value: Coin,
//    override val parent: ActionWheelMenuItem?,
//    label: () -> String,
//    enabled: Boolean,
//    val onClick: (Coin) -> Unit,
//    private val startAnimationFrom: Coin? = null
//): ActionWheelMenuItem() {
//    override var label: () -> String by mutableStateOf(label)
//    override var enabled: Boolean by mutableStateOf(enabled)
//
//    override val expandMode: MenuExpandMode = MenuExpandMode.None
//    override val subMenu: SnapshotStateList<ActionWheelMenuItem> = mutableStateListOf()
//    var animatingFrom: Coin? by mutableStateOf(startAnimationFrom)
//    var animationDone: Boolean by mutableStateOf(startAnimationFrom == null)
//    override fun onActionOptionsExpandChange(item: ActionWheelMenuItem, expanded: Boolean) {
//        // Do nothing
//    }
//}
