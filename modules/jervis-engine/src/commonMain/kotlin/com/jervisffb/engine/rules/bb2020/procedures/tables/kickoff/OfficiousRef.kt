package com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff

import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
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
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.commands.compositeCommandOf

data class OfficiousRefContext(
    val kickingTeamRoll: D6Result,
    val kickingTeamFanFactor: Int,
    val kickingTeamResult: Int,
    val receivingTeamRoll: D6Result? = null,
    val receivingTeamFanFactor: Int = -1,
    val receivingTeamResult: Int = -1,
    val kickingTeamPlayerSelected: Player? = null,
    val receivingTeamPlayerSelected: Player? = null,
    val kickingTeamRefereeRoll: D6Result? = null,
    val receivingTeamRefereeRoll: D6Result? = null,
): ProcedureContext

/**
 * Procedure for handling the Kick-Off Event: "Officious Ref" as described on page 41
 * of the rulebook.
 *
 * Developer's Commentary:
 * It isn't defined in the rules, which team resolve their roll first, so we have just
 * decided on the receiving team (it shouldn't matter either, since there is currently no
 * way to affect the rolls)
 *
 * Also, each team and roll has gotten its own node, since all the permutations created a pretty big
 * mess in fewer nodes.
 */
object OfficiousRef : Procedure() {
    override val initialNode: Node = KickingTeamRollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<OfficiousRefContext>()

    object KickingTeamRollDie: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D6Result>(action) { d6 ->
                val fanFactor = state.kickingTeam.fanFactor
                val result =  d6.value + fanFactor
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.OFFICIOUS_REF_FAN_FACTOR, d6),
                    ReportGameProgress("${state.kickingTeam.name} rolled [ ${d6.value} + $fanFactor = $result ]"),
                    SetContext(
                        OfficiousRefContext(
                        kickingTeamRoll = d6,
                        kickingTeamFanFactor = fanFactor,
                        kickingTeamResult = result
                    )
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
                val fanFactor = state.receivingTeam.fanFactor
                val result =  d6.value + fanFactor
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.OFFICIOUS_REF_FAN_FACTOR, d6),
                    ReportGameProgress("${state.receivingTeam.name} rolled [ ${d6.value} + $fanFactor = $result ]"),
                    SetContext(state.getContext<OfficiousRefContext>().copy(
                        receivingTeamRoll = d6,
                        receivingTeamFanFactor = fanFactor,
                        receivingTeamResult = result
                    )),
                    GotoNode(SelectPlayerFromReceivingTeam),
                )
            }
        }
    }

    object SelectPlayerFromReceivingTeam: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<OfficiousRefContext>()
            return if (context.kickingTeamResult >= context.receivingTeamResult) {
                selectFromTeam(state.receivingTeam, rules)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                // Special situation where no players from the receiving team are on the field
                Continue -> GotoNode(SelectPlayerFromKickingTeam)
                else -> {
                    val context = state.getContext<OfficiousRefContext>()
                    checkTypeAndValue<RandomPlayersSelected>(state, rules, action) { selectAction ->
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
        override fun actionOwner(state: Game, rules: Rules): Team? = null

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<OfficiousRefContext>()
            return checkType<D6Result>(action) { d6 ->
                val player = context.kickingTeamPlayerSelected!!
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.OFFICIOUS_REF_REFEREE, d6),
                    createPlayerChangeCommands(player, d6),
                    SetContext(context.copy(kickingTeamRefereeRoll = d6)),
                    GotoNode(SelectPlayerFromKickingTeam)
                )
            }
        }
    }

    object SelectPlayerFromKickingTeam: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<OfficiousRefContext>()
            return if (context.kickingTeamResult <= context.receivingTeamResult) {
                selectFromTeam(state.kickingTeam, rules)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                // Special situation where no players from the kicking team are on the field
                // Since all rolls have been made at this point, just exit
                Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("No players from ${state.kickingTeam.name} are on the field"),
                        ExitProcedure()
                    )
                }
                else -> {
                    val context = state.getContext<OfficiousRefContext>()
                    checkTypeAndValue<RandomPlayersSelected>(state, rules, action) { selectAction ->
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
        override fun actionOwner(state: Game, rules: Rules): Team? = null

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<OfficiousRefContext>()
            return checkType<D6Result>(action) { d6 ->
                val player = context.receivingTeamPlayerSelected!!
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.OFFICIOUS_REF_REFEREE, d6),
                    createPlayerChangeCommands(player, d6),
                    SetContext(context.copy(receivingTeamRefereeRoll = d6)),
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
                SetPlayerState(player, PlayerState.BANNED),
                SetPlayerLocation(player, DogOut),
                ReportGameProgress("${player.name} angered the Ref and was Sent-off")
            )
            else -> compositeCommandOf(
                SetPlayerState(player, PlayerState.STUNNED, hasTackleZones = false),
                ReportGameProgress("${player.name} came to blows with the Ref and was Stunned"),
            )
        }
    }
}
