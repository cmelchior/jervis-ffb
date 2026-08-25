package com.jervisffb.engine.bb2025.procedures.table.kickoff

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.context.AlertDefenseContext
import com.jervisffb.engine.common.procedures.dicerolls.createFinalAtLeastObservation
import com.jervisffb.engine.common.reports.ReportAlertDefenseResult
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.MovePlayerIntoSquareContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler


/**
 * Procedure for handling the Kick-Off Event: "Alert Defense".
 *
 * See page 11 in Spike 22.
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
 *
 * TODO Alert Defense behave like Quick Snap, but for the defense. The logic here needs to be reworked
 */
object AlertDefense : Procedure(), ChanceObservationHandler {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<AlertDefenseContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3, type = DiceRollType.ALERT_DEFENSE))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D3Result>(action) { d3 ->
                val extraPlayerCount = getExtraPlayersCount(state)
                val chanceObservation = createFinalAtLeastObservation(
                    state = state,
                    team = state.kickingTeam,
                    rollType = DiceRollType.ALERT_DEFENSE,
                    die = d3,
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.ALERT_DEFENSE, d3),
                    chanceObservation?.let(::AddChanceObservation),
                    AddContext(AlertDefenseContext(roll = d3, extraPlayers = 1)),
                    ReportAlertDefenseResult(state.kickingTeam, d3, extraPlayerCount),
                    GotoNode(SelectPlayerOrEndSetup),
                )
            }
        }
    }

    object SelectPlayerOrEndSetup: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Max D3 + 1 players must be selected; once a player has moved, it cannot move again
            val context = state.getContext<AlertDefenseContext>()
            val extraPlayerCount = getExtraPlayersCount(state)
            return if (context.playersMoved.size >= context.roll.value + extraPlayerCount) {
                listOf(EndSetupWhenReady)
            } else {
                // Already moved players can no longer move, otherwise all open players are eligible.
                val eligiblePlayers = state.kickingTeam
                    .filter { rules.isStanding(it) }
                    .filter { rules.isOpen(it) }
                    .toSet() - context.playersMoved.toSet()
                when (eligiblePlayers.isNotEmpty()) {
                    true -> listOf(SelectPlayer.fromPlayers(eligiblePlayers), EndSetupWhenReady)
                    false -> listOf(EndSetupWhenReady)
                }
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndSetup -> ExitProcedure()
                else -> {
                    castAction<PlayerSelected>(action) {
                        val context = state.getContext<AlertDefenseContext>()
                        compositeCommandOf(
                            UpdateContext(context.copy(currentPlayer = it.getPlayer(state))),
                            GotoNode(SelectSquare),
                        )
                    }
                }
            }
        }
    }

    object SelectSquare: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<AlertDefenseContext>()
            val currentLocation = context.currentPlayer!!.coordinates
            // Player is allowed to move into any square next to it
            return currentLocation.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
                .filter { state.pitch[it].isUnoccupied() }
                .map { TargetSquare.setup(it) }
                .let { unOccupiedSquares ->
                    listOf(SelectPitchLocation(unOccupiedSquares + TargetSquare.setup(currentLocation)))
                }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<PitchSquareSelected>(action) { squareSelected ->
                val context = state.getContext<AlertDefenseContext>()
                return if (squareSelected.coordinate == context.currentPlayer!!.coordinates) {
                    // If the same square is selected, just treat the player as not having moved at all
                    compositeCommandOf(
                        UpdateContext(context.copy(currentPlayer = null)),
                        GotoNode(SelectPlayerOrEndSetup),
                    )
                } else {
                    compositeCommandOf(
                        UpdateContext(context.copy(target = squareSelected.coordinate)),
                        GotoNode(MovePlayer),
                    )
                }
            }
        }
    }

    /**
     * Move the player into the target square.
     *
     * Developer's Commentary:
     * This takes into account all rules that might affect this, like Treacherous Trapdoors.
     * The rules are unclear if this is actually the case, but if it didn't apply here, it
     * should also not apply to e.g. Blitz which would be a bit weird.
     */
    object MovePlayer: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<AlertDefenseContext>()
            return AddContext(
                MovePlayerIntoSquareContext(
                    player = context.currentPlayer!!,
                    target = context.target!!
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.movePlayerIntoSquare
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<AlertDefenseContext>()
            val updatedPlayersMoved = context.playersMoved + context.currentPlayer!!
            return compositeCommandOf(
                RemoveContext<MovePlayerIntoSquareContext>(),
                UpdateContext(context.copy(
                    playersMoved = updatedPlayersMoved,
                    currentPlayer = null,
                    target = null,
                )),
                // Automatically exit Alert Defense when no more players can be moved
                if (updatedPlayersMoved.size == (context.roll.value + getExtraPlayersCount(state))) {
                    ExitProcedure()
                } else {
                    GotoNode(SelectPlayerOrEndSetup)
                }
            )
        }
    }

    //
    // HELPER FUNCTIONS
    //
    private fun getExtraPlayersCount(state: Game): Int {
        require(state.rules.gameType == GameType.BB7) { "Invalid game type: ${state.rules.gameType}" }
        // Since Alert Defense is only used in BB7, the number of players moved
        // is always D3 + 1.
        return 1
    }
}
