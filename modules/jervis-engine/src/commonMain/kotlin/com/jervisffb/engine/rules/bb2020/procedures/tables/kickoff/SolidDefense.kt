package com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff

import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.commands.compositeCommandOf

data class SolidDefenseContext(
    val roll: D3Result,
    // Track all players moved, should be size <= roll + 3
    val playersMoved: Set<Player> = emptySet(),
    // Current player being moved
    val currentPlayer: Player? = null,
): ProcedureContext

/**
 * Procedure for handling the Kick-Off Event: "Solid Defense" as described on page 41
 * of the rulebook.
 */
object SolidDefense : Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<SolidDefenseContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D3Result>(action) { d3 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.SOLID_DEFENSE, d3),
                    SetContext(SolidDefenseContext(roll = d3)),
                    ReportGameProgress("Solid Defense: ${state.kickingTeam.name} may move [${d3.value} + 3 = ${d3.value + 3}] players"),
                    GotoNode(SelectPlayerOrEndSetup),
                )
            }
        }
    }

    object SelectPlayerOrEndSetup: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Max D3 + 3 players must be selected, including those in the playersMoved list.
            // If player is not already in the playersMoved list, they must be open.
            val context = state.getContext<SolidDefenseContext>()
            return if (context.playersMoved.size >= context.roll.value + 3) {
                // Max number of players has already been moved. So only they can move now.
                context.playersMoved.map { SelectPlayer(it) } + EndSetupWhenReady
            } else {
                // All already selected players can move regardless of them being open or not.
                // All other players must be open to be able to move
                val eligiblePlayers = state.kickingTeam
                    .filter { rules.isStanding(it) }
                    .filter { rules.isOpen(it) }
                    .toSet() + context.playersMoved.toSet()
                eligiblePlayers.map { SelectPlayer(it) } + EndSetupWhenReady
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndSetup -> GotoNode(EndSetupAndValidate)
                else -> {
                    checkTypeAndValue<PlayerSelected>(state, rules, action) {
                        val context = state.getContext<SolidDefenseContext>()
                        compositeCommandOf(
                            SetContext(context.copy(currentPlayer = it.getPlayer(state))),
                            GotoNode(PlacePlayer),
                        )
                    }
                }
            }
        }
    }

    object PlacePlayer: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<SolidDefenseContext>()
            // Allow players to be placed on the kicking teams side. At this stage, the more
            // elaborate rules are not enforced. That will first happen in `EndSetupAndValidate`
            val isHomeTeam = state.kickingTeam.isHomeTeam()
            val freeFields: List<SelectFieldLocation> =
                state.field
                    .filter {
                        // Only select from fields on teams half
                        // TODO How does this generalize to BB7?
                        if (isHomeTeam) {
                            it.x < rules.fieldWidth / 2
                        } else {
                            it.x >= rules.fieldWidth / 2
                        }
                    }
                    .filter { it.isUnoccupied() }
                    .map { SelectFieldLocation.setup(it.coordinates) }

            val playerCoordinates = context.currentPlayer!!.coordinates
            return freeFields + SelectFieldLocation.setup(playerCoordinates)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkTypeAndValue<FieldSquareSelected>(state, rules, action) { squareSelected ->
                when (state.kickingTeam.isHomeTeam()) {
                    true -> if (squareSelected.coordinate.isOnAwaySide(rules)) INVALID_ACTION(action)
                    false -> if (squareSelected.coordinate.isOnHomeSide(rules)) INVALID_ACTION(action)
                }
                val context = state.getContext<SolidDefenseContext>()
                val movingPlayer = context.currentPlayer!!
                compositeCommandOf(
                    SetPlayerLocation(movingPlayer, squareSelected.coordinate),
                    SetContext(context.copy(currentPlayer = null, playersMoved = context.playersMoved.plus(movingPlayer))),
                    GotoNode(SelectPlayerOrEndSetup),
                )
            }
        }
    }

    object EndSetupAndValidate : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return if (rules.isValidSetup(state, state.kickingTeam)) {
                ExitProcedure()
            } else {
                GotoNode(InformOfInvalidSetup)
            }
        }
    }

    object InformOfInvalidSetup : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(ConfirmWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return GotoNode(SelectPlayerOrEndSetup)
        }
    }
}
