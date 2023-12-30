package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.reports.ReportBounce
import dk.ilios.jervis.rules.Direction
import dk.ilios.jervis.rules.Rules

/**
 * Resolve a Bounce.
 *
 * See page 25 in the rulebook.
 */
object Bounce: Procedure() {
    override val initialNode: Node = RollDirection
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollDirection: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D8))
        }

        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
            return checkType<D8Result>(action) { d8 ->
                val direction: Direction = rules.randomDirection(d8)
                val newLocation = state.ball.location.move(direction, 1)
                val outOfBounds = newLocation.isOutOfBounds(rules)
                val nextNode = if (outOfBounds) {
                    // TODO Throw-in or trigger touchback
                    compositeCommandOf(
                        SetBallState.outOfBounds(state.ball.location),
                        if (state.abortIfBallOutOfBounds) {
                            ExitProcedure()
                        } else {
                            GotoNode(ResolveThrowIn)
                        }
                    )
                } else {
                    val eligiblePlayerForCatching = state.field[newLocation].player?.let {
                        it.state == PlayerState.STANDING && it.hasTackleZones
                    } ?: false
                    if (eligiblePlayerForCatching) {
                        GotoNode(ResolveBounce)
                    } else {
                        GotoNode(ResolveCatch)
                    }
                }
                return compositeCommandOf(
                    SetBallLocation(newLocation),
                    ReportBounce(newLocation, if (outOfBounds) state.ball.location else null),
                    nextNode
                )
            }
        }
    }

    object ResolveThrowIn: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }

    object ResolveBounce: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }

    object ResolveCatch: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }
}