package com.jervisffb.engine.bb2025.procedures

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.AddTeamReroll
import com.jervisffb.engine.common.commands.RemoveTeamReroll
import com.jervisffb.engine.common.commands.SetTeamRerollEnabled
import com.jervisffb.engine.common.context.SetupTeamContext
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

object SetupTeam : Procedure() {
    override val initialNode: Node = SelectPlayerOrEndSetup
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        // If the team has a Leader re-roll, it start out as disabled, and is
        // only enabled if a Leader is move into the pitch
        val context = state.getContext<SetupTeamContext>()
        val team = context.team
        val leaderReroll = team.rerolls.singleOrNull { rules.isLeaderReroll(it) }
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
                    val inReserve = (it.location == Dogout && it.state == PlayerDogoutState.RESERVE)
                    val onPitch = (it.location is PitchCoordinate && it.state == PlayerPitchState.STANDING)
                    rules.isPlayerAvailableForSetup(it) && (inReserve || onPitch)
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
            val freeSquares: List<TargetSquare> =
                state.pitch
                    .filter { rules.isInSetupArea(context.team, it) }
                    .filter { it.isUnoccupied() }
                    .map { TargetSquare.setup(it.coordinates) }

            val playerLocation = context.currentPlayer!!.location
            var playerSquare: List<TargetSquare> = emptyList()
            if (playerLocation is PitchCoordinate) {
                playerSquare = listOf(TargetSquare.setup(playerLocation))
            }
            return listOf(SelectDogout, SelectPitchLocation(playerSquare + freeSquares))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<SetupTeamContext>()
            val player = context.currentPlayer!!
            return when (action) {
                DogoutSelected -> {
                    compositeCommandOf(
                        getRemoveLeaderRerollCommand(player),
                        SetPlayerLocation(player, Dogout),
                        SetPlayerState(player, PlayerDogoutState.RESERVE),
                        UpdateContext(context.copy(currentPlayer = null)),
                        GotoNode(SelectPlayerOrEndSetup),
                    )
                }
                is PitchSquareSelected -> {
                    when (context.team.isHomeTeam()) {
                        true -> if (action.coordinate.isOnAwaySide(rules)) INVALID_ACTION(action)
                        false -> if (action.coordinate.isOnHomeSide(rules)) INVALID_ACTION(action)
                    }
                    compositeCommandOf(
                        getAddLeaderRerollCommand(player),
                        SetPlayerLocation(player, PitchCoordinate(action.x, action.y)),
                        SetPlayerState(player, PlayerPitchState.STANDING),
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
                true -> {
                    // If a player with Secret Weapon is on the pitch, we need to track it.
                    val trackSecretWeaponCommands = team
                        .filter { it.location.isOnPitch(rules) }
                        .filter { it.hasSkill(SkillType.SECRET_WEAPON) }
                        .mapNotNull { player ->
                            rules.setSecretWeaponOnPitchDuringDrive(player, onPitch = true)
                        }
                    when (trackSecretWeaponCommands.isNotEmpty()) {
                        true -> compositeCommandOf(trackSecretWeaponCommands) + ExitProcedure()
                        false -> ExitProcedure()
                    }
                }
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
    // If a new Leader is added to the pitch, also add a Leader reroll
    // Only 1 leader reroll is allowed.
    fun getAddLeaderRerollCommand(playerAdded: Player): Command? {
        if (!playerAdded.isSkillAvailable(SkillType.LEADER)) return null
        val team = playerAdded.team
        val rules = team.game.rules
        val reroll = team.rerolls.firstOrNull { rules.isLeaderReroll(it) }
        val startOfHalf = (team.turnMarker == 0 && team.game.halfNo <= rules.halfsPrGame)
        val otherLeaderOnPitch = team
            .filterNot { it == playerAdded }
            .any { it.location.isOnPitch(rules) && it.hasSkill(SkillType.LEADER) }

        return when {
            startOfHalf && !otherLeaderOnPitch -> AddTeamReroll(team,rules.createLeaderTeamReroll(team))
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
        val reroll = team.rerolls.firstOrNull { rules.isLeaderReroll(it) }
        val startOfHalf = (team.turnMarker == 0 && team.game.halfNo <= rules.halfsPrGame)
        val otherLeaderOnPitch = team
            .filterNot { it == playerRemoved }
            .any { it.location.isOnPitch(rules) && it.hasSkill(SkillType.LEADER) }

        return when {
            startOfHalf && !otherLeaderOnPitch -> RemoveTeamReroll(team, reroll!!)
            startOfHalf && otherLeaderOnPitch -> null
            !startOfHalf && !otherLeaderOnPitch -> SetTeamRerollEnabled(team, reroll!!, enabled = false)
            !startOfHalf && otherLeaderOnPitch -> null
            else -> INVALID_GAME_STATE("Unsupported state: (startOfHalf=$startOfHalf, otherLeader=$otherLeaderOnPitch, reroll=$reroll)")
        }
    }
}
