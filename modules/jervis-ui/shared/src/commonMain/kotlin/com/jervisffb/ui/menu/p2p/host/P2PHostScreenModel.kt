package com.jervisffb.ui.menu.p2p.host

import androidx.compose.runtime.snapshots.SnapshotStateList
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.CoachType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialization.GameFileData
import com.jervisffb.net.GameId
import com.jervisffb.net.LightServer
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.ui.ICON_FACTORY
import com.jervisffb.ui.game.UiGameClientType
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.model.ModelRef
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.P2PActionProvider
import com.jervisffb.ui.game.state.RandomActionProvider
import com.jervisffb.ui.game.state.RemoteActionProvider
import com.jervisffb.ui.game.view.SidebarEntry
import com.jervisffb.ui.game.view.SidebarEntryState
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.GameScreen
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.Manual
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.components.TeamInfo
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

/**
 * ViewModel class for the P2P Host screen. This view model is responsible
 * for controlling the entire flow of setting up and connecting players, up until
 * running the game.
 */
class P2PHostScreenModel(private val navigator: Navigator, val menuViewModel: MenuViewModel) : ScreenModel {

    companion object {
        val LOG = jervisLogger()

        // How long to wait for our own server to confirm that we joined it. Everything happens over
        // loopback, so this only needs to be long enough to rule out a slow machine.
        private val JOIN_OWN_SERVER_TIMEOUT = 10.seconds
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
    val globalGameUrl: StateFlow<String>
        field = MutableStateFlow("")
    val localGameUrl: StateFlow<String>
        field = MutableStateFlow("")
    val globalGameUrlError = MutableStateFlow<String?>(null)
    private var server: LightServer? = null

    // Page 3: Wait for opponent
    // `true` while the server is shutting down. Shutting down takes long enough to be noticeable,
    // and the hosting coach stays on whatever page they came from until it completes, so the page
    // needs to explain why nothing is happening.
    val isServerShuttingDown: StateFlow<Boolean>
        field = MutableStateFlow(false)

    // Page 4: Accept game
    val acceptGameModel = StartP2PGameScreenModel(networkAdapter, menuViewModel)

    // Page 5: Loading screen
    private var gameViewModel: GameScreenModel? = null

    /** Sidebar states captured when "Start Game" locked the steps behind it, see [lockCompletedSteps]. */
    private var stateBeforeLockingSteps: List<SidebarEntryState>? = null

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

    // Starts the server on the Host and join it immediately. Returns `false` if
    // the server could not be started, e.g. because the port is already in use.
    // In that case an error dialog has already been shown to the hosting coach,
    // and the caller must leave the screen on the page it came from rather
    // than advancing to "Wait For Opponent".
    private suspend fun startServer(): Boolean {
        val team = selectedTeam.value?.teamData ?: error("Only on-client teams supported for now")
        val port = setupGameModel.port.value ?: error("Missing port")
        val newServer = if (saveGameData != null) {
            val saveGame = saveGameData!!
            LightServer(
                gameName = setupGameModel.gameName.value,
                rules = saveGame.game.rules,
                hostCoach = saveGame.homeTeam.coach.id,
                hostTeam = saveGame.homeTeam,
                clientCoach = saveGame.awayTeam.coach.id,
                clientTeam = saveGame.awayTeam,
                initialActions = saveGame.actions,
                testMode = true,
                port = port,
            )
        } else {
            LightServer(
                gameName = setupGameModel.gameName.value,
                rules = setupGameModel.createRules(),
                hostCoach = CoachId("host-coach"), // TODO figure out what to do here
                hostTeam = selectedTeam.value?.teamData!!.model,
                clientCoach = null,
                clientTeam = null,
                initialActions = emptyList(),
                testMode = true,
                port = port,
            )
        }
        server = newServer
        try {
            newServer.start()
            LOG.i { "Server started on port ${newServer.port}" }
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Throwable) {
            LOG.e { "[P2PHostScreen] Could not start the server on port $port:\n${ex.stackTraceToString()}" }
            resetServer()
            menuViewModel.showErrorDialog(
                title = "Could not start the game server",
                message = "Port $port is not available. Check that no other program (or game) is using it.",
            )
            return false
        }
        networkAdapter.joinHost(
            gameUrl = "ws://127.0.0.1:$port/joinGame?id=${setupGameModel.gameName.value}",
            coachName = setupGameModel.coachSetupModel.coachName.value,
            coachType = setupGameModel.coachSetupModel.coachType.value,
            gameId = GameId(setupGameModel.gameName.value),
            teamIfHost = selectedTeam.value?.teamData?.model ?: error("Missing team"),
            handler = object : AbstractClintNetworkMessageHandler() { /* No op */ }
        )
        networkAdapter.teamSelected(selectedTeam.value!!)

        // Confirm that we actually got into our own server before telling the coach we are waiting
        // for an opponent. `joinHost()` reports connection problems through the connection's close
        // reason instead of throwing, so a failed join is silent here. Left unchecked, that leaves
        // a server running that nobody is hosting: a Client can still connect to it, and would be
        // offered the Host's own team because the server does not know about it yet.
        val joinedOwnServer = withTimeoutOrNull(JOIN_OWN_SERVER_TIMEOUT) {
            networkAdapter.homeTeam.first { it != null }
        } != null
        if (!joinedOwnServer) {
            LOG.e { "[P2PHostScreen] Timed out joining our own server on port $port" }
            resetServer()
            menuViewModel.showErrorDialog(
                title = "Could not join the game server",
                message = "The server started on port $port, but did not accept the host. Try again.",
            )
            return false
        }
        return true
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
                sidebarEntries[index] = sidebarEntries[index].copy(state = SidebarEntryState.DONE_NOT_AVAILABLE)
            }
        }
        sidebarEntries[currentPage] = sidebarEntries[currentPage].copy(state = SidebarEntryState.DONE_AVAILABLE)
        sidebarEntries[nextPage] = sidebarEntries[nextPage].copy(state = SidebarEntryState.ACTIVE)
        this.currentPage.value = nextPage
    }

    // Reaching "Start Game" is a commitment: the Client is waiting for an answer. Lock the steps
    // behind us, so the side menu cannot be used to walk out of it without the Client being told.
    // Rejecting or accepting the game are the only ways on from here, and both report back.
    private fun lockCompletedSteps() {
        if (stateBeforeLockingSteps != null) return
        stateBeforeLockingSteps = sidebarEntries.map { it.state }
        for (index in 0 until currentPage.value) {
            sidebarEntries[index] = sidebarEntries[index].copy(state = SidebarEntryState.DONE_NOT_AVAILABLE)
        }
    }

    // Put the side menu back the way it was before "Start Game" locked it. The states are restored
    // rather than recomputed, because a step can legitimately have been unavailable beforehand:
    // loading a game from a file skips "Select Team" and marks it done-but-not-available.
    private fun unlockCompletedSteps() {
        val previousStates = stateBeforeLockingSteps ?: return
        stateBeforeLockingSteps = null
        previousStates.forEachIndexed { index, state ->
            sidebarEntries[index] = sidebarEntries[index].copy(state = state)
        }
    }

    private fun goBackToPage(previousPage: Int) {
        if (previousPage >= currentPage.value) {
            error("It is only allowed to go back: $previousPage")
        }
        unlockCompletedSteps()
        sidebarEntries[previousPage] = sidebarEntries[previousPage].copy(state = SidebarEntryState.ACTIVE)
        for (index in previousPage + 1..currentPage.value) {
            sidebarEntries[index] = sidebarEntries[index].copy(state = SidebarEntryState.NOT_READY)
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
        globalGameUrl.value = ""
        localGameUrl.value = ""
        globalGameUrlError.value = null
        val oldServer = server
        server = null
        // TODO Do we need both of these? Probably this could be optimized.
        // networkAdapter.sendServerClosed()

        // Wait for the shutdown to complete. If we don't wait, we risk having
        // the port still in use when a new attempt was made at starting the
        // server.
        oldServer?.let { server ->
            isServerShuttingDown.value = true
            try {
                server.stop()
            } finally {
                isServerShuttingDown.value = false
            }
            LOG.i { "Server stopped on port ${server.port}" }
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
        homeTeam.model.coach = networkAdapter.homeCoach.value!!
        val awayTeam = networkAdapter.awayTeam.value!!
        awayTeam.model.coach = networkAdapter.awayCoach.value!!
        val game = Game(rules, homeTeam.model, awayTeam.model)
        val gameController = GameEngineController(game, networkAdapter.initialActions)

        val homeActionProvider = when (setupGameModel.coachSetupModel.coachType.value) {
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
            uiClientType = UiGameClientType.P2P_HOST,
            uiMode = TeamActionMode.HOME_TEAM,
            gameController = gameController,
            homeTeam = gameController.state.homeTeam,
            awayTeam = gameController.state.awayTeam,
            actionProvider = actionProvider,
            mode = Manual(TeamActionMode.HOME_TEAM),
            menuViewModel = menuViewModel,
            onEngineInitialized = {
                menuViewModel.controller = gameController
                menuViewModel.navigatorContext.launch {
                    networkAdapter.sendGameStarted()
                }
            },
            onGameStopped = {
                menuViewModel.backgroundContext.launch {
                    server?.stop()
                }
            }
        ).also {
            it.waitForOpponent()
        }
        // Push the game on top rather than replacing this screen. Popping it here removed the
        // only screen the Client-rejects-the-game path has to come back to, so that path dropped
        // the Host all the way out to the main menu, and `goBackToPage(2)` below adjusted a screen
        // that was no longer on the stack. Worse, removing the screen runs `onDispose()`, which
        // shut down the server the game is about to be played on.
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
        val homeTeamLogo = ICON_FACTORY.loadRosterIcon(
            homeTeam.id,
            homeTeam.teamLogo ?: homeTeam.roster.logo,
            LogoSize.SMALL
        )
        selectedTeam.value = TeamInfo(
            teamId = homeTeam.id,
            teamName = homeTeam.name,
            version = homeTeam.version,
            type = homeTeam.type,
            teamRoster = homeTeam.roster.name,
            teamValue = homeTeam.teamValue,
            rerolls = homeTeam.rerolls.size,
            logo = homeTeamLogo,
            teamData = ModelRef(homeTeam.id, homeTeam)
        )
    }

    // Go into "Waiting for Opponent" screen from either "Setup" or "Select Team".
    // Returns `false` if the server could not be started, in which case the
    // hosting coach has already been told why. See [startServer].
    private suspend fun prepareWaitingForOpponent(): Boolean {
        globalGameUrl.value = "Fetching..."
        localGameUrl.value = "Fetching..."
        menuViewModel.backgroundContext.launch {
            val localIp = getLocalIpAddress()
            localGameUrl.value = "ws://$localIp:${setupGameModel.port.value}/joinGame?id=${setupGameModel.gameName.value}"
            val publicIp = getPublicIpAddress()
            if (publicIp.isNullOrBlank()) {
                globalGameUrlError.value = "Unable to get IP address. Goto https://api.ipify.org to see your public IP address"
            }
            globalGameUrl.value = "ws://$publicIp:${setupGameModel.port.value}/joinGame?id=${setupGameModel.gameName.value}"
        }
        return startServer()
    }

    // Handle all state transitions
    inner class Workflow() {
        // Must be single-threaded to serialize state updates
        private val stateChangeScope = CoroutineScope(singleThreadDispatcher("HostScreenThread"))

        // A single-threaded dispatcher only serializes the steps of a transition, not the whole
        // transition. `changeState` suspends (starting a server, joining it, loading a game), and
        // while it is suspended the thread happily runs the next queued transition, which then
        // reads a `currentState` that has not been updated yet and repeats work the first one is
        // still doing. This mutex makes a transition atomic with respect to the others.
        private val stateChangeMutex = Mutex()
        private var currentState = P2PHostState.START
        fun handleHostStateChange(newState: P2PHostState) {
            stateChangeScope.launch {
                // A failed state change must never take down the app. `stateChangeScope` has no
                // parent to report to, so anything escaping here would surface as an uncaught
                // coroutine exception (see `LightServer.start()` failing to bind its port).
                try {
                    stateChangeMutex.withLock {
                        changeState(newState)
                    }
                } catch (ex: CancellationException) {
                    throw ex
                } catch (ex: Throwable) {
                    LOG.e { "[P2PHostScreen] Failed state change: $currentState -> $newState\n${ex.stackTraceToString()}" }
                    menuViewModel.showErrorDialog("Error while setting up the P2P game.", error = ex)
                }
            }
        }

        private suspend fun changeState(newState: P2PHostState) {
            LOG.d { "[P2PHostScreen] state change: $currentState -> $newState" }
            if (newState == currentState) return
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
                            if (!prepareWaitingForOpponent()) return
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
                            if (!prepareWaitingForOpponent()) return
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
                            lockCompletedSteps()
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

        fun getStartingSidebarEntries(): List<SidebarEntry> {
            return listOf(
                SidebarEntry(
                    name = "1. Configure Game",
                    state = SidebarEntryState.ACTIVE,
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
        // Reaching here means the Host left the P2P flow for good: the game screen sits on top of
        // this one, so this runs after it has been popped. Always release the server. The previous
        // `hostState != RUN_GAME` guard was racing the server's own state update, and it decided
        // to shut the server down at the exact moment the Host accepted a game.
        menuViewModel.backgroundContext.launch {
            server?.stop()
        }
    }
}
