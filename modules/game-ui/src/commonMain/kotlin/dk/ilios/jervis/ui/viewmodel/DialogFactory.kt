package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectDiceResult
import dk.ilios.jervis.controller.ActionsRequest
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.context.CatchRollContext
import dk.ilios.jervis.model.context.MoveContext
import dk.ilios.jervis.model.context.PickupRollContext
import dk.ilios.jervis.model.context.RushRollContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.Bounce
import dk.ilios.jervis.procedures.CatchRoll
import dk.ilios.jervis.procedures.CoinTossContext
import dk.ilios.jervis.procedures.DetermineKickingTeam
import dk.ilios.jervis.procedures.DeviateRoll
import dk.ilios.jervis.procedures.FanFactorRolls
import dk.ilios.jervis.procedures.PickupRoll
import dk.ilios.jervis.procedures.PrayersToNuffleRoll
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.procedures.Scatter
import dk.ilios.jervis.procedures.SetupTeam
import dk.ilios.jervis.procedures.TheKickOff
import dk.ilios.jervis.procedures.TheKickOffEvent
import dk.ilios.jervis.procedures.WeatherRoll
import dk.ilios.jervis.procedures.actions.block.BlockRoll
import dk.ilios.jervis.procedures.actions.block.BothDown
import dk.ilios.jervis.procedures.actions.block.BothDownContext
import dk.ilios.jervis.procedures.actions.block.PushContext
import dk.ilios.jervis.procedures.actions.block.PushStep
import dk.ilios.jervis.procedures.actions.block.Stumble
import dk.ilios.jervis.procedures.actions.block.StumbleContext
import dk.ilios.jervis.procedures.actions.foul.ArgueTheCallRoll
import dk.ilios.jervis.procedures.actions.foul.FoulContext
import dk.ilios.jervis.procedures.actions.foul.FoulStep
import dk.ilios.jervis.procedures.actions.move.DodgeRoll
import dk.ilios.jervis.procedures.actions.move.RushRoll
import dk.ilios.jervis.procedures.actions.pass.AccuracyRoll
import dk.ilios.jervis.procedures.actions.pass.PassContext
import dk.ilios.jervis.procedures.bb2020.kickoff.BrilliantCoaching
import dk.ilios.jervis.procedures.bb2020.kickoff.CheeringFans
import dk.ilios.jervis.procedures.bb2020.kickoff.OfficiousRef
import dk.ilios.jervis.procedures.bb2020.kickoff.OfficiousRefContext
import dk.ilios.jervis.procedures.bb2020.prayers.BadHabits
import dk.ilios.jervis.procedures.injury.ArmourRoll
import dk.ilios.jervis.procedures.injury.CasualtyRoll
import dk.ilios.jervis.procedures.injury.InjuryRoll
import dk.ilios.jervis.procedures.injury.LastingInjuryRoll
import dk.ilios.jervis.procedures.injury.RiskingInjuryContext
import dk.ilios.jervis.procedures.injury.RiskingInjuryRoll
import dk.ilios.jervis.procedures.weather.SwelteringHeat
import dk.ilios.jervis.rules.skills.Block
import dk.ilios.jervis.rules.skills.Dodge
import dk.ilios.jervis.rules.skills.Tackle

/**
 * Class responsible for setting up modal dialogs specifically for dice rolls.
 * If no dialog could be created `null` is returned.
 *
 * Detect if a visible dialog is necessary and return it. `null` if this needs to be handled
 * by some other part of the UI.
 */
