package com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff

import compositeCommandOf
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.Scatter
import com.jervisffb.engine.rules.bb2020.procedures.ScatterRollContext
import com.jervisffb.engine.rules.bb2020.procedures.WeatherRoll
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.Weather

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
            return SetContext(
                ScatterRollContext(
                ball = state.singleBall(),
                from = state.singleBall().location
            )
            )
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
