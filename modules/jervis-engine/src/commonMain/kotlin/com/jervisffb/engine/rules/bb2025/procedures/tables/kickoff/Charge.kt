package com.jervisffb.engine.rules.bb2025.procedures.tables.kickoff

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.actions.ForegoActivationSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectForgoActivation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayers
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.ResetAvailableTeamActions
import com.jervisffb.engine.commands.SetActiveTeam
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerAvailability
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetPlayerTemporaryStats
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
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
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.ChargeContext
import com.jervisffb.engine.model.context.ForegoActivationContext
import com.jervisffb.engine.model.context.KickOffEventContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportEndingCharge
import com.jervisffb.engine.reports.ReportStartingCharge
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.ForegoActivation
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.procedures.getResetPlayerAvailabilityCommands
import com.jervisffb.engine.rules.common.procedures.getResetTeamTemporaryModifiersCommands
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.tables.KickOffEvent
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlin.math.min

/**
 * Procedure for controlling kicking teams when rolling Charge on the Kickoff
 * Event table.
 *
 * See page 48 in the BB2025 rulebook.
 *
 * While Charge only mentions two turn-over events: Fall Over and Knocked Down,
 * these are the only ones that matter as the rest involve the ball, which
 * is still high in the air when Charge is activated.
 *
 * The rules also state that you need to select all players before activating
 * them. This does introduce a little wonkiness, but it does not seem that we
 * can avoid it as e.g., a failed blitz (Push instead of POW) might influence
 * which players you want to move next.
 *
 * Developer's Commentary:
 * The exact functionality of Charge is still a bit unclear in a number of areas:
 *
 * Performing vs. Declaring actions:
 * The wording only mentions "performing" actions, not "declaring" them, but
 * normally you cannot perform actions without declaring _something_ first.
 *
 * This opens up a lot of weird edge cases, like all negatraits only triggering
 * when you declare actions.
 *
 * Special Actions:
 * By listing the number of actions you can perform, it opens up a question if
 * Special Actions are allowed. Right now the assumption is _no_ as Kick
 * Team-mate is a special action and is the only one explicitly mentioned.
 *
 * Turn or not:
 * The wording is "may then be activated, one at a time, exactly as if it was
 * their teams turn", isn't exactly clear and leads to a number of
 * interpretations:
 *
 * 1) The entire Charge is treated as a normal team turn. This enables things
 *    like team rerolls, but also Wizards (that trigger end-of-turn)
 * 2) Charge is _not_ a team turn, and the above sentence only applies to the
 *    activation and not when performing the action itself. This means that
 *    skills like Dodge and Break Tackle cannot be used.
 * 3) Charge is _not_ a team turn, but the above sentence applies to the entire
 *    action itself. This means that skills like Dodge can be used.
 *
 * All of this probably needs a FAQ to be clarified, so for now we are going
 * with an educated guess that requires the least amount of weirdness. These
 * proposed semantics:
 *
 * - All players can declare a Move action
 * - One player can declare a Blitz action
 * - One player can declare a Throw Team-mate action
 * - One player can declare a Kick Team-mate action
 * - Special Actions that replace the block part of a Blitz action will work.
 * - All skills/traits granting standalone special actions will not work. This
 *   includes:
 *   - Ball & Chain
 *   - Bombardier
 *   - Breathe Fire
 *   - Chainsaw
 *   - Monstrous Mouth
 *   - Projectile Vomit
 *   - Punt
 *   - Stab (as block)
 * - Charge is not a Team Turn, so the team is not "active". This means that
 *   team rerolls do not work and effects that trigger end-of-turn cannot be
 *   applied (like Wizards). This is similar to BB2020.
 * - You are not forced to activate or forego activation on all players.
 *   This is entirely optional.
 * - All skills that only work during a team turn will work for the activated
 *   players as the "exactly as a team turn" are assumed to extend to performing
 *   the action as well. This avoids the weirdness BB2020 had with a lot of
 *   skills not working during a Blitz.
 */
object Charge : Procedure() {
    override val initialNode: Node = RollForPlayers
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            SetActiveTeam(state.kickingTeam),
            // CurrentBall was set for the kick-off event because it makes some things easier, but a Charge!
            // is more like a normal turn, so for this procedure we do not want it set automatically. It will
            // be re-set again when leaving this procedure.
            SetCurrentBall(null),
            // TODO Why are we setting these at the beginning of the turn, and not the end?
            getResetTurnActionCommands(state, rules),
            *resetPlayerTemporaryStats(state, rules),
            *getResetAvailablePlayers(state),
            *resetSkillsUsed(state, rules),
            ReportStartingCharge(state.kickingTeam),
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContextOrNull<ChargeContext>()
        return compositeCommandOf(
            if (context != null) RemoveContext(context) else null,
            SetActiveTeam(null),
            SetTurnOver(null),
            SetCurrentBall(state.singleBall()),
            ReportEndingCharge(state.kickingTeam, state.turnOver),
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getContext<KickOffEventContext>()
        if (context.result != KickOffEvent.CHARGE) {
            INVALID_GAME_STATE("Wrong Kick-off Event: ${context.result}")
        }
    }