object DialogFactory {
    fun createDialogIfPossible(controller: GameController, request: ActionsRequest, mapUnknownActions: (ActionsRequest) -> List<GameAction>): UserInput? {
        val rules = controller.rules
        val userInput: UserInputDialog? =
            when (controller.stack.currentNode()) {

                is AccuracyRoll.RollDice -> {
                    DiceRollUserInputDialog.createAccuracyRollDialog(controller.state.getContext<PassContext>(), rules)
                }

                is ArgueTheCallRoll.RollDice -> {
                    DiceRollUserInputDialog.createArgueTheCallRollDialog(controller.state.getContext<FoulContext>(), rules)
                }

                is ArmourRoll.RollDice -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    DiceRollUserInputDialog.createArmourRollDialog(player)
                }

                BadHabits.RollDie -> {
                    DiceRollUserInputDialog.createBadHabitsRoll()
                }

                is BlockRoll.ChooseResultOrReRollSource -> {
                    SingleChoiceInputDialog.createChooseBlockResultOrReroll(
                        mapUnknownActions(controller.getAvailableActions()),
                    )
                }

                is BlockRoll.ReRollDie,
                is BlockRoll.RollDice -> {
                    val diceCount = (request.actions.first() as RollDice).dice.size
                    DiceRollUserInputDialog.createBlockRollDialog(diceCount, controller.state.blockContext!!.isBlitzing)
                }

                is BlockRoll.SelectBlockResult -> {
                    DiceRollUserInputDialog.createSelectBlockDie(
                        request.actions.first() as SelectDiceResult
                    )
                }

                is BothDown.AttackerChooseToUseBlock -> {
                    val context = controller.state.getContext<BothDownContext>()
                    SingleChoiceInputDialog.createUseSkillDialog(context.attacker, context.attacker.getSkill<Block>())
                }

                is BothDown.DefenderChooseToUseBlock -> {
                    val context = controller.state.getContext<BothDownContext>()
                    SingleChoiceInputDialog.createUseSkillDialog(context.defender, context.defender.getSkill<Block>())
                }

                is Bounce.RollDirection -> {
                    SingleChoiceInputDialog.createBounceBallDialog(rules, D8Result.allOptions())
                }

                is BrilliantCoaching.KickingTeamRollDie -> {
                    DiceRollUserInputDialog.createBrilliantCoachingRolLDialog(controller.state.kickingTeam)
                }
                is BrilliantCoaching.ReceivingTeamRollDie -> {
                    DiceRollUserInputDialog.createBrilliantCoachingRolLDialog(controller.state.kickingTeam)
                }

                is CasualtyRoll.RollDie -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    DiceRollUserInputDialog.createCasualtyRollDialog(rules, player)
                }

                is CatchRoll.ChooseReRollSource -> {
                    SingleChoiceInputDialog.createCatchRerollDialog(
                        mapUnknownActions(controller.getAvailableActions()),
                    )
                }

                CatchRoll.ReRollDie,
                is CatchRoll.RollDie,
                    -> {
                    SingleChoiceInputDialog.createCatchBallDialog(
                        controller.state.getContext<CatchRollContext>().catchingPlayer,
                        D6Result.allOptions(),
                    )
                }

                CheeringFans.KickingTeamRollDie -> {
                    DiceRollUserInputDialog.createCheeringFansRollDialog(controller.state.kickingTeam)
                }
                CheeringFans.ReceivingTeamRollDie -> {
                    DiceRollUserInputDialog.createCheeringFansRollDialog(controller.state.receivingTeam)
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
                    SingleChoiceInputDialog.createTossDialog(CoinTossResult.allOptions())
                }

                is DetermineKickingTeam.SelectCoinSide -> {
                    SingleChoiceInputDialog.createSelectKickoffCoinTossResultDialog(
                        controller.state.awayTeam,
                        CoinSideSelected.allOptions(),
                    )
                }

                is DeviateRoll.RollDice -> {
                    val diceRolls = mutableListOf<DiceResults>()
                    D8Result.allOptions().forEach { d8 ->
                        D6Result.allOptions().forEach { d6 ->
                            diceRolls.add(DiceResults(d8, d6))
                        }
                    }
                    DiceRollUserInputDialog.createDeviateDialog(rules, false)
                }

                is DodgeRoll.ReRollDie,
                is DodgeRoll.RollDie -> {
                    val context = controller.state.getContext<MoveContext>()
                    DiceRollUserInputDialog.createDodgeRollDialog(context.player, context.target!!)
                }

                is DodgeRoll.ChooseReRollSource -> {
                    SingleChoiceInputDialog.createDodgeRerollDialog(
                        mapUnknownActions(controller.getAvailableActions()),
                    )
                }

                is FoulStep.DecideToArgueTheCall -> {
                    SingleChoiceInputDialog.createArgueTheCallDialog(controller.state.getContext<FoulContext>())
                }

