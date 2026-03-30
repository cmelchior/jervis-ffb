@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.WeatherRoll
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ActionButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.DieButtonData
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.dialogs.wheel.RollAnimationData
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlin.time.ExperimentalTime


/**
 * Control Weather Roll (D6 + D6).
 */
object WeatherRollWheelController : ActionWheelDialogController() {

    override val nodes: Set<Node> = setOf(WeatherRoll.RollWeatherDice)

    // There is no "real" center for this, so we place it, assuming that the UI
    // is showing the refee in the center. The referee is placed at the center of the field
    // occupying both squares there. The Away Captain is placed just above this on the
    // Away side
    override fun getActionWheelCenter(state: Game): FieldCoordinate? = null

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper,
    ) {
        val diceButtons = listOf(
            // We want D6 on the "right" side, so it has to go first in the list
            // as we start on the clockwise direction
            DieButtonData(
                id = ButtonId("weather-1-d6"),
                label = { "" },
                diceRollType = DiceRollType.WEATHER,
                diceValue = D6Result.random(),
                action = { /* Do nothing */ },
                options = D6Result.allOptions(),
                expandable = true,
            ),
            DieButtonData(
                id = ButtonId("weather-2-d6"),
                label = { "" },
                diceRollType = DiceRollType.WEATHER,
                diceValue = D6Result.random(),
                action = { /* Do nothing */ },
                options = D6Result.allOptions(),
                expandable = true,
                preferLtr = false,
            ),
        )

        val actionButtons = listOf(
            ActionButtonData(
                id = ButtonId("confirm"),
                label = {
                    val table = acc.game.rules.weatherTable
                    val result = table.roll(diceButtons.first().diceValue, diceButtons.last().diceValue)
                    result.title
                },
                icon = ActionIcon.CONFIRM,
                action = {
                    val dice = diceButtons.map { it.diceValue }
                    provider.userActionSelected(DiceRollResults(dice))
                }
            )
        )

        val wheelState = ActionWheelUiStateData(
            center = getActionWheelCenter(acc.game),
            topItems = diceButtons,
            topExpandMode = MenuExpandMode.Compact(),
            topAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            bottomItems = actionButtons,
            bottomExpandMode = MenuExpandMode.Compact(),
            bottomAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            onDismiss = null,
            animationOnly = false,
        )
        acc.addActionWheelEvent(wheelState)
    }

    // Animate rolling the die, but only for clients
    override fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        val dice = selectedAction.safeCast<DiceRollResults>().let { diceResults ->
            diceResults.rolls.last() as D6Result to diceResults.first() as D6Result
        }
        val serverRoll = (acc.gameController.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER)
        if (serverRoll) {
            val diceButtons = listOf(
                DieButtonData(
                    id = ButtonId("weather-1-d6"),
                    label = { null },
                    diceRollType = DiceRollType.WEATHER,
                    diceValue = dice.first,
                    action = { /* Do nothing */ },
                    options = D6Result.allOptions(),
                    expandable = false,
                    animateRoll = RollAnimationData(
                        endValue = dice.first,
                    ),
                ),
                DieButtonData(
                    id = ButtonId("weather-2-d6"),
                    label = { null },
                    diceRollType = DiceRollType.WEATHER,
                    diceValue = dice.second,
                    action = { /* Do nothing */ },
                    options = D6Result.allOptions(),
                    expandable = false,
                    animateRoll = RollAnimationData(
                        endValue = dice.second,
                    ),
                    preferLtr = false,
                ),
            )
            val wheelState = ActionWheelUiStateData(
                center = getActionWheelCenter(acc.game),
                topItems = diceButtons,
                topExpandMode = MenuExpandMode.Compact(),
                topAnimationType = ButtonLayoutMode.ANIMATING_ROLL,
                bottomItems = emptyList(),
                bottomAnimationType = ButtonLayoutMode.CONTRACT_NEW_SUBMENU,
                onDismiss = null,
                animationOnly = true,
                bottomMessage = "Weather Roll"
            )
            acc.addActionWheelEvent(wheelState)
            return true
        }
        return false
    }
}
