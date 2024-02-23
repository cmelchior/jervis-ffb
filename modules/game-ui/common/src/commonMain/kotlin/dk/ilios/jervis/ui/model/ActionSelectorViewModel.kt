package dk.ilios.jervis.ui.model

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
import dk.ilios.jervis.actions.RerollSourceSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectAction
import dk.ilios.jervis.actions.SelectCoinSide
import dk.ilios.jervis.actions.SelectDogout
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.actions.SelectRandomPlayers
import dk.ilios.jervis.actions.SelectSkillRerollSource
import dk.ilios.jervis.actions.SelectTeamRerollSource
import dk.ilios.jervis.actions.TossCoin
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fumbbl.CalculatedJervisAction
import dk.ilios.jervis.fumbbl.FumbblReplayAdapter
import dk.ilios.jervis.fumbbl.JervisAction
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.procedures.SetupTeam
import dk.ilios.jervis.ui.GameMode
import dk.ilios.jervis.ui.Manual
import dk.ilios.jervis.ui.Replay
import dk.ilios.jervis.utils.createRandomAction
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class ActionSelectorViewModel(
    private val mode: GameMode,
    private val controller: GameController,
    private val actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
    private val actionSelectedChannel: Channel<GameAction>,
    private val fumbbl: FumbblReplayAdapter?
) {
    val scope = CoroutineScope(CoroutineName("ActionSelectorScope") + Dispatchers.Default)
    private val _availableActions: MutableSharedFlow<List<GameAction>> = MutableSharedFlow(extraBufferCapacity = 1)
    private val userSelectedAction = Channel<GameAction>(1)
    val availableActions: Flow<List<GameAction>> = _availableActions

    fun start() {
        scope.launch {
            when(mode) {
                Manual -> {
                    launch {
                        val actionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
                            val action: GameAction = runBlocking(Dispatchers.Default) {
                                actionRequestChannel.send(Pair(controller, availableActions))
                                actionSelectedChannel.receive()
                            }
                            action
                        }
                        controller.startCallbackMode(actionProvider)
                    }
                    startUserActionSelector()
                }
                dk.ilios.jervis.ui.Random -> startRandomMode()
                is Replay -> startReplayMode()
            }
        }
    }

    private fun startReplayMode() {
        var index = 0
        val replayCommands = fumbbl?.getCommands()
        scope.launch(Dispatchers.Default) {
            controller.startManualMode()
            while (!controller.stack.isEmpty()) {
                if (replayCommands != null && index <= replayCommands.size) {
                    val commandFromReplay = replayCommands[index]
                    if (commandFromReplay.expectedNode != controller.stack.currentNode()) {
                        throw IllegalStateException("""
                    Current node: ${controller.stack.currentNode()::class.qualifiedName}
                    Expected node: ${commandFromReplay.expectedNode::class.qualifiedName}
                    Action: ${
                            when(commandFromReplay) {
                                is CalculatedJervisAction -> commandFromReplay.actionFunc(controller.state, controller.rules)
                                is JervisAction -> commandFromReplay.action
                            }}
                """.trimIndent())
                    }
                    when(commandFromReplay) {
                        is CalculatedJervisAction -> controller.processAction(commandFromReplay.actionFunc(controller.state, controller.rules))
                        is JervisAction -> controller.processAction(commandFromReplay.action)
                    }
                    index += 1
                    delay(20)
                    continue
                }
            }
        }
    }

    private suspend fun startRandomMode() {
        val start = Instant.now()
        controller.startManualMode()
        while (!controller.stack.isEmpty()) {
            val actions = controller.getAvailableActions()
            if (!useManualAutomatedActions(controller)) {
                val selectedAction = createRandomAction(controller.state, actions)
                delay(20)
                controller.processAction(selectedAction)
            }
        }
        printStats(controller, start)
    }

    private fun printStats(controller: GameController, start: Instant) {
        val end = Instant.now()
        val duration = ChronoUnit.MILLIS.between(start, end)
        val commands = controller.commands.size
        val msPrCommand: Float = duration / commands.toFloat()
        println("Total time: $duration ms., Commands: $commands, timePrCommand: $msPrCommand ms.")
    }

    private suspend fun startUserActionSelector() {
        scope.launch {
            actions@while(true) {
                val (controller, actions) = actionRequestChannel.receive()
                if (useAutomatedActions(controller)) continue@actions
                val availableActions: List<GameAction> = actions.map { action ->
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
                        is SelectSkillRerollSource -> RerollSourceSelected(action.skill)
                        is SelectTeamRerollSource -> RerollSourceSelected(action.reroll)
                    }
                }
                _availableActions.emit(availableActions)
                val selectedAction = userSelectedAction.receive()
                actionSelectedChannel.send(selectedAction)
            }
        }
    }

    private suspend fun useManualAutomatedActions(controller: GameController): Boolean {
        if (controller.stack.firstOrNull()?.procedure == SetupTeam && controller.stack.firstOrNull()?.currentNode() == SetupTeam.SelectPlayerOrEndSetup) {
            if (controller.state.activeTeam.isHomeTeam()) {
                handleManualHomeKickingSetup(controller)
            } else {
                handleManualAwayKickingSetup(controller)
            }
            controller.processAction(EndSetup)
            return true
//        } else if (controller.stack.firstOrNull()?.procedure == TheKickOffEvent && controller.stack.firstOrNull()?.currentNode() == TheKickOffEvent.ResolveKickOffEvent) {
//            actionSelectedChannel.send(DiceResults(listOf(D6Result(2), D6Result(4))))
//            return true
        } else {
            return false
        }
    }

    private suspend fun handleManualHomeKickingSetup(
        controller: GameController
    ) {
        val game: Game = controller.state
        val team = game.activeTeam

        setupPlayer(team, PlayerNo(1), FieldCoordinate(12, 6), controller)
        setupPlayer(team, PlayerNo(2), FieldCoordinate(12, 7), controller)
        setupPlayer(team, PlayerNo(3), FieldCoordinate(12, 8), controller)
        setupPlayer(team, PlayerNo(4), FieldCoordinate(10, 1), controller)
        setupPlayer(team, PlayerNo(5), FieldCoordinate(10, 4), controller)
        setupPlayer(team, PlayerNo(6), FieldCoordinate(10, 10), controller)
        setupPlayer(team, PlayerNo(7), FieldCoordinate(10, 13), controller)
        setupPlayer(team, PlayerNo(8), FieldCoordinate(8, 1), controller)
        setupPlayer(team, PlayerNo(9), FieldCoordinate(8, 4), controller)
        setupPlayer(team, PlayerNo(10), FieldCoordinate(8, 10), controller)
        setupPlayer(team, PlayerNo(11), FieldCoordinate(8, 13), controller)
    }

    private suspend fun handleManualAwayKickingSetup(
        controller: GameController
    ) {
        val game: Game = controller.state
        val team = game.activeTeam

        setupPlayer(team, PlayerNo(1), FieldCoordinate(13, 6), controller)
        setupPlayer(team, PlayerNo(2), FieldCoordinate(13, 7), controller)
        setupPlayer(team, PlayerNo(3), FieldCoordinate(13, 8), controller)
        setupPlayer(team, PlayerNo(4), FieldCoordinate(15, 1), controller)
        setupPlayer(team, PlayerNo(5), FieldCoordinate(15, 4), controller)
        setupPlayer(team, PlayerNo(6), FieldCoordinate(15, 10), controller)
        setupPlayer(team, PlayerNo(7), FieldCoordinate(15, 13), controller)
        setupPlayer(team, PlayerNo(8), FieldCoordinate(17, 1), controller)
        setupPlayer(team, PlayerNo(9), FieldCoordinate(17, 4), controller)
        setupPlayer(team, PlayerNo(10), FieldCoordinate(17, 10), controller)
        setupPlayer(team, PlayerNo(11), FieldCoordinate(17, 13), controller)
    }



    private suspend fun useAutomatedActions(controller: GameController): Boolean {
        if (controller.stack.firstOrNull()?.procedure == SetupTeam && controller.stack.firstOrNull()?.currentNode() == SetupTeam.SelectPlayerOrEndSetup) {
            if (controller.state.activeTeam.isHomeTeam()) {
                handleHomeKickingSetup(controller, actionRequestChannel, actionSelectedChannel)
            } else {
                handleAwayKickingSetup(controller, actionRequestChannel, actionSelectedChannel)
            }
            actionSelectedChannel.send(EndSetup)
            return true
//        } else if (controller.stack.firstOrNull()?.procedure == TheKickOffEvent && controller.stack.firstOrNull()?.currentNode() == TheKickOffEvent.ResolveKickOffEvent) {
//            actionSelectedChannel.send(DiceResults(listOf(D6Result(2), D6Result(4))))
//            return true
        } else {
            return false
        }
    }

    private suspend fun handleHomeKickingSetup(
        controller: GameController,
        actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
        actionSelectedChannel: Channel<GameAction>
    ) {
        val game: Game = controller.state
        val team = game.activeTeam

        setupPlayer(team, PlayerNo(1), FieldCoordinate(12, 6), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(2), FieldCoordinate(12, 7), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(3), FieldCoordinate(12, 8), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(4), FieldCoordinate(10, 1), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(5), FieldCoordinate(10, 4), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(6), FieldCoordinate(10, 10), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(7), FieldCoordinate(10, 13), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(8), FieldCoordinate(8, 1), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(9), FieldCoordinate(8, 4), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(10), FieldCoordinate(8, 10), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(11), FieldCoordinate(8, 13), actionRequestChannel, actionSelectedChannel)
    }

    private suspend fun handleAwayKickingSetup(
        controller: GameController,
        actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
        actionSelectedChannel: Channel<GameAction>
    ) {
        val game: Game = controller.state
        val team = game.activeTeam

        setupPlayer(team, PlayerNo(1), FieldCoordinate(13, 6), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(2), FieldCoordinate(13, 7), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(3), FieldCoordinate(13, 8), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(4), FieldCoordinate(15, 1), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(5), FieldCoordinate(15, 4), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(6), FieldCoordinate(15, 10), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(7), FieldCoordinate(15, 13), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(8), FieldCoordinate(17, 1), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(9), FieldCoordinate(17, 4), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(10), FieldCoordinate(17, 10), actionRequestChannel, actionSelectedChannel)
        setupPlayer(team, PlayerNo(11), FieldCoordinate(17, 13), actionRequestChannel, actionSelectedChannel)
    }

    private fun setupPlayer(
        team: Team,
        playerNo: PlayerNo,
        fieldCoordinate: FieldCoordinate,
        controller: GameController
    ) {
        controller.processAction(PlayerSelected(team[playerNo]!!))
        controller.processAction(FieldSquareSelected(fieldCoordinate))
    }


    private suspend fun setupPlayer(
        team: Team,
        playerNo: PlayerNo,
        fieldCoordinate: FieldCoordinate,
        actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
        actionSelectedChannel: Channel<GameAction>
    ) {
        actionSelectedChannel.send(PlayerSelected(team[playerNo]!!))
        actionRequestChannel.receive()
        actionSelectedChannel.send(FieldSquareSelected(fieldCoordinate))
        actionRequestChannel.receive()
    }

    fun actionSelected(action: GameAction) {
        scope.launch {
            userSelectedAction.send(action)
        }
    }

}