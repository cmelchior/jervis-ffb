package com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportQuickSnapResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MovePlayerIntoSquare
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MovePlayerIntoSquareContext
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType

data class QuickSnapContext(
    val roll: D3Result,
    // Track all players moved, should be size <= roll + 3
    val playersMoved: Set<Player> = emptySet(),
    // Current player being moved
    val currentPlayer: Player? = null,
    val target: FieldCoordinate? = null,
): ProcedureContext

/**
 * Procedure for handling the Kick-Off Event: "Quick Snap" as described on page 41
 * of the rulebook.
 */
object QuickSnap : Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<QuickSnapContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D3Result>(action) { d3 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.QUICK_SNAP, d3),
                    SetContext(QuickSnapContext(roll = d3)),
                    ReportQuickSnapResult(state.receivingTeam, d3),
                    GotoNode(SelectPlayerOrEndSetup),
                )
            }
        }
    }

    object SelectPlayerOrEndSetup: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Max D3 + 3 players must be selected, once a player has moved, it cannot move again
            val context = state.getContext<QuickSnapContext>()
            return if (context.playersMoved.size >= context.roll.value + 3) {
                listOf(EndSetupWhenReady)
            } else {
                // Already moved players can no longer move, otherwise all open players are eligible.
                val eligiblePlayers = state.receivingTeam
                    .filter { it.isStanding(rules) }
                    .filter { rules.isOpen(it) }
                    .toSet() - context.playersMoved.toSet()
                eligiblePlayers.map { SelectPlayer(it) } + EndSetupWhenReady
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndSetup -> ExitProcedure()
                else -> {
                    checkTypeAndValue<PlayerSelected>(state, rules, action) {
                        val context = state.getContext<QuickSnapContext>()
                        compositeCommandOf(
                            SetContext(context.copy(currentPlayer = it.getPlayer(state))),
                            GotoNode(SelectSquare),
                        )
                    }
                }
            }
        }
    }

    object SelectSquare: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<QuickSnapContext>()
            val currentLocation = context.currentPlayer!!.coordinates
            // Player is allowed to move into any square next to it
            return currentLocation.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
                .filter { state.field[it].isUnoccupied() }
                .map { TargetSquare.setup(it) }
                .let { unOccupiedSquares ->
                    listOf(SelectFieldLocation(unOccupiedSquares + TargetSquare.setup(currentLocation)))
                }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkTypeAndValue<FieldSquareSelected>(state, rules, action) { squareSelected ->
                val context = state.getContext<QuickSnapContext>()
                return if (squareSelected.coordinate == context.currentPlayer!!.coordinates) {
                    // If the same field is selected, just treat the player as not having moved at all
                    compositeCommandOf(
                        SetContext(context.copy(currentPlayer = null)),
                        GotoNode(SelectPlayerOrEndSetup),
                    )
                } else {
                    compositeCommandOf(
                        SetContext(context.copy(target = squareSelected.coordinate)),
                        GotoNode(MovePlayer),
                    )
                }
            }
        }
    }

    /**
     * Move the player into target square.
     *
     * Developer's Commentary:
     * This takes into account all rules that might affect this, like Treacherous Trapdoors.
     * The rules are unclear if this is actually the case, but if it didn't apply here, it
     * should also not apply to e.g. Blitz which would be a bit weird.
     */
    object MovePlayer: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<QuickSnapContext>()
            return SetContext(
                MovePlayerIntoSquareContext(
                    player = context.currentPlayer!!,
                    target = context.target!!
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = MovePlayerIntoSquare
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<QuickSnapContext>()
            val updatedPlayersMoved = context.playersMoved + context.currentPlayer!!
            return compositeCommandOf(
                RemoveContext<MovePlayerIntoSquareContext>(),
                SetContext(context.copy(
                    playersMoved = updatedPlayersMoved,
                    currentPlayer = null,
                    target = null,
                )),
                // Automatically exit Quick Snap when no more players can be moved
                if (updatedPlayersMoved.size == (context.roll.value + 3)) {
                    ExitProcedure()
                } else {
                    GotoNode(SelectPlayerOrEndSetup)
                }
            )
        }
    }
}