    object RollForPlayers: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D3Result>(action) { roll ->
                val maxPlayers = roll.value + 3
                val availablePlayers = state.kickingTeam.filter { rules.isOpen(it) }
                val playersToSelect = min(availablePlayers.size, maxPlayers)
                buildCompositeCommand {
                    add(ReportDiceRoll(DiceRollType.CHARGE, roll))
                    if (availablePlayers.isEmpty()) {
                        addAll(
                            ReportEndingCharge(state.kickingTeam, turnOver = null, noPlayers = true),
                            ExitProcedure(),
                        )
                    } else {
                        addAll(
                            AddContext(
                                ChargeContext(
                                    roll = roll,
                                    playersToSelect = playersToSelect,
                                )
                            ),
                            GotoNode(SelectPlayersToActivate),
                        )
                    }
                }
            }
        }
    }

    object SelectPlayersToActivate: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<ChargeContext>()
            val availablePlayers = state.kickingTeam
                .filter { rules.isOpen(it) }
                .map { it.id }

            return when (availablePlayers.isNotEmpty()) {
                true -> listOf(SelectPlayers(context.playersToSelect, availablePlayers), CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayersSelected -> {
                    val context = state.getContext<ChargeContext>()
                    compositeCommandOf(
                        UpdateContext(context.copy(selectedPlayers = action.getPlayers(state).toSet())),
                        GotoNode(SelectPlayerOrEndTurn),
                    )
                }
                Continue,
                Cancel -> {
                    GotoNode(SelectPlayersToActivate)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object SelectPlayerOrEndTurn : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<ChargeContext>()
            val availablePlayers = (context.selectedPlayers - context.activatedPlayers)
            return listOfNotNull(
                EndTurnWhenReady,
                if (availablePlayers.isNotEmpty()) SelectPlayer.fromPlayers(availablePlayers) else null,
                if (availablePlayers.isNotEmpty()) SelectForgoActivation.fromPlayers(availablePlayers) else null
            )
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
                EndTurn -> GotoNode(ResolveEndOfTurn)
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ActivatePlayer: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = com.jervisffb.engine.rules.common.procedures.ActivatePlayer
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ActivatePlayerContext>()
            val chargeContext = state.getContext<ChargeContext>()
            return buildCompositeCommand {
                if (context.markActionAsUsed) {
                    add(UpdateContext(chargeContext.copy(activatedPlayers = chargeContext.activatedPlayers.add(context.player))))
                }
                add(RemoveContext(context))
                when (state.turnOver != null) {
                    true -> add(GotoNode(ResolveEndOfTurn))
                    false -> add(GotoNode(SelectPlayerOrEndTurn))
                }
            }
        }
    }

    // Unlike normal turns, during a Charge turn, we only support manually foregoing a player
    // activation. Ending the Charge, just leaves the remaining player standing as they are.
    object ForegoPlayerActivation : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ForegoActivation
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ForegoActivationContext>()
            val chargeContext = state.getContext<ChargeContext>()
            return compositeCommandOf(
                RemoveContext<ForegoActivationContext>(),
                UpdateContext(chargeContext.copy(activatedPlayers = chargeContext.activatedPlayers.add(context.player))),
                GotoNode(SelectPlayerOrEndTurn),
            )
        }
    }

    object ResolveEndOfTurn : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {

            // TODO Implement end-of-turn things
            //  - Players stunned at the beginning of the turn are now prone

            val progressStunnedCommands = state.kickingTeam
                .filter { it.state == PlayerState.STUNNED_OWN_TURN }
                .map { SetPlayerState(it, PlayerState.STUNNED) }
                .toTypedArray()

            // Reset Player availability. We mostly do this for UI purposes, so we should probably move towards
            // removing this again. But we need to make sure that "Availability" is correctly defined before that.
            // If we do not do this here, players will appear "grayed out" during the other players' turn, which
            // looks odd.
            val resetPlayerAvailabilityCommands = getResetPlayerAvailabilityCommands(state, rules)

            // Ending a Charge isn't considered a real "end-of-turn", so we only reset state we used for
            // book keeping, but not anything that would trigger at the end of a turn.
            val resetCommands = getResetTeamTemporaryModifiersCommands(state, rules, Duration.END_OF_TURN)
            return compositeCommandOf(
                *progressStunnedCommands,
                *resetPlayerAvailabilityCommands,
                *resetCommands,
                ExitProcedure(),
            )
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // HELPER FUNCTIONS

    private fun getResetTurnActionCommands(state: Game, rules: Rules): ResetAvailableTeamActions {
        val moveActions = rules.teamActions.move.availablePrTurn
        val passActions = 0
        val handOffActions = 0
        val blockActions = 0
        val blitzActions = 1
        val foulActions = 0
        val throwTeamMateActions = 1
        val secureTheBallActions = 0
        val specialActions = emptySet<PlayerAction>() // TODO Missing support for Kick Team-mate
        return ResetAvailableTeamActions(
            state.kickingTeam,
            moveActions,
            passActions,
            handOffActions,
            blockActions,
            blitzActions,
            foulActions,
            throwTeamMateActions,
            secureTheBallActions,
            buildMap {
                specialActions.forEach {
                    put(it.type as PlayerSpecialActionType, it.availablePrTurn)
                }
            }
        )
    }

    // During a Charge, only Open Players can be selecte
    private fun getAvailablePlayers(state: Game, rules: Rules): List<Player> {
        return state.kickingTeam
            .filter { it.available == Availability.AVAILABLE } // Players that hasn't already been activated
            .filter { rules.isOpen(it) }
    }

    // Reset player stats back to start, this e.g. include moves
    private fun resetPlayerTemporaryStats(state: Game, rules: Rules): Array<Command> {
        return state.kickingTeam
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
        return state.kickingTeam
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

    private fun getResetAvailablePlayers(state: Game): Array<SetPlayerAvailability> {
        val rules = state.rules
        // TODO Is there anyone who should not be made available? I.e. Stunned players will be turned KO
        return state.kickingTeam.map {
            if (it.location.isOnField(rules) && (it.state == PlayerState.STANDING || it.state == PlayerState.PRONE)) {
                SetPlayerAvailability(it, Availability.AVAILABLE)
            } else {
                SetPlayerAvailability(it, Availability.UNAVAILABLE)
            }
        }.toTypedArray()
    }
}
