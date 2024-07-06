package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D12Result
import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.D20Result
import dk.ilios.jervis.actions.D2Result
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D4Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DeselectPlayer
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndSetupWhenReady
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.EndTurnWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectAction
import dk.ilios.jervis.actions.SelectCoinSide
import dk.ilios.jervis.actions.SelectDogout
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.actions.SelectRandomPlayers
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.actions.TossCoin
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.procedures.DetermineKickingTeam
import dk.ilios.jervis.procedures.GameDrive
import dk.ilios.jervis.procedures.RollForStartingFanFactor
import dk.ilios.jervis.procedures.RollForTheWeather
import dk.ilios.jervis.procedures.SetupTeam
import dk.ilios.jervis.procedures.TheKickOff
import dk.ilios.jervis.procedures.TheKickOffEvent
import dk.ilios.jervis.rules.KickOffEventTable
import dk.ilios.jervis.rules.tables.Blizzard
import dk.ilios.jervis.rules.tables.PerfectConditions
import dk.ilios.jervis.rules.tables.PouringRain
import dk.ilios.jervis.rules.tables.SwelteringHeat
import dk.ilios.jervis.rules.tables.VerySunny
import dk.ilios.jervis.ui.GameScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class ManualModeUiActionFactory(model: GameScreenModel, private val actions: List<GameAction>) : UiActionFactory(model) {
    override suspend fun start(scope: CoroutineScope) {
        scope.launch {
            var initialActionsIndex = 0
            _fieldActions.emit(WaitingForUserInput)
            val actionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
                if (initialActionsIndex < actions.size) {
                    val action = actions[initialActionsIndex]
                    initialActionsIndex++
                    Thread.sleep(10) // TODO Give UI a chance to catch up. Right now some events are missed for some reason.
                    action
                } else {
                    val action: GameAction = runBlocking(Dispatchers.Default) {
                        model.actionRequestChannel.send(Pair(controller, availableActions))
                        model.actionSelectedChannel.receive()
                    }
                    action
//                }
                }
            }
            model.controller.startCallbackMode(actionProvider)
        }
        startUserActionSelector(scope)
    }

    private suspend fun startUserActionSelector(scope: CoroutineScope) {
        scope.launch {
            actions@while(true) {
                val (controller, actions) = model.actionRequestChannel.receive()
                // if (useAutomatedActions(controller)) continue@actions
                val userInput = detectDialogPopup(controller)
                if (userInput == null) {
                    detectAndSendUserInput(actions)
                } else {
                    sendToRelevantUserInputChannel(userInput)
                }
                val selectedAction = userSelectedAction.receive()
                model.actionSelectedChannel.send(selectedAction)
            }
        }
    }

    private suspend fun detectAndSendUserInput(actions: List<ActionDescriptor>) {
        val actionGroups: List<UserInput> = actions.groupBy { it::class }.map {
            when {
                it.key == SelectPlayer::class -> {
                    SelectPlayerInput(it.value.map { PlayerSelected((it as SelectPlayer).player) })
                }
                it.key == SelectFieldLocation::class -> {
                    SelectFieldLocationInput(it.value.map { FieldSquareSelected((it as SelectFieldLocation).x, it.y) })
                }
                else -> UnknownInput(mapUnknownActions(it.value)) // TODO This breaks if using multiple times
            }
        }
        actionGroups.forEach {
            sendToRelevantUserInputChannel(it)
        }
    }

    /**
     * Detects if a visible dialog is needed and return it. `null` if some other actions are needed.
     */
    private fun detectDialogPopup(controller: GameController): UserInputDialog? {
        val userInput = when (controller.stack.currentNode()) {
            is RollForStartingFanFactor.SetFanFactorForHomeTeam -> {
                UserInputDialog.createFanFactorDialog(controller.state.homeTeam, D3Result.allOptions())
            }

            is RollForStartingFanFactor.SetFanFactorForAwayTeam -> {
                UserInputDialog.createFanFactorDialog(controller.state.awayTeam, D3Result.allOptions())
            }

            is RollForTheWeather.RollWeatherDice -> {
                // Fake the rolls for the specific results
                val rolls = listOf(
                    DiceResults(D6Result(1), D6Result(1)) to "[2] ${SwelteringHeat.title}",
                    DiceResults(D6Result(1), D6Result(2)) to "[3] ${VerySunny.title}",
                    DiceResults(D6Result(2), D6Result(2)) to "[4] ${PerfectConditions.title}",
                    DiceResults(D6Result(5), D6Result(6)) to "[11] ${PouringRain.title}",
                    DiceResults(D6Result(6), D6Result(6)) to "[12] ${Blizzard.title}",
                ).reversed()
                UserInputDialog.createWeatherRollDialog(rolls)
            }

            is DetermineKickingTeam.SelectCoinSide -> {
                UserInputDialog.createSelectKickoffCoinTossResultDialog(
                    controller.state.activeTeam,
                    CoinSideSelected.allOptions()
                )
            }

            is DetermineKickingTeam.CoinToss -> {
                UserInputDialog.createTossDialog(CoinTossResult.allOptions())
            }

            is DetermineKickingTeam.ChooseKickingTeam -> {
                val choices = listOf(
                    Confirm to "Kickoff",
                    Cancel to "Receive"
                )
                UserInputDialog.createChooseToKickoffDialog(controller.state.activeTeam, choices)
            }

            is SetupTeam.InformOfInvalidSetup -> {
                UserInputDialog.createInvalidSetupDialog(controller.state.activeTeam)
            }

            is TheKickOff.TheKickDeviates -> {
                // TODO Find a better way to show the options here
                val diceRolls = mutableListOf<GameAction>()
                D8Result.allOptions().forEach { d8 ->
                    D6Result.allOptions().forEach { d6 ->
                        diceRolls.add(DiceResults(d8, d6))
                    }
                }
                UserInputDialog.createKickOffDeviatesDialog(diceRolls)
            }

            is TheKickOffEvent.RollForKickOffEvent -> {
                // Fake the rolls for the specific results
                val rolls = listOf(
                    DiceResults(1.d6, 1.d6) to "[2] ${KickOffEventTable.roll(1.d6, 1.d6).name}",
                    DiceResults(1.d6, 2.d6) to "[2] ${KickOffEventTable.roll(1.d6, 2.d6).name}",
                    DiceResults(1.d6, 3.d6) to "[2] ${KickOffEventTable.roll(1.d6, 3.d6).name}",
                    DiceResults(1.d6, 4.d6) to "[2] ${KickOffEventTable.roll(1.d6, 4.d6).name}",
                    DiceResults(1.d6, 5.d6) to "[2] ${KickOffEventTable.roll(1.d6, 5.d6).name}",
                    DiceResults(1.d6, 6.d6) to "[2] ${KickOffEventTable.roll(1.d6, 6.d6).name}",
                    DiceResults(2.d6, 6.d6) to "[2] ${KickOffEventTable.roll(2.d6, 6.d6).name}",
                    DiceResults(3.d6, 6.d6) to "[2] ${KickOffEventTable.roll(3.d6, 6.d6).name}",
                    DiceResults(4.d6, 6.d6) to "[2] ${KickOffEventTable.roll(4.d6, 6.d6).name}",
                    DiceResults(5.d6, 6.d6) to "[2] ${KickOffEventTable.roll(5.d6, 6.d6).name}",
                    DiceResults(6.d6, 6.d6) to "[2] ${KickOffEventTable.roll(6.d6, 6.d6).name}",
                )
                UserInputDialog.createKickOffEventDialog(rolls)
            }

            else -> {
                null
            }
        }
        return userInput
    }

    // This method will select the relevant channel to send user input to.
    // Do it here, so `startUserActionSelector` can be cleaner.
    private suspend fun sendToRelevantUserInputChannel(uiEvent: UserInput) {
        when(uiEvent) {
            is SelectPlayerInput -> {
                _fieldActions.emit(uiEvent)
            }
            is UnknownInput -> _unknownActions.emit(uiEvent)
            is UserInputDialog -> dialogActions.emit(uiEvent)
            is SelectFieldLocationInput -> _fieldActions.emit(uiEvent)
            is WaitingForUserInput -> {
                _fieldActions.emit(uiEvent)
                // Also send to other channels?
            }
        }
    }

    /**
     * If we cannot determine the current state of the game. Just report [UnknownInput]
     * and convert [ActionDescriptor]'s in a best-effort.
     */
    private fun mapUnknownActions(actions: List<ActionDescriptor>): List<GameAction> {
        return actions.map { action ->
            when (action) {
                ContinueWhenReady -> Continue
                EndTurnWhenReady -> EndTurn
                is RollDice -> {
                    val rolls = action.dice.map {
                        when(it) {
                            Dice.D2 -> D2Result()
                            Dice.D3 -> D3Result()
                            Dice.D4 -> D4Result()
                            Dice.D6 -> D6Result()
                            Dice.D8 -> D8Result()
                            Dice.D12 -> D12Result()
                            Dice.D16 -> D16Result()
                            Dice.D20 -> D20Result()
                            Dice.BLOCK -> DBlockResult()
                        }
                    }
                    if (rolls.size == 1) {
                        rolls.first()
                    } else {
                        DiceResults(rolls)
                    }
                }
                ConfirmWhenReady -> Confirm
                EndSetupWhenReady -> EndSetup
                SelectDogout -> DogoutSelected
                is SelectFieldLocation -> FieldSquareSelected(action.x, action.y)
                is SelectPlayer -> PlayerSelected(action.player)
                is DeselectPlayer -> PlayerDeselected
                is SelectAction -> PlayerActionSelected(action.action)
                EndActionWhenReady -> EndAction
                CancelWhenReady -> Cancel
                SelectCoinSide -> {
                    when(Random.nextInt(2)) {
                        0 -> CoinSideSelected(Coin.HEAD)
                        1 -> CoinSideSelected(Coin.TAIL)
                        else -> throw IllegalStateException("Unsupported value")
                    }
                }
                TossCoin -> {
                    when(Random.nextInt(2)) {
                        0 -> CoinTossResult(Coin.HEAD)
                        1 -> CoinTossResult(Coin.TAIL)
                        else -> throw IllegalStateException("Unsupported value")
                    }
                }

                is SelectRandomPlayers -> {
                    RandomPlayersSelected(action.players.shuffled().subList(0, action.count))
                }

                SelectNoReroll -> NoRerollSelected
                is SelectRerollOption -> RerollOptionSelected(action.option)
            }
        }
    }
}

