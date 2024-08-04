package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.reports.ReportBounce
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.Direction

/**
 * Resolve a Bounce.
 *
 * See page 25 in the rulebook.
 */
object Bounce : Procedure() {
    override val initialNode: Node = RollDirection

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? {
        if (state.ball.state != BallState.BOUNCING) {
            throw IllegalStateException("Ball is not bouncing, but ${state.ball.state}")
        }
        return null
    }

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object RollDirection : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D8))
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkType<D8Result>(action) { d8 ->
                val direction: Direction = rules.direction(d8)
                val newLocation: FieldCoordinate = state.ball.location.move(direction, 1)
                val outOfBounds: Boolean = newLocation.isOutOfBounds(rules)
                val playerAtTarget: Player? = if (!outOfBounds) state.field[newLocation].player else null
                val nextNode: Command =
                    if (outOfBounds) {
                        // TODO Throw-in or trigger touchback
                        // TODO For kick-offs, bouncing across the halfline is also considered out-of-bounds
                        compositeCommandOf(
                            SetBallState.outOfBounds(state.ball.location),
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
                    SetBallLocation(newLocation),
                    ReportBounce(newLocation, if (outOfBounds) state.ball.location else null),
                    nextNode,
                )
            }
        }
    }

    object ResolveLandingOnTheGround : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            return compositeCommandOf(
                SetBallState.onGround(),
                ExitProcedure(),
            )
        }
    }

    object ResolveThrowIn : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = ThrowIn

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command = ExitProcedure()
    }

    object ResolveBounce : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = Bounce

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command = ExitProcedure()
    }

    object ResolveCatch : ParentNode() {
        override fun getChildProcedure(
            state: Game,
            rules: Rules,
        ): Procedure = Catch

        override fun onExitNode(
            state: Game,
            rules: Rules,
        ): Command = ExitProcedure()
    }
}
