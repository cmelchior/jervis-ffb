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
import com.jervisffb.net.GameId
import com.jervisffb.net.LightServer
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.P2PActionProvider
import com.jervisffb.ui.game.state.RandomActionProvider
import com.jervisffb.ui.game.state.RemoteActionProvider
import com.jervisffb.ui.game.view.SideBarEntryState
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
import com.jervisffb.utils.getLocalIpAddress
import com.jervisffb.utils.getPublicIpAddress
import com.jervisffb.utils.jervisLogger
import com.jervisffb.utils.singleThreadDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel class for the P2P Host screen. This view model is responsible
 * for controlling the entire flow of setting up and connecting players, up until
 * running the game.
 */
class P2PHostScreenModel(private val navigator: Navigator, private val menuViewModel: MenuViewModel) : ScreenModel {

    companion object {
        val LOG = jervisLogger()
    }

    // Handles all state transitions
    var workflow: Workflow = Workflow()

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
    ) { teamSelected ->
        selectedTeam.value = teamSelected
    }
    val selectedTeam = MutableStateFlow<TeamInfo?>(null)
    private val _globalGameUrl = MutableStateFlow("")
    val globalUrl: StateFlow<String> = _globalGameUrl
    private val _localGameUrl = MutableStateFlow("")
    val localUrl: StateFlow<String> = _localGameUrl
    val globalGameUrlError = MutableStateFlow<String?>(null)
    private var server: LightServer? = null

    // Page 3: Wait for opponent
    // No current model or state is exposed

    // Page 4: Accept game
    val acceptGameModel = StartP2PGameScreenModel(networkAdapter, menuViewModel)

    // Page 5: Loading screen
    private var gameViewModel: GameScreenModel? = null

    init {
        sidebarEntries.addAll(workflow.getStartingSidebarEntries())
        // Start listening to state changes sent by the server
        menuViewModel.navigatorContext.launch {
            networkAdapter.hostState.collect { newState ->
                workflow.handleHostStateChange(newState)
            }
        }
    }

    // Called from the UI when pressing "Next" on the "Configure" screen
    fun userAcceptedGameSetup() {
        if (isLoadingGameFromFile()) {
            workflow.handleHostStateChange(P2PHostState.WAIT_FOR_CLIENT)
        } else {
            workflow.handleHostStateChange(P2PHostState.SELECT_TEAM)
        }
    }

    // Called from the UI when pressing "Next" on the "Select Team" screen
    fun userAcceptedTeam() {
        workflow.handleHostStateChange(P2PHostState.WAIT_FOR_CLIENT)
    }

    // Starts the server on the Host and join it immediately
    private suspend fun startServer() {
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
        server?.start()
        networkAdapter.joinHost(
            gameUrl = "ws://127.0.0.1:${setupGameModel.port.value}/joinGame?id=${setupGameModel.gameName.value}",
            coachName = setupGameModel.coachSetupModel.coachName.value,
            gameId = GameId(setupGameModel.gameName.value),
            teamIfHost = selectedTeam.value?.teamData ?: error("Missing team"),
            handler = object : AbstractClintNetworkMessageHandler() { /* No op */ }
        )
        networkAdapter.teamSelected(selectedTeam.value!!)
    }

    fun userAcceptGame(gameAccepted: Boolean) {
        menuViewModel.navigatorContext.launch {
            if (gameAccepted) {
                workflow.handleHostStateChange(P2PHostState.ACCEPTED_GAME)
            } else {
                workflow.handleHostStateChange(P2PHostState.SELECT_TEAM)
            }
        }
    }

    fun userCopyUrlToClipboard(url: String) {
        copyToClipboard(url)
    }

    // Returns `true` if the configuration is defining loading a save file rather than starting a new
    // game. This effects the flow of the sidebar.
    private fun isLoadingGameFromFile(): Boolean {
        val selectedGameTab = setupGameModel.gameSetupModel.selectedGameTab.value
        return setupGameModel.gameSetupModel.tabs[selectedGameTab].type == ConfigType.FROM_FILE
    }

    private fun gotoNextPage(nextPage: Int) {
        // If next page is not the immediate next, we just automatically skip them
        // and do not allow you go jump to them (but they are still marked as "done")
        val currentPage = currentPage.value
        val skipPages = (nextPage - currentPage > 1)
        if (skipPages) {
            for (index in nextPage - 1 downTo currentPage) {
                sidebarEntries[index] = sidebarEntries[index].copy(state = SideBarEntryState.DONE_NOT_AVAILABLE)
            }
        }
        sidebarEntries[currentPage] = sidebarEntries[currentPage].copy(state = SideBarEntryState.DONE_AVAILABLE)
        sidebarEntries[nextPage] = sidebarEntries[nextPage].copy(state = SideBarEntryState.ACTIVE)
        this.currentPage.value = nextPage
    }

    private fun goBackToPage(previousPage: Int) {
        if (previousPage >= currentPage.value) {
            error("It is only allowed to go back: $previousPage")
        }
        sidebarEntries[previousPage] = sidebarEntries[previousPage].copy(state = SideBarEntryState.ACTIVE)
        for (index in previousPage + 1..currentPage.value) {
            sidebarEntries[index] = sidebarEntries[index].copy(state = SideBarEntryState.NOT_READY)
        }
        currentPage.value = previousPage
    }

    private fun resetRulesSelection() {
        saveGameData = null
        rules = null
    }

    private fun resetTeamSelection() {
        selectTeamModel.reset()
        selectedTeam.value = null
    }

    private suspend fun resetServer() {
        _globalGameUrl.value = ""
        _localGameUrl.value = ""
        globalGameUrlError.value = null
        val oldServer = server
        server = null
        menuViewModel.backgroundContext.launch {
            // TODO Do we need both of these? Probably this could be optimized.
            // networkAdapter.sendServerClosed()
            oldServer?.stop()
        }
    }

    // Called in case the Host itself rejects the game
    private suspend fun resetScreenModel(page: Int = 0) {
        resetTeamSelection()
        if (page == 0) {
            resetRulesSelection()
        }
        resetServer()
    }

    private fun initializeGameModel() {
        val rules = networkAdapter.rules!!
        val homeTeam = networkAdapter.homeTeam.value!!
        homeTeam.coach = networkAdapter.homeCoach.value!!
        val awayTeam = networkAdapter.awayTeam.value!!
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
        }.also {
            it.waitForOpponent()
        }
        navigator.pop()
        navigator.push(GameScreen(menuViewModel, gameViewModel!!))
    }

    // Go from Configure -> Select Team
    private fun prepareTeamSelection() {
        rules = setupGameModel.createRules()
        selectTeamModel.initializeTeamList(rules!!)
    }

    // Go from Configure -> Waiting for Opponent (due to selecting a save file)
    // We can only get here if the load file is valid.
    private suspend fun prepareSaveFile() {
        saveGameData = setupGameModel.gameSetupModel.loadFileModel.gameFile ?: error("Game file is not loaded")
        val homeTeam = saveGameData!!.homeTeam
        val homeTeamLogo = IconFactory.loadRosterIcon(
            homeTeam.id,
            homeTeam.teamLogo ?: homeTeam.roster.logo,
            LogoSize.SMALL
        )
        selectedTeam.value = TeamInfo(
            teamId = homeTeam.id,
            teamName = homeTeam.name,
            type = homeTeam.type,
            teamRoster = homeTeam.roster.name,
            teamValue = homeTeam.teamValue,
            rerolls = homeTeam.rerolls.size,
            logo = homeTeamLogo,
            teamData = homeTeam
        )
    }

    // Go into "Waiting for Opponent" screen from either "Setup" or "Select Team"
    private suspend fun prepareWaitingForOpponent() {
        _globalGameUrl.value = "Fetching..."
        _localGameUrl.value = "Fetching..."
        menuViewModel.backgroundContext.launch {
            val localIp = getLocalIpAddress()
            _localGameUrl.value = "ws://$localIp:${setupGameModel.port.value}/joinGame?id=${setupGameModel.gameName.value}"
            val publicIp = getPublicIpAddress()
            if (publicIp.isNullOrBlank()) {
                globalGameUrlError.value = "Unable to get IP address. Goto https://api.ipify.org to see your public IP address"
            }
            _globalGameUrl.value = "ws://$publicIp:${setupGameModel.port.value}/joinGame?id=${setupGameModel.gameName.value}"
        }
        startServer()
    }

    // Handle all state transitions
    inner class Workflow() {
        // Must be single-threaded to serialize state updates
        private val stateChangeScope = CoroutineScope(singleThreadDispatcher("HostScreenThread"))
        private var currentState = P2PHostState.START
        fun handleHostStateChange(newState: P2PHostState) {
            stateChangeScope.launch {
                LOG.d { "[P2PHostScreen] state change: $currentState -> $newState" }
                if (newState == currentState) return@launch
                when (currentState) {
                    P2PHostState.START -> checkState(newState, P2PHostState.SETUP_GAME)
                    P2PHostState.SETUP_GAME -> {
                        // If Save File Game, move directly to Waiting for Opponent
                        // If New Game, move to selecting a team
                        when (newState) {
                            P2PHostState.SELECT_TEAM -> {
                                prepareTeamSelection()
                                gotoNextPage(1)
                            }
                            P2PHostState.WAIT_FOR_CLIENT -> {
                                prepareSaveFile()
                                prepareWaitingForOpponent()
                                gotoNextPage(2)
                            }
                            else -> unsupportedStateChange(newState)
                        }
                    }
                    P2PHostState.SELECT_TEAM -> {
                        when (newState) {
                            P2PHostState.SETUP_GAME -> {
                                resetTeamSelection()
                                resetRulesSelection()
                                goBackToPage(0)
                            }
                            P2PHostState.WAIT_FOR_CLIENT -> {
                                prepareWaitingForOpponent()
                                gotoNextPage(2)
                            }
                            else -> unsupportedStateChange(newState)
                        }
                    }
                    P2PHostState.START_SERVER -> { /* Ignore, should just be a temporary state */ }
                    P2PHostState.JOIN_SERVER -> error("Server state not supported: $currentState -> $newState")
                    P2PHostState.WAIT_FOR_CLIENT -> {
                        when (newState) {
                            P2PHostState.SETUP_GAME -> {
                                resetServer()
                                resetTeamSelection()
                                resetRulesSelection()
                                goBackToPage(0)
                            }
                            P2PHostState.SELECT_TEAM -> {
                                resetServer()
                                resetTeamSelection()
                                goBackToPage(1)
                            }
                            P2PHostState.ACCEPT_GAME -> {
                                // Selected teams
                                gotoNextPage(3)
                            }
                            else -> unsupportedStateChange(newState)
                        }
                    }
                    P2PHostState.ACCEPT_GAME -> {
                        when (newState) {
                            P2PHostState.START -> TODO()
                            P2PHostState.SETUP_GAME -> {
                                networkAdapter.gameAccepted(false)
                                resetServer()
                                resetTeamSelection()
                                resetRulesSelection()
                                goBackToPage(0)
                            }
                            P2PHostState.SELECT_TEAM -> {
                                resetServer()
                                resetTeamSelection()
                                goBackToPage(1)
                            }
                            P2PHostState.WAIT_FOR_CLIENT -> {
                                // How to show Client rejection?
                                goBackToPage(2)
                            }
                            P2PHostState.ACCEPT_GAME -> TODO()
                            P2PHostState.ACCEPTED_GAME -> {
                                networkAdapter.gameAccepted(true)
                                initializeGameModel()
                            }
                            P2PHostState.RUN_GAME -> {
                                // Should trigger next step on the loading screen
                                gameViewModel?.gameAcceptedByAllPlayers() ?: error("GameViewModel game not available")
                            }
                            P2PHostState.CLOSE_GAME -> TODO()
                            P2PHostState.DONE -> TODO()
                            P2PHostState.START_SERVER -> TODO()
                            P2PHostState.JOIN_SERVER -> TODO()
                        }
                    }
                    P2PHostState.ACCEPTED_GAME -> {
                        when (newState) {
                            P2PHostState.WAIT_FOR_CLIENT -> {
                                // Client rejected the game
                                navigator.pop()
                                goBackToPage(2)
                            }
                            P2PHostState.RUN_GAME -> {
                                gameViewModel?.gameAcceptedByAllPlayers() ?: error("GameViewModel game not available")
                            }
                            else -> unsupportedStateChange(newState)
                        }
                    }
                    P2PHostState.RUN_GAME -> TODO()
                    P2PHostState.CLOSE_GAME -> TODO()
                    P2PHostState.DONE -> { /* Ignore, no need to handle this */ }
                }

                // After preparing the UI, update the UI state
                currentState = newState
            }
        }

        fun getStartingSidebarEntries(): List<SidebarEntry> {
            return listOf(
                SidebarEntry(
                    name = "1. Configure Game",
                    state = SideBarEntryState.ACTIVE,
                    onClick = { workflow.handleHostStateChange(P2PHostState.SETUP_GAME) },
                ),
                SidebarEntry(
                    name = "2. Select Team",
                    onClick = { workflow.handleHostStateChange(P2PHostState.SELECT_TEAM) },
                ),
                SidebarEntry(
                    name = "3. Wait For Opponent",
                    onClick = { workflow.handleHostStateChange(P2PHostState.WAIT_FOR_CLIENT) },
                ),
                SidebarEntry(
                    name = "4. Start Game",
                    onClick = { workflow.handleHostStateChange(P2PHostState.ACCEPT_GAME) },
                )
            )
        }

        private fun checkState(newState: P2PHostState, expectedState: P2PHostState) {
            if (newState != expectedState) {
                error("Unsupported state change: $currentState -> $newState")
            }
        }

        private fun unsupportedStateChange(newState: P2PHostState) {
            error("Unsupported state change (from file): $currentState -> $newState")
        }
    }

    override fun onDispose() {
        menuViewModel.backgroundContext.launch {
            if (networkAdapter.hostState.value != P2PHostState.RUN_GAME) {
                server?.stop()
            }
        }
    }
}
