package dk.ilios.jervis.procedures.tables.kickoff

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectRandomPlayers
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetHasTackleZones
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.DogOut
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType

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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D6Result>(action) { d6 ->
                val fanFactor = state.kickingTeam.fanFactor
                val result =  d6.value + fanFactor
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.OFFICIOUS_REF_FAN_FACTOR, d6),
                    ReportGameProgress("${state.kickingTeam.name} rolled [ ${d6.value} + $fanFactor = $result ]"),
                    SetContext(OfficiousRefContext(
                        kickingTeamRoll = d6,
                        kickingTeamFanFactor = fanFactor,
                        kickingTeamResult = result
                    )),
                    GotoNode(ReceivingTeamRollDie),
                )
            }
        }
    }

    object ReceivingTeamRollDie: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
                    checkTypeAndValue<RandomPlayersSelected>(state, rules, action, this) { selectAction ->
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

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
                    checkTypeAndValue<RandomPlayersSelected>(state, rules, action, this) { selectAction ->
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

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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

    private fun selectFromTeam(team: Team, rules: Rules): List<ActionDescriptor> {
        return team
            .filter { it.location.isOnField(rules) }
            .let { players ->
                if (players.isNotEmpty()) {
                    listOf(SelectRandomPlayers(1, players.map { it.id })
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
