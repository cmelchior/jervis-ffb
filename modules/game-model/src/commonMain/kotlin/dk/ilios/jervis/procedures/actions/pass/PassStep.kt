package dk.ilios.jervis.procedures.actions.pass

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.Bounce
import dk.ilios.jervis.procedures.Catch
import dk.ilios.jervis.procedures.DeviateRoll
import dk.ilios.jervis.procedures.DeviateRollContext
import dk.ilios.jervis.procedures.Scatter
import dk.ilios.jervis.procedures.ScatterRollContext
import dk.ilios.jervis.procedures.ThrowIn
import dk.ilios.jervis.procedures.ThrowInContext
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.Range
import dk.ilios.jervis.rules.tables.Weather
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Procedure for handling the passing part of a [PassAction].
 *
 * See page 48 in the rulebook.
 */
object PassStep: Procedure() {
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PassContext>()
    }
    override val initialNode: Node = DeclareTargetSquare
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object DeclareTargetSquare: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PassContext>().thrower.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<PassContext>()
            val targetSquares = context.thrower.location.coordinate.getSurroundingCoordinates(rules, rules.rangeRuler.maxDistance)
                .filter {
                    val range = rules.rangeRuler.measure(context.thrower.location.coordinate, it)
                    when (range) {
                        Range.PASSING_PLAYER -> false
                        Range.QUICK_PASS -> true
                        Range.SHORT_PASS -> true
                        Range.LONG_PASS -> state.weather != Weather.BLIZZARD
                        Range.LONG_BOMB -> state.weather != Weather.BLIZZARD
                        Range.OUT_OF_RANGE -> false
                    }
                }
                .map { SelectFieldLocation.throwTarget(it) }
            return targetSquares + listOf(CancelWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Cancel -> ExitProcedure() // Abort the throw
                else -> {
                    checkTypeAndValue<FieldSquareSelected>(state, rules, action, this) {
                        val context = state.getContext<PassContext>()
                        val distance = rules.rangeRuler.measure(context.thrower.location.coordinate, it.coordinate)
                        compositeCommandOf(
                            SetContext(context.copy(target = it.coordinate, range = distance)),
                            SetBallState.accurateThrow(), // Until proven otherwise. Should we invent a new type?
                            SetBallLocation(it.coordinate),
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
            return compositeCommandOf(
                SetBallState.accurateThrow(),
                SetBallLocation(context.target!!),
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
            return compositeCommandOf(
                SetBallState.scattered(),
                SetBallLocation(context.target!!),
                SetContext(ScatterRollContext(from = context.target ))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Scatter
        override fun onExitNode(state: Game, rules: Rules): Command {
            // The opposite team can now run interference. How this is done
            // depends on if the scattered ball is about to go out of bounds or not.
            val context = state.getContext<ScatterRollContext>()
            return if (context.outOfBoundsAt != null) {
                compositeCommandOf(
                    SetBallState.outOfBounds(context.outOfBoundsAt),
                    SetBallLocation(FieldCoordinate.OUT_OF_BOUNDS),
                    RemoveContext<ScatterRollContext>(),
                    GotoNode(AttemptPassingInterferenceBeforeGoingOutOfBounds)
                )
            } else {
                compositeCommandOf(
                    SetBallState.scattered(),
                    SetBallLocation(context.landsAt!!),
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
            return compositeCommandOf(
                SetBallState.deviating(),
                SetBallLocation(passContext.thrower.location.coordinate),
                SetContext(DeviateRollContext(passContext.thrower.location.coordinate))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = DeviateRoll

        override fun onExitNode(state: Game, rules: Rules): Command {
            // The opposite team can now run interference. How this is done
            // depends on if the deviated ball is about to go out of bounds or not.
            val context = state.getContext<DeviateRollContext>()
            return if (context.outOfBoundsAt != null) {
                compositeCommandOf(
                    SetBallState.outOfBounds(context.outOfBoundsAt),
                    SetBallLocation(FieldCoordinate.OUT_OF_BOUNDS),
                    RemoveContext<DeviateRollContext>(),
                    GotoNode(AttemptPassingInterferenceBeforeGoingOutOfBounds)
                )
            } else {
                compositeCommandOf(
                    SetBallState.deviating(),
                    SetBallLocation(context.landsAt!!),
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
            return compositeCommandOf(
                SetBallState.bouncing(),
                SetBallLocation(state.getContext<PassContext>().thrower.location.coordinate)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetTurnOver(true),
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
            return SetOldContext(Game::passingInteferenceContext, PassingInteferenceContext(
                thrower = passContext.thrower,
                target = state.ball.outOfBoundsAt!!,
            ))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PassingInterferenceStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If the ball was not deflected, it will continue going out of bounds.
            // If it was successfully deflected. Regardless of that outcome, the thrower's action
            // is over.
            val context = state.passingInteferenceContext!!
            return if (context.didDeflect || context.didIntercept) {
                // TODO Unclear exactly which information we need to retain from passing interference, and how
                compositeCommandOf(
                    SetContext(state.getContext<PassContext>().copy(passingInterference = context)),
                    SetOldContext(Game::passingInteferenceContext, null),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    SetContext(state.getContext<PassContext>().copy(passingInterference = context)),
                    SetOldContext(Game::passingInteferenceContext, null),
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
            return SetOldContext(Game::passingInteferenceContext, PassingInteferenceContext(
                thrower = passContext.thrower,
                target = state.ball.location,
            ))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PassingInterferenceStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If the ball was not deflected, it will continue to land at the target location.
            // If it was successfully deflected. Regardless of that outcome, the thrower's action
            // is over.
            val context = state.passingInteferenceContext!!
            return if (context.didDeflect || context.didIntercept) {
                // TODO Unclear exactly which information we need to retain from passing interference, and how
                compositeCommandOf(
                    SetContext(state.getContext<PassContext>().copy(passingInterference = context)),
                    SetOldContext(Game::passingInteferenceContext, null),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    SetContext(state.getContext<PassContext>().copy(passingInterference = context)),
                    SetOldContext(Game::passingInteferenceContext, null),
                    GotoNode(ResolveBounceOrCatch)
                )
            }
        }
    }

    /**
     * The ball was on its way out of bounds and was not deflected.
     * The ball will continue going out of bounds
     */
    object ResolveGoingOutOfBounds: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            return SetContext(ThrowInContext(state.ball.outOfBoundsAt!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If the ball didn't end up getting caught by the throwers team, it is a turnover.
            // Otherwise, the throwers team can continue their turn.
            val passContext = state.getContext<PassContext>()
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                if (!rules.teamHasBall(passContext.thrower.team)) SetTurnOver(true) else null,
                ExitProcedure()
            )
        }
    }

    /**
     * The ball reached its target location and will either bounce or will be attempted
     * to be caught.
     */
    object ResolveBounceOrCatch: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            return if (state.ballSquare.player != null) {
                SetBallState.scattered()
            } else {
                SetBallState.bouncing()
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return if (state.ballSquare.player != null) {
                Catch
            } else {
                Bounce
            }
        }

        // TODO How to check for Star Player Points
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PassContext>()
            return compositeCommandOf(
                if (!rules.teamHasBall(context.thrower.team)) SetTurnOver(true) else null,
                ExitProcedure()
            )
        }
    }
}
