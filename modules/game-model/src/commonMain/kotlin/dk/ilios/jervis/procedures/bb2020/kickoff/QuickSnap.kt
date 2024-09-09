package dk.ilios.jervis.procedures.bb2020.kickoff

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndSetupWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.actions.move.MovePlayerIntoSquare
import dk.ilios.jervis.procedures.actions.move.MovePlayerIntoSquareContext
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportQuickSnapResult
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType

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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
                    checkTypeAndValue<PlayerSelected>(state, rules, action, this) {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<QuickSnapContext>()
            val currentLocation = context.currentPlayer!!.location.coordinate
            // Player is allowed to move into any square next to it
            return currentLocation.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
                .filter { state.field[it].isUnoccupied() }
                .map { SelectFieldLocation.setup(it) } + SelectFieldLocation.setup(currentLocation)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkTypeAndValue<FieldSquareSelected>(state, rules, action, this) { squareSelected ->
                val context = state.getContext<QuickSnapContext>()
                return if (squareSelected.coordinate == context.currentPlayer!!.location.coordinate) {
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
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val context = state.getContext<QuickSnapContext>()
            return SetContext(MovePlayerIntoSquareContext(
                player = context.currentPlayer!!,
                target = context.target!!
            ))
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
