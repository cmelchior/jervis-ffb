package com.jervisffb.engine.rules.bb2025.procedures.actions.pass

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.NoOpCommand
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.reports.ReportStartingPass
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.Catch
import com.jervisffb.engine.rules.common.procedures.ScatterRoll
import com.jervisffb.engine.rules.common.procedures.ScatterRollContext
import com.jervisffb.engine.rules.common.procedures.ThrowIn
import com.jervisffb.engine.rules.common.procedures.ThrowInContext
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassAction
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for handling the passing part of a [PassAction] when using [HailMary].
 *
 * See page 70 in the BB2025 rulebook.
 * See page 129 in the BB2025 rulebook.
 */
object HailMaryPassStep: Procedure() {
    override val initialNode: Node = DeclareTargetSquare
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PassContext>()
        state.currentBall()
    }

    object DeclareTargetSquare: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PassContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PassContext>()
            val targetSquares = buildList {
                for (x in 0 until state.rules.fieldWidth) {
                    for (y in 0 until state.rules.fieldHeight) {
                        val square = state.field[x, y]
                        if (square.player != context.thrower) {
                            add(TargetSquare.throwTarget(square.coordinates))
                        }
                    }
                }
            }
            return listOf(SelectFieldLocation(targetSquares), CancelWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Cancel -> ExitProcedure() // Abort the throw
                else -> {
                    castAction<FieldSquareSelected>(action) {
                        val context = state.getContext<PassContext>()
                        val distance = Range.LONG_BOMB
                        val ball = context.thrower.ball!!
                        val newLocation = it.coordinate
                        compositeCommandOf(
                            ReportStartingPass(context),
                            SetContext(
                                context.copy(
                                    target = newLocation,
                                    range = distance
                                )
                            ),
                            SetBallState.accurateThrow(ball), // Until proven otherwise. Should we invent a new type?
                            SetBallLocation(ball, newLocation),
                            GotoNode(TestForAccuracy)
                        )
                    }
                }
            }
        }
    }

    object TestForAccuracy: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PassAccuracyRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return when (val result = context.passingResult) {
                PassingType.INACCURATE -> GotoNode(ResolveInaccuratePass)
                PassingType.FUMBLED -> GotoNode(ChooseToUseSafePass)
                else -> INVALID_GAME_STATE("Unsupported passing result: $result")
            }
        }
    }

    /**
     * If the pass is Inaccurate, the ball will scatter from the target location.
     */
    object ResolveInaccuratePass: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            // Ball was inaccurate. It goes to the target square and then scatters.
            val context = state.getContext<PassContext>()
            val ball = state.currentBall()
            return compositeCommandOf(
                SetBallState.Companion.scattered(ball),
                SetBallLocation(ball, context.target!!),
                SetContext(
                    ScatterRollContext(
                        from = context.target
                    )
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScatterRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            // The opposite team can now run interference. How this is done
            // depends on if the scattered ball is about to go out of bounds or not.
            val context = state.getContext<ScatterRollContext>()
            val passContext = state.getContext<PassContext>()
            val ball = state.currentBall()
            return if (context.outOfBoundsAt != null) {
                compositeCommandOf(
                    SetBallState.Companion.outOfBounds(ball, context.outOfBoundsAt),
                    SetBallLocation(ball, context.landsAt!!),
                    SetContext(passContext.copy(target = context.landsAt)),
                    RemoveContext<ScatterRollContext>(),
                    GotoNode(ResolveGoingOutOfBounds)
                )
            } else {
                compositeCommandOf(
                    SetBallState.Companion.scattered(ball),
                    SetBallLocation(ball, context.landsAt!!),
                    SetContext(passContext.copy(target = context.landsAt)),
                    RemoveContext<ScatterRollContext>(),
                    GotoNode(ResolveBounceOrCatch)
                )
            }
        }
    }

    object ChooseToUseSafePass: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<PassContext>().thrower.team
        }

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PassContext>()
            val player = context.thrower
            val hasSafePass = player.isSkillAvailable(SkillType.SAFE_PASS)
            // Safe Pass requires a natural 1 on the dice. It doesn't work on a modified 1 and below.
            val isSafePassEligible = (context.passingRoll?.result == 1.d6)
            val isFumble = (context.passingResult == PassingType.FUMBLED)
            return when (hasSafePass && isSafePassEligible && isFumble) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            val ball = state.currentBall()
            val useSafePass = (action == Confirm)

            return if (useSafePass) {
                compositeCommandOf(
                    ReportSkillUsed(context.thrower, SkillType.SAFE_PASS),
                    SetBallState.carried(ball, context.thrower),
                    SetContext(context.copy(useSafePass = true)),
                    ExitProcedure()
                )
            } else {
                GotoNode(ResolveFumbledPass)
            }
        }
    }

    /**
     * If the pass is fumbled, the ball will bounce from the thrower's location
     * and a turnover happens. Regardless of who, if any, catches the ball.
     */
    object ResolveFumbledPass: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            val passContext = state.getContext<PassContext>()
            return compositeCommandOf(
                SetBallState.bouncing(ball),
                SetContext(passContext.copy(target = null)),
                SetBallLocation(ball, state.getContext<PassContext>().thrower.coordinates),
                SetTurnOver(TurnOver.STANDARD), // A Fumbled Pass is always a turn-over, regardless of where the ball lands
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }

    /**
     * The ball was on its way out of bounds and was not deflected.
     * The ball will continue going out of bounds.
     */
    object ResolveGoingOutOfBounds: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            return SetContext(ThrowInContext(ball, ball.outOfBoundsAt!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If the ball didn't end up getting caught by the throwers team, it is a turnover.
            // Otherwise, the throwers team can continue their turn.
            val passContext = state.getContext<PassContext>()
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                if (!rules.teamHasBall(
                        passContext.thrower.team,
                        state.currentBall()
                    )
                ) SetTurnOver(TurnOver.STANDARD) else null,
                ExitProcedure()
            )
        }
    }

    /**
     * The ball reached its target location and will either bounce or will be attempted
     * to be caught.
     */
    object ResolveBounceOrCatch: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            val playerInSquare = state.field[ball.location].player
            val canCatch = playerInSquare?.let { rules.canCatch(it) } ?: false
            return if (!canCatch) {
                SetBallState.bouncing(ball)
            } else {
                NoOpCommand
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return if (state.currentBall().state == BallState.BOUNCING) {
                Bounce
            } else {
                Catch
            }
        }

        // TODO How to check for Star Player Points
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return compositeCommandOf(
                if (!rules.teamHasBall(
                        context.thrower.team,
                        state.currentBall()
                    )
                ) SetTurnOver(TurnOver.STANDARD) else null,
                ExitProcedure()
            )
        }
    }
}
