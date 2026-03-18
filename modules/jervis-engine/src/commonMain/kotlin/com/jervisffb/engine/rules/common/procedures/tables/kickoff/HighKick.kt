package com.jervisffb.engine.rules.common.procedures.tables.kickoff

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "HighKick"
 *
 * See page 41 in the BB2020 rulebook.
 * See page 48 in the BB2025 rulebook.
 *
 * Developer's Commentary:
 * Following the strict ordering of the rules, the Kick-Off Event is resolved
 * before "What Goes Up...". This means that the touchback rule cannot yet be
 * applied when High Kick is resolved.
 *
 * No-where is it stated that the high kick player cannot enter the opponent's
 * side. This means it is allowed to move a player into the opponent's field and
 * then resolve the ball coming down.
 *
 * This would result in a touchback, and the ball could be given to the
 * player that moved into the opponent's half.
 */
object HighKick : Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SelectPlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val openPlayers = state.receivingTeam
                .filter { it.location.isOnField(rules) && rules.isOpen(it) }
                .let {
                    if (it.isEmpty()) {
                        null
                    } else {
                        com.jervisffb.engine.actions.SelectPlayer.fromPlayers(it)
                    }
                }

            val ball = state.currentBall()
            return if (
                ball.coordinates.isOnField(rules) &&
                state.field[ball.coordinates].isUnoccupied() &&
                openPlayers != null
            ) {
                listOf(openPlayers)
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
                    castAction<PlayerSelected>(action) {
                        compositeCommandOf(
                            SetPlayerLocation(it.getPlayer(state), state.currentBall().coordinates),
                            ReportGameProgress("${it.getPlayer(state).name} had time to move under the ball due to a High Kick"),
                            ExitProcedure()
                        )
                    }
                }
            }
        }
    }
}
