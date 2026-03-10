package com.jervisffb.engine.rules.common.procedures.tables.kickoff

import com.jervisffb.engine.actions.ConfirmWhenReady
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
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.KickOffEventContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.tables.KickOffEvent
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class SolidDefenseContext(
    val roll: D3Result,
    // Track all players moved, should be size <= roll + 3/1
    val playersMoved: Set<Player> = emptySet(),
    // Current player being moved
    val currentPlayer: Player? = null,
): ProcedureContext

/**
 * Procedure for handling the Kick-Off Event: "Solid Defense".
 *
 * See page 41 in the BB2020 rulebook.
 * See page 48 in the BB2025 rulebook.
 *
 * It also supports the BB7 variant of the event, which is described on page 94
 * in Death Zone (2020).
 *
 * Developer's Commentary:
 * The rules say to remove the selected players from the pitch and place them
 * again. This feels needlessly annoying, so instead we simply track the number
 * of players selected and allow moving them around on the pitch as much as the
 * coach would like.
 *
 * Also, as a convenience, if you select a player and put it in the same
 * location, it doesn't count against the limit. This only happens when they
 * move to a new location (after which they can move as many times as the coach
 * would like).
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
            return castDiceRoll<D3Result>(action) { d3 ->
                val extraPlayerCount = getExtraPlayersCount(state)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.SOLID_DEFENSE, d3),
                    AddContext(SolidDefenseContext(roll = d3)),
                    ReportGameProgress("Solid Defense: ${state.kickingTeam.name} may move [${d3.value} + $extraPlayerCount = ${d3.value + extraPlayerCount}] players"),
                    GotoNode(SelectPlayerOrEndSetup),
                )
            }
        }
    }

    object SelectPlayerOrEndSetup: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Max D3 + 3/1 players must be selected, including those in the playersMoved list.
            // If the player is not already in the playersMoved list, they must be open.
            val extraPlayerCount = getExtraPlayersCount(state)
            val context = state.getContext<SolidDefenseContext>()
            return if (context.playersMoved.size >= context.roll.value + extraPlayerCount) {
                // Max number of players has already been moved. So only they can move now.
                listOf(SelectPlayer.fromPlayers(context.playersMoved), EndSetupWhenReady)
            } else {
                // All already selected players can move regardless of them being open or not.
                // All other players must be open to be able to move
                val eligiblePlayers = state.kickingTeam
                    .filter { rules.isStanding(it) }
                    .filter { rules.isOpen(it) }
                    .toSet() + context.playersMoved.toSet()
                listOf(SelectPlayer.fromPlayers(eligiblePlayers), EndSetupWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndSetup -> GotoNode(EndSetupAndValidate)
                else -> {
                    castAction<PlayerSelected>(action) {
                        val context = state.getContext<SolidDefenseContext>()
                        compositeCommandOf(
                            UpdateContext(context.copy(currentPlayer = it.getPlayer(state))),
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
            val freeFields: List<TargetSquare> =
                state.field
                    .filter {rules.isInSetupArea(state.kickingTeam, it) }
                    .filter { it.isUnoccupied() }
                    .map { TargetSquare.setup(it.coordinates) }

            val playerCoordinates = context.currentPlayer!!.coordinates
            return listOf(
                SelectFieldLocation(freeFields + TargetSquare.setup(playerCoordinates))
            )
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<FieldSquareSelected>(action) { squareSelected ->
                when (state.kickingTeam.isHomeTeam()) {
                    true -> if (squareSelected.coordinate.isOnAwaySide(rules)) INVALID_ACTION(action)
                    false -> if (squareSelected.coordinate.isOnHomeSide(rules)) INVALID_ACTION(action)
                }
                val context = state.getContext<SolidDefenseContext>()
                val movingPlayer = context.currentPlayer!!
                val isPlayerMoved = (movingPlayer.location != squareSelected.coordinate)
                // Do not count the player if they are just placed in their original position
                if (isPlayerMoved) {
                    compositeCommandOf(
                        SetPlayerLocation(movingPlayer, squareSelected.coordinate),
                        UpdateContext(
                            context.copy(
                                currentPlayer = null,
                                playersMoved = context.playersMoved.plus(movingPlayer)
                            )
                        ),
                        GotoNode(SelectPlayerOrEndSetup),
                    )
                } else {
                    compositeCommandOf(
                        UpdateContext(context.copy(currentPlayer = null)),
                        GotoNode(SelectPlayerOrEndSetup),
                    )
                }
            }
        }
    }

    object EndSetupAndValidate : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return if (rules.isSetupValid(state, state.kickingTeam).isEmpty()) {
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

    //
    // HELPER FUNCTIONS
    //
    private fun getExtraPlayersCount(state: Game): Int {
        val context = state.getContext<KickOffEventContext>()
        val type = context.result as? KickOffEvent ?: INVALID_GAME_STATE("Unexpected table result: ${context.result}")
        return when (type) {
            KickOffEvent.SOLID_DEFENSE -> 3
            KickOffEvent.SOLID_DEFENSE_BB7 -> 1
            else -> INVALID_GAME_STATE("Unsupported Kickoff Event: ${type.name}")
        }
    }
}
