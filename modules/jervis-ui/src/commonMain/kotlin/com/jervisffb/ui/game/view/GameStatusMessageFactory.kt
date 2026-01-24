package com.jervisffb.ui.game.view

import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.AccuracyRoll
import com.jervisffb.engine.rules.bb2025.procedures.skills.ShadowingRoll
import com.jervisffb.engine.rules.bb2025.procedures.skills.UseShadowingStep
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.CatchRoll
import com.jervisffb.engine.rules.common.procedures.DeviateRoll
import com.jervisffb.engine.rules.common.procedures.PickupRoll
import com.jervisffb.engine.rules.common.procedures.ScatterRoll
import com.jervisffb.engine.rules.common.procedures.TheKickOff
import com.jervisffb.engine.rules.common.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.RushRoll
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.LocalActionProvider
import com.jervisffb.ui.game.state.P2PActionProvider
import com.jervisffb.ui.game.state.UiActionProvider

/**
 * Class responsible for setting the game status message for the current step.
 *
 * For Hotseat games it will always show the message for the active coach.
 * For P2P games, each client will see the message relevant for the respective
 * coach.
 */
class GameStatusMessageFactory(private val state: Game) {

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
        while (currentNode != null && message == null) {
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

