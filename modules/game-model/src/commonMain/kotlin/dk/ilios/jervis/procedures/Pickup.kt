package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetRollContext
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.DiceModifier
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.reports.ReportPickup
import dk.ilios.jervis.rules.Rules

enum class PickupModifier(override val modifier: Int, override val description: String) : DiceModifier {
    POURING_RAIN(-1, "Pouring Rain"),
    MARKED(-1, "Marked"),
}

data class PickupRollContext(
    val player: Player,
    val diceRollTarget: Int,
    val modifiers: List<DiceModifier>,
) {
    // The sum of modifiers
    fun diceModifier(): Int = modifiers.fold(0) { acc: Int, el: DiceModifier -> acc + el.modifier }
}

data class PickupRollResultContext(
    val player: Player,
    val target: Int,
    val modifiers: List<DiceModifier>,
    val roll: D6DieRoll,
    val success: Boolean,
) {
    val rerolled: Boolean = roll.rerollSource != null && roll.rerolledResult != null
}

/**
 * Resolve a Pickup, i.e when a player moves into a field where the ball is placed..
 *
 * See page XX in the rulebook.
 */
object Pickup : Procedure() {
    override val initialNode: Node = RollToPickup

    override fun isValid(
        state: Game,
        rules: Rules,
    ) {
        super.isValid(state, rules)
        if (state.ball.state != BallState.ON_GROUND) {
            throw IllegalStateException("Ball is not on the ground, but ${state.ball.state}")
        }
        if (state.activePlayer?.location != state.ball.location) {
            throw IllegalStateException(
                "Active player is not on the ball: ${state.activePlayer?.location} vs. ${state.ball.location}",
            )
        }
    }

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command {
        // Determine target and modifiers for the Catch roll
        val pickupPlayer = state.field[state.ball.location].player!!
        val diceRollTarget = pickupPlayer.agility
        val modifiers = mutableListOf<DiceModifier>()
        // TODO Check for disturbing presence (maybe).

        // Check for field being marked
        rules.addMarkedModifiers(state, pickupPlayer.team, state.ballSquare, modifiers)
        val rollContext = PickupRollContext(pickupPlayer, diceRollTarget, modifiers)
        return compositeCommandOf(
            SetRollContext(Game::pickupRollContext, rollContext),
        )
    }

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? {
        return compositeCommandOf(
            SetRollContext(Game::pickupRollContext, null),
            SetRollContext(Game::pickupRollResultContext, null)
        )
    }

    object RollToPickup : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = PickupRoll

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            val result = state.pickupRollResultContext!!
            return if (result.success) {
                compositeCommandOf(
                    SetBallState.carried(result.player),
                    ReportPickup(result.player, result.target, result.modifiers, result.roll.result, true),
                    ExitProcedure(),
                )
            } else {
                compositeCommandOf(
                    SetBallState.bouncing(),
                    ReportPickup(result.player, result.target, result.modifiers, result.roll.result, false),
                    GotoNode(PickupFailed),
                )
            }
        }
    }

    object PickupFailed : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = Bounce

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command {
            return compositeCommandOf(
                state.activePlayer?.let { SetTurnOver(true) },
                ExitProcedure(), // This is copied from Catch, which has a comment about it.
            )
        }
    }
}
