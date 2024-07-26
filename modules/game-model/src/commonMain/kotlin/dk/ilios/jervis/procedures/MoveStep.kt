package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerMoveLeft
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for handling a player moving 1 square as part of a Move, Blitz, Pass, Hand-Off or Foul action.
 *
 * Jumping are handled in [JumpStep]
 */
object MoveStep: Procedure() {
    override val initialNode: Node = CheckTargetSquare
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    object CheckTargetSquare: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val moveTo = state.moveStepTarget!!.second
            val movingPlayer = state.activePlayer!!
            val pickupBall = state.field[moveTo].ball != null
            return compositeCommandOf(
                SetPlayerMoveLeft(movingPlayer, movingPlayer.moveLeft - 1),
                SetPlayerLocation(movingPlayer, moveTo),
                if (pickupBall) {
                    GotoNode(PickUpBall)
                } else {
                    ExitProcedure()
                }
            )
        }
    }

    // If a player moved into the ball as part of the action, they must pick it up.
    object PickUpBall: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Pickup
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}