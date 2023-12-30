package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportCatch
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Resolve a player attempting to catch the ball.
 *
 * This can be used as a placeholder during development or testing.
 */
object Catch: Procedure() {
    override val initialNode: Node = AttemptCatch
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        // Check that this is only called on a standing player with tacklezones
        val ballLocation = state.ball.location
        if (!rules.canCatch(state, state.field[ballLocation].player!!)) {
            INVALID_GAME_STATE("Player is not eligible for catching the ball")
        }
        return null
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    // TODO Automatically catch the ball
    object AttemptCatch: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val player = state.field[state.ball.location].player!!
            return compositeCommandOf(
                SetBallState.carried(player),
                ReportCatch(player),
                ExitProcedure()
            )
        }
    }
}