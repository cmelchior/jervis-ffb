package com.jervisffb.ui.viewmodel

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.InducementSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayerSubActionSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectCoinSide
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectInducement
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.SelectSkill
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.actions.TossCoin
import com.jervisffb.engine.controller.ActionsRequest
import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOff
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushStep
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.calculateOptionsForMoveType
import com.jervisffb.ui.DialogFactory
import com.jervisffb.ui.GameScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.reflect.KClass

/**
 * A variant of [UiActionFactory] that requires manual input from the user.
 * This works by converting [ActionDescriptor] objects into [UserInput] objects.
 *
 * Then UI will then use those objects to determine how to modify the UI so
 * input can be requested. An example could be enabling click listeners.
 * This can also modify the UI, .e.g., by showing push directions or dialogs.
 */
class ManualModeUiActionFactory(model: GameScreenModel, private val preloadedActions: List<GameAction>) : UiActionFactory(
    model,
) {
    override suspend fun start(scope: CoroutineScope) {
        scope.launch(errorHandler) {
            var initialActionsIndex = 0
            emitToField(WaitingForUserInput)
            val actionProvider: suspend (GameController, ActionsRequest) -> GameAction = { controller: GameController, request: ActionsRequest ->
                if (initialActionsIndex < preloadedActions.size) {
                    val action = preloadedActions[initialActionsIndex]
                    initialActionsIndex++
                    action
                } else {
                    model.actionRequestChannel.send(Pair(controller, request))
                    val action = model.actionSelectedChannel.receive()
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
                val (controller, request) = model.actionRequestChannel.receive()
                var selectedUserAction = calculateAutomaticResponse(controller, request.actions)
                if (selectedUserAction == null) {
                    DialogFactory.createDialogIfPossible(
                        controller,
                        request,
                        { actionDescriptors-> mapUnknownActions(request.actions) }
                    )?.let { dialogInput ->
                        sendToRelevantUserInputChannel(listOf(dialogInput))
                    } ?: detectAndSendNonDialogUserInput(controller, request)
                    // After input has been sent to the UI, wait for a response
                    selectedUserAction = userSelectedAction.receive()
                }
                model.actionSelectedChannel.send(selectedUserAction)
            }
        }
    }

    /**
     * Check if we can respond automatically to an event without having to involve the user.
     *
     * Some requirements:
     * - Any action returned this way should also have an entry in [Feature]
     */
    private fun calculateAutomaticResponse(
        controller: GameController,
        actions: List<ActionDescriptor>,
    ): GameAction? {

        // When reacting to an `Undo` command, all automatic responses are disabled.
        // If not disabled, UNDO'ing an action might put us in a state that will
        // automatically move us forward again, which will make it appear as the
        // UNDO didn't work.
        if (controller.lastActionWasUndo) {
            return null
        }

        if (model.menuViewModel.isFeatureEnabled(Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS)) {
            if (actions.filterIsInstance<SelectNoReroll>().count { it.rollSuccessful == true} > 0) {
                return NoRerollSelected()
            }
        }

        if (controller.currentProcedure()?.currentNode() == TheKickOff.NominateKickingPlayer && model.menuViewModel.isFeatureEnabled(Feature.SELECT_KICKING_PLAYER)) {
            return (controller.currentProcedure()!!.currentNode() as ActionNode).getAvailableActions(controller.state, controller.rules)
                .random().let {
                    PlayerSelected((it as SelectPlayer).player)
                }
        }

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
        if (currentNode == StandardBlockChooseResult.SelectBlockResult && actions.size == 1) {
            val choices = (actions.first() as SelectDicePoolResult).pools
            if (choices.size == 1 && choices.first().dice.size == 1) {
                DicePoolResultsSelected(listOf(
                    DicePoolChoice(id = 0, listOf(choices.first().dice.single().result))
                ))
            }
        }

        if (currentNode is PushStep.DecideToFollowUp && actions.size == 1) {
            return actions.first() as Confirm
        }

        return null
    }

    private suspend fun detectAndSendNonDialogUserInput(
        controller: GameController,
        request: ActionsRequest,
    ) {
        val userInputs: List<UserInput> =
            request.actions.groupBy { it::class }.map { action: Map.Entry<KClass<out ActionDescriptor>, List<ActionDescriptor>> ->
                when {
                    action.key == SelectPlayer::class -> {
                        SelectPlayerInput(action.value.map { PlayerSelected((it as SelectPlayer).player) })
                    }
                    action.key == SelectMoveType::class -> {
                        val values = action.value as List<SelectMoveType>
                        val player = controller.state.activePlayer!!

                        // For move selectings, some types of moves we want to display on the field
                        // others should be a specific action that must be selected.
                        // On-field moves are actually shortcutting the Rules engine, so we need
                        val inputs: UserInput = CompositeUserInput(values.map { actionDescriptor ->
                            when (actionDescriptor.type) {
                                MoveType.JUMP -> {
                                    SelectPlayerSubActionInput(
                                        activePlayerLocation = player.coordinates,
                                        actions = listOf(PlayerSubActionSelected("Jump", MoveTypeSelected(MoveType.JUMP)))
                                    )
                                }
                                MoveType.LEAP -> {
                                    SelectPlayerSubActionInput(
                                        activePlayerLocation = player.coordinates,
                                        actions = listOf(PlayerSubActionSelected("Leap", MoveTypeSelected(MoveType.LEAP)))
                                    )
                                }
//                                MoveType.RUSH -> {
//                                    // Rush moves should be converted to jus choosing the square on the field
//                                    val pathFinder = controller.rules.pathFinder
//                                    val startLocation = (controller.state.activePlayer!!.location as FieldCoordinate).coordinate
//                                    val allPaths = pathFinder.calculateAllPaths(
//                                        controller.state,
//                                        startLocation,
//                                        player.rushesLeft,
//                                    )
//                                    SelectMoveActionFieldLocationInput(
//                                        wrapperAction = calculateOptionsForMoveType(controller.state, controller.rules, player, MoveType.RUSH).map {
//                                            FieldSquareAction(
//                                                coordinate = (it as SelectFieldLocation).coordinate,
//                                                action = CompositeGameAction(
//                                                    list = listOf(
//                                                        MoveTypeSelected(MoveType.RUSH),
//                                                        FieldSquareSelected(it.coordinate)
//                                                    )
//                                                )
//                                            )
//                                        },
//                                        distances = allPaths
//                                    )                                }
                                MoveType.STANDARD -> {
                                    // Normal moves are converted to just choosing the square on the field
                                    val pathFinder = controller.rules.pathFinder
                                    val startLocation = controller.state.activePlayer!!.coordinates
                                    val requiresDodge = controller.rules.calculateMarks(controller.state, player.team, startLocation) > 0
                                    val allPaths = pathFinder.calculateAllPaths(
                                        controller.state,
                                        startLocation,
                                        if (requiresDodge) 1 else player.movesLeft,
                                    )
                                    SelectMoveActionFieldLocationInput(
                                        wrapperAction = calculateOptionsForMoveType(controller.state, controller.rules, player, MoveType.STANDARD).map { it: ActionDescriptor ->
                                            FieldSquareAction(
                                                coordinate = (it as SelectFieldLocation).coordinate,
                                                action = CompositeGameAction(
                                                    list = listOf(
                                                        MoveTypeSelected(MoveType.STANDARD),
                                                        FieldSquareSelected(it.coordinate)
                                                    )
                                                ),
                                                requiresRoll = it.requiresRush || it.requiresDodge
                                            )
                                        },
                                        distances = allPaths
                                    )
                                }
                                MoveType.STAND_UP -> {
                                    SelectPlayerSubActionInput(
                                        activePlayerLocation = player.coordinates,
                                        actions = listOf(PlayerSubActionSelected("Stand Up", MoveTypeSelected(MoveType.STAND_UP)))
                                    )
                                }
                                else -> TODO()
                            }
                        })
                        inputs
                    }
                    action.key == SelectFieldLocation::class -> {
                        SelectFieldLocationInput(
                            action.value.map {
                                val coords = (it as SelectFieldLocation).coordinate
                                FieldSquareAction(
                                    coordinate = coords,
                                    action = FieldSquareSelected(coords),
                                    requiresRoll = it.requiresRush
                                )
                            }
                        )
                    }
                    action.key == DeselectPlayer::class -> {
                        DeselectPlayerInput(action.value.map { PlayerDeselected((it as DeselectPlayer).player) })
                    }
                    action.key == SelectPlayerAction::class -> {
                        val playerLocation = controller.state.activePlayer?.location as FieldCoordinate
                        SelectPlayerActionInput(
                            playerLocation,
                            action.value.map { PlayerActionSelected((it as SelectPlayerAction).action.type) },
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

        // TODO Rethink how events are grouped and propagated all over. This is getting too complex :/
        fun sendEvent(event: UserInput) {
            when (event) {
                is CompositeUserInput -> {
                    error("Should not occur here: $event")
                }
                is DeselectPlayerInput -> fieldInputs.add(event)
                is DicePoolUserInputDialog -> dialogInputs.add(event)
                is EndActionInput -> fieldInputs.add(event)
                is SelectFieldLocationInput -> fieldInputs.add(event)
                is SelectPlayerActionInput -> fieldInputs.add(event)
                is SelectPlayerSubActionInput -> fieldInputs.add(event)
                is SelectPlayerInput -> fieldInputs.add(event)
                is UnknownInput -> unknownInputs.add(event)
                is DiceRollUserInputDialog -> dialogInputs.add(event)
                is SingleChoiceInputDialog -> dialogInputs.add(event)
                is SelectMoveActionFieldLocationInput -> fieldInputs.add(event)
                is IgnoreUserInput -> fieldInputs.add(event)
                is ResumeUserInput -> fieldInputs.add(event)
                is WaitingForUserInput -> {
                    fieldInputs.add(event)
                    unknownInputs.add(event)
                }

            }
        }

        uiEvents.forEach { event: UserInput ->
            when(event){
                is CompositeUserInput -> event.inputs.forEach { sendEvent(it) }
                else -> sendEvent(event)
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
                CancelWhenReady -> Cancel
                ConfirmWhenReady -> Confirm
                ContinueWhenReady -> Continue
                is DeselectPlayer -> PlayerDeselected(action.player)
                EndActionWhenReady -> EndAction
                EndSetupWhenReady -> EndSetup
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
                        DiceRollResults(rolls)
                    }
                }
                is SelectBlockType -> BlockTypeSelected(action.type)
                SelectCoinSide -> {
                    when (Random.nextInt(2)) {
                        0 -> CoinSideSelected(Coin.HEAD)
                        1 -> CoinSideSelected(Coin.TAIL)
                        else -> throw IllegalStateException("Unsupported value")
                    }
                }
                is SelectDicePoolResult -> {
                    DicePoolResultsSelected(action.pools.map { pool ->
                        DicePoolChoice(pool.id, pool.dice.shuffled().subList(0, pool.selectDice).map { it.result })
                    })
                }
                SelectDogout -> DogoutSelected
                is SelectFieldLocation -> FieldSquareSelected(action.x, action.y)
                is SelectInducement -> InducementSelected(action.id)
                is SelectMoveType -> MoveTypeSelected(action.type)
                is SelectNoReroll -> NoRerollSelected(action.dicePoolId)
                is SelectPlayer -> PlayerSelected(action.player)
                is SelectPlayerAction -> PlayerActionSelected(action.action.type)
                is SelectRandomPlayers -> {
                    RandomPlayersSelected(action.players.shuffled().subList(0, action.count))
                }
                is SelectRerollOption -> RerollOptionSelected(action.option)
                is SelectSkill -> SkillSelected(action.skill)
                TossCoin -> {
                    when (Random.nextInt(2)) {
                        0 -> CoinTossResult(Coin.HEAD)
                        1 -> CoinTossResult(Coin.TAIL)
                        else -> throw IllegalStateException("Unsupported value")
                    }
                }
            }
        }
    }
}