                is InjuryRoll.RollDice -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    DiceRollUserInputDialog.createInjuryRollDialog(rules, player)
                }

                is LastingInjuryRoll.RollDice -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    DiceRollUserInputDialog.createLastingInjuryRollDialog(rules, player)
                }

                is PickupRoll.ChooseReRollSource -> {
                    SingleChoiceInputDialog.createPickupRerollDialog(
                        mapUnknownActions(controller.getAvailableActions()),
                    )
                }
                is PickupRoll.ReRollDie,
                is PickupRoll.RollDie,
                    -> {
                    SingleChoiceInputDialog.createPickupBallDialog(
                        controller.state.getContext<PickupRollContext>().player,
                        D6Result.allOptions(),
                    )
                }

                PrayersToNuffleRoll.RollDie -> {
                    val context = controller.state.getContext<PrayersToNuffleRollContext>()
                    DiceRollUserInputDialog.createPrayersToNuffleRollDialog(controller.rules, context.rollsRemaining)
                }

                is PushStep.DecideToFollowUp -> {
                    SingleChoiceInputDialog.createFollowUpDialog(
                        controller.state.getContext<PushContext>().pusher
                    )
                }

                is RiskingInjuryRoll.ChooseToUseApothecary -> {
                    val context = controller.state.getContext<RiskingInjuryContext>()
                    SingleChoiceInputDialog.createUseApothecaryDialog(context)
                }

                is FanFactorRolls.SetFanFactorForAwayTeam -> {
                    SingleChoiceInputDialog.createFanFactorDialog(controller.state.awayTeam, D3Result.allOptions())
                }
                is FanFactorRolls.SetFanFactorForHomeTeam -> {
                    SingleChoiceInputDialog.createFanFactorDialog(controller.state.homeTeam, D3Result.allOptions())
                }
                is WeatherRoll.RollWeatherDice -> {
                    val diceRolls = mutableListOf<DiceResults>()
                    D6Result.allOptions().forEach { firstD6 ->
                        D6Result.allOptions().forEach { secondD6 ->
                            diceRolls.add(DiceResults(firstD6, secondD6))
                        }
                    }
                    DiceRollUserInputDialog.createWeatherRollDialog(rules)
                }

                is RushRoll.ReRollDie,
                is RushRoll.RollDie -> {
                    val context = controller.state.getContext<RushRollContext>()
                    DiceRollUserInputDialog.createRushRollDialog(context.player, context.target)
                }

                is RushRoll.ChooseReRollSource -> {
                    SingleChoiceInputDialog.createRushRerollDialog(
                        mapUnknownActions(controller.getAvailableActions()),
                    )
                }

                is Scatter.RollDice -> {
                    DiceRollUserInputDialog.createScatterRollDialog(rules)
                }

                is SetupTeam.InformOfInvalidSetup -> {
                    SingleChoiceInputDialog.createInvalidSetupDialog(controller.state.activeTeam)
                }

                is Stumble.ChooseToUseDodge -> {
                    val defender = controller.state.getContext<StumbleContext>().defender
                    SingleChoiceInputDialog.createUseSkillDialog(defender, defender.getSkill<Dodge>())
                }

                is Stumble.ChooseToUseTackle -> {
                    val defender = controller.state.getContext<StumbleContext>().attacker
                    SingleChoiceInputDialog.createUseSkillDialog(defender, defender.getSkill<Tackle>())
                }

                is SwelteringHeat.RollForAwayTeam,
                is SwelteringHeat.RollForHomeTeam -> {
                    DiceRollUserInputDialog.createSwelteringHeatRollDialog()
                }

                is TheKickOff.TheKickDeviates -> {
                    val diceRolls = mutableListOf<DiceResults>()
                    D8Result.allOptions().forEach { d8 ->
                        D6Result.allOptions().forEach { d6 ->
                            diceRolls.add(DiceResults(d8, d6))
                        }
                    }
                    DiceRollUserInputDialog.createDeviateDialog(
                        rules,
                    )
                }

                is TheKickOffEvent.RollForKickOffEvent -> {
                    DiceRollUserInputDialog.createKickOffEventDialog(rules)
                }

                is OfficiousRef.KickingTeamRollDie -> {
                    DiceRollUserInputDialog.createOfficiousRefRollDialog(controller.state.kickingTeam)
                }
                is OfficiousRef.ReceivingTeamRollDie -> {
                    DiceRollUserInputDialog.createOfficiousRefRollDialog(controller.state.kickingTeam)
                }
                is OfficiousRef.RollForReceivingTemSelectedPlayer -> {
                    val context = controller.state.getContext<OfficiousRefContext>()
                    DiceRollUserInputDialog.createOfficiousRefPlayerRollDialog(context.receivingTeamPlayerSelected!!)
                }
                is OfficiousRef.RollForKickingTeamSelectedPlayer -> {
                    val context = controller.state.getContext<OfficiousRefContext>()
                    DiceRollUserInputDialog.createOfficiousRefPlayerRollDialog(context.kickingTeamPlayerSelected!!)
                }

                else -> {
                    null
                }
            }

        return if (userInput == null && request.actions.size == 1 && request.actions.first() is RollDice) {
            DiceRollUserInputDialog.createUnknownDiceRoll(request.actions.first() as RollDice).apply {
                this.owner = request.team
            }
        } else {
            userInput.apply {
                this?.owner = request.team
            }
        }
    }
}
