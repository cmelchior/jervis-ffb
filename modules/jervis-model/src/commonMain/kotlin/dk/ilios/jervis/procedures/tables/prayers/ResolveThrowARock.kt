package dk.ilios.jervis.procedures.tables.prayers

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryContext
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryMode
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryRoll
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_ACTION

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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
            return when(action) {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
