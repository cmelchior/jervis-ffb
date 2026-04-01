package com.jervisffb.engine.rules.bb2025.procedures

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.actions.ForegoActivationSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectForgoActivation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.ResetAvailableTeamActions
import com.jervisffb.engine.commands.SetCanUseTeamRerolls
import com.jervisffb.engine.commands.SetPlayerAvailability
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetPlayerTemporaryStats
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.SetTurnMarker
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.ForegoActivationContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.reports.ReportEndingTurn
import com.jervisffb.engine.reports.ReportStartingTurn
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.procedures.getResetPlayerAvailabilityCommands
import com.jervisffb.engine.rules.common.procedures.getResetTeamTemporaryModifiersCommands
import com.jervisffb.engine.rules.common.procedures.inducements.ActivateInducementContext
import com.jervisffb.engine.rules.common.procedures.inducements.ActivateInducements
import com.jervisffb.engine.rules.common.procedures.tables.prayers.ResolveThrowARock
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.tables.PrayerToNuffle
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Procedure for controlling the active teams turn.
 * See page 50 in the BB2025 rulebook.
 *
 * The BB2025 Team Turn differs from BB2020 because of Forego Activation:
 * - Instead of only selecting a player, you can also choose to forego their
 *   activation.
 * - When ending a turn manually (not through a turnover), all remaining players
 *   that haven't activated must forego their activation.
 */
