package dk.ilios.jervis.procedures.bb2020.kickoff

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "HighKick" as described on page 41
 * of the rulebook.
 *
 *  Developer's Commentary:
 *  Following the strict ordering of the rules, the Kick-Off Event is resolved
 *  before "What Goes Up, Must Come Down". This means that the touchback rule cannot
 *  yet be applied when High Kick is resolved.
 *
 *  No-where is it stated that the high kick player cannot enter the opponent's field.
 *  So in theory, it would be allowed to move a player into the opponent's field and
 *  then resolve the ball coming down.
 *
 *  This would result in a touchback, and the the ball could be given to the player
 *  that moved into the opponent's half.
 */
object HighKick : Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SelectPlayer : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val openPlayers = state.receivingTeam
                .filter { rules.isOpen(it) }
                .map { SelectPlayer(it) }

            return if (
                state.ball.location.isOnField(rules) &&
                state.ballSquare.isUnoccupied() &&
                openPlayers.isNotEmpty()
            ) {
                openPlayers
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("No player could be selected for High Kick"),
                        ExitProcedure(),
                    )

                }
                else -> {
                    checkTypeAndValue<PlayerSelected>(state, rules, action, this) {
                        compositeCommandOf(
                            SetPlayerLocation(it.getPlayer(state), state.ball.location),
                            ReportGameProgress("${it.getPlayer(state).name} had time to move under the ball due to a High Kick"),
                            ExitProcedure()
                        )
                    }
                }
            }
        }
    }
}
