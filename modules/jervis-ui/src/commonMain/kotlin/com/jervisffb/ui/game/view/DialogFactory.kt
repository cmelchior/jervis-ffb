package com.jervisffb.ui.game.view

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.model.context.CatchRollContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.StumbleContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.rules.bb2020.procedures.CatchRoll
import com.jervisffb.engine.rules.bb2020.procedures.CoinTossContext
import com.jervisffb.engine.rules.bb2020.procedures.DetermineKickingTeam
import com.jervisffb.engine.rules.bb2020.procedures.DeviateRoll
import com.jervisffb.engine.rules.bb2020.procedures.FanFactorRolls
import com.jervisffb.engine.rules.bb2020.procedures.PickupRoll
import com.jervisffb.engine.rules.bb2020.procedures.PrayersToNuffleRoll
import com.jervisffb.engine.rules.bb2020.procedures.PrayersToNuffleRollContext
import com.jervisffb.engine.rules.bb2020.procedures.ScatterRoll
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeam
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeamContext
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOff
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOffEvent
import com.jervisffb.engine.rules.bb2020.procedures.WeatherRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BothDown
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BothDownContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushStepInitialMoveSequence
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.Stumble
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseReroll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRerollDice
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRollDice
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.ArgueTheCallRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FoulContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FoulStep
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.DodgeRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.RushRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.StandingUpRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.StandingUpRollContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.AccuracyRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.ArmourRoll
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.CasualtyRoll
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.InjuryRoll
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.LastingInjuryRoll
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.PatchUpPlayer
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.UseBB7Apothecary
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.BrilliantCoaching
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.CheeringFans
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.OfficiousRef
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.OfficiousRefContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.BadHabits
import com.jervisffb.engine.rules.bb2020.procedures.tables.weather.SwelteringHeat
import com.jervisffb.engine.rules.bb2020.skills.Block
import com.jervisffb.engine.rules.bb2020.skills.Dodge
import com.jervisffb.engine.rules.bb2020.skills.Sidestep
import com.jervisffb.engine.rules.bb2020.skills.Tackle
import com.jervisffb.ui.game.dialogs.DiceRollUserInputDialog
import com.jervisffb.ui.game.dialogs.SingleChoiceInputDialog
import com.jervisffb.ui.game.dialogs.UserInputDialog

/**
 * Class responsible for setting up modal dialogs specifically for dice rolls.
 * If no dialog could be created `null` is returned.
 *
 * Detect if a visible dialog is necessary and return it. `null` if this needs to be handled
 * by some other part of the UI.
 */
object DialogFactory {
    fun createDialogIfPossible(controller: GameEngineController, request: ActionRequest, mapUnknownActions: (ActionRequest) -> List<GameAction>): UserInputDialog? {
        val rules = controller.rules
        val userInput: UserInputDialog? =
            when (controller.state.stack.currentNode()) {

                is AccuracyRoll.RollDice -> {
                    DiceRollUserInputDialog.createAccuracyRollDialog(controller.state.getContext<PassContext>(), rules)
                }

                is ArgueTheCallRoll.RollDice -> {
                    DiceRollUserInputDialog.createArgueTheCallRollDialog(
                        controller.state.getContext<FoulContext>(),
                        rules
                    )
                }

                is ArmourRoll.RollDice -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    DiceRollUserInputDialog.createArmourRollDialog(player)
                }

                BadHabits.RollDie -> {
                    DiceRollUserInputDialog.createBadHabitsRoll()
                }

                is StandardBlockChooseReroll.ReRollSourceOrAcceptRoll -> {
                    SingleChoiceInputDialog.createChooseBlockResultOrReroll(
                        state = controller.state,
                        mapUnknownActions(controller.getAvailableActions()),
                    )
                }

                is StandardBlockRollDice.RollDice,
                is StandardBlockRerollDice.ReRollDie -> {
                    val diceCount = (request.actions.first() as RollDice).dice.size
                    DiceRollUserInputDialog.createBlockRollDialog(
                        diceCount,
                        controller.state.getContext<BlockContext>().isBlitzing
                    )
                }

                is StandardBlockChooseResult.SelectBlockResult -> {
                    DiceRollUserInputDialog.createSelectBlockDie(
                        request.actions.first() as SelectDicePoolResult
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
                        state = controller.state,
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

                is DeviateRoll.RollDice -> {
                    val diceRolls = mutableListOf<DiceRollResults>()
                    D8Result.allOptions().forEach { d8 ->
                        D6Result.allOptions().forEach { d6 ->
                            diceRolls.add(DiceRollResults(d8, d6))
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
                        state = controller.state,
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

                is LastingInjuryRoll.RollDie -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    DiceRollUserInputDialog.createLastingInjuryRollDialog(rules, player)
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

                is PickupRoll.ChooseReRollSource -> {
                    SingleChoiceInputDialog.createPickupRerollDialog(
                        state = controller.state,
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

                is PushStepInitialMoveSequence.DecideToFollowUp -> {
                    SingleChoiceInputDialog.createFollowUpDialog(
                        controller.state.getContext<PushContext>().firstPusher
                    )
                }
                is PushStepInitialMoveSequence.DecideToUseSidestep -> {
                    val player = controller.state.getContext<PushContext>().pushee()
                    SingleChoiceInputDialog.createUseSkillDialog(
                        player,
                        player.getSkill<Sidestep>()
                    )
                }

                is PatchUpPlayer.ChooseToUseApothecary -> {
                    val context = controller.state.getContext<RiskingInjuryContext>()
                    SingleChoiceInputDialog.createUseApothecaryDialog(context)
                }

                is FanFactorRolls.SetFanFactorForAwayTeam -> {
                    DiceRollUserInputDialog.createFanFactorDialog(controller.state.awayTeam)
                }
                is FanFactorRolls.SetFanFactorForHomeTeam -> {
                    DiceRollUserInputDialog.createFanFactorDialog(controller.state.homeTeam)
                }
                is WeatherRoll.RollWeatherDice -> {
                    val diceRolls = mutableListOf<DiceRollResults>()
                    D6Result.allOptions().forEach { firstD6 ->
                        D6Result.allOptions().forEach { secondD6 ->
                            diceRolls.add(DiceRollResults(firstD6, secondD6))
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
                        state = controller.state,
                        mapUnknownActions(controller.getAvailableActions()),
                    )
                }

                is ScatterRoll.RollDice -> {
                    DiceRollUserInputDialog.createScatterRollDialog(rules)
                }

                is SetupTeam.InformOfInvalidSetup -> {
                    SingleChoiceInputDialog.createInvalidSetupDialog(controller.state.getContext<SetupTeamContext>().team)
                }

                is StandingUpRoll.RollDie,
                is StandingUpRoll.ReRollDie -> {
                    val player = controller.state.getContext<StandingUpRollContext>().player
                    DiceRollUserInputDialog.createStandingUpRollDialog(player)
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
                    val diceRolls = mutableListOf<DiceRollResults>()
                    D8Result.allOptions().forEach { d8 ->
                        D6Result.allOptions().forEach { d6 ->
                            diceRolls.add(DiceRollResults(d8, d6))
                        }
                    }
                    DiceRollUserInputDialog.createDeviateDialog(
                        rules,
                    )
                }

                is TheKickOffEvent.RollForKickOffEvent -> {
                    DiceRollUserInputDialog.createKickOffEventDialog(rules)
                }

                is UseBB7Apothecary.ApothecaryInjuryReroll -> {
                    val player = controller.state.getContext<RiskingInjuryContext>().player
                    DiceRollUserInputDialog.createApothecaryInjuryRollDialog(player)
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
