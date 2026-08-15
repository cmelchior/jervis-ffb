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
import com.jervisffb.engine.actions.SelectInducements
import com.jervisffb.engine.bb2020.procedures.table.kickoff.BB2020CheeringFans
import com.jervisffb.engine.common.context.CoinTossContext
import com.jervisffb.engine.common.context.FoulContext
import com.jervisffb.engine.common.context.OfficiousRefContext
import com.jervisffb.engine.common.context.PrayersToNuffleRollContext
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.context.SetupTeamContext
import com.jervisffb.engine.common.procedures.DetermineKickingTeamStep
import com.jervisffb.engine.common.procedures.FanFactorRolls
import com.jervisffb.engine.common.procedures.PrayersToNuffleRoll
import com.jervisffb.engine.common.procedures.ScatterRoll
import com.jervisffb.engine.common.procedures.SetupTeam
import com.jervisffb.engine.common.procedures.WeatherRoll
import com.jervisffb.engine.common.procedures.actions.foul.ArgueTheCallRoll
import com.jervisffb.engine.common.procedures.actions.foul.BeingSentOff
import com.jervisffb.engine.common.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.common.procedures.inducements.BuyInducements
import com.jervisffb.engine.common.procedures.tables.injury.ArmourRoll
import com.jervisffb.engine.common.procedures.tables.injury.CasualtyRoll
import com.jervisffb.engine.common.procedures.tables.injury.InjuryRoll
import com.jervisffb.engine.common.procedures.tables.injury.LastingInjuryRoll
import com.jervisffb.engine.common.procedures.tables.injury.PatchUpPlayer
import com.jervisffb.engine.common.procedures.tables.injury.UseBB7Apothecary
import com.jervisffb.engine.common.procedures.tables.kickoff.BrilliantCoaching
import com.jervisffb.engine.common.procedures.tables.kickoff.OfficiousRef
import com.jervisffb.engine.common.procedures.tables.prayers.BadHabits
import com.jervisffb.engine.common.procedures.tables.weather.SwelteringHeat
import com.jervisffb.engine.model.context.ScoringATouchDownContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.BuyInducementsDialog
import com.jervisffb.ui.game.dialogs.MultipleChoiceUserInputDialog
import com.jervisffb.ui.game.dialogs.SingleChoiceInputDialog
import com.jervisffb.ui.game.dialogs.UserInputDialog
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.menu.LocalPitchDataWrapper

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
        sharedData: LocalPitchDataWrapper,
        acc: UiSnapshotAccumulator,
        mapUnknownActions: (ActionRequest) -> List<GameAction>
    ): UserInputDialog? {
        val rules = controller.rules
        val id = controller.nextActionIndex()
        val userInput: UserInputDialog? =
            when (val currentNode = controller.state.stack.currentNode()) {

                is ArgueTheCallRoll.RollDie -> {
                    MultipleChoiceUserInputDialog.createArgueTheCallRollDialog(
                        id,
                        controller.state.getContext<FoulContext>(),
                        rules
                    )
                }

                is ArmourRoll.ReRollDice,
                is ArmourRoll.RollDice -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    MultipleChoiceUserInputDialog.createArmourRollDialog(id, player)
                }

                BadHabits.RollDie -> {
                    MultipleChoiceUserInputDialog.createBadHabitsRoll(id)
                }

                is BrilliantCoaching.KickingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createBrilliantCoachingRolLDialog(id, controller.state.kickingTeam)
                }

                is BrilliantCoaching.ReceivingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createBrilliantCoachingRolLDialog(id, controller.state.kickingTeam)
                }

                is CasualtyRoll.RollDie -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    MultipleChoiceUserInputDialog.createCasualtyRollDialog(id, rules, player)
                }

                BB2020CheeringFans.KickingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createCheeringFansRollDialog(id, controller.state.kickingTeam)
                }

                BB2020CheeringFans.ReceivingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createCheeringFansRollDialog(id, controller.state.receivingTeam)
                }

                is DetermineKickingTeamStep.ChooseKickingTeam -> {
                    val choices =
                        listOf(
                            Confirm to "Kickoff",
                            Cancel to "Receive",
                        )
                    val context = controller.state.getContext<CoinTossContext>()
                    SingleChoiceInputDialog.createChooseToKickoffDialog(id, context.winner!!, choices)
                }

                is DetermineKickingTeamStep.CoinToss -> {
                    SingleChoiceInputDialog.createTossDialog(
                        id,
                        state = controller.state,
                        CoinTossResult.allOptions())
                }

                is DetermineKickingTeamStep.SelectCoinSide -> {
                    SingleChoiceInputDialog.createSelectKickoffCoinTossResultDialog(
                        controller.state.awayTeam,
                        id,
                        CoinSideSelected.allOptions(),
                    )
                }

                is BeingSentOff.DecideToArgueTheCall -> {
                    SingleChoiceInputDialog.createArgueTheCallDialog(id, controller.state.getContext<FoulContext>())
                }

                BuyInducements.HigherCtvBuyPurchaseInducements,
                BuyInducements.LowerCtvBuyPurchaseInducements -> {
                    val descriptor = request.get<SelectInducements>()
                    BuyInducementsDialog(
                        team = request.team!!,
                        treasury = descriptor.treasury,
                        pettyCash = descriptor.pettyCash,
                        nextActionId = request.id
                    )
                }

                is InjuryRoll.RollDice -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    MultipleChoiceUserInputDialog.createInjuryRollDialog(id, rules, player)
                }

                is LastingInjuryRoll.RollDie -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    MultipleChoiceUserInputDialog.createLastingInjuryRollDialog(id, rules, player)
                }

                is OfficiousRef.KickingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createOfficiousRefRollDialog(id, controller.state.kickingTeam)
                }

                is OfficiousRef.ReceivingTeamRollDie -> {
                    MultipleChoiceUserInputDialog.createOfficiousRefRollDialog(id, controller.state.kickingTeam)
                }

                is OfficiousRef.RollForReceivingTemSelectedPlayer -> {
                    val context = controller.state.getContext<OfficiousRefContext>()
                    MultipleChoiceUserInputDialog.createOfficiousRefPlayerRollDialog(id, context.receivingTeamPlayerSelected!!)
                }

                is OfficiousRef.RollForKickingTeamSelectedPlayer -> {
                    val context = controller.state.getContext<OfficiousRefContext>()
                    MultipleChoiceUserInputDialog.createOfficiousRefPlayerRollDialog(id, context.kickingTeamPlayerSelected!!)
                }

                PrayersToNuffleRoll.RollDie -> {
                    val context = controller.state.getContext<PrayersToNuffleRollContext>()
                    MultipleChoiceUserInputDialog.createPrayersToNuffleRollDialog(id, controller.rules, context.rollsRemaining)
                }

                is PatchUpPlayer.ChooseToUseApothecary -> {
                    val context = controller.state.getContext<RiskingInjuryContext>()
                    SingleChoiceInputDialog.createUseApothecaryDialog(id, context)
                }

                is FanFactorRolls.SetFanFactorForAwayTeam -> {
                    SingleChoiceInputDialog.createFanFactorDialog(id, controller.state.awayTeam)
                }

                is FanFactorRolls.SetFanFactorForHomeTeam -> {
                    SingleChoiceInputDialog.createFanFactorDialog(id, controller.state.awayTeam)
                }

                is WeatherRoll.RollWeatherDice -> {
                    val diceRolls = mutableListOf<DiceRollResults>()
                    D6Result.allOptions().forEach { firstD6 ->
                        D6Result.allOptions().forEach { secondD6 ->
                            diceRolls.add(DiceRollResults(firstD6, secondD6))
                        }
                    }
                    MultipleChoiceUserInputDialog.createWeatherRollDialog(id, rules)
                }

                is ScatterRoll.RollDice -> {
                    MultipleChoiceUserInputDialog.createScatterRollDialog(id, rules)
                }

                is ScoringATouchdown.InformOfTouchdown -> {
                    SingleChoiceInputDialog.createTouchdownScoredDialog(id, controller.state.getContext<ScoringATouchDownContext>().player)
                }

                is SetupTeam.InformOfInvalidSetup -> {
                    SingleChoiceInputDialog.createInvalidSetupDialog(id, controller.state.getContext<SetupTeamContext>().team)
                }

                //    (currentNode == StandingUpRoll.RollDie),
                //    (currentNode == StandingUpRoll.ReRollDie) -> {
                //        val player = controller.state.getContext<StandingUpRollContext>().player
                //        MultipleChoiceUserInputDialog.createStandingUpRollDialog(player)
                //    }

                is SwelteringHeat.RollForAwayTeam,
                is SwelteringHeat.RollForHomeTeam -> {
                    MultipleChoiceUserInputDialog.createSwelteringHeatRollDialog(id)
                }

                is UseBB7Apothecary.ApothecaryInjuryReroll -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    MultipleChoiceUserInputDialog.createApothecaryInjuryRollDialog(id, player)
                }

                else -> {
                    null
                }
            }

        return if (userInput == null && request.actions.size == 1 && request.actions.first() is RollDice) {
            MultipleChoiceUserInputDialog.createUnknownDiceRoll(id, request.actions.first() as RollDice).apply {
                this.owner = request.team
            }
        } else {
            userInput.apply {
                this?.owner = request.team
            }
        }
    }
}
