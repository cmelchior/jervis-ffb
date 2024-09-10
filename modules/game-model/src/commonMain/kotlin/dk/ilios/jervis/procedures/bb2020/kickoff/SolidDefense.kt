package dk.ilios.jervis.procedures.bb2020.kickoff

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.ConfirmWhenReady
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
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_ACTION

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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
                    checkTypeAndValue<PlayerSelected>(state, rules, action, this) {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
                    .map { SelectFieldLocation.setup(it.coordinate) }

            val playerCoordinates = context.currentPlayer!!.location.coordinate
            return freeFields + SelectFieldLocation.setup(playerCoordinates)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkTypeAndValue<FieldSquareSelected>(state, rules, action, this) { squareSelected ->
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(ConfirmWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return GotoNode(SelectPlayerOrEndSetup)
        }
    }
}
