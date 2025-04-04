package com.jervisffb.ui.menu.p2p.host

import androidx.compose.runtime.snapshots.SnapshotStateList
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialize.GameFileData
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.net.GameId
import com.jervisffb.net.LightServer
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.P2PActionProvider
import com.jervisffb.ui.game.state.RandomActionProvider
import com.jervisffb.ui.game.state.RemoteActionProvider
import com.jervisffb.ui.game.view.SidebarEntry
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.GameScreen
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.Manual
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.components.TeamInfo
import com.jervisffb.ui.menu.components.coach.CoachType
import com.jervisffb.ui.menu.components.setup.ConfigType
import com.jervisffb.ui.menu.p2p.AbstractClintNetworkMessageHandler
import com.jervisffb.ui.menu.p2p.P2PClientNetworkAdapter
import com.jervisffb.ui.menu.p2p.SelectP2PTeamScreenModel
import com.jervisffb.ui.menu.p2p.StartP2PGameScreenModel
import com.jervisffb.utils.copyToClipboard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel class for the P2P Host screen. This view model is responsible
 * for controlling the entire flow of setting up and connecting players, up until
 * running the game.
 */
class P2PHostScreenModel(private val navigator: Navigator, private val menuViewModel: MenuViewModel) : ScreenModel {

    val sidebarEntries: SnapshotStateList<SidebarEntry> = SnapshotStateList()

    // Which page are currently being shown
    val totalPages = 4
    val currentPage = MutableStateFlow(0)

    val networkAdapter = P2PClientNetworkAdapter()

    // Page 1: Setup Game
    val setupGameModel = SetupGameScreenModel(menuViewModel, this)
    var saveGameData: GameFileData? = null
    var rules: Rules? = null

    // Page 2: Select team
    val selectTeamModel = SelectP2PTeamScreenModel(
        menuViewModel = menuViewModel,
        getCoach = { setupGameModel.getCoach()!! },
        onTeamSelected = { teamSelected ->
            selectedTeam.value = teamSelected
        },
        getRules = { networkAdapter.rules ?: error("Rules are not loaded yet") }
    )
    val selectedTeam = MutableStateFlow<TeamInfo?>(null)
    private val _gameUrl = MutableStateFlow("")
    val gameUrl: StateFlow<String> = _gameUrl
    private var server: LightServer? = null

    // Page 3: Wait for opponent

    // Page 4: Accept game
    val acceptGameModel = StartP2PGameScreenModel(networkAdapter, menuViewModel)

    private var gameViewModel: GameScreenModel? = null

    init {
        val startEntries = listOf(
            SidebarEntry(
                name = "1. Configure Game",
                enabled = true,
                active = true,
            ),
            SidebarEntry(name = "2. Select Team"),
            SidebarEntry(name = "3. Wait For Opponent"),
            SidebarEntry(name = "4. Start Game")
        )
        sidebarEntries.addAll(startEntries)

        menuViewModel.navigatorContext.launch {
            networkAdapter.hostState.collect {
                // TODO We move state optimistically, so we probably need to check if things needs to be reset somehow.
                when (it) {
                    P2PHostState.START -> { /* Do nothing */ }
                    P2PHostState.SETUP_GAME -> {
                        if (currentPage.value > 0) {
                            goBackToPage(0)
                        }
                    }
                    P2PHostState.SELECT_TEAM -> {
                        TODO("Is this ever called")
                        currentPage.value = 1
                    }
                    P2PHostState.START_SERVER -> {
                        TODO("Is this ever called")
                        currentPage.value = 2
                    }
                    P2PHostState.JOIN_SERVER -> { }
                    P2PHostState.WAIT_FOR_CLIENT -> {
                        gotoNextPage(2)
                    }
                    P2PHostState.ACCEPT_GAME -> {
                        // Both teams have been chosen.
                        gotoNextPage(3)
                    }
                    P2PHostState.RUN_GAME -> {
                        val rules = networkAdapter.rules!!
                        val homeTeam = JervisSerialization.fixTeamRefs(networkAdapter.homeTeam.value!!)
                        homeTeam.coach = networkAdapter.homeCoach.value!!
                        val awayTeam = JervisSerialization.fixTeamRefs(networkAdapter.awayTeam.value!!)
                        awayTeam.coach = networkAdapter.awayCoach.value!!
                        val game = Game(rules, homeTeam, awayTeam, Field.Companion.createForRuleset(rules))
                        val gameController = GameEngineController(game, networkAdapter.initialActions)

                        val homeActionProvider = when (setupGameModel.coachSetupModel.playerType.value) {
                            CoachType.HUMAN -> ManualActionProvider(
                                gameController,
                                menuViewModel,
                                TeamActionMode.HOME_TEAM,
                                GameSettings(gameRules = rules),
                            )
                            // For now, we only support the Random AI player, so create it directly
                            CoachType.COMPUTER -> RandomActionProvider(TeamActionMode.HOME_TEAM, gameController).also { it.startActionProvider() }
                        }

                        val awayActionProvider = RemoteActionProvider(
                            TeamActionMode.HOME_TEAM,
                            gameController,
                        )

                        val actionProvider = P2PActionProvider(
                            gameController,
                            GameSettings(gameRules = rules),
                            homeActionProvider,
                            awayActionProvider,
                            networkAdapter
                        )

                        gameViewModel = GameScreenModel(
                            TeamActionMode.HOME_TEAM,
                            gameController = gameController,
                            gameController.state.homeTeam,
                            gameController.state.awayTeam,
                            actionProvider,
                            mode = Manual(TeamActionMode.HOME_TEAM),
                            menuViewModel = menuViewModel
                        ) {
                            menuViewModel.controller = gameController
                            menuViewModel.navigatorContext.launch {
                                networkAdapter.sendGameStarted()
                            }
                        }
                        navigator.push(GameScreen(menuViewModel, gameViewModel!!))
                    }
                    P2PHostState.CLOSE_GAME -> {}
                    P2PHostState.DONE -> {}
                }
            }
        }
    }

