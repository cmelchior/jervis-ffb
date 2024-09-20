package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.reports.ReportBounce
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.tables.Direction

/**
 * Resolve a Bounce.
 *
 * See page 25 in the rulebook.
 */
object Bounce : Procedure() {
    override val initialNode: Node = RollDirection
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val ball = state.currentBall()
        if (ball.state != BallState.BOUNCING) throw IllegalStateException("Ball is not bouncing, but ${ball.state}")
    }

    object RollDirection : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = null
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D8))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D8Result>(action) { d8 ->
                val direction: Direction = rules.direction(d8)
                val ball = state.currentBall()
                val newLocation: FieldCoordinate = ball.location.move(direction, 1)
                val outOfBounds: Boolean = newLocation.isOutOfBounds(rules)
                val playerAtTarget: Player? = if (!outOfBounds) state.field[newLocation].player else null
                val nextNode: Command =
                    if (outOfBounds) {
                        // TODO Throw-in or trigger touchback
                        // TODO For kick-offs, bouncing across the halfline is also considered out-of-bounds
                        compositeCommandOf(
                            SetBallState.outOfBounds(ball, ball.location),
                            if (state.abortIfBallOutOfBounds) {
                                ExitProcedure()
                            } else {
                                GotoNode(ResolveThrowIn)
                            },
                        )
                    } else if (playerAtTarget != null) {
                        val eligiblePlayerForCatching = rules.canCatch(state, playerAtTarget)
                        if (eligiblePlayerForCatching) {
                            GotoNode(ResolveCatch)
                        } else {
                            GotoNode(ResolveBounce)
                        }
                    } else {
                        GotoNode(ResolveLandingOnTheGround)
                    }

                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BOUNCE, d8),
                    SetBallLocation(ball, newLocation),
                    ReportBounce(newLocation, if (outOfBounds) ball.location else null),
                    nextNode,
                )
            }
        }
    }

    object ResolveLandingOnTheGround : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            return compositeCommandOf(
                SetBallState.onGround(ball),
                ExitProcedure(),
            )
        }
    }

    object ResolveThrowIn : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            return SetContext(ThrowInContext(
                ball = ball,
                outOfBoundsAt = ball.outOfBoundsAt!!,
            ))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                ExitProcedure()
            )
        }
    }

    object ResolveBounce : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }

    object ResolveCatch : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }
}
