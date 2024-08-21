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
import dk.ilios.jervis.actions.SelectDiceResult
import dk.ilios.jervis.actions.SelectDogout
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.actions.SelectRandomPlayers
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.actions.TossCoin
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.procedures.Bounce
import dk.ilios.jervis.procedures.CatchRoll
import dk.ilios.jervis.procedures.DetermineKickingTeam
import dk.ilios.jervis.procedures.PickupRoll
import dk.ilios.jervis.procedures.RollForStartingFanFactor
import dk.ilios.jervis.procedures.RollForTheWeather
import dk.ilios.jervis.procedures.SetupTeam
import dk.ilios.jervis.procedures.TheKickOff
import dk.ilios.jervis.procedures.TheKickOffEvent
import dk.ilios.jervis.procedures.actions.block.BlockRoll
import dk.ilios.jervis.procedures.actions.block.PushStep
import dk.ilios.jervis.procedures.actions.move.MoveAction
import dk.ilios.jervis.ui.GameScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class ManualModeUiActionFactory(model: GameScreenModel, private val actions: List<GameAction>) : UiActionFactory(
    model,
) {
    override suspend fun start(scope: CoroutineScope) {
        scope.launch(errorHandler) {
            var initialActionsIndex = 0
            emitToField(WaitingForUserInput)
            val actionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
                if (initialActionsIndex < actions.size) {
                    val action = actions[initialActionsIndex]
                    initialActionsIndex++
                    action
                } else {
                    val action =
                        runBlocking(Dispatchers.Default) {
                            model.actionRequestChannel.send(Pair(controller, availableActions))
                            model.actionSelectedChannel.receive()
                        }
                    action
                }
            }
            model.controller.startCallbackMode(actionProvider)
        }
        startUserActionSelector(scope)
    }

    private fun startUserActionSelector(scope: CoroutineScope) {
        scope.launch(errorHandler) {
            actions@while (true) {
                val (controller, actions) = model.actionRequestChannel.receive()
                var selectedUserAction = calculateAutomaticResponse(controller, actions)
                if (selectedUserAction == null) {
                    createDialogPopupIfNeeded(controller, actions)?.let { dialogInput ->
                        sendToRelevantUserInputChannel(listOf(dialogInput))
                    } ?: detectAndSendNonDialogUserInput(controller, actions)
                    // After input has been sent to the UI, wait for a response
                    selectedUserAction = userSelectedAction.receive()
                }
                model.actionSelectedChannel.send(selectedUserAction)
            }
        }
    }

    /**
     * Check if we can respond automatically to an event without having to involve the user.
     * Some examples:
     * - During an action and the only choice is EndAction
     */
    private fun calculateAutomaticResponse(
        controller: GameController,
        actions: List<ActionDescriptor>,
    ): GameAction? {

        val currentNode = controller.currentProcedure()?.currentNode()

        // If a team has no further actions, just end their turn immediately
        if (actions.size == 1 && actions.first() is EndActionWhenReady) {
            return EndAction
        }

        // When selecting a pushback, but only one choice is available.
        if (actions.size == 1 && actions.first() is SelectFieldLocation && currentNode is PushStep.SelectPushDirection) {
            val loc = actions.first() as SelectFieldLocation
            return FieldSquareSelected(loc.coordinate)
        }

        // When selecting block results after reroll and only 1 dice is available.
        if (currentNode == BlockRoll.SelectBlockResult && actions.size == 1) {
            return (actions.first() as SelectDiceResult).choices.first() as DBlockResult
        }

        return null
    }

    private suspend fun detectAndSendNonDialogUserInput(
        controller: GameController,
        actions: List<ActionDescriptor>,
    ) {
        val userInputs: List<UserInput> =
            actions.groupBy { it::class }.map { action ->
                when {
                    action.key == SelectPlayer::class -> {
                        SelectPlayerInput(action.value.map { PlayerSelected((it as SelectPlayer).player) })
                    }
                    action.key == SelectFieldLocation::class && controller.currentProcedure()?.currentNode() == MoveAction.SelectSquareOrEndAction -> {
                        val pathFinder = controller.rules.pathFinder
                        val startLocation = (controller.state.activePlayer!!.location as FieldCoordinate).coordinate
                        SelectMoveActionFieldLocationInput(
                            action.value.map { FieldSquareSelected((it as SelectFieldLocation).x, it.y) },
                            pathFinder.calculateAllPaths(
                                controller.state,
                                startLocation,
                                controller.state.activePlayer!!.moveLeft,
                            ),
                        )
                    }
                    action.key == SelectFieldLocation::class -> {
                        SelectFieldLocationInput(
                            action.value.map { FieldSquareSelected((it as SelectFieldLocation).x, it.y) },
                        )
                    }
                    action.key == DeselectPlayer::class -> {
                        DeselectPlayerInput(listOf(PlayerDeselected))
                    }
                    action.key == SelectAction::class -> {
                        val playerLocation = controller.state.activePlayer?.location as FieldCoordinate
                        SelectPlayerActionInput(
                            playerLocation,
                            action.value.map { PlayerActionSelected((it as SelectAction).action) },
                        )
                    }
                    action.key == EndActionWhenReady::class -> {
                        val playerLocation = controller.state.activePlayer?.location as FieldCoordinate
                        EndActionInput(playerLocation, listOf(EndAction))
                    }
                    else -> UnknownInput(mapUnknownActions(action.value)) // TODO This breaks if using multiple times
                }
            }

        sendToRelevantUserInputChannel(userInputs)
    }

    /**
     * Detects if a visible dialog is necessary and return it. `null` if this needs to be handled
     * by some other part of the UI.
     */
    private fun createDialogPopupIfNeeded(controller: GameController, actions: List<ActionDescriptor>): UserInput? {
        val userInput =
            when (controller.stack.currentNode()) {
                is RollForStartingFanFactor.SetFanFactorForHomeTeam -> {
                    SingleChoiceInputDialog.createFanFactorDialog(controller.state.homeTeam, D3Result.allOptions())
                }

                is RollForStartingFanFactor.SetFanFactorForAwayTeam -> {
                    SingleChoiceInputDialog.createFanFactorDialog(controller.state.awayTeam, D3Result.allOptions())
                }

                is RollForTheWeather.RollWeatherDice -> {
                    val diceRolls = mutableListOf<DiceResults>()
                    D8Result.allOptions().forEach { d8 ->
                        D6Result.allOptions().forEach { d6 ->
                            diceRolls.add(DiceResults(d8, d6))
                        }
                    }
                    DiceRollUserInputDialog.createWeatherRollDialog(controller.rules)
                }

                is DetermineKickingTeam.SelectCoinSide -> {
                    SingleChoiceInputDialog.createSelectKickoffCoinTossResultDialog(
                        controller.state.activeTeam,
                        CoinSideSelected.allOptions(),
                    )
                }

                is DetermineKickingTeam.CoinToss -> {
                    SingleChoiceInputDialog.createTossDialog(CoinTossResult.allOptions())
                }

                is DetermineKickingTeam.ChooseKickingTeam -> {
                    val choices =
                        listOf(
                            Confirm to "Kickoff",
                            Cancel to "Receive",
                        )
                    SingleChoiceInputDialog.createChooseToKickoffDialog(controller.state.activeTeam, choices)
                }

                is SetupTeam.InformOfInvalidSetup -> {
                    SingleChoiceInputDialog.createInvalidSetupDialog(controller.state.activeTeam)
                }

                is TheKickOff.TheKickDeviates -> {
                    val diceRolls = mutableListOf<DiceResults>()
                    D8Result.allOptions().forEach { d8 ->
                        D6Result.allOptions().forEach { d6 ->
                            diceRolls.add(DiceResults(d8, d6))
                        }
                    }
                    DiceRollUserInputDialog.createKickOffDeviatesDialog(
                        controller.rules,
                    )
                }

                is TheKickOffEvent.RollForKickOffEvent -> {
                    DiceRollUserInputDialog.createKickOffEventDialog(controller.rules)
                }

                CatchRoll.ReRollDie,
                is CatchRoll.RollDie,
                -> {
                    SingleChoiceInputDialog.createCatchBallDialog(
                        controller.state.catchRollContext!!.catchingPlayer,
                        D6Result.allOptions(),
                    )
                }

                is Bounce.RollDirection -> {
                    SingleChoiceInputDialog.createBounceBallDialog(controller.rules, D8Result.allOptions())
                }

                is PickupRoll.ReRollDie,
                is PickupRoll.RollDie,
                -> {
                    SingleChoiceInputDialog.createPickupBallDialog(
                        controller.state.pickupRollContext!!.player,
                        D6Result.allOptions(),
                    )
                }

                is PickupRoll.ChooseReRollSource -> {
                    SingleChoiceInputDialog.createPickupRerollDialog(
                        controller.state.pickupRollResultContext!!,
                        mapUnknownActions(controller.getAvailableActions()),
                    )
                }

                is BlockRoll.ReRollDie,
                is BlockRoll.RollDice -> {
                    val diceCount = (actions.first() as RollDice).dice.size
                    DiceRollUserInputDialog.createBlockRollDialog(diceCount, controller.state.blockRollContext!!.isBlitzing)
                }

                is PushStep.DecideToFollowUp -> {
                    SingleChoiceInputDialog.createFollowUpDialog(
                        controller.state.pushContext!!.pusher
                    )
                }

                is BlockRoll.ChooseResultOrReRollSource -> {
                    SingleChoiceInputDialog.createChooseBlockResultOrReroll(
                        mapUnknownActions(controller.getAvailableActions()),
                    )
                }

                else -> {
                    null
                }
            }
        return userInput
    }

    // This method will select the relevant channel to send user input to.
    // Do it here, so `startUserActionSelector` can be cleaner.
    // We should group all input to a single channel in on event, this is so,
    // we can replay the channel correctly. If multiple input is sent it should
    // be wrapped in a CompositeUserInput.
    private suspend fun sendToRelevantUserInputChannel(uiEvents: List<UserInput>) {
        // Group events into channels
        val fieldInputs = mutableListOf<UserInput>()
        val dialogInputs = mutableListOf<UserInput>()
        val unknownInputs = mutableListOf<UserInput>()

        uiEvents.forEach {
            when (it) {
                is CompositeUserInput -> error("Should not occur here")
                is DeselectPlayerInput -> fieldInputs.add(it)
                is EndActionInput -> fieldInputs.add(it)
                is SelectFieldLocationInput -> fieldInputs.add(it)
                is SelectPlayerActionInput -> fieldInputs.add(it)
                is SelectPlayerInput -> fieldInputs.add(it)
                is UnknownInput -> unknownInputs.add(it)
                is DiceRollUserInputDialog -> dialogInputs.add(it)
                is SingleChoiceInputDialog -> dialogInputs.add(it)
                is SelectMoveActionFieldLocationInput -> fieldInputs.add(it)
                is IgnoreUserInput -> fieldInputs.add(it)
                is ResumeUserInput -> fieldInputs.add(it)
                is WaitingForUserInput -> {
                    fieldInputs.add(it)
                    unknownInputs.add(it)
                }
            }
        }

        if (fieldInputs.isNotEmpty()) {
            val action: UserInput = if (fieldInputs.size == 1) fieldInputs.first() else CompositeUserInput(fieldInputs)
            emitToField(action)
        }

        if (dialogInputs.isNotEmpty()) {
            val dialogInput =
                if (dialogInputs.size == 1) {
                    dialogInputs.first()
                } else {
                    error(
                        "Only 1 dialog allow: ${dialogInputs.size}",
                    )
                }
            dialogActions.emit(dialogInput as UserInputDialog?)
        }

        if (unknownInputs.isNotEmpty()) {
            emitToUnknown(
                if (unknownInputs.size == 1) unknownInputs.first() else CompositeUserInput(unknownInputs),
            )
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
                    val rolls =
                        action.dice.map {
                            when (it) {
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
                    when (Random.nextInt(2)) {
                        0 -> CoinSideSelected(Coin.HEAD)
                        1 -> CoinSideSelected(Coin.TAIL)
                        else -> throw IllegalStateException("Unsupported value")
                    }
                }
                TossCoin -> {
                    when (Random.nextInt(2)) {
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
                is SelectDiceResult -> action.choices.random()
            }
        }
    }
}
