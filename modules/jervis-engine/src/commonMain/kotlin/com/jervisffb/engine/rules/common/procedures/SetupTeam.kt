package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.AddTeamReroll
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveTeamReroll
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTeamRerollEnabled
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.rerolls.LeaderTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class SetupTeamContext(
    val team: Team,
    var currentPlayer: Player? = null
): ProcedureContext

object SetupTeam : Procedure() {
    override val initialNode: Node = SelectPlayerOrEndSetup
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        // If the team has a Leader re-roll, it start out as disabled, and is
        // only enabled if a Leader is move into the pitch
        val context = state.getContext<SetupTeamContext>()
        val team = context.team
        val leaderReroll = team.rerolls.singleOrNull { it is LeaderTeamReroll }
        val startOfHalf = (state.halfNo <= rules.halfsPrGame && team.turnMarker == 0)
        return when (!startOfHalf && leaderReroll != null) {
            true -> SetTeamRerollEnabled(team, leaderReroll, enabled = false)
            false -> null
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<SetupTeamContext>()

    object SelectPlayerOrEndSetup : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<SetupTeamContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<SetupTeamContext>()
            val availablePlayers =
                context.team.filter {
                    val inReserve = (it.location == DogOut && it.state == PlayerState.RESERVE)
                    val onField = (it.location is FieldCoordinate && it.state == PlayerState.STANDING)
                    inReserve || onField
                }.let { players ->
                    if (players.isNotEmpty()) {
                        SelectPlayer.fromPlayers(players)
                    } else {
                        null
                    }
                }
            return listOfNotNull(availablePlayers, EndSetupWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<SetupTeamContext>()
            return when (action) {
                EndSetup -> GotoNode(EndSetupAndValidate)
                else -> {
                    castAction<PlayerSelected>(action) { playerSelected ->
                        compositeCommandOf(
                            UpdateContext(context.copy(currentPlayer = playerSelected.getPlayer(state))),
                            GotoNode(PlacePlayer),
                        )
                    }
                }
            }
        }
    }

    object PlacePlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<SetupTeamContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Allow players to be placed on the kicking teams side. At this stage, the more
            // elaborate rules are not enforced. That will first happen in `EndSetupAndValidate`
            val context = state.getContext<SetupTeamContext>()
            val freeFields: List<TargetSquare> =
                state.field
                    .filter { rules.isInSetupArea(context.team, it) }
                    .filter { it.isUnoccupied() }
                    .map { TargetSquare.setup(it.coordinates) }

            val playerLocation = context.currentPlayer!!.location
            var playerSquare: List<TargetSquare> = emptyList()
            if (playerLocation is FieldCoordinate) {
                playerSquare = listOf(TargetSquare.setup(playerLocation))
            }
            return listOf(SelectDogout, SelectFieldLocation(playerSquare + freeFields))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<SetupTeamContext>()
            val player = context.currentPlayer!!
            return when (action) {
                DogoutSelected -> {
                    compositeCommandOf(
                        getRemoveLeaderRerollCommand(player),
                        SetPlayerLocation(player, DogOut),
                        SetPlayerState(player, PlayerState.RESERVE),
                        UpdateContext(context.copy(currentPlayer = null)),
                        GotoNode(SelectPlayerOrEndSetup),
                    )
                }
                is FieldSquareSelected -> {
                    when (context.team.isHomeTeam()) {
                        true -> if (action.coordinate.isOnAwaySide(rules)) INVALID_ACTION(action)
                        false -> if (action.coordinate.isOnHomeSide(rules)) INVALID_ACTION(action)
                    }
                    compositeCommandOf(
                        getAddLeaderRerollCommand(player),
                        SetPlayerLocation(player, FieldCoordinate(action.x, action.y)),
                        SetPlayerState(player, PlayerState.STANDING),
                        UpdateContext(context.copy(currentPlayer = null)),
                        GotoNode(SelectPlayerOrEndSetup),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object EndSetupAndValidate : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<SetupTeamContext>()
            val team = context.team
            return when (rules.isSetupValid(state, team).isEmpty()) {
                true -> ExitProcedure()
                false -> GotoNode(InformOfInvalidSetup)
            }
        }
    }

    // Mostly relevant to give the UI a hook to show "invalid setup" messages
    object InformOfInvalidSetup : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<SetupTeamContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(ConfirmWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<Confirm>(action) {
                GotoNode(SelectPlayerOrEndSetup)
            }
        }
    }

    // -- HELPER METHODS --
    // If a new Leader is added to the field, also add a Leader reroll
    // Only 1 leader reroll is allowed.
    fun getAddLeaderRerollCommand(playerAdded: Player): Command? {
        if (!playerAdded.isSkillAvailable(SkillType.LEADER)) return null
        val team = playerAdded.team
        val rules = team.game.rules
        val reroll = team.rerolls.firstOrNull { it is LeaderTeamReroll }
        val startOfHalf = (team.turnMarker == 0 && team.game.halfNo <= rules.halfsPrGame)
        val otherLeaderOnPitch = team
            .filterNot { it == playerAdded }
            .any { it.location.isOnField(rules) && it.hasSkill(SkillType.LEADER) }

        return when {
            startOfHalf && !otherLeaderOnPitch -> AddTeamReroll(team,LeaderTeamReroll(team.id))
            startOfHalf && otherLeaderOnPitch -> null
            !startOfHalf && !otherLeaderOnPitch -> SetTeamRerollEnabled(team, reroll!!, enabled = true)
            !startOfHalf && otherLeaderOnPitch -> null
            else -> INVALID_GAME_STATE("Unsupported state: (startOfHalf=$startOfHalf, otherLeader=$otherLeaderOnPitch, reroll=$reroll)")
        }
    }

    // During Setup, if a Leader is removed from the Pitch, either completely
    // remove reroll (if start of half and no other leader) or disable it.
    fun getRemoveLeaderRerollCommand(playerRemoved: Player): Command? {
        if (!playerRemoved.isSkillAvailable(SkillType.LEADER)) return null
        val team = playerRemoved.team
        val rules = team.game.rules
        val reroll = team.rerolls.firstOrNull { it is LeaderTeamReroll }
        val startOfHalf = (team.turnMarker == 0 && team.game.halfNo <= rules.halfsPrGame)
        val otherLeaderOnPitch = team
            .filterNot { it == playerRemoved }
            .any { it.location.isOnField(rules) && it.hasSkill(SkillType.LEADER) }

        return when {
            startOfHalf && !otherLeaderOnPitch -> RemoveTeamReroll(team, reroll!!)
            startOfHalf && otherLeaderOnPitch -> null
            !startOfHalf && !otherLeaderOnPitch -> SetTeamRerollEnabled(team, reroll!!, enabled = false)
            !startOfHalf && otherLeaderOnPitch -> null
            else -> INVALID_GAME_STATE("Unsupported state: (startOfHalf=$startOfHalf, otherLeader=$otherLeaderOnPitch, reroll=$reroll)")
        }
    }
}
