package dk.ilios.jervis.procedures.actions.pass

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
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
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE


/**
 * Procedure for handling the passing part of a [PassAction].
 *
 * See page 48 in the rulebook.
 */
object PassStep: Procedure() {
    override val initialNode: Node = DeclareTargetSquare
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        if (state.passContext == null) {
            INVALID_GAME_STATE("Missing pass context")
        }
        return null
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object DeclareTargetSquare: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.passContext!!
            val targetSquares = context.thrower.location.coordinate.getSurroundingCoordinates(rules, rules.rangeRuler.maxDistance)
                .filter { rules.rangeRuler.measure(context.thrower.location.coordinate, it) != Range.OUT_OF_RANGE }
                .map { SelectFieldLocation.throwTarget(it) }
            return targetSquares + listOf(CancelWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Cancel -> {
                    // Abort the throw
                    ExitProcedure()
                }
                is FieldSquareSelected -> {
                    val context = state.passContext!!
                    // We should only accept valid ranges, it is considered an error to pass in
                    // invalid ranges here
                    val distance = rules.rangeRuler.measure(
                        context.thrower.location.coordinate, action.coordinate
                    ). also {
                        if (it == Range.OUT_OF_RANGE) INVALID_GAME_STATE("Invalid target: ${action.coordinate}")
                    }
                    compositeCommandOf(
                        SetOldContext(Game::passContext, context.copy(target = action.coordinate, range = distance)),
                        SetBallState.accurateThrow(), // Until proven otherwise. Should we invent a new type?
                        SetBallLocation(action.coordinate),
                        GotoNode(TestForAccuracy)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object TestForAccuracy: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = AccuracyRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.passContext!!
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
            val context = state.passContext!!
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
            val context = state.passContext!!
            return compositeCommandOf(
                SetBallState.scattered(),
                SetBallLocation(context.target!!),
                SetOldContext(Game::scatterRollContext, ScatterRollContext(from = context.target ))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Scatter
        override fun onExitNode(state: Game, rules: Rules): Command {
            // The opposite team can now run interference. How this is done
            // depends on if the scattered ball is about to go out of bounds or not.
            val context = state.scatterRollContext!!
            return if (context.outOfBoundsAt != null) {
                compositeCommandOf(
                    SetBallState.outOfBounds(context.outOfBoundsAt),
                    SetBallLocation(FieldCoordinate.OUT_OF_BOUNDS),
                    SetOldContext(Game::scatterRollContext, null),
                    GotoNode(AttemptPassingInterferenceBeforeGoingOutOfBounds)
                )
            } else {
                compositeCommandOf(
                    SetBallState.scattered(),
                    SetBallLocation(context.landsAt!!),
                    SetOldContext(Game::scatterRollContext, null),
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
            val passContext = state.passContext!!
            return compositeCommandOf(
                SetBallState.deviating(),
                SetBallLocation(passContext.thrower.location.coordinate),
                SetOldContext(Game::deviateRollContext, DeviateRollContext(passContext.thrower.location.coordinate))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = DeviateRoll

        override fun onExitNode(state: Game, rules: Rules): Command {
            // The opposite team can now run interference. How this is done
            // depends on if the deviated ball is about to go out of bounds or not.
            val context = state.deviateRollContext!!
            return if (context.outOfBoundsAt != null) {
                compositeCommandOf(
                    SetBallState.outOfBounds(context.outOfBoundsAt),
                    SetBallLocation(FieldCoordinate.OUT_OF_BOUNDS),
                    SetOldContext(Game::deviateRollContext, null),
                    GotoNode(AttemptPassingInterferenceBeforeGoingOutOfBounds)
                )
            } else {
                compositeCommandOf(
                    SetBallState.deviating(),
                    SetBallLocation(context.landsAt!!),
                    SetOldContext(Game::deviateRollContext, null),
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
                SetBallLocation(state.passContext!!.thrower.location.coordinate)
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
            val passContext = state.passContext!!
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
                    SetOldContext(Game::passContext, state.passContext!!.copy(passingInterference = context)),
                    SetOldContext(Game::passingInteferenceContext, null),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    SetOldContext(Game::passContext, state.passContext!!.copy(passingInterference = context)),
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
            val passContext = state.passContext!!
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
                    SetOldContext(Game::passContext, state.passContext!!.copy(passingInterference = context)),
                    SetOldContext(Game::passingInteferenceContext, null),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    SetOldContext(Game::passContext, state.passContext!!.copy(passingInterference = context)),
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
            return SetOldContext(Game::throwInContext, ThrowInContext(state.ball.outOfBoundsAt!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If the ball didn't end up getting caught by the throwers team, it is a turnover.
            // Otherwise, the throwers team can continue their turn.
            val passContext = state.passContext!!
            return compositeCommandOf(
                SetOldContext(Game::throwInContext, null),
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
            val context = state.passContext!!
            return compositeCommandOf(
                if (!rules.teamHasBall(context.thrower.team)) SetTurnOver(true) else null,
                ExitProcedure()
            )
        }
    }
}
