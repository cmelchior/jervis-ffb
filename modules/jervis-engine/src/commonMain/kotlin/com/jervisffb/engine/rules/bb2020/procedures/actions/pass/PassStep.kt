package com.jervisffb.engine.rules.bb2020.procedures.actions.pass

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.NoOpCommand
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.PassingInterferenceContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportPassResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.rules.bb2020.procedures.Catch
import com.jervisffb.engine.rules.bb2020.procedures.DeviateRoll
import com.jervisffb.engine.rules.bb2020.procedures.DeviateRollContext
import com.jervisffb.engine.rules.bb2020.procedures.ScatterRoll
import com.jervisffb.engine.rules.bb2020.procedures.ScatterRollContext
import com.jervisffb.engine.rules.bb2020.procedures.ThrowIn
import com.jervisffb.engine.rules.bb2020.procedures.ThrowInContext
import com.jervisffb.engine.rules.bb2020.tables.Range
import com.jervisffb.engine.rules.bb2020.tables.Weather
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for handling the passing part of a [PassAction].
 *
 * See page 48 in the rulebook.
 */
object PassStep: Procedure() {
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
            val targetSquares = context.thrower.coordinates.getSurroundingCoordinates(rules, rules.rangeRuler.MAX_DISTANCE)
                .filter {
                    val range = rules.rangeRuler.measure(context.thrower, it)
                    when (range) {
                        Range.PASSING_PLAYER -> false
                        Range.QUICK_PASS -> true
                        Range.SHORT_PASS -> true
                        Range.LONG_PASS -> state.weather != Weather.BLIZZARD
                        Range.LONG_BOMB -> state.weather != Weather.BLIZZARD
                        Range.OUT_OF_RANGE -> false
                    }
                }
                .map { TargetSquare.throwTarget(it) }
                .let { SelectFieldLocation(it) }
            return listOf(targetSquares, CancelWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Cancel -> ExitProcedure() // Abort the throw
                else -> {
                    checkTypeAndValue<FieldSquareSelected>(state, action) {
                        val context = state.getContext<PassContext>()
                        val distance = rules.rangeRuler.measure(context.thrower, it.coordinate)
                        val ball = context.thrower.ball!!
                        val newLocation = it.coordinate
                        compositeCommandOf(
                            ReportPassResult(context),
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = AccuracyRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return when (context.passingResult) {
                PassingType.ACCURATE -> GotoNode(ResolveAccuratePass)
                PassingType.INACCURATE -> GotoNode(ResolveInaccuratePass)
                PassingType.WILDLY_INACCURATE -> GotoNode(ResolveWildlyInaccuratePass)
                PassingType.FUMBLED -> GotoNode(ResolveFumbledPass)
                null -> INVALID_GAME_STATE("Missing passing result value")
            }
        }
    }

    object ResolveAccuratePass: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // Ball was successfully thrown to the target square.
            // Move the ball and check for interference
            val context = state.getContext<PassContext>()
            val ball = state.currentBall()
            return compositeCommandOf(
                SetBallState.accurateThrow(ball),
                SetBallLocation(ball, context.target!!),
                GotoNode(AttemptPassingInterference)
            )
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
                SetBallState.scattered(ball),
                SetBallLocation(ball, context.target!!),
                SetContext(
                    ScatterRollContext(
                        ball = ball,
                        from = context.target)
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
                    SetBallState.outOfBounds(ball, context.outOfBoundsAt),
                    SetBallLocation(ball, FieldCoordinate.OUT_OF_BOUNDS),
                    SetContext(passContext.copy(target = FieldCoordinate.OUT_OF_BOUNDS)),
                    RemoveContext<ScatterRollContext>(),
                    GotoNode(AttemptPassingInterferenceBeforeGoingOutOfBounds)
                )
            } else {
                compositeCommandOf(
                    SetBallState.scattered(ball),
                    SetBallLocation(ball, context.landsAt!!),
                    SetContext(passContext.copy(target = context.landsAt)),
                    RemoveContext<ScatterRollContext>(),
                    GotoNode(AttemptPassingInterference)
                )
            }
        }
    }

    /**
     * If the pass is Wildly Accurate, the ball will deviate from the thrower's location.
     */
    object ResolveWildlyInaccuratePass: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val passContext = state.getContext<PassContext>()
            val ball = state.currentBall()
            return compositeCommandOf(
                SetBallState.deviating(ball),
                SetBallLocation(ball, passContext.thrower.coordinates),
                SetContext(DeviateRollContext(passContext.thrower.coordinates))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = DeviateRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            // The opposite team can now run interference. How this is done
            // depends on if the deviated ball is about to go out of bounds or not.
            val context = state.getContext<DeviateRollContext>()
            val passContext = state.getContext<PassContext>()
            val ball = state.currentBall()
            return if (context.outOfBoundsAt != null) {
                compositeCommandOf(
                    SetBallState.outOfBounds(ball, context.outOfBoundsAt),
                    SetBallLocation(ball, FieldCoordinate.OUT_OF_BOUNDS),
                    SetContext(passContext.copy(target = FieldCoordinate.OUT_OF_BOUNDS)),
                    RemoveContext<DeviateRollContext>(),
                    GotoNode(AttemptPassingInterferenceBeforeGoingOutOfBounds)
                )
            } else {
                compositeCommandOf(
                    SetBallState.deviating(ball),
                    SetBallLocation(ball, context.landsAt!!),
                    SetContext(passContext.copy(target = context.landsAt)),
                    RemoveContext<DeviateRollContext>(),
                    GotoNode(AttemptPassingInterference)
                )
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
                SetBallLocation(ball, state.getContext<PassContext>().thrower.coordinates)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetTurnOver(TurnOver.STANDARD),
                ExitProcedure()
            )
        }
    }

    /**
     * Attempt to interfere with a pass that is about to go out of bounds. If successful,
     * the ball doesn't go out of bounds (at least not due to the original pass).
     *
     * Designer's Commentary: If the ball goes out of bounds. Passing Interference is checked at
     * the square just before the ball goes out of bounds.
     */
    object AttemptPassingInterferenceBeforeGoingOutOfBounds: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val passContext = state.getContext<PassContext>()
            val interferenceContext = PassingInterferenceContext(
                thrower = passContext.thrower,
                target = state.currentBall().outOfBoundsAt!!,
            )
            return SetContext(interferenceContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PassingInterferenceStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If the ball was not deflected, it will continue going out of bounds.
            // If it was successfully deflected. Regardless of that outcome, the thrower's action
            // is over.
            val context = state.getContext<PassingInterferenceContext>()
            return if (!context.continueThrow) {
                compositeCommandOf(
                    SetContext(state.getContext<PassContext>().copy(passingInterference = context)),
                    RemoveContext<PassingInterferenceContext>(),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    SetContext(state.getContext<PassContext>().copy(passingInterference = context)),
                    RemoveContext<PassingInterferenceContext>(),
                    GotoNode(ResolveGoingOutOfBounds)
                )
            }
        }
    }

    /**
     * Attempt to interfere with a pass that landed on the field without the ball going out of bounds first.
     */
    object AttemptPassingInterference: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val passContext = state.getContext<PassContext>()
            val interferenceContext = PassingInterferenceContext(
                thrower = passContext.thrower,
                target = state.currentBall().location,
            )
            return SetContext(interferenceContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PassingInterferenceStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If the ball was not deflected, it will continue to land at the target location.
            // If it was successfully deflected. Regardless of that outcome, the thrower's action
            // is over.
            val context = state.getContext<PassingInterferenceContext>()
            return if (!context.continueThrow) {
                compositeCommandOf(
                    SetContext(state.getContext<PassContext>().copy(passingInterference = context)),
                    RemoveContext<PassingInterferenceContext>(),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    SetContext(state.getContext<PassContext>().copy(passingInterference = context)),
                    RemoveContext<PassingInterferenceContext>(),
                    GotoNode(ResolveBounceOrCatch)
                )
            }
        }
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
                if (!rules.teamHasBall(passContext.thrower.team, state.currentBall())) SetTurnOver(TurnOver.STANDARD) else null,
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
            val canCatch = playerInSquare?.let { rules.canCatch(state, it) } ?: false
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
                if (!rules.teamHasBall(context.thrower.team, state.currentBall())) SetTurnOver(TurnOver.STANDARD) else null,
                ExitProcedure()
            )
        }
    }
}
