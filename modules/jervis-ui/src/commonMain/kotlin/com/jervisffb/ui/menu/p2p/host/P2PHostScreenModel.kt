package com.jervisffb.ui.menu.p2p.host

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.net.GameId
import com.jervisffb.net.LightServer
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.P2PActionProvider
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.GameScreen
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.Manual
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.components.TeamInfo
import com.jervisffb.ui.menu.p2p.AbstractClintNetworkMessageHandler
import com.jervisffb.ui.menu.p2p.P2PClientGameController
import com.jervisffb.ui.menu.p2p.SelectP2PTeamScreenModel
import com.jervisffb.ui.menu.p2p.StartP2PGameScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel class for the P2P Host screen. This view model is responsible
 * for controlling the entire flow of setting up and connecting players, up until
 * running the game.
 */
class P2PHostScreenModel(private val navigator: Navigator, private val menuViewModel: MenuViewModel) : ScreenModel {

    // Which page are currently being shown
    val totalPages = 4
    val currentPage = MutableStateFlow(0)

    // TODO Split out game creation
    val controller = P2PClientGameController()

    // Page 1: Setup Game
    val setupGameModel = SetupGameScreenModel(menuViewModel, this)

    // Page 2: Select team
    val selectTeamModel = SelectP2PTeamScreenModel(
        menuViewModel = menuViewModel,
        getCoach = { setupGameModel.getCoach()!! },
        onTeamSelected = { teamSelected ->
            selectedTeam.value = teamSelected
        },
        getRules = { controller.rules ?: error("Rules are not loaded yet") }
    )
    val selectedTeam = MutableStateFlow<TeamInfo?>(null)
    private val _gameUrl = MutableStateFlow("")
    val gameUrl: StateFlow<String> = _gameUrl
    private var server: LightServer? = null

    // Page 3: Wait for opponent


    // Page 4: Accept game
    val acceptGameModel = StartP2PGameScreenModel(controller, menuViewModel)

    private var gameScreenModel: GameScreenModel? = null

    val validGameSetup = MutableStateFlow(true)
    val validTeamSelection = MutableStateFlow(false)
    val validWaitingForOpponent = MutableStateFlow(false)


    init {
        menuViewModel.navigatorContext.launch {
            controller.hostState.collect {
                // TODO We move state optimistically, so we probably need to check if things needs to be reset somehow.
                when (it) {
                    P2PHostState.START -> { /* Do nothing */ }
                    P2PHostState.SETUP_GAME -> { currentPage.value = 0 }
                    P2PHostState.SELECT_TEAM -> { currentPage.value = 1 }
                    P2PHostState.START_SERVER -> { currentPage.value = 2 }
                    P2PHostState.JOIN_SERVER -> { }
                    P2PHostState.WAIT_FOR_CLIENT -> {
                        currentPage.value = 2
                    }
                    P2PHostState.ACCEPT_GAME -> {
                        // Both teams have been chosen.
                        currentPage.value = 3
                    }
                    P2PHostState.RUN_GAME -> {
                        val rules = controller.rules!!
                        val homeTeam = JervisSerialization.fixTeamRefs(controller.homeTeam.value!!)
                        homeTeam.coach = controller.homeCoach.value!!
                        val awayTeam = JervisSerialization.fixTeamRefs(controller.awayTeam.value!!)
                        awayTeam.coach = controller.awayCoach.value!!
                        val game = Game(rules, homeTeam, awayTeam, Field.Companion.createForRuleset(rules))
                        val gameController = GameEngineController(game)

                        val homeActionProvider = ManualActionProvider(
                            gameController,
                            menuViewModel,
                            TeamActionMode.HOME_TEAM,
                            GameSettings(gameRules = rules),
                        )
                        val awayActionProvider = ManualActionProvider(
                            gameController,
                            menuViewModel,
                            TeamActionMode.HOME_TEAM,
                            GameSettings(gameRules = rules),
                        )

                        val actionProvider = P2PActionProvider(
                            gameController,
                            GameSettings(gameRules = rules),
                            homeActionProvider,
                            awayActionProvider,
                            controller
                        )

                        gameScreenModel = GameScreenModel(
                            gameController = gameController,
                            gameController.state.homeTeam,
                            gameController.state.awayTeam,
                            actionProvider,
                            mode = Manual(TeamActionMode.HOME_TEAM),
                            menuViewModel = menuViewModel
                        ) {
                            menuViewModel.controller = gameController
                            menuViewModel.navigatorContext.launch {
                                controller.sendGameStarted()
                            }
                        }
                        navigator.push(GameScreen(gameScreenModel!!))
                    }
                    P2PHostState.CLOSE_GAME -> {}
                    P2PHostState.DONE -> {}
                }
            }
        }
    }

    fun gameSetupDone() {
        // Should anything be saved here?
        currentPage.value = 1
    }

    fun teamSelectionDone() {
        _gameUrl.value = "ws://127.0.0.1:${setupGameModel.port.value}/joinGame?id=${setupGameModel.gameName.value}"
        startServer()
        currentPage.value = 2
    }

    private fun startServer() {
        val team = selectedTeam.value?.teamData ?: error("Only on-client teams supported for now")
        val rules = setupGameModel.createRules()
        server = LightServer(rules, team, setupGameModel.gameName.value, testMode = true)
        menuViewModel.navigatorContext.launch {
            server?.start()
            controller.joinHost(
                gameUrl = "ws://127.0.0.1:${setupGameModel.port.value}/joinGame?id=${setupGameModel.gameName.value}",
                coachName = setupGameModel.coachName.value,
                gameId = GameId(setupGameModel.gameName.value),
                teamIfHost = selectedTeam.value?.teamData ?: error("Missing team"),
                handler = object: AbstractClintNetworkMessageHandler() { /* No op */ }
            )
            controller.teamSelected(selectedTeam.value!!)
        }
    }

    fun goBackToPage(previousPage: Int) {
        if (previousPage >= currentPage.value) {
            error("It is only allowed to go back: $previousPage")
        }
        currentPage.value = previousPage
    }

    fun userAcceptGame(gameAccepted: Boolean) {
        menuViewModel.navigatorContext.launch {
            if (gameAccepted) {
                controller.gameAccepted(gameAccepted)
            } else {
                controller.gameAccepted(gameAccepted) // Server will terminate connection
//                selectedTeam.value = null
//                canCreateGame.value = false
//                joinHostModel.reset()
//                selectTeamModel.reset()
//                acceptGameModel.reset()
//                lastValidPage = 0
                // TODO
                currentPage.value = 0
            }
        }
    }
}