    fun gameSetupDone() {
        menuViewModel.navigatorContext.launch {
            val logoSize = LogoSize.SMALL
            if (isLoadingGame()) {
                saveGameData = setupGameModel.gameSetupModel.loadFileModel.gameFile ?: error("Game file is not loaded")
                val homeTeam = saveGameData!!.homeTeam
                val homeTeamLogo = IconFactory.loadRosterIcon(
                    homeTeam.id,
                    homeTeam.teamLogo ?: homeTeam.roster.logo,
                    logoSize
                )
                selectedTeam.value = TeamInfo(
                    teamId = homeTeam.id,
                    teamName = homeTeam.name,
                    teamRoster = homeTeam.roster.name,
                    teamValue = homeTeam.teamValue,
                    rerolls = homeTeam.rerolls.size,
                    logo = homeTeamLogo,
                    teamData = homeTeam
                )
                teamSelectionDone(gotoNextPage = false)
                gotoNextPage(2, skipPages = true)
            } else {
                rules = setupGameModel.createRules()
                gotoNextPage(1)
            }
        }
    }

    private fun isLoadingGame(): Boolean {
        val selectedGameTab = setupGameModel.gameSetupModel.selectedGameTab.value
        return setupGameModel.gameSetupModel.tabs[selectedGameTab].type == ConfigType.FROM_FILE
    }

    fun teamSelectionDone(gotoNextPage: Boolean = true) {
        _gameUrl.value = "ws://127.0.0.1:${setupGameModel.port.value}/joinGame?id=${setupGameModel.gameName.value}"
        startServer()
        if (gotoNextPage) {
            gotoNextPage(2)
        }
    }

    private fun startServer() {
        val team = selectedTeam.value?.teamData ?: error("Only on-client teams supported for now")
        if (saveGameData != null) {
            val saveGame = saveGameData!!
            server = LightServer(
                gameName = setupGameModel.gameName.value,
                rules = saveGame.game.rules,
                hostCoach = saveGame.homeTeam.coach.id,
                hostTeam = saveGame.homeTeam,
                clientCoach = saveGame.awayTeam.coach.id,
                clientTeam = saveGame.awayTeam,
                initialActions = saveGame.actions,
                testMode = true
            )
        } else {
            server = LightServer(
                gameName = setupGameModel.gameName.value,
                rules = setupGameModel.createRules(),
                hostCoach = CoachId("host-coach"), // TODO figure out what to do here
                hostTeam = selectedTeam.value?.teamData!!,
                clientCoach = null,
                clientTeam = null,
                initialActions = emptyList(),
                testMode = true
            )
        }
        menuViewModel.navigatorContext.launch {
            server?.start()
            networkAdapter.joinHost(
                gameUrl = "ws://127.0.0.1:${setupGameModel.port.value}/joinGame?id=${setupGameModel.gameName.value}",
                coachName = setupGameModel.coachSetupModel.coachName.value,
                gameId = GameId(setupGameModel.gameName.value),
                teamIfHost = selectedTeam.value?.teamData ?: error("Missing team"),
                handler = object: AbstractClintNetworkMessageHandler() { /* No op */ }
            )
            networkAdapter.teamSelected(selectedTeam.value!!)
        }
    }

    fun goBackToPage(previousPage: Int) {
        if (previousPage >= currentPage.value) {
            error("It is only allowed to go back: $previousPage")
        }
        sidebarEntries[previousPage] = sidebarEntries[previousPage].copy(active = true, onClick = null)
        for (index in previousPage + 1 .. currentPage.value) {
            sidebarEntries[index] = sidebarEntries[index].copy(active = false, enabled = false, onClick = null)
        }
        currentPage.value = previousPage
    }

    private fun gotoNextPage(nextPage: Int, skipPages: Boolean = false) {
        // Disable intermediate pages (if needed)
        val currentPage = currentPage.value
        if (skipPages && nextPage > currentPage + 1) {
            for (index in nextPage - 1 downTo currentPage) {
                sidebarEntries[index] = sidebarEntries[index].copy(active = false, enabled = false, onClick = null)
            }
        }
        sidebarEntries[currentPage] = sidebarEntries[currentPage].copy(enabled = true, active = false, onClick = { goBackToPage(currentPage) })
        sidebarEntries[nextPage] = sidebarEntries[nextPage].copy(enabled = true, active = true, onClick = null)
        this.currentPage.value = nextPage
    }

    fun userAcceptGame(gameAccepted: Boolean) {
        menuViewModel.navigatorContext.launch {
            if (gameAccepted) {
                networkAdapter.gameAccepted(gameAccepted)
            } else {
                networkAdapter.gameAccepted(gameAccepted) // Server will terminate connection
                goBackToPage(0)
            }
        }
    }

    fun copyUrlToClipboard(url: String) {
        copyToClipboard(url)
    }
}
