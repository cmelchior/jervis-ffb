package com.jervisffb.ui.game.view

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.AccuracyRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball.SecureTheBallRoll
import com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball.SecureTheBallStep
import com.jervisffb.engine.rules.bb2025.procedures.skills.ShadowingRoll
import com.jervisffb.engine.rules.bb2025.procedures.skills.UseShadowingStep
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.CatchRoll
import com.jervisffb.engine.rules.common.procedures.DeviateRoll
import com.jervisffb.engine.rules.common.procedures.DeviateRollContext
import com.jervisffb.engine.rules.common.procedures.Pickup
import com.jervisffb.engine.rules.common.procedures.PickupRoll
import com.jervisffb.engine.rules.common.procedures.ScatterRoll
import com.jervisffb.engine.rules.common.procedures.TheKickOff
import com.jervisffb.engine.rules.common.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.JumpRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.JumpStep
import com.jervisffb.engine.rules.common.procedures.actions.move.RushRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.ArmourRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.InjuryRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.UseBB11Apothecary
import com.jervisffb.engine.rules.common.procedures.tables.injury.UseBB7Apothecary
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.LocalActionProvider
import com.jervisffb.ui.game.state.P2PActionProvider
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.viewmodel.MenuViewModel

/**
 * Class responsible for setting the game status message for the current step.
 *
 * For Hotseat games it will always show the message for the active coach.
 * For P2P games, each client will see the message relevant for the respective
 * coach.
 */
class GameStatusMessageFactory(private val menuViewModel: MenuViewModel, private val state: Game) {

