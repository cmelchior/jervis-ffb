package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.EndTurnWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.ResetAvailableTeamActions
import dk.ilios.jervis.commands.SetCanUseTeamRerolls
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerAvailability
import dk.ilios.jervis.commands.SetPlayerStats
import dk.ilios.jervis.commands.SetSkillUsed
import dk.ilios.jervis.commands.SetTurnMarker
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Availability
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.inducements.Timing
import dk.ilios.jervis.procedures.inducements.ActivateInducementContext
import dk.ilios.jervis.procedures.inducements.ActivateInducements
import dk.ilios.jervis.procedures.tables.prayers.ResolveThrowARock
import dk.ilios.jervis.reports.ReportEndingTurn
import dk.ilios.jervis.reports.ReportStartingTurn
import dk.ilios.jervis.rules.PlayerSpecialActionType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Duration
import dk.ilios.jervis.rules.tables.PrayerToNuffle
import dk.ilios.jervis.utils.INVALID_ACTION

/**
 * Procedure for controlling the active teams turn.
 *
 * See page 42 in the rulebook
 */
object TeamTurn : Procedure() {
    override val initialNode: Node = SelectPlayerOrEndTurn

    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val turn = state.activeTeam.turnMarker + 1
        // TODO Check for stalling players at this point.
        // If any player is starting the turn with the ball, check if they are stalling
        // We also need to check for this whenever a player receives the ball during their
        // turn
        return compositeCommandOf(
            SetCanUseTeamRerolls(true),
            SetTurnMarker(state.activeTeam, turn),
            getResetTurnActionCommands(state, rules),
            *resetPlayerStats(state, rules),
            *getResetAvailablePlayers(state, rules),
            *resetSkillsUsed(state, rules),
            ReportStartingTurn(state.activeTeam, turn),
        )
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            SetTurnOver(false),
            SetCanUseTeamRerolls(false),
            ReportEndingTurn(state.activeTeam, state.activeTeam.turnMarker),
        )
    }

    object UseSpecialEffects: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            return SetContext(ActivateInducementContext(state.activeTeam, Timing.END_OF_TURN))
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
        override fun actionOwner(state: Game, rules: Rules) = state.activeTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(EndTurnWhenReady) + getAvailablePlayers(state, rules).map { SelectPlayer(it) }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayerSelected -> {
                    compositeCommandOf(
                        SetContext(ActivatePlayerContext(action.getPlayer(state))),
                        GotoNode(ActivatePlayer),
                    )
                }
                EndTurn -> GotoNode(ResolveEndOfTurn)
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ActivatePlayer: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = dk.ilios.jervis.procedures.ActivatePlayer
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ActivatePlayerContext>(),
                if (state.isTurnOver || state.goalScored) {
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
            val nextNodeCommand = if(state.activeTeam.otherTeam().activePrayersToNuffle.contains(PrayerToNuffle.THROW_A_ROCK)) {
                GotoNode(CheckForThrowARock)
            } else {
                ExitProcedure()
            }

            return compositeCommandOf(
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

    private fun getResetTurnActionCommands(state: Game, rules: Rules): ResetAvailableTeamActions {
        val moveActions = rules.teamActions.move.availablePrTurn
        val passActions = rules.teamActions.pass.availablePrTurn
        val handOffActions = rules.teamActions.handOff.availablePrTurn
        val blockActions = rules.teamActions.block.availablePrTurn
        val blitzActions = rules.teamActions.blitz.availablePrTurn
        val foulActions = rules.teamActions.foul.availablePrTurn
        val specialActions = rules.teamActions.specialActions
        return ResetAvailableTeamActions(
            state.activeTeam,
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
        return state.activeTeam
            .filter {
                it.available == Availability.AVAILABLE
            } // Players that hasn't already been activated
            .filter { it.state == PlayerState.STANDING || it.state == PlayerState.PRONE } // Only Standing/Prone players
    }

    // Reset player stats back to start, this e.g. include moves/rushes used/temporary skills
    private fun resetPlayerStats(state: Game, rules: Rules): Array<Command> {
        return state.activeTeam
            .filter { it.location.isOnField(rules) }
            .map {
                SetPlayerStats(
                    it,
                    it.baseMove,
                )
            }.toTypedArray()
    }

    // Reset player stats back to start, this e.g. include moves/rushes used/temporary skills
    private fun resetSkillsUsed(state: Game, rules: Rules): Array<Command> {
        return state.activeTeam
            .map {
                val skillsThatReset = it.skills.filter { it.used  && it.resetAt == Duration.END_OF_TURN}
                Pair(it, skillsThatReset)
            }
            .flatMap {
                it.second.map { skill -> SetSkillUsed(it.first, skill, false) }
            }.toTypedArray()
    }

    private fun getResetAvailablePlayers(
        state: Game,
        rules: Rules,
    ): Array<SetPlayerAvailability> {
        // TODO Is there anyone who should not be made available? I.e. Stunned players will be turned KO
        return state.activeTeam.map {
            if (it.location.isOnField(rules) && (it.state == PlayerState.STANDING || it.state == PlayerState.PRONE)) {
                SetPlayerAvailability(it, Availability.AVAILABLE)
            } else {
                SetPlayerAvailability(it, Availability.UNAVAILABLE)
            }
        }.toTypedArray()
    }
}
