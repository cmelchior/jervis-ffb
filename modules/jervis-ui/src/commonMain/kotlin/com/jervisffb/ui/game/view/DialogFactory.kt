package com.jervisffb.ui.game.view

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.model.context.FoulContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.common.procedures.CoinTossContext
import com.jervisffb.engine.rules.common.procedures.DetermineKickingTeam
import com.jervisffb.engine.rules.common.procedures.FanFactorRolls
import com.jervisffb.engine.rules.common.procedures.PrayersToNuffleRoll
import com.jervisffb.engine.rules.common.procedures.PrayersToNuffleRollContext
import com.jervisffb.engine.rules.common.procedures.ScatterRoll
import com.jervisffb.engine.rules.common.procedures.SetupTeam
import com.jervisffb.engine.rules.common.procedures.SetupTeamContext
import com.jervisffb.engine.rules.common.procedures.WeatherRoll
import com.jervisffb.engine.rules.common.procedures.actions.foul.ArgueTheCallRoll
import com.jervisffb.engine.rules.common.procedures.actions.foul.FoulStep
import com.jervisffb.engine.rules.common.procedures.actions.move.StandingUpRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.StandingUpRollContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.ArmourRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.CasualtyRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.InjuryRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.LastingInjuryRoll
import com.jervisffb.engine.rules.common.procedures.tables.injury.PatchUpPlayer
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.UseBB7Apothecary
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.BrilliantCoaching
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.CheeringFans
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.OfficiousRef
import com.jervisffb.engine.rules.common.procedures.tables.kickoff.OfficiousRefContext
import com.jervisffb.engine.rules.common.procedures.tables.prayers.BadHabits
import com.jervisffb.engine.rules.common.procedures.tables.weather.SwelteringHeat
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.MultipleChoiceUserInputDialog
import com.jervisffb.ui.game.dialogs.SingleChoiceInputDialog
import com.jervisffb.ui.game.dialogs.UserInputDialog
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import io.ktor.client.request.request

/**
 * Class responsible for setting up modal dialogs specifically for dice rolls.
 * If no dialog could be created `null` is returned.
 *
 * Detect if a visible dialog is necessary and return it. `null` if this needs to be handled
 * by some other part of the UI.
 */
object DialogFactory {
    fun createDialogIfPossible(
        controller: GameEngineController,
        request: ActionRequest,
        provider: UiActionProvider,
        sharedData: LocalFieldDataWrapper,
        acc: UiSnapshotAccumulator,
        mapUnknownActions: (ActionRequest) -> List<GameAction>
    ): UserInputDialog? {
        val rules = controller.rules
        val userInput: UserInputDialog? =
            when (val currentNode = controller.state.stack.currentNode()) {

                is ArgueTheCallRoll.RollDice -> {
                    MultipleChoiceUserInputDialog.createArgueTheCallRollDialog(
                        controller.state.getContext<FoulContext>(),
                        rules
                    )
                }

                is ArmourRoll.RollDice -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    MultipleChoiceUserInputDialog.createArmourRollDialog(player)
                }

                BadHabits.RollDie -> {
                    MultipleChoiceUserInputDialog.createBadHabitsRoll()
                }

                is BrilliantCoaching.KickingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createBrilliantCoachingRolLDialog(controller.state.kickingTeam)
                }

