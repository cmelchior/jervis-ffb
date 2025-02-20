package com.jervisffb.ui.screen.p2p.host

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.net.GameId
import com.jervisffb.net.LightServer
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.ui.screen.GameScreen
import com.jervisffb.ui.screen.GameScreenModel
import com.jervisffb.ui.screen.Manual
import com.jervisffb.ui.screen.TeamActionMode
import com.jervisffb.ui.screen.p2p.AbstractClintNetworkMessageHandler
import com.jervisffb.ui.screen.p2p.P2PClientGameController
import com.jervisffb.ui.screen.p2p.StartP2PGameScreenModel
import com.jervisffb.ui.screen.p2p.TeamSelectorScreenModel
import com.jervisffb.ui.screen.p2p.client.SingleTeamNetworkGameRunner
import com.jervisffb.ui.viewmodel.MenuViewModel
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
    val selectTeamModel = TeamSelectorScreenModel(menuViewModel, { setupGameModel.getCoach()!! }, { teamSelected ->
        selectedTeam.value = teamSelected
    })
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
                        val runner = SingleTeamNetworkGameRunner(
                            controller.homeTeam.value!!,
                            controller
                        ) { clientIndex, clientAction ->
                            if (clientIndex > controller.lastServerActionIndex) {
                                menuViewModel.navigatorContext.launch {
                                    controller.sendActionToServer(clientIndex, clientAction)
                                }
                            }
                        }
                        gameScreenModel = GameScreenModel(
                            null,
                            null,
                            mode = Manual(TeamActionMode.HOME_TEAM),
                            menuViewModel = menuViewModel,
                            injectedGameRunner = runner,
                            onEngineInitialized = {
                                menuViewModel.navigatorContext.launch {
                                    controller.sendGameStarted()
                                }
                            }
                        )
                        controller.runner = runner
                        navigator.push(GameScreen(gameScreenModel!!))
                        // lastValidPage = 2
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
        server = LightServer(team, setupGameModel.gameName.value, testMode = true)
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
