@file:OptIn(ExperimentalTime::class)

package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.safeCast
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.DodgySnackContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.bb2025.procedures.tables.kickoff.DodgySnack
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.BrilliantCoaching
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.CheeringFans
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.PitchInvasion
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


abstract class D6RollWheelController: ActionWheelDialogController() {

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
            val buttons = D6Result.allOptions().map { d6Option ->
                DieButtonData(
                    id = ButtonId("$buttonIdPrefix-${d6Option.value}"),
                    label = { null },
                    diceValue = d6Option,
                    action = { provider.userActionSelected(d6Option) },
                    options = D6Result.allOptions(),
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
            val d6Roll = roll.rolls.first() as D6Result
            val buttonId = ButtonId("$buttonIdPrefix-${d6Roll.value}")
            DieButtonData(
                id = buttonId,
                label = { "" },
                diceRollType = diceRollType,
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
            animationOnly = true,
            bottomMessage = diceRollType.description
        )
        acc.addActionWheelEvent(wheelState)
        return true
    }
}

object CheeringFansKickingTeamRollWheelController: D6RollWheelController() {
    override val buttonIdPrefix: String = "cheering-fans-kicking-team"
    override val rollDiceNode: Node = CheeringFans.KickingTeamRollDie
    override val diceRollType: DiceRollType = DiceRollType.CHEERING_FANS

    // There is no "real" center for this, so we place it in the middle of the Kicking Team Half
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val isHomeTeam = state.kickingTeam.isHomeTeam()
        return when (isHomeTeam) {
            true -> getHomeCenterCoordinates(state)
            false -> getAwayCenterCoordinates(state)
        }
    }
}

object CheeringFansReceivingTeamRollWheelController: D6RollWheelController() {
    override val buttonIdPrefix: String = "cheering-fans-receiving-team"
    override val rollDiceNode: Node = CheeringFans.ReceivingTeamRollDie
    override val diceRollType: DiceRollType = DiceRollType.CHEERING_FANS

    // There is no "real" center for this, so we place it in the middle of the Kicking Team Half
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val isHomeTeam = state.receivingTeam.isHomeTeam()
        return when (isHomeTeam) {
            true -> getHomeCenterCoordinates(state)
            false -> getAwayCenterCoordinates(state)
        }
    }
}

object BrilliantCoachingKickingTeamRollWheelController: D6RollWheelController() {
    override val buttonIdPrefix: String = "brilliant-coaching-kicking-team"
    override val rollDiceNode: Node = BrilliantCoaching.KickingTeamRollDie
    override val diceRollType: DiceRollType = DiceRollType.BRILLIANT_COACHING

    // There is no "real" center for this, so we place it in the middle of the Kicking Team Half
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val isHomeTeam = state.kickingTeam.isHomeTeam()
        return when (isHomeTeam) {
            true -> getHomeCenterCoordinates(state)
            false -> getAwayCenterCoordinates(state)
        }
    }
}

object BrilliantCoachingReceivingTeamRollWheelController: D6RollWheelController() {
    override val buttonIdPrefix: String = "brilliant-coaching-receiving-team"
    override val rollDiceNode: Node = BrilliantCoaching.ReceivingTeamRollDie
    override val diceRollType: DiceRollType = DiceRollType.BRILLIANT_COACHING

    // There is no "real" center for this, so we place it in the middle of the Kicking Team Half
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val isHomeTeam = state.receivingTeam.isHomeTeam()
        return when (isHomeTeam) {
            true -> getHomeCenterCoordinates(state)
            false -> getAwayCenterCoordinates(state)
        }
    }
}

object DodgySnackKickingTeamRollWheelController: D6RollWheelController() {
    override val buttonIdPrefix: String = "dodgy-snack-kicking-team"
    override val rollDiceNode: Node = DodgySnack.KickingTeamRollDie
    override val diceRollType: DiceRollType = DiceRollType.DODGY_SNACK_ROLL_OFF

    // There is no "real" center for this, so we place it in the middle of the Kicking Team Half
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val isHomeTeam = state.kickingTeam.isHomeTeam()
        return when (isHomeTeam) {
            true -> getHomeCenterCoordinates(state)
            false -> getAwayCenterCoordinates(state)
        }
    }
}

object DodgySnackReceivingTeamRollWheelController: D6RollWheelController() {
    override val buttonIdPrefix: String = "dodgy-snack-receiving-team"
    override val rollDiceNode: Node = DodgySnack.ReceivingTeamRollDie
    override val diceRollType: DiceRollType = DiceRollType.DODGY_SNACK_ROLL_OFF

    // There is no "real" center for this, so we place it in the middle of the Kicking Team Half
    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val isHomeTeam = state.receivingTeam.isHomeTeam()
        return when (isHomeTeam) {
            true -> getHomeCenterCoordinates(state)
            false -> getAwayCenterCoordinates(state)
        }
    }
}

object DodgySnackEffectOnKickingTeamRollWheelController: D6RollWheelController() {
    override val buttonIdPrefix: String = "dodgy-snack-effect-kicking"
    override val rollDiceNode: Node = DodgySnack.RollForKickingTeamSelectedPlayer
    override val diceRollType: DiceRollType = DiceRollType.DODGY_SNACK_EFFECT

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<DodgySnackContext>()
        return context.kickingTeamPlayerSelected?.coordinates ?: error("Missing kicking team player: $state")
    }
}

object DodgySnackEffectOnReceivingTeamRollWheelController: D6RollWheelController() {
    override val buttonIdPrefix: String = "dodgy-snack-effect-kicking"
    override val rollDiceNode: Node = DodgySnack.RollForReceivingTemSelectedPlayer
    override val diceRollType: DiceRollType = DiceRollType.DODGY_SNACK_EFFECT

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val context = state.getContext<DodgySnackContext>()
        return context.receivingTeamPlayerSelected?.coordinates ?: error("Missing receiving team player: $state")
    }
}

object PitchInvasionKickingTeamRollWheelController: D6RollWheelController() {
    override val buttonIdPrefix: String = "pitch-invasion-kicking"
    override val rollDiceNode: Node = PitchInvasion.RollForKickingTeamFans
    override val diceRollType: DiceRollType = DiceRollType.PITCH_INVASION_FAN_FACTOR

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val isHomeTeam = state.kickingTeam.isHomeTeam()
        return when (isHomeTeam) {
            true -> getHomeCenterCoordinates(state)
            false -> getAwayCenterCoordinates(state)
        }
    }
}

object PitchInvasionReceivingTeamRollWheelController: D6RollWheelController() {
    override val buttonIdPrefix: String = "pitch-invasion-receiing"
    override val rollDiceNode: Node = PitchInvasion.RollForReceivingTeamFans
    override val diceRollType: DiceRollType = DiceRollType.PITCH_INVASION_FAN_FACTOR

    override fun getActionWheelCenter(state: Game): FieldCoordinate {
        val isHomeTeam = state.receivingTeam.isHomeTeam()
        return when (isHomeTeam) {
            true -> getHomeCenterCoordinates(state)
            false -> getAwayCenterCoordinates(state)
        }
    }
}

