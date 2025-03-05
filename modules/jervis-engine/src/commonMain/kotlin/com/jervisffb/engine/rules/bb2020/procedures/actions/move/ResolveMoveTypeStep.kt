package com.jervisffb.engine.rules.bb2020.procedures.actions.move

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayerContext
import com.jervisffb.engine.rules.bb2020.procedures.Pickup

/**
 * Procedure for handling a player moving "one step". "One step" is categorized as
 * any of the following moves:
 *
 *  - Normal move
 *  - Standing Up,
 *  - Rush
 *  - Jump
 *  - Leap
 *
 *  Turnovers are handled in the procedures that called this one.
 *
 *  --------------
 *  Developer's Notes:
 *
 *  To make it easier to handle the different kinds of movement, it is required
 *  to select the type before choosing the target.
 *
 *  This means a normal move is represented as
 *  [MoveType.STANDARD, Square(x,y), MoveType.STANDARD, Square(x1, y1), ...]
 *
 * The other option would have been to calculate all possible targets and
 * then enhance the field location with that type data. This was considered, but
 * rejected, because it would lead to a lot of calculations on each move,
 * especially if you also want to support the UI moving multiple steps in one go.
 *
 * Mixing the two would fundamentally mean that UI logic bleeds into the rules
 * layer, which is also not desirable.
 *
 * Even though the move logic looks a little more ugly due to this, we can
 * hide it in the UI layer using automated actions.
 *
 * The FUMBBL Client also has different UI for e.g., rushing (yellow square) vs.
 * Jump (context menu action).
 *
 * Jumping are handled in [JumpStep]
 * Standing up are handled in [StandingUpStep]
 */
object ResolveMoveTypeStep : Procedure() {
    override val initialNode: Node = ResolveMove
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<MoveContext>()

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return when(val moveType = state.getContext<MoveContext>().moveType) {
                MoveType.STANDARD -> StandardMoveStep
                MoveType.STAND_UP -> StandingUpStep
                MoveType.JUMP -> JumpStep
                MoveType.LEAP -> TODO("Not supported: $moveType")
            }
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            val activeContext = state.getContext<ActivatePlayerContext>()
            val endNow = activeContext.activationEndsImmediately || state.isTurnOver()
            val player = moveContext.player
            val pickupBall = (
                player.isStanding(rules) &&
                    state.field[player.location as FieldCoordinate].balls.isNotEmpty() &&
                    state.field[player.location as FieldCoordinate].balls.all { it.state == BallState.ON_GROUND }
            )

            return if (pickupBall && !endNow) {
                compositeCommandOf(
                    if (moveContext.hasMoved) SetContext(activeContext.copy(markActionAsUsed = true)) else null,
                    GotoNode(PickUpBall)
                )
            } else if (endNow) {
                compositeCommandOf(
                    if (moveContext.hasMoved) SetContext(activeContext.copy(markActionAsUsed = true)) else null,
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    if (moveContext.hasMoved) SetContext(activeContext.copy(markActionAsUsed = true)) else null,
                    GotoNode(CheckForScoring)
                )
            }
        }
    }

    // If a player moved into the ball as part of the action, they must pick it up.
    object PickUpBall : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val ball = state.field[context.player.coordinates].balls.single()
            return SetCurrentBall(ball)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure =
            Pickup
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                GotoNode(CheckForScoring)
            )
        }
    }

    // Finally, once all rolls have been resolved, check if the moving player is scoring
    // a touch down
    object CheckForScoring : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            return SetContext(ScoringATouchDownContext(context.player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }

}
