package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.DeselectPlayer
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.EndTurnWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectAction
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ResetAvailableTeamActions
import dk.ilios.jervis.commands.SetActiveAction
import dk.ilios.jervis.commands.SetActivePlayer
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
import dk.ilios.jervis.procedures.tables.prayers.ResolveThrowARock
import dk.ilios.jervis.procedures.inducements.ActivateInducementContext
import dk.ilios.jervis.procedures.inducements.ActivateInducements
import dk.ilios.jervis.reports.ReportActionSelected
import dk.ilios.jervis.reports.ReportEndingTurn
import dk.ilios.jervis.reports.ReportStartingTurn
import dk.ilios.jervis.rules.PlayerAction
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
        val turn = state.activeTeam.turnData.turnMarker + 1
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
            ReportEndingTurn(state.activeTeam, state.activeTeam.turnData.turnMarker),
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

    object SelectPlayerOrEndTurn : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.activeTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(EndTurnWhenReady) + getAvailablePlayers(state, rules).map { SelectPlayer(it) }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayerSelected -> {
                    compositeCommandOf(
                        SetActivePlayer(action.getPlayer(state)),
                        SetPlayerAvailability(action.getPlayer(state), Availability.IS_ACTIVE),
                        GotoNode(DeselectPlayerOrSelectAction),
                    )
                }
                EndTurn -> GotoNode(ResolveEndOfTurn)
                else -> INVALID_ACTION(action)
            }
        }
    }

    object DeselectPlayerOrSelectAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.activeTeam

        private fun getAvailableSpecialActions(state: Game, rules: Rules): Set<PlayerAction> {
            return emptySet() // TODO Hypnotic Gaze, Ball & Chain, others?
        }

        private fun getAvailableTeamActions(state: Game, rules: Rules): Set<PlayerAction> {
            val actions = mutableSetOf<PlayerAction>()
            state.activeTeam.turnData.let {
                if (it.moveActions > 0) actions.add(rules.teamActions.move.action)
                if (it.passActions > 0) actions.add(rules.teamActions.pass.action)
                if (it.handOffActions > 0) actions.add(rules.teamActions.handOff.action)
                if (it.blockActions > 0) actions.add(rules.teamActions.block.action)
                if (it.blitzActions > 0) actions.add(rules.teamActions.blitz.action)
                if (it.foulActions > 0) actions.add(rules.teamActions.foul.action)
            }
            return actions
        }

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val teamActions: Set<PlayerAction> = getAvailableTeamActions(state, rules)
            val specialPlayerActions: Set<PlayerAction> = getAvailableSpecialActions(state, rules)
            val allActions: List<SelectAction> =
                (teamActions + specialPlayerActions).map {
                    SelectAction(it)
                }
            val availableActions: List<SelectAction> = (
                allActions.firstOrNull {
                    it.action.compulsory
                }?.let { listOf(it) } ?: allActions
            )
            val deselectPlayer: List<ActionDescriptor> = listOf(DeselectPlayer(state.activePlayer!!))
            return deselectPlayer + availableActions
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                PlayerDeselected ->
                    compositeCommandOf(
                        SetPlayerAvailability(state.activePlayer!!, Availability.AVAILABLE),
                        SetActivePlayer(null),
                        GotoNode(SelectPlayerOrEndTurn),
                    )
                is PlayerActionSelected -> {
//                    val type: PlayerActionType = action.action.type
                    // TODO We should probably not modify it here/
                    // While it is probably technically correct, we allow
                    // players to cancel most actions until they start
                    // moving or rolling dice.
//                    val modifyAvailableActions: Command =
//                        when (type) {
//                            PlayerActionType.MOVE,
//                            PlayerActionType.PASS,
//                            PlayerActionType.HAND_OFF,
//                            PlayerActionType.BLOCK,
//                            PlayerActionType.BLITZ,
//                            PlayerActionType.FOUL,
//                            -> {
//                                val currentValue: Int = state.activeTeam.turnData.availableActions[type]!!
//                                // Mark the action as used. Regardless what happens from here, the action
//                                // has been considered "selected" and thus count as used.
//                                SetAvailableActions(state.activeTeam, type, currentValue - 1)
//                            }
//                            PlayerActionType.SPECIAL ->
//                                INVALID_ACTION(
//                                    action,
//                                ) // TODO Figure out what needs to happen here
//                        }
                    val selectedAction = rules.teamActions[action.action].action
                    compositeCommandOf(
//                        modifyAvailableActions,
                        SetActiveAction(selectedAction),
                        ReportActionSelected(state.activePlayer!!, selectedAction),
                        GotoNode(ActivatePlayer),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    // Activating a player is an implicit step when choosing a player action (see page 42 in the rulebook)
    // However, some skills will modify the behavior of the player, when this happens, so we inj
    object ActivatePlayer : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // TODO Some rules to consider here: Bone Head
            return compositeCommandOf(
                GotoNode(ResolveSelectedAction),
            )
        }
    }

    object ResolveSelectedAction : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = state.activePlayerAction!!.procedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetPlayerAvailability(state.activePlayer!!, Availability.HAS_ACTIVATED),
                SetActivePlayer(null),
                SetActiveAction(null),
                if (state.isTurnOver) {
                    GotoNode(ResolveEndOfTurn)
                } else {
                    GotoNode(SelectPlayerOrEndTurn)
                },
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
        return ResetAvailableTeamActions(
            state.activeTeam,
            moveActions,
            passActions,
            handOffActions,
            blockActions,
            blitzActions,
            foulActions,
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
