package com.jervisffb.engine.bb2025.procedures.injury

import com.jervisffb.engine.bb2025.procedures.getResetChompedStateCommands
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.AddNigglingInjuries
import com.jervisffb.engine.common.commands.AddPlayerStatModifier
import com.jervisffb.engine.common.commands.SetMissNextGame
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.common.reports.ReportInjuryResult
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.tables.CasualtyResult
import com.jervisffb.engine.rules.common.tables.InjuryResult
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure that handles any effect that can patch up any injury after it has been rolled,
 * this includes both Knocked Out and casualties.
 */
object PatchUpPlayer: Procedure() {
    override val initialNode: Node = ChooseToUseApothecary
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
    }

    // Sub procedure responsible for choosing an apothecary (if any) and applying it
    object ChooseToUseApothecary: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.useApothecaryStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ApplyInjury)
        }
    }

    /**
     * Take into accounts all injury rolls and apothecaries and apply the result.
     *
     * BB11 and BB7 differ on which rolls the apothecary is used.
     */
    object ApplyInjury: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val player = context.player
            return buildCompositeCommand {
                val injuryCommand = when {
                    context.injuryResult == InjuryResult.KO && context.apothecaryUsed != null -> {
                        if (context.mode == RiskingInjuryMode.PUSHED_INTO_CROWD) {
                            compositeCommandOf(
                                SetPlayerState(player, PlayerDogoutState.RESERVE),
                                getResetChompedStateCommands(player),
                                SetPlayerLocation(player, Dogout),
                            )
                        } else {
                            if (state.activeTeamOrThrow() == player.team) {
                                SetPlayerState(player, PlayerPitchState.STUNNED_OWN_TURN)
                            } else {
                                SetPlayerState(player, PlayerPitchState.STUNNED)
                            }
                        }
                    }
                    context.injuryResult == InjuryResult.KO && context.apothecaryUsed == null -> {
                        compositeCommandOf(
                            SetPlayerState(player, PlayerDogoutState.KNOCKED_OUT),
                            getResetChompedStateCommands(player),
                            SetPlayerLocation(player, Dogout),
                        )
                    }
                    context.injuryResult == InjuryResult.KO && context.apothecaryUsed != null -> {
                        if (state.activeTeamOrThrow() == player.team) {
                            SetPlayerState(player, PlayerPitchState.STUNNED_OWN_TURN)
                        } else {
                            SetPlayerState(player, PlayerPitchState.STUNNED)
                        }
                    }

                    // In rulesets where we do not use the Casualty Table, we just use the Injury result directly.
                    (
                        context.finalCasualtyResult == null
                            && (
                                context.injuryResult == InjuryResult.BADLY_HURT
                                    || context.injuryResult == InjuryResult.SERIOUSLY_HURT
                                    || context.injuryResult == InjuryResult.DEAD
                            )
                    ) -> {
                        buildCompositeCommand {
                            getResetChompedStateCommands(player)?.let { add(it) }
                            add(SetPlayerLocation(player, Dogout))
                            if (context.apothecaryUsed != null && context.apothecaryInjuryRollSuccess) {
                                add(SetPlayerState(player, PlayerDogoutState.RESERVE))
                            } else {
                                val playerState = when (context.injuryResult) {
                                    InjuryResult.BADLY_HURT -> PlayerDogoutState.BADLY_HURT
                                    InjuryResult.SERIOUSLY_HURT -> PlayerDogoutState.SERIOUSLY_HURT
                                    InjuryResult.DEAD -> PlayerDogoutState.DEAD
                                    else -> INVALID_GAME_STATE("Unsupported injury result: ${context.injuryResult}")
                                }
                                add(SetPlayerState(player, playerState))
                            }
                        }
                    }

                    // In rulesets where the Casualty Table is used, handle them here (Standard)
                    context.finalCasualtyResult != null -> {
                        when (context.finalCasualtyResult) {
                            CasualtyResult.BADLY_HURT -> {
                                if (context.apothecaryUsed != null) {
                                    compositeCommandOf(
                                        SetPlayerState(player, PlayerDogoutState.RESERVE),
                                        getResetChompedStateCommands(player),
                                        SetPlayerLocation(player, Dogout)
                                    )
                                } else {
                                    compositeCommandOf(
                                        SetPlayerState(player, PlayerDogoutState.BADLY_HURT),
                                        getResetChompedStateCommands(player),
                                        SetPlayerLocation(player, Dogout)
                                    )
                                }
                            }
                            CasualtyResult.SERIOUSLY_HURT -> {
                                compositeCommandOf(
                                    SetMissNextGame(player, true),
                                    SetPlayerState(player, PlayerDogoutState.SERIOUSLY_HURT),
                                    getResetChompedStateCommands(player),
                                    SetPlayerLocation(player, Dogout),
                                )
                            }
                            CasualtyResult.SERIOUS_INJURY -> {
                                compositeCommandOf(
                                    SetMissNextGame(player, true),
                                    AddNigglingInjuries(player,1),
                                    SetPlayerState(player, PlayerDogoutState.SERIOUS_INJURY),
                                    getResetChompedStateCommands(player),
                                    SetPlayerLocation(player, Dogout),
                                )
                            }
                            CasualtyResult.LASTING_INJURY -> {
                                compositeCommandOf(
                                    SetPlayerState(player, PlayerDogoutState.LASTING_INJURY),
                                    AddPlayerStatModifier(player, context.finalLastingInjury!!),
                                    getResetChompedStateCommands(player),
                                    SetPlayerLocation(player, Dogout),
                                )
                            }
                            CasualtyResult.DEAD -> {
                                compositeCommandOf(
                                    SetPlayerState(player, PlayerDogoutState.DEAD),
                                    getResetChompedStateCommands(player),
                                    SetPlayerLocation(player, Dogout),
                                )
                            }
                            null -> INVALID_GAME_STATE("Missing casualty result: $context")
                        }
                    }
                    else -> INVALID_GAME_STATE("Unsupported state: $context")
                }
                add(injuryCommand)
                add(ReportInjuryResult(state.getContext<RiskingInjuryContext>()))
                add(ExitProcedure())
            }
        }
    }
}
