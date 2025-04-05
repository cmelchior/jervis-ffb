package com.jervisffb.engine.rules.bb2020.procedures.tables.prayers

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.PrayersToNuffleRollContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.utils.INVALID_ACTION

data class ThrowARockContext(
    val stallingPlayers: List<Player>,
    val currentPlayer: Player? = null
): ProcedureContext

/**
 * Procedure for handling the Prayer to Nuffle "Throw a Rock" at the end of a drive where it was active.
 *
 * Developer's Comments:
 * Does Throw a Rock also work in the dogout? For now we assume the answer is no.
 */
object ResolveThrowARock : Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        // Check for stalling players on the team ending their turn. Any
        // stalling players risk getting hit.
        // It is unclear if people in the DogOut can be hit by a Rock, for now we are
        // not checking for it, which means it would be allowed. But due to how
        // Stalling is defined, it will probably never happen.
        val stallingPlayers = state.activeTeam.filter { it.isStalling && it.location.isOnField(rules) }
        return SetContext(ThrowARockContext(stallingPlayers = stallingPlayers))
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<ThrowARockContext>()
    }

    object SelectPlayer: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PrayersToNuffleRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<ThrowARockContext>()
            return if (context.stallingPlayers.isEmpty()) {
                listOf(ContinueWhenReady)
            } else {
                context.stallingPlayers.map {
                    SelectPlayer(it)
                }
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Continue -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.getContext<ThrowARockContext>()
                    val updatedContext = context.copy(
                        stallingPlayers = if (context.stallingPlayers.size == 1) {
                            emptyList()
                        } else {
                            context.stallingPlayers.dropLast(1)
                        },
                        currentPlayer = context.stallingPlayers.last()
                    )
                    compositeCommandOf(
                        SetContext(updatedContext),
                        GotoNode(RollDie)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ThrowARockContext>().currentPlayer!!.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<ThrowARockContext>()
                val player = context.currentPlayer!!
                return if (d6.value >= 5) {
                    compositeCommandOf(
                        ReportDiceRoll(DiceRollType.THROW_A_ROCK, d6),
                        SetPlayerState(player, PlayerState.KNOCKED_DOWN, hasTackleZones = false),
                        ReportGameProgress("${state.activeTeam} hit ${player.name} with a rock"),
                        GotoNode(ResolveInjuryByRock),
                    )
                } else {
                    compositeCommandOf(
                        ReportDiceRoll(DiceRollType.THROW_A_ROCK, d6),
                        ReportGameProgress("${state.activeTeam} ignores ${player.name}"),
                        GotoNode(SelectPlayer),
                    )
                }
            }
        }
    }

    object ResolveInjuryByRock: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val throwContext = state.getContext<ThrowARockContext>()
            return SetContext(
                RiskingInjuryContext(
                    player = throwContext.currentPlayer!!,
                    mode = RiskingInjuryMode.HIT_BY_ROCK
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                GotoNode(SelectPlayer)
            )
        }
    }
}
