package com.jervisffb.engine.rules.common.procedures.actions.move

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.ScoringATouchDownContext
import com.jervisffb.engine.model.context.SecureTheBallContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.JumpStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.LeapStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.PogoStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball.SecureTheBallStep
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.procedures.Pickup
import com.jervisffb.engine.utils.INVALID_GAME_STATE

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
 *  Turnovers should be handled in the parent procedure that called this one.
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
 * then enhance the field location with that type data. This was considered but
 * rejected because it would lead to a lot of calculations on each move,
 * especially if you also want to support the UI moving multiple steps in one go.
 *
 * Mixing the two would fundamentally mean that UI logic bleeds into the rule
 * layer, which is also not desirable.
 *
 * Even though the move logic looks a little more ugly due to this, we can
 * hide it in the UI layer using automated actions.
 *
 * The FUMBBL Client also has a different UI for e.g., rushing (yellow square)
 * vs. Jump (context menu action).
 *
 * Jumping are handled in [JumpStep]
 * Standing up are handled in [StandingUpStep]
 */
object ResolveMoveTypeStep : Procedure() {
    override val initialNode: Node = ResolveMove
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        return null
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<MoveContext>()

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return when (state.getContext<MoveContext>().moveType) {
                MoveType.STANDARD -> StandardMoveStep
                MoveType.STAND_UP -> StandingUpStep
                MoveType.JUMP -> {
                    when (rules.baseVersion) {
                        GameVersion.BB2020 -> JumpStep
                        GameVersion.BB2025 -> com.jervisffb.engine.rules.bb2025.procedures.actions.move.JumpStep
                    }
                }
                MoveType.LEAP -> {
                    when (rules.baseVersion) {
                        GameVersion.BB2020 -> TODO()
                        GameVersion.BB2025 -> LeapStep
                    }
                }
                MoveType.POGO -> {
                    when (rules.baseVersion) {
                        GameVersion.BB2020 -> TODO()
                        GameVersion.BB2025 -> PogoStep
                    }
                }
            }
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            val secureTheBallContext = state.getContextOrNull<SecureTheBallContext>()
            val activeContext = state.getContext<ActivatePlayerContext>()
            val endNow = state.endActionImmediately()
            val player = moveContext.player
            val pickupBall = (
                rules.isStanding(player)
                    && state.field[player.location as FieldCoordinate].balls.isNotEmpty()
                    && state.field[player.location as FieldCoordinate].balls.all { it.state == BallState.ON_GROUND }
            )
            val secureTheBall = (secureTheBallContext?.player === player) && pickupBall
            return when {
                // Securing the Ball takes precedence over picking up the ball.
                secureTheBall && !endNow -> {
                    compositeCommandOf(
                        if (moveContext.hasMoved) UpdateContext(activeContext.copyWithMarkedAction(true)) else null,
                        GotoNode(SecureTheBall)
                    )
                }
                pickupBall && !endNow -> {
                    compositeCommandOf(
                        if (moveContext.hasMoved) UpdateContext(activeContext.copyWithMarkedAction(true)) else null,
                        GotoNode(PickUpBall)
                    )
                }
                endNow -> {
                    compositeCommandOf(
                        if (moveContext.hasMoved) UpdateContext(activeContext.copyWithMarkedAction(true)) else null,
                        ExitProcedure()
                    )
                }
                else -> {
                    compositeCommandOf(
                        if (moveContext.hasMoved) UpdateContext(activeContext.copyWithMarkedAction(true)) else null,
                        when (player.hasBall()) {
                            true -> GotoNode(CheckForScoring)
                            false -> ExitProcedure()
                        }
                    )
                }
            }
        }
    }

    // If a player moved into the ball as part of a Secure The Ball action, they must now attempt to secure it.
    object SecureTheBall : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val ball = state.field[context.player.coordinates].balls.single()
            if (ball.coordinates != context.player.coordinates) {
                INVALID_GAME_STATE("Ball ${ball.coordinates} must be at ${context.player.coordinates}")
            }
            return SetCurrentBall(ball)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SecureTheBallStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            return compositeCommandOf(
                SetCurrentBall(null),
                if (context.player.hasBall()) GotoNode(CheckForScoring) else ExitProcedure()
            )
        }
    }

    // If a player moved into the ball as part of the action, they must pick it up.
    object PickUpBall : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val ball = state.field[context.player.coordinates].balls.single()
            if (!context.player.coordinates.overlap(ball.coordinates)) {
                INVALID_GAME_STATE("Ball ${ball.coordinates} must be at ${context.player.coordinates}")
            }
            return SetCurrentBall(ball)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Pickup
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Pickup is checking for pickup success/failure and touchdowns, so just
            // abort here.
            return compositeCommandOf(
                SetCurrentBall(null),
                ExitProcedure()
            )
        }
    }

    // Finally, once all rolls have been resolved, check if the moving player is scoring
    // a touchdown.
    object CheckForScoring : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            return AddContext(ScoringATouchDownContext(context.player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ScoringATouchDownContext>(),
                ExitProcedure()
            )
        }
    }
}
