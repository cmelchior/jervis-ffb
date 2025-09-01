package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
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
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.reports.ReportEndingTurn
import com.jervisffb.engine.reports.ReportStartingTurn
import com.jervisffb.engine.rules.PlayerSpecialActionType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.inducements.ActivateInducementContext
import com.jervisffb.engine.rules.bb2020.procedures.inducements.ActivateInducements
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.ResolveThrowARock
import com.jervisffb.engine.rules.bb2020.tables.PrayerToNuffle
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Procedure for controlling the active teams turn.
 *
 * See page 42 in the rulebook
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
            return SetContext(ActivateInducementContext(state.activeTeamOrThrow(), Timing.END_OF_TURN))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ActivateInducements
        override fun onExitNode(state: Game, rules: Rules): Command {
            // TODO Do we need to check for anything here? Could we have a turn-over already?
            return GotoNode(SelectPlayerOrEndTurn)
        }
    }

    // According to the rules, you cannot take back activating a player, but that feels needlessly restrictive.
    // So instead, we implement a multi-select process as following:
    // 1. Select Player
    // 2. Deselecting the player is possible and free.
    // 3. Select Player Action, which is equivalent to Activating them.
    // 4. Check for any activation events.
    // 5. Start on the action. Until the player moves or roll dice, it is still allowed to end the
    //    action without it counting as used.
    object SelectPlayerOrEndTurn : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.activeTeamOrThrow()

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // If this team scored in the opponent's turn, their next turn ends immediately.
            return if (state.turnOver == TurnOver.INACTIVE_TEAM_TOUCHDOWN) {
                listOf(ContinueWhenReady)
            } else {
                listOf(EndTurnWhenReady) + getAvailablePlayers(state, rules).map { SelectPlayer(it) }
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayerSelected -> {
                    checkTypeAndValue<PlayerSelected>(state, action) { playerSelected ->
                        compositeCommandOf(
                            SetContext(ActivatePlayerContext(playerSelected.getPlayer(state))),
                            GotoNode(ActivatePlayer),
                        )
                    }
                }
                // We actually scored in the previous turn, but for some odd reason, the rulebook defines it
                // as happening in the next turn (where the team is active).
                Continue -> compositeCommandOf(
                    SetTurnOver(TurnOver.ACTIVE_TEAM_TOUCHDOWN),
                    GotoNode(ResolveEndOfTurn),
                )
                EndTurn -> GotoNode(ResolveEndOfTurn)
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ActivatePlayer: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayer
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

            // Reset Player availability. We mostly do this for UI purposes, so we should propably move towards
            // removing this again. But we need to make sure that "Availability" is correctly defined before that.
            // If we do not do this here, players will appear "grayed out" during the other players turn which
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
            val resetCommands = getResetTemporaryModifiersCommands(state, rules, Duration.END_OF_TURN)
            val nextNodeCommand = if (state.activeTeamOrThrow().otherTeam().activePrayersToNuffle.contains(PrayerToNuffle.THROW_A_ROCK)) {
                GotoNode(CheckForThrowARock)
            } else {
                ExitProcedure()
            }

            return compositeCommandOf(
                *progressStunnedCommands,
                *turnOverStunnedPlayersCommands,
                *resetPlayerAvailabilityCommands,
                *resetCommands,
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
        val specialActions = rules.teamActions.specialActions
        return ResetAvailableTeamActions(
            state.activeTeamOrThrow(),
            moveActions,
            passActions,
            handOffActions,
            blockActions,
            blitzActions,
            foulActions,
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