                is BrilliantCoaching.ReceivingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createBrilliantCoachingRolLDialog(controller.state.kickingTeam)
                }

                is CasualtyRoll.RollDie -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    MultipleChoiceUserInputDialog.createCasualtyRollDialog(rules, player)
                }

                CheeringFans.KickingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createCheeringFansRollDialog(controller.state.kickingTeam)
                }

                CheeringFans.ReceivingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createCheeringFansRollDialog(controller.state.receivingTeam)
                }

                is DetermineKickingTeam.ChooseKickingTeam -> {
                    val choices =
                        listOf(
                            Confirm to "Kickoff",
                            Cancel to "Receive",
                        )
                    val context = controller.state.getContext<CoinTossContext>()
                    SingleChoiceInputDialog.createChooseToKickoffDialog(context.winner!!, choices)
                }

                is DetermineKickingTeam.CoinToss -> {
                    SingleChoiceInputDialog.createTossDialog(
                        state = controller.state,
                        CoinTossResult.allOptions())
                }

                is DetermineKickingTeam.SelectCoinSide -> {
                    SingleChoiceInputDialog.createSelectKickoffCoinTossResultDialog(
                        controller.state.awayTeam,
                        CoinSideSelected.allOptions(),
                    )
                }

                is FoulStep.DecideToArgueTheCall -> {
                    SingleChoiceInputDialog.createArgueTheCallDialog(controller.state.getContext<FoulContext>())
                }

                is InjuryRoll.RollDice -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    MultipleChoiceUserInputDialog.createInjuryRollDialog(rules, player)
                }

                is LastingInjuryRoll.RollDie -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    MultipleChoiceUserInputDialog.createLastingInjuryRollDialog(rules, player)
                }

                is OfficiousRef.KickingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createOfficiousRefRollDialog(controller.state.kickingTeam)
                }

                is OfficiousRef.ReceivingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createOfficiousRefRollDialog(controller.state.kickingTeam)
                }

                is OfficiousRef.RollForReceivingTemSelectedPlayer -> {
                    val context = controller.state.getContext<OfficiousRefContext>()
                    MultipleChoiceUserInputDialog.createOfficiousRefPlayerRollDialog(context.receivingTeamPlayerSelected!!)
                }

                is OfficiousRef.RollForKickingTeamSelectedPlayer -> {
                    val context = controller.state.getContext<OfficiousRefContext>()
                    MultipleChoiceUserInputDialog.createOfficiousRefPlayerRollDialog(context.kickingTeamPlayerSelected!!)
                }

                PrayersToNuffleRoll.RollDie -> {
                    val context = controller.state.getContext<PrayersToNuffleRollContext>()
                    MultipleChoiceUserInputDialog.createPrayersToNuffleRollDialog(controller.rules, context.rollsRemaining)
                }

                is PatchUpPlayer.ChooseToUseApothecary -> {
                    val context = controller.state.getContext<RiskingInjuryContext>()
                    SingleChoiceInputDialog.createUseApothecaryDialog(context)
                }

                is FanFactorRolls.SetFanFactorForAwayTeam -> {
                    SingleChoiceInputDialog.createFanFactorDialog(controller.state.awayTeam)
                }

                is FanFactorRolls.SetFanFactorForHomeTeam -> {
                    SingleChoiceInputDialog.createFanFactorDialog(controller.state.awayTeam)
                }

                is WeatherRoll.RollWeatherDice -> {
                    val diceRolls = mutableListOf<DiceRollResults>()
                    D6Result.allOptions().forEach { firstD6 ->
                        D6Result.allOptions().forEach { secondD6 ->
                            diceRolls.add(DiceRollResults(firstD6, secondD6))
                        }
                    }
                    MultipleChoiceUserInputDialog.createWeatherRollDialog(rules)
                }

                is ScatterRoll.RollDice -> {
                    MultipleChoiceUserInputDialog.createScatterRollDialog(rules)
                }

                is SetupTeam.InformOfInvalidSetup -> {
                    SingleChoiceInputDialog.createInvalidSetupDialog(controller.state.getContext<SetupTeamContext>().team)
                }

                is StandingUpRoll.RollDie,
                is StandingUpRoll.ReRollDie -> {
                    val player = controller.state.getContext<StandingUpRollContext>().player
                    MultipleChoiceUserInputDialog.createStandingUpRollDialog(player)
                }

                is SwelteringHeat.RollForAwayTeam,
                is SwelteringHeat.RollForHomeTeam -> {
                    MultipleChoiceUserInputDialog.createSwelteringHeatRollDialog()
                }

                is UseBB7Apothecary.ApothecaryInjuryReroll -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    MultipleChoiceUserInputDialog.createApothecaryInjuryRollDialog(player)
                }

                else -> {
                    null
                }
            }

        return if (userInput == null && request.actions.size == 1 && request.actions.first() is RollDice) {
            MultipleChoiceUserInputDialog.createUnknownDiceRoll(request.actions.first() as RollDice).apply {
                this.owner = request.team
            }
        } else {
            userInput.apply {
                this?.owner = request.team
            }
        }
    }
}
