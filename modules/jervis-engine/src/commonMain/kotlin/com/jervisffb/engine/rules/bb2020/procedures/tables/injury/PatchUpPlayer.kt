package com.jervisffb.engine.rules.bb2020.procedures.tables.injury

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.AddNigglingInjuries
import com.jervisffb.engine.commands.AddPlayerStatModifier
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetApothecaryUsed
import com.jervisffb.engine.commands.SetMissNextGame
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.inducements.ApothecaryType
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.reports.ReportApothecaryUsed
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportInjuryResult
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.Regeneration
import com.jervisffb.engine.rules.bb2020.tables.CasualtyResult
import com.jervisffb.engine.rules.bb2020.tables.InjuryResult
import com.jervisffb.engine.utils.INVALID_ACTION
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.useApothecaryBehavior.procedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ChooseToUseRegeneration)
        }
    }

    // If the player is still suffering from a casualty after using an apothecary, choose to use
    // regeneration or not. Normally a player does not have both an apothecary and regeneration
    // available, but e.g. using Sweatband of Conquest does allow it.
    object ChooseToUseRegeneration: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            if (context.player.hasSkill<Regeneration>()) {
                return listOf(
                    ConfirmWhenReady,
                    CancelWhenReady,
                )
            } else {
                return listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Confirm -> GotoNode(RollRegeneration)
                Cancel,
                Continue -> GotoNode(ApplyInjury)
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Make the first regeneration roll.
     */
    object RollRegeneration: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<RiskingInjuryContext>()
                val isSuccess = (d6.value >= 4)
                val updatedContext = context.copy(regenerationRoll = d6, regenerationSuccess = isSuccess)
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.REGENERATION, d6),
                    SetContext(updatedContext),
                    if (isSuccess) GotoNode(ApplyInjury) else GotoNode(ChooseToUseMortuaryAssistant),
                )
            }
        }
    }

    /**
     * If the team has a Mortuary Assistant, they can be used to re-roll failed results.
     * See page 91 in the rulebook.
     */
    object ChooseToUseMortuaryAssistant: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val isAvailable = context.player.team.getApothecaries().any {
                it.type == ApothecaryType.MORTUARY_ASSISTANT && !it.used
            }
            return if (isAvailable) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Confirm -> {
                    val context = state.getContext<RiskingInjuryContext>()
                    val team = context.player.team
                    val apothecary = team.getApothecaries().first {
                        it.type == ApothecaryType.MORTUARY_ASSISTANT  && !it.used
                    }
                    compositeCommandOf(
                        SetApothecaryUsed(team, apothecary, true),
                        ReportApothecaryUsed(team, apothecary),
                        SetContext(context.copy(regenerationApothecaryUsed = apothecary)),
                    )
                }
                Cancel,
                Continue -> {
                    GotoNode(ChooseToUsePlagueDoctor)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * If the team has a Plague Doctor, they can be used to reroll failed results.
     * See page 91 in the rulebook.
     */
    object ChooseToUsePlagueDoctor: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val isAvailable = context.player.team.getApothecaries().any {
                it.type == ApothecaryType.PLAGUE_DOCTOR && !it.used
            }
            return if (isAvailable) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Confirm -> {
                    val context = state.getContext<RiskingInjuryContext>()
                    val team = context.player.team
                    val apothecary = team.getApothecaries().first {
                        it.type == ApothecaryType.PLAGUE_DOCTOR  && !it.used
                    }
                    compositeCommandOf(
                        SetApothecaryUsed(team, apothecary, true),
                        ReportApothecaryUsed(team, apothecary),
                        SetContext(context.copy(regenerationApothecaryUsed = apothecary)),
                        GotoNode(ReRollRegeneration)
                    )
                }
                Cancel,
                Continue -> {
                    GotoNode(ApplyInjury)
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * An effect allowed the regeneration roll to be re-rolled.
     */
    object ReRollRegeneration: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<RiskingInjuryContext>()
                val isSuccess = (d6.value >= 4)
                val updatedContext = context.copy(regenerationReRoll = d6, regenerationSuccess = isSuccess)
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.REGENERATION, d6),
                    SetContext(updatedContext),
                    GotoNode(ApplyInjury)
                )
            }
        }
    }

    /**
     * Take into accounts all injury rolls, apothecaries and regeneration results and
     * apply the result.
     *
     * BB11 and BB7 differs on which rolls the apothecary is used.
     */
    object ApplyInjury: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val player = context.player
            return buildCompositeCommand {
                // If regeneration was used successfully, it will overrule everything
                val injuryCommand = when {
                    context.regenerationSuccess -> {
                        compositeCommandOf(
                            SetPlayerState(player, com.jervisffb.engine.model.PlayerState.RESERVE),
                            SetPlayerLocation(player, com.jervisffb.engine.model.locations.DogOut)
                        )
                    }
                    context.injuryResult == InjuryResult.KO && context.apothecaryUsed != null -> {
                        if (context.mode == RiskingInjuryMode.PUSHED_INTO_CROWD) {
                            compositeCommandOf(
                                SetPlayerState(player, com.jervisffb.engine.model.PlayerState.RESERVE),
                                SetPlayerLocation(player, com.jervisffb.engine.model.locations.DogOut),
                            )
                        } else {
                            if (state.activeTeamOrThrow() == player.team) {
                                SetPlayerState(player, com.jervisffb.engine.model.PlayerState.STUNNED_OWN_TURN)
                            } else {
                                SetPlayerState(player, com.jervisffb.engine.model.PlayerState.STUNNED)
                            }
                        }
                    }
                    context.injuryResult == InjuryResult.KO && context.apothecaryUsed == null -> {
                        compositeCommandOf(
                            SetPlayerState(player, com.jervisffb.engine.model.PlayerState.KNOCKED_OUT),
                            SetPlayerLocation(player, com.jervisffb.engine.model.locations.DogOut),
                        )
                    }
                    context.injuryResult == InjuryResult.KO && context.apothecaryUsed != null -> {
                        if (state.activeTeamOrThrow() == player.team) {
                            SetPlayerState(player, com.jervisffb.engine.model.PlayerState.STUNNED_OWN_TURN)
                        } else {
                            SetPlayerState(player, com.jervisffb.engine.model.PlayerState.STUNNED)
                        }
                    }

                    // In rulesets where we do not use the Casualty Table, we just use the Injury result directly.
                    (
                        context.finalCasualtyResult == null && context.injuryResult == InjuryResult.BADLY_HURT
                            || context.injuryResult == InjuryResult.SERIOUSLY_HURT
                            || context.injuryResult == InjuryResult.DEAD
                    ) -> {
                        compositeCommandOf(
                            SetPlayerLocation(player, DogOut),
                            if (context.apothecaryUsed != null && context.apothecaryInjuryRollSuccess) {
                                SetPlayerState(player, PlayerState.RESERVE)
                            } else {
                                val playerState = when (context.injuryResult) {
                                    InjuryResult.BADLY_HURT -> PlayerState.BADLY_HURT
                                    InjuryResult.SERIOUSLY_HURT -> PlayerState.SERIOUSLY_HURT
                                    InjuryResult.DEAD -> PlayerState.DEAD
                                    else -> INVALID_GAME_STATE("Unsupported injury result: $context")
                                }
                                SetPlayerState(player, playerState)
                            }
                        )
                    }

                    // In rulesets where the Casualty Table is used, handle them here (Standard)
                    context.finalCasualtyResult != null -> {
                        when (context.finalCasualtyResult) {
                            CasualtyResult.BADLY_HURT -> {
                                if (context.apothecaryUsed != null) {
                                    SetPlayerState(player, PlayerState.RESERVE)
                                } else {
                                    SetPlayerLocation(player, DogOut)
                                }
                            }
                            CasualtyResult.SERIOUSLY_HURT -> {
                                compositeCommandOf(
                                    SetMissNextGame(player, true),
                                    SetPlayerState(player, PlayerState.SERIOUSLY_HURT),
                                    SetPlayerLocation(player, DogOut),
                                )
                            }
                            CasualtyResult.SERIOUS_INJURY -> {
                                compositeCommandOf(
                                    SetMissNextGame(player, true),
                                    AddNigglingInjuries(player,1),
                                    SetPlayerState(player, PlayerState.SERIOUS_INJURY),
                                    SetPlayerLocation(player, DogOut),
                                )
                            }
                            CasualtyResult.LASTING_INJURY -> {
                                compositeCommandOf(
                                    SetPlayerState(player, PlayerState.LASTING_INJURY),
                                    AddPlayerStatModifier(player, context.finalLastingInjury!!),
                                    SetPlayerLocation(player, DogOut),
                                )
                            }
                            CasualtyResult.DEAD -> {
                                compositeCommandOf(
                                    SetPlayerState(player, PlayerState.DEAD),
                                    SetPlayerLocation(player, DogOut),
                                )
                            }
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
