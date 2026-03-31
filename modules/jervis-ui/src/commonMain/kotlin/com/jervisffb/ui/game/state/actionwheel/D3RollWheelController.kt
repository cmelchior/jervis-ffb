@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.bb2025.procedures.tables.kickoff.Charge
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.FanFactorRolls
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.PitchInvasion
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.QuickSnap
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.SolidDefense
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.DieButtonData
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.dialogs.wheel.RollAnimationData
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import kotlin.time.ExperimentalTime


abstract class D3RollWheelController: ActionWheelDialogController() {

    abstract val buttonIdPrefix: String
    abstract val rollDiceNode: Node
    abstract val diceRollType: DiceRollType
    override val nodes: Set<Node> by lazy {
        setOf(rollDiceNode)
    }
    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalFieldDataWrapper,
    ) {
        if (acc.stack.currentNode() == rollDiceNode) {
            val buttons = D3Result.allOptions().map { d3Option ->
                DieButtonData(
                    id = ButtonId("$buttonIdPrefix-${d3Option.value}"),
                    label = { null },
                    diceValue = d3Option,
                    action = { provider.userActionSelected(d3Option) },
                    options = D3Result.allOptions(),
                    expandable = false,
                    diceRollType = diceRollType,
                )
            }
            val wheelState = ActionWheelUiStateData(
                center = getActionWheelCenter(acc.game),
                topItems = buttons,
                topExpandMode = MenuExpandMode.FanOut(spread = 360f),
                topAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
                bottomAnimationType = ButtonLayoutMode.HIDE,
                onDismiss = null,
                animationOnly = false
            )
            acc.addActionWheelEvent(wheelState)
        }
    }

    // Animate rolling the die, but only for clients with server dice rolls enabled
    // as they would already have chosen the result in `onDecorateActions`
    override fun onPostActionAnimation(
        acc: UiSnapshotAccumulator,
        selectedAction: GameAction,
    ): Boolean {
        val serverRoll = (acc.gameController.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER)
        val currentNode = acc.stack.currentNode()
        if (!((currentNode == rollDiceNode) && serverRoll)) return false

        val button = selectedAction.safeCast<DiceRollResults>().let { roll ->
            val d3Roll = roll.rolls.first() as D3Result
            val buttonId = ButtonId("$buttonIdPrefix-${d3Roll.value}")
            DieButtonData(
                id = buttonId,
                label = { "" },
                diceRollType = diceRollType,
                diceValue = d3Roll,
                action = { /* Do nothing */ },
                options = emptyList(),
                expandable = false,
                enabled = false,
                animateRoll = RollAnimationData(
                    endValue = d3Roll,
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
            animationOnly = true,
            bottomMessage = diceRollType.description
        )
        acc.addActionWheelEvent(wheelState)
        return true
    }
}

object HomeTeamFanFactorRoll: D3RollWheelController() {
    override val buttonIdPrefix: String = "ff"
    override val rollDiceNode: Node = FanFactorRolls.SetFanFactorForHomeTeam
    override val diceRollType: DiceRollType = DiceRollType.FAN_FACTOR

    // There is no "real" center for this, so we place it, assuming that the UI
    // is showing the player "captain". The referee is placed at the center of the field
    // occupying both squares there. The Home Captain is placed just above this on the
    // Home side
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val y = (state.rules.fieldHeight / 2) - 1
        val x = (state.rules.fieldWidth / 2) - 2
        return FieldCoordinate(x, y)
    }
}

object AwayTeamFanFactorRoll: D3RollWheelController() {
    override val buttonIdPrefix: String = "ff" // Must be same as HomeTeam to animate correctly
    override val rollDiceNode: Node = FanFactorRolls.SetFanFactorForAwayTeam
    override val diceRollType: DiceRollType = DiceRollType.FAN_FACTOR

    // There is no "real" center for this, so we place it, assuming that the UI
    // is showing the player "captain". The referee is placed at the center of the field
    // occupying both squares there. The Away Captain is placed just above this on the
    // Away side
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val y = (state.rules.fieldHeight / 2) - 1
        val x = (state.rules.fieldWidth / 2) + 1
        return FieldCoordinate(x, y)
    }
}

object ChargePlayersRollWheelController: D3RollWheelController() {
    override val buttonIdPrefix: String = "charge-players"
    override val rollDiceNode: Node = Charge.RollForPlayers
    override val diceRollType: DiceRollType = DiceRollType.CHARGE

    // There is no "real" center for this, so we place it in the center of the Field
    override fun getActionWheelCenter(state: Game): FieldCoordinate? = null
}

object QuickSnapRollWheelController: D3RollWheelController() {
    override val buttonIdPrefix: String = "quick-snap"
    override val rollDiceNode: Node = QuickSnap.RollDie
    override val diceRollType: DiceRollType = DiceRollType.QUICK_SNAP

    // There is no "real" center for this, so we place it in the center of the Field
    override fun getActionWheelCenter(state: Game): FieldCoordinate? = null
}

object SolidDefenseWheelController: D3RollWheelController() {
    override val buttonIdPrefix: String = "solid-defense"
    override val rollDiceNode: Node = SolidDefense.RollDie
    override val diceRollType: DiceRollType = DiceRollType.SOLID_DEFENSE

    // There is no "real" center for this, so we place it in the center of the Field
    override fun getActionWheelCenter(state: Game): FieldCoordinate? = null
}

object PitchInvasionKickingTeamPlayersAffectedRollWheelController: D3RollWheelController() {
    override val buttonIdPrefix: String = "pitch-invasion-kicking-players-affected"
    override val rollDiceNode: Node = PitchInvasion.RollForKickingTeamStuns
    override val diceRollType: DiceRollType = DiceRollType.PITCH_INVASION_PLAYERS_AFFECTED

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val isHomeTeam = state.kickingTeam.isHomeTeam()
        return when (isHomeTeam) {
            true -> getHomeCenterCoordinates(state)
            false -> getAwayCenterCoordinates(state)
        }
    }
}

object PitchInvasionReceivingTeamPlayersAffectedRollWheelController: D3RollWheelController() {
    override val buttonIdPrefix: String = "pitch-invasion-receiving-players-affected"
    override val rollDiceNode: Node = PitchInvasion.RollForReceivingTeamStuns
    override val diceRollType: DiceRollType = DiceRollType.PITCH_INVASION_PLAYERS_AFFECTED

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val isHomeTeam = state.receivingTeam.isHomeTeam()
        return when (isHomeTeam) {
            true -> getHomeCenterCoordinates(state)
            false -> getAwayCenterCoordinates(state)
        }
    }
}


