package com.jervisffb.engine.rules.bb2025.procedures.tables.kickoff

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.commands.AddPlayerStatModifier
import com.jervisffb.engine.commands.AddPlayerTemporaryEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.DodgySnackContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.modifiers.KickoffStatModifier
import com.jervisffb.engine.model.modifiers.TemporaryEffect
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "Dodgy Snack" as described on page
 * 48 of the BB2025 rulebook.
 *
 * Developer's Commentary:
 * It isn't defined in the rules, which team resolves their roll first, so we
 * have just decided on the receiving team (it shouldn't matter either, since
 * there is currently no way to affect the rolls)
 */
object DodgySnack : Procedure() {
    override val initialNode: Node = KickingTeamRollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<DodgySnackContext>()

    object KickingTeamRollDie: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D6Result>(action) { d6 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.DODGY_SNACK_ROLL_OFF, d6),
                    ReportGameProgress("${state.kickingTeam.name} rolled [ ${d6.value} ]"),
                    SetContext(
                        DodgySnackContext(kickingTeamRoll = d6)
                    ),
                    GotoNode(ReceivingTeamRollDie),
                )
            }
        }
    }

    object ReceivingTeamRollDie: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D6Result>(action) { d6 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.DODGY_SNACK_ROLL_OFF, d6),
                    ReportGameProgress("${state.receivingTeam.name} rolled [ ${d6.value} ]"),
                    SetContext(state.getContext<DodgySnackContext>().copy(
                        receivingTeamRoll = d6,
                    )),
                    GotoNode(SelectPlayerFromReceivingTeam),
                )
            }
        }
    }

    object SelectPlayerFromReceivingTeam: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<DodgySnackContext>()
            // In case of a draw, both teams must select a player.
            return if (context.kickingTeamRoll.value >= context.receivingTeamRoll!!.value) {
                selectFromTeam(state.receivingTeam, rules)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                // The receiving team did not need to select a player, or no players from the
                // receiving team are on the field (very unlikely).
                Continue -> {
                    val availablePlayers = state.receivingTeam.any { it.location.isOnField(rules) }
                    compositeCommandOf(
                        if (!availablePlayers) ReportGameProgress("No players from ${state.receivingTeam.name} are on the field") else null,
                        GotoNode(SelectPlayerFromKickingTeam)
                    )
                }
                else -> {
                    val context = state.getContext<DodgySnackContext>()
                    checkTypeAndValue<RandomPlayersSelected>(state, action) { selectAction ->
                        val player = selectAction.getPlayers(state).first()
                        return compositeCommandOf(
                            SetContext(context.copy(kickingTeamPlayerSelected = player)),
                            GotoNode(RollForKickingTeamSelectedPlayer)
                        )
                    }
                }
            }
        }
    }

    object RollForKickingTeamSelectedPlayer: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DodgySnackContext>()
            return checkType<D6Result>(action) { d6 ->
                val player = context.kickingTeamPlayerSelected!!
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.DODGY_SNACK_EFFECT, d6),
                    createPlayerChangeCommands(player, d6),
                    SetContext(context.copy(kickingTeamSnackRoll = d6)),
                    GotoNode(SelectPlayerFromKickingTeam)
                )
            }
        }
    }

    object SelectPlayerFromKickingTeam: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<DodgySnackContext>()
            return if (context.kickingTeamRoll.value <= context.receivingTeamRoll!!.value) {
                selectFromTeam(state.kickingTeam, rules)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                // Kicking team did not need to select a player, or we have a special situation
                // where there was no players on the field.
                // Since all rolls have been made at this point, just exit
                Continue -> {
                    val availablePlayers = state.kickingTeam.any { it.location.isOnField(rules) }
                    compositeCommandOf(
                        if (!availablePlayers) ReportGameProgress("No players from ${state.kickingTeam.name} are on the field") else null,
                        ExitProcedure()
                    )
                }
                else -> {
                    val context = state.getContext<DodgySnackContext>()
                    checkTypeAndValue<RandomPlayersSelected>(state, action) { selectAction ->
                        val player = selectAction.getPlayers(state).first()
                        return compositeCommandOf(
                            SetContext(context.copy(receivingTeamPlayerSelected = player)),
                            GotoNode(RollForReceivingTemSelectedPlayer)
                        )
                    }
                }
            }
        }
    }

    object RollForReceivingTemSelectedPlayer: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DodgySnackContext>()
            return checkType<D6Result>(action) { d6 ->
                val player = context.receivingTeamPlayerSelected!!
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.DODGY_SNACK_EFFECT, d6),
                    createPlayerChangeCommands(player, d6),
                    SetContext(context.copy(receivingTeamSnackRoll = d6)),
                    ExitProcedure()
                )
            }
        }
    }

    private fun selectFromTeam(team: Team, rules: Rules): List<GameActionDescriptor> {
        return team
            .filter { it.location.isOnField(rules) }
            .let { players ->
                if (players.isNotEmpty()) {
                    listOf(
                        SelectRandomPlayers(1, players.map { it.id })
                    )
                } else {
                    listOf(ContinueWhenReady)
                }
            }
    }

    private fun createPlayerChangeCommands(player: Player, d6: D6Result): Command {
        return when (d6.value) {
            1 -> compositeCommandOf(
                SetPlayerState(player, PlayerState.DODGY_SNACK),
                SetPlayerLocation(player, DogOut),
                ReportGameProgress("${player.name} ate a bad snack and spends the drive on the lavatory")
            )
            else -> compositeCommandOf(
                AddPlayerTemporaryEffect(player, TemporaryEffect.dodgySnack()),
                AddPlayerStatModifier(player, KickoffStatModifier.DODGY_SNACK_MA),
                AddPlayerStatModifier(player, KickoffStatModifier.DODGY_SNACK_AV),
                ReportGameProgress("${player.name} ate a bad snack and does not feel well (-1 MA/AV)"),
            )
        }
    }
}