    private val messageFactories = mutableMapOf<Node, (isActiveClient: Boolean, serverDiceRolls: Boolean, game: Game) -> String?>(
        AccuracyRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Pass the Ball"
                else -> null
            }
        },
        AccuracyRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Pass Result or Reroll D6?"
                else -> null
            }
        },
        AccuracyRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Pass the Ball"
                else -> null
            }
        },

        com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Pass the Ball"
                else -> null
            }
        },
        com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Pass Result or Reroll D6?"
                else -> null
            }
        },
        com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassAccuracyRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Pass the Ball"
                else -> null
            }
        },

        Bounce.RollDirection to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D8 to Bounce the Ball"
                else -> null
            }
        },

        CatchRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Catch the Ball"
                else -> null
            }
        },
        CatchRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Catch Result or Reroll D6?"
                else -> null
            }
        },
        CatchRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Catch the Ball"
                else -> null
            }
        },

        DeviateRoll.RollDice to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D8 + D6 to Deviate the Ball"
                else -> "Deviate Roll"
            }
        },

        DodgeRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Dodge"
                else -> null
            }
        },
        DodgeRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Dodge Result or Reroll D6?"
                else -> null
            }
        },
        DodgeRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Dodge"
                else -> null
            }
        },


        TheKickOff.PlaceTheKick to { isActiveClient, serverDiceRolls, state ->
            when (isActiveClient) {
                true -> "Place the Kick"
                false -> "Kick is being placed"
            }
        },
        TheKickOff.TheKickDeviates to { isActiveClient, serverDiceRolls, state ->
            "The Kick Deviates"
        },

        PickupRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Pickup the Ball"
                else -> null
            }
        },
        PickupRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Pickup Result or Reroll D6?"
                else -> null
            }
        },
        PickupRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Pickup the Ball"
                else -> null
            }
        },

        RushRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Rush"
                else -> null
            }
        },
        RushRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Rush Result or Reroll D6?"
                else -> null
            }
        },
        RushRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Rush"
                else -> null
            }
        },

        ScatterRoll.RollDice to { _, _, _ ->
            "Scatter Roll"
        },

        SecureTheBallRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Secure the Ball"
                else -> null
            }
        },
        SecureTheBallRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Secure the Ball Result or Reroll D6?"
                else -> null
            }
        },
        SecureTheBallRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Secure the Ball"
                else -> null
            }
        },

        ShadowingRoll.RollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to shadow player"
                else -> null
            }
        },
        ShadowingRoll.ChooseReRollSource to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient) -> "Accept Shadowing Result or Reroll D6?"
                else -> null
            }
        },
        ShadowingRoll.ReRollDie to { isActiveClient, serverDiceRolls, state ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to shadow player"
                else -> null
            }
        },

        UseShadowingStep.CheckIfShadowingIsAvailable to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Select player to use Shadowing"
                false -> "Waiting for player to use Shadowing"
            }
        },

        Pickup.ChooseToUseBigHand to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Big Hand?"
                false -> "Waiting for player to use Big Hand"
            }
        },
        SecureTheBallStep.ChooseToUseBigHand to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Big Hand?"
                false -> "Waiting for player to use Big Hand"
            }
        },

        JumpStep.ChooseToUseVeryLongLegs to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Very Long Legs?"
                false -> "Waiting for player to use Very Long Legs"
            }
        },

        JumpRoll.RollDie to { isActiveClient, serverDiceRolls, _ ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Roll D6 to Jump"
                else -> null
            }
        },
        JumpRoll.ChooseReRollSource to { isActiveClient, _, _ ->
            when {
                (isActiveClient) -> "Accept Jump Result or Reroll D6?"
                else -> null
            }
        },
        JumpRoll.ReRollDie to { isActiveClient, serverDiceRolls, _ ->
            when {
                (isActiveClient && !serverDiceRolls) -> "Re-roll D6 to Jump?"
                else -> null
            }
        },
        TheKickOff.ChooseToUseKick to { isActiveClient, _, state ->
            val d6 = state.getContext<DeviateRollContext>().deviateRoll.last() as D6Result
            when (isActiveClient) {
                true -> "Use Kick to reduce distance from ${d6.value} (D6) to ${d6.toD3().value} (D3)?"
                false -> "Waiting for opponent to use Kick"
            }
        },

        ArmourRoll.ChooseToUseDirtyPlayer to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Dirty Player on Armour Roll?"
                false -> "Waiting for opponent to use Dirty Player"
            }
        },
        InjuryRoll.ChooseToUseDirtyPlayer to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Dirty Player on Injury Roll?"
                false -> "Waiting for opponent to use Dirty Player"
            }
        },

        UseBB11Apothecary.ChooseToUseApothecary to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Apothecary?"
                false -> "Waiting for opponent to use Apothecary"
            }
        },

        UseBB7Apothecary.ChooseToUseApothecary to { isActiveClient, _, _ ->
            when (isActiveClient) {
                true -> "Use Apothecary?"
                false -> "Waiting for opponent to use Apothecary"
            }
        }
    )

    private fun isActiveStep(actionProvider: UiActionProvider): Boolean {
        return when (actionProvider) {
            is LocalActionProvider -> true
            is P2PActionProvider -> actionProvider.currentClientIsCreatingAction()
            else -> error("Unsupported action provider: $actionProvider")
        }
    }

    // Set a game status message for the current game state. This is done by going back
    // through the chain of procedure nodes, using the first node that returns an message
    // to show.
    // If we already have an automated action, we will skip showing a message as it might cause flickering.
    fun applyMessage(
        actionProvider: UiActionProvider,
        acc: UiSnapshotAccumulator
    ) {
        val serverDiceRolls = state.rules.diceRollsOwner == DiceRollOwner.ROLL_ON_SERVER
        val isActiveCoach = isActiveStep(actionProvider)
        val stack = acc.gameController.stack
        var currentIndex = 0
        var currentNode: Node? = stack.currentNode()
        var message: String? = null
        val automatedAction = actionProvider.hasQueuedActions()
        while (!automatedAction && currentNode != null && message == null) {
            val messageFactory = messageFactories[currentNode]
            if (messageFactory != null) {
                message = messageFactory(isActiveCoach, serverDiceRolls, state)
            }
            if (message == null) {
                currentIndex -= 1
                currentNode = stack.getOrNull(currentIndex)?.currentNode()
            }
        }
        acc.setGameStatusText(message)
    }
}

