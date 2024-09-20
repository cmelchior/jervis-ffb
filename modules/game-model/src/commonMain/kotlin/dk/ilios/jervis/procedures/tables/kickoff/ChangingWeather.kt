package dk.ilios.jervis.procedures.tables.kickoff

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.procedures.Scatter
import dk.ilios.jervis.procedures.ScatterRollContext
import dk.ilios.jervis.procedures.WeatherRoll
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.Weather

/**
 * Procedure for handling the Kick-Off Event: "Changing Weather" as described on page 41
 * of the rulebook.
 */
object ChangingWeather : Procedure() {
    override val initialNode: Node = ChangeWeather
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        return ReportGameProgress("Rolled Changing Weather")
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object ChangeWeather : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = WeatherRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If the ball is not out-of-bounds already it scatters further
            return if (
                state.weather == Weather.PERFECT_CONDITIONS &&
                state.singleBall().location.isOnField(rules)
            ) {
                GotoNode(ScatterBall)
            } else {
                ExitProcedure()
            }
        }
    }

    object ScatterBall : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return SetContext(ScatterRollContext(
                ball = state.singleBall(),
                from = state.singleBall().location
            ))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Scatter
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ScatterRollContext>()
            val ball = state.singleBall()
            return if (context.outOfBoundsAt != null) {
                compositeCommandOf(
                    SetBallState.outOfBounds(ball, context.outOfBoundsAt),
                    SetBallLocation(ball, FieldCoordinate.OUT_OF_BOUNDS),
                    RemoveContext<ScatterRollContext>(),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    SetBallState.scattered(ball),
                    SetBallLocation(ball, context.landsAt!!),
                    RemoveContext<ScatterRollContext>(),
                    ExitProcedure()
                )
            }
        }
    }
}
