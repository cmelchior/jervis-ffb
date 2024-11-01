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
 *  Turnovers are handled in procedures calling this one.
 *
 *  --------------
 *  Developer's Notes:
 *
 *  To make it easier to handle each type, it is required to select
 *  the type before choosing the target.
 *
 *  This means a normal move is represented as [NormalMove, (x,y), NormalMove, (x1, y1), ...].
 *
 * The other option would have been to calculate all possible targets and
 * then enhance the field location with type data. This was considered, but
 * rejected, because it would lead to a lot of calculations on each move.
 *
 * Mixing the two would fundamentally mean that UI logic bleeds into this
 * layer, which is also not desirable.
 *
 * The FUMBBL Client mixes the two options as e.g. rushes are shown on the field,
 * but you have to choose Jump as an action. The Jervis UI can choose which
 * route to go by using automated actions instead. Which allows us to change the
 * UI without touching this layer.
 *
 * Jumping are handled in [JumpStep]
 * Standing up are handled in [StandUpStep]
 */
object ResolveMoveTypeStep : Procedure() {
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<MoveContext>()

    }
    override val initialNode: Node = ResolveMove
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return when(val moveType = state.getContext<MoveContext>().moveType) {
                MoveType.STANDARD -> StandardMoveStep
                MoveType.STAND_UP -> StandUpStep
                MoveType.JUMP,
                MoveType.LEAP -> TODO("Not supported: $moveType")
            }
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            val activeContext = state.getContext<ActivatePlayerContext>()
            val player = moveContext.player
            val pickupBall = (
                player.isStanding(rules) &&
                state.field[player.location as FieldCoordinate].balls.isNotEmpty() &&
                state.field[player.location as FieldCoordinate].balls.all { it.state == BallState.ON_GROUND }
            )

            return if (pickupBall && !state.isTurnOver()) {
                compositeCommandOf(
                    if (moveContext.hasMoved) SetContext(activeContext.copy(markActionAsUsed = true)) else null,
                    GotoNode(PickUpBall)
                )
            } else {
                compositeCommandOf(
                    if (moveContext.hasMoved) SetContext(activeContext.copy(markActionAsUsed = true)) else null,
                    ExitProcedure()
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
                ExitProcedure()
            )
        }
    }
}