object TeamTurn : Procedure() {
    override val initialNode: Node = SelectPlayerOrEndTurn
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val turn = state.activeTeamOrThrow().turnMarker + 1
        // TODO Check for stalling players at this point.
        // If any player is starting the turn with the ball, check if they are stalling
        // We also need to check for this whenever a player receives the ball during their
        // turn
        return compositeCommandOf(
            SetCanUseTeamRerolls(true),
            SetTurnMarker(state.activeTeamOrThrow(), turn),
            // TODO Why are we setting these at the beginning of the turn, and not the end?
            getResetTurnActionCommands(state, rules),
            *resetPlayerTemporaryStats(state, rules),
            *getResetAvailablePlayers(state, rules),
            *resetSkillsUsed(state, rules),
            ReportStartingTurn(state.activeTeamOrThrow(), turn),
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            SetCanUseTeamRerolls(false),
            ReportEndingTurn(state.activeTeamOrThrow(), state.activeTeamOrThrow().turnMarker, state.turnOver),
        )
    }

    object UseSpecialEffects: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return AddContext(ActivateInducementContext(state.activeTeamOrThrow(), Timing.END_OF_TURN))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ActivateInducements
        override fun onExitNode(state: Game, rules: Rules): Command {
            // TODO Do we need to check for anything here? Could we have a turn-over already?
            return GotoNode(SelectPlayerOrEndTurn)
        }
    }

    // According to the rules, all players must either be activated or forego their activation
    // before you can end your turn. This is needlessly restrictive, so we also allow you to
    // end a turn manually, and then Jervis will handle foregoing the activation of the remaining
    // players.
    object SelectPlayerOrEndTurn : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.activeTeamOrThrow()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // If this team scored in the opponent's turn, their turn ends immediately.
            return if (state.turnOver == TurnOver.INACTIVE_TEAM_TOUCHDOWN) {
                listOf(ContinueWhenReady)
            } else {
                val availablePlayers = getAvailablePlayers(state, rules)
                listOfNotNull(
                    EndTurnWhenReady,
                    if (availablePlayers.isNotEmpty()) SelectPlayer.fromPlayers(availablePlayers) else null,
                    if (availablePlayers.isNotEmpty()) SelectForgoActivation.fromPlayers(availablePlayers) else null
                )
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayerSelected -> {
                    compositeCommandOf(
                        AddContext(ActivatePlayerContext(action.getPlayer(state))),
                        GotoNode(ActivatePlayer),
                    )
                }
                is ForegoActivationSelected -> {
                    compositeCommandOf(
                        AddContext(ForegoActivationContext(action.getPlayer(state), isEndingTurn = false)),
                        GotoNode(ForegoPlayerActivation),
                    )
                }
                // We actually scored in the previous turn, but for some odd reason, the rulebook defines it
                // as happening in the next turn (where the team is active).
                Continue -> compositeCommandOf(
                    SetTurnOver(TurnOver.ACTIVE_TEAM_TOUCHDOWN),
                    GotoNode(ResolveEndOfTurn),
                )
                EndTurn -> {
                    val availablePlayers = getAvailablePlayers(state, rules)
                    if (availablePlayers.isEmpty()) {
                        GotoNode(ResolveEndOfTurn)
                    } else {
                        compositeCommandOf(
                            AddContext(ForegoActivationContext(availablePlayers.first(), isEndingTurn = true)),
                            GotoNode(ForegoPlayerActivation),
                        )
                    }
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    // Foregoing a player activation can be triggered both manually and automatically when
    // ending a turn as all remaining players must then forego their activation before the
    // turn truly ends. In the automatic process, we loop through all available players
    // foregoing their activations one-by-one.
    object ForegoPlayerActivation : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ForegoActivation
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ForegoActivationContext>()
            val availablePlayers = getAvailablePlayers(state, rules)
            return when {
                context.isEndingTurn && availablePlayers.isNotEmpty() -> {
                    compositeCommandOf(
                        UpdateContext(context.copy(player = availablePlayers.first())),
                        GotoNode(ForegoPlayerActivation)
                    )
                }
                context.isEndingTurn && availablePlayers.isEmpty() -> {
                    compositeCommandOf(
                        RemoveContext<ForegoActivationContext>(),
                        GotoNode(ResolveEndOfTurn)
                    )
                }
                else -> {
                    compositeCommandOf(
                        RemoveContext<ForegoActivationContext>(),
                        GotoNode(SelectPlayerOrEndTurn),
                    )
                }
            }
        }
    }

    object ActivatePlayer: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = com.jervisffb.engine.rules.common.procedures.ActivatePlayer
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ActivatePlayerContext>(),
                if (state.turnOver != null) {
                    GotoNode(ResolveEndOfTurn)
                } else {
                    GotoNode(SelectPlayerOrEndTurn)
                }
            )
        }
    }

    object ResolveEndOfTurn : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // TODO Implement end-of-turn things
            //  - Players stunned at the beginning of the turn are now prone

            val turnOverStunnedPlayersCommands = state.activeTeamOrThrow()
                .filter { it.state == PlayerState.STUNNED }
                .map { SetPlayerState(it, PlayerState.PRONE) }
                .toTypedArray()

            val progressStunnedCommands = state.activeTeamOrThrow()
                .filter { it.state == PlayerState.STUNNED_OWN_TURN }
                .map { SetPlayerState(it, PlayerState.STUNNED) }
                .toTypedArray()

            // Reset Player availability. We mostly do this for UI purposes, so we should probably move towards
            // removing this again. But we need to make sure that "Availability" is correctly defined before that.
            // If we do not do this here, players will appear "grayed out" during the other players' turn, which
            // looks odd.
            val resetPlayerAvailabilityCommands = getResetPlayerAvailabilityCommands(state, rules)

            // It isn't well-defined in which order things happen at the end of the turn.
            // E.g. it is unclear if Special Play Cards like Assassination Attempt trigger before or
            // after Throw a Rock and when temporary skills or abilities are moved.
            //
            // For now we choose the (somewhat arbitrary) order:
            //
            // - Prayers Of Nuffle (Throw a Rock)
            // - Special Play Cards
            // - Temporary Skills/Characteristics are removed
            // - Stunned Players are now prone
            val resetCommands = getResetTeamTemporaryModifiersCommands(state, Duration.END_OF_TURN)
            val activeTeamResetCommands = getResetTeamTemporaryModifiersCommands(state, Duration.END_OF_OWN_TEAM_TURN)

            val throwRockActive = state.activeTeamOrThrow().otherTeam().activePrayersToNuffle.contains(PrayerToNuffle.THROW_A_ROCK)
            val nextNodeCommand = when (throwRockActive) {
                true -> GotoNode(CheckForThrowARock)
                false -> ExitProcedure()
            }

            return compositeCommandOf(
                *progressStunnedCommands,
                *turnOverStunnedPlayersCommands,
                *resetPlayerAvailabilityCommands,
                *resetCommands,
                *activeTeamResetCommands,
                nextNodeCommand
            )
        }
    }

    object CheckForThrowARock : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ResolveThrowARock
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // HELPER FUNCTIONS

    private fun getResetTurnActionCommands(state: Game, rules: Rules): ResetAvailableTeamActions {
        val moveActions = rules.teamActions.move.availablePrTurn
        val passActions = rules.teamActions.pass.availablePrTurn
        val handOffActions = rules.teamActions.handOff.availablePrTurn
        val blockActions = rules.teamActions.block.availablePrTurn
        val blitzActions = rules.teamActions.blitz.availablePrTurn
        val foulActions = rules.teamActions.foul.availablePrTurn
        val throwTeamActions = rules.teamActions.throwTeamMate.availablePrTurn
        val secureTheBallActions = rules.teamActions.secureTheBall.availablePrTurn
        val specialActions = rules.teamActions.specialActions
        return ResetAvailableTeamActions(
            state.activeTeamOrThrow(),
            moveActions,
            passActions,
            handOffActions,
            blockActions,
            blitzActions,
            foulActions,
            throwTeamActions,
            secureTheBallActions,
            buildMap {
                specialActions.forEach {
                    put(it.type as PlayerSpecialActionType, it.availablePrTurn)
                }
            }
        )
    }

    private fun getAvailablePlayers(state: Game, rules: Rules): List<Player> {
        return state.activeTeamOrThrow()
            .filter { it.available == Availability.AVAILABLE } // Players that hasn't already been activated
            .filter { it.state == PlayerState.STANDING || it.state == PlayerState.PRONE } // Only Standing/Prone players
    }

    // Reset player stats back to start, this e.g. include moves
    private fun resetPlayerTemporaryStats(state: Game, rules: Rules): Array<Command> {
        return state.activeTeamOrThrow()
            .filter { it.location.isOnField(rules) }
            .map {
                SetPlayerTemporaryStats(
                    it,
                    it.baseMove,
                )
            }.toTypedArray()
    }

    // Reset player stats back to start, this e.g. include temporary skills
    private fun resetSkillsUsed(state: Game, rules: Rules): Array<Command> {
        return state.activeTeamOrThrow()
            .map {
                val skillsThatReset = it.skills.filter {
                    skill -> skill.used  && skill.resetAt == Duration.END_OF_TURN
                }
                Pair(it, skillsThatReset)
            }
            .flatMap {
                it.second.map { skill ->
                    SetSkillUsed(it.first, skill, false)
                }
            }.toTypedArray()
    }

    private fun getResetAvailablePlayers(state: Game, rules: Rules): Array<SetPlayerAvailability> {
        // TODO Is there anyone who should not be made available? I.e. Stunned players will be turned KO
        return state.activeTeamOrThrow().map {
            if (it.location.isOnField(rules) && (it.state == PlayerState.STANDING || it.state == PlayerState.PRONE)) {
                SetPlayerAvailability(it, Availability.AVAILABLE)
            } else {
                SetPlayerAvailability(it, Availability.UNAVAILABLE)
            }
        }.toTypedArray()
    }
}
