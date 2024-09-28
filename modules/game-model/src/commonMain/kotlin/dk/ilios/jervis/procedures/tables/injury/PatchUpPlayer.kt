package dk.ilios.jervis.procedures.tables.injury

import buildCompositeCommand
import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.AddNigglingInjuries
import dk.ilios.jervis.commands.AddPlayerStatModifier
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetApothecaryUsed
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetMissNextGame
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.model.inducements.ApothecaryType
import dk.ilios.jervis.model.locations.DogOut
import dk.ilios.jervis.reports.ReportApothecaryUsed
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportInjuryResult
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.Regeneration
import dk.ilios.jervis.rules.tables.CasualtyResult
import dk.ilios.jervis.rules.tables.InjuryResult
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE

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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = UseApothecary
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ChooseToUseRegeneration)
        }
    }

    // If the player is still suffering from a casualty after using an apothecary, choose to use
    // regeneration or not. Normally a player does not have both an apothecary and regeneration
    // available, but e.g. using Sweatband of Conquest does allow it.
    object ChooseToUseRegeneration: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
                            SetPlayerState(player, PlayerState.RESERVE),
                            SetPlayerLocation(player, DogOut)
                        )
                     }
                    context.injuryResult == InjuryResult.KO && context.apothecaryUsed != null -> {
                        if (context.mode == RiskingInjuryMode.PUSHED_INTO_CROWD) {
                            compositeCommandOf(
                                SetPlayerState(player, PlayerState.RESERVE),
                                SetPlayerLocation(player, DogOut),
                            )
                        } else {
                            SetPlayerState(player, PlayerState.STUNNED)
                        }
                    }
                    context.injuryResult == InjuryResult.KO && context.apothecaryUsed == null -> {
                        compositeCommandOf(
                            SetPlayerState(player, PlayerState.KNOCKED_OUT),
                            SetPlayerLocation(player, DogOut),
                        )
                    }
                    context.finalCasualtyResult != null -> {
                        when (context.finalCasualtyResult) {
                            CasualtyResult.BADLY_HURT -> {
                                if (context.apothecaryUsed != null) {
                                    SetPlayerState(player, PlayerState.RESERVE)
                                } else {
                                    SetPlayerLocation(player, DogOut)
                                }
                            }
                            CasualtyResult.SERIOUS_HURT -> {
                                compositeCommandOf(
                                    SetMissNextGame(player, true),
                                    SetPlayerState(player, PlayerState.SERIOUS_HURT),
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
