package dk.ilios.jervis.ui.viewmodel

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.CompositeGameAction
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
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndSetupWhenReady
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.EndTurnWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.InducementSelected
import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.PlayerSubActionSelected
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectAction
import dk.ilios.jervis.actions.SelectCoinSide
import dk.ilios.jervis.actions.SelectDiceResult
import dk.ilios.jervis.actions.SelectDogout
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectInducement
import dk.ilios.jervis.actions.SelectMoveType
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.actions.SelectRandomPlayers
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.actions.SelectSkill
import dk.ilios.jervis.actions.SkillSelected
import dk.ilios.jervis.actions.TossCoin
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.context.CatchRollContext
import dk.ilios.jervis.model.context.MoveContext
import dk.ilios.jervis.model.context.PickupRollContext
import dk.ilios.jervis.model.context.RushRollContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.Bounce
import dk.ilios.jervis.procedures.CatchRoll
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
import dk.ilios.jervis.procedures.actions.block.PushStep
import dk.ilios.jervis.procedures.actions.block.Stumble
import dk.ilios.jervis.procedures.actions.foul.ArgueTheCallRoll
import dk.ilios.jervis.procedures.actions.foul.FoulContext
import dk.ilios.jervis.procedures.actions.foul.FoulStep
import dk.ilios.jervis.procedures.actions.move.DodgeRoll
import dk.ilios.jervis.procedures.actions.move.RushRoll
import dk.ilios.jervis.procedures.actions.move.calculateOptionsForMoveType
import dk.ilios.jervis.procedures.actions.pass.AccuracyRoll
import dk.ilios.jervis.procedures.actions.pass.PassContext
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
import dk.ilios.jervis.ui.GameScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.reflect.KClass

class ManualModeUiActionFactory(model: GameScreenModel, private val actions: List<GameAction>) : UiActionFactory(
    model,
) {
    override suspend fun start(scope: CoroutineScope) {
        scope.launch(errorHandler) {
            var initialActionsIndex = 0
            emitToField(WaitingForUserInput)
            val actionProvider: suspend (GameController, List<ActionDescriptor>) -> GameAction = { controller: GameController, availableActions: List<ActionDescriptor> ->
                if (initialActionsIndex < actions.size) {
                    val action = actions[initialActionsIndex]
                    initialActionsIndex++
                    action
                } else {
                    model.actionRequestChannel.send(Pair(controller, availableActions))
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

        if (!model.menuViewModel.isFeatureEnabled(Feature.REROLL_SUCCESSFUL_ACTIONS)) {
            if (actions.filterIsInstance<SelectNoReroll>().count { it.rollSuccessful == true} > 0) {
                return NoRerollSelected
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
        if (currentNode == BlockRoll.SelectBlockResult && actions.size == 1) {
            val choices: List<DieResult> = (actions.first() as SelectDiceResult).choices
            if (choices.size == 1) {
                return choices.first() as DBlockResult
            }
        }

        return null
    }

    private suspend fun detectAndSendNonDialogUserInput(
        controller: GameController,
        actions: List<ActionDescriptor>,
    ) {
        val userInputs: List<UserInput> =
            actions.groupBy { it::class }.map { action: Map.Entry<KClass<out ActionDescriptor>, List<ActionDescriptor>> ->
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
                                        activePlayerLocation = player.location.coordinate,
                                        actions = listOf(PlayerSubActionSelected("Jump", MoveTypeSelected(MoveType.JUMP)))
                                    )
                                }
                                MoveType.LEAP -> {
                                    SelectPlayerSubActionInput(
                                        activePlayerLocation = player.location.coordinate,
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
                                    val startLocation = (controller.state.activePlayer!!.location as FieldCoordinate).coordinate
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
                                        activePlayerLocation = player.location.coordinate,
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
                        DeselectPlayerInput(listOf(PlayerDeselected))
                    }
                    action.key == SelectAction::class -> {
                        val playerLocation = controller.state.activePlayer?.location as FieldCoordinate
                        SelectPlayerActionInput(
                            playerLocation,
                            action.value.map { PlayerActionSelected((it as SelectAction).action.type) },
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
        val rules = controller.rules
        val userInput =
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
                val diceCount = (actions.first() as RollDice).dice.size
                DiceRollUserInputDialog.createBlockRollDialog(diceCount, controller.state.blockContext!!.isBlitzing)
            }

            is BlockRoll.SelectBlockResult -> {
                DiceRollUserInputDialog.createSelectBlockDie(
                    actions.first() as SelectDiceResult
                )
            }

            is BothDown.AttackerChooseToUseBlock -> {
                val context = controller.state.bothDownContext!!
                SingleChoiceInputDialog.createUseSkillDialog(context.attacker, context.attacker.getSkill<Block>())
            }

            is BothDown.DefenderChooseToUseBlock -> {
                val context = controller.state.bothDownContext!!
                SingleChoiceInputDialog.createUseSkillDialog(context.defender, context.defender.getSkill<Block>())
            }

            is Bounce.RollDirection -> {
                SingleChoiceInputDialog.createBounceBallDialog(rules, D8Result.allOptions())
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

            is DetermineKickingTeam.ChooseKickingTeam -> {
                val choices =
                    listOf(
                        Confirm to "Kickoff",
                        Cancel to "Receive",
                    )
                SingleChoiceInputDialog.createChooseToKickoffDialog(controller.state.activeTeam, choices)
            }

            is DetermineKickingTeam.CoinToss -> {
                SingleChoiceInputDialog.createTossDialog(CoinTossResult.allOptions())
            }

            is DetermineKickingTeam.SelectCoinSide -> {
                SingleChoiceInputDialog.createSelectKickoffCoinTossResultDialog(
                    controller.state.activeTeam,
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
                    controller.state.pushContext!!.pusher
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
                val defender = controller.state.stumbleContext!!.defender
                SingleChoiceInputDialog.createUseSkillDialog(defender, defender.getSkill<Dodge>())
            }

            is Stumble.ChooseToUseTackle -> {
                val defender = controller.state.stumbleContext!!.attacker
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

        // TODO Rethink how events are grouped and propagated all over. This is getting too complex :/
        fun sendEvent(event: UserInput) {
            when (event) {
                is CompositeUserInput -> {
                    error("Should not occur here: $event")
                }
                is DeselectPlayerInput -> fieldInputs.add(event)
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
                is SelectAction -> PlayerActionSelected(action.action.type)
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

                is SelectNoReroll -> NoRerollSelected
                is SelectRerollOption -> RerollOptionSelected(action.option)
                is SelectDiceResult -> action.choices.random()
                is SelectMoveType -> MoveTypeSelected(action.type)
                is SelectSkill -> SkillSelected(action.skill)
                is SelectInducement -> InducementSelected(action.id)
            }
        }
    }
}
