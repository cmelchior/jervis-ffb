package com.jervisffb.ui.menu.p2p.client

import androidx.compose.runtime.snapshots.SnapshotStateList
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.model.CoachType.COMPUTER
import com.jervisffb.engine.model.CoachType.HUMAN
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.serialization.JervisTeamFile
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.messages.InvalidTeamServerError
import com.jervisffb.net.messages.P2PClientState
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
import com.jervisffb.ui.menu.p2p.Connected
import com.jervisffb.ui.menu.p2p.Connecting
import com.jervisffb.ui.menu.p2p.Disconnected
import com.jervisffb.ui.menu.p2p.P2PClientNetworkAdapter
import com.jervisffb.ui.menu.p2p.SelectP2PTeamScreenModel
import com.jervisffb.ui.menu.p2p.StartP2PGameScreenModel
import com.jervisffb.ui.menu.p2p.host.P2PHostScreenModel.Companion.LOG
import com.jervisffb.utils.singleThreadDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

/**
 * ViewModel class for the P2P Join screen. This view model is responsible
 * for controlling the entire flow of joining the host, selecting the team up
 * until running the actual game.
 */
class P2PClientScreenModel(private val navigator: Navigator, val menuViewModel: MenuViewModel) : ScreenModel {

    // Handles all state transitions
    private val workflow: Workflow = Workflow()

    val sidebarEntries: SnapshotStateList<SidebarEntry> = SnapshotStateList()

    // Adapter responsible for mapping network events to events that can be handled by the UI
    val networkAdapter = P2PClientNetworkAdapter()

    // Which page are currently being shown
    val totalPages = 3
    val currentPage = MutableStateFlow(0) // 0-indexed

    private val _isConnectingToHost = MutableStateFlow(false)
    val isConnectingToHost: StateFlow<Boolean> = _isConnectingToHost

    // Page 1: Join Host
    val joinHostModel = JoinHostScreenModel(menuViewModel, this)

    // Page 2: Team selection
    val selectTeamModel: SelectP2PTeamScreenModel = SelectP2PTeamScreenModel(
        menuViewModel = menuViewModel,
        getCoach = { joinHostModel.getCoach()!! },
    ) { teamSelected ->
        selectedTeam.value = teamSelected
        canCreateGame.value = (teamSelected != null)
    }

    // Page 3: Accept game and load resources
    val acceptGameModel = StartP2PGameScreenModel(networkAdapter, menuViewModel)

    private var gameViewModel: GameScreenModel? = null
    private var serverStoppedHandled = false

    /** Sidebar states captured when "Start Game" locked the steps behind it, see [lockCompletedSteps]. */
    private var stateBeforeLockingSteps: List<SidebarEntryState>? = null

    val validGameSetup = MutableStateFlow(true)
    val validTeamSelection = MutableStateFlow(false)
    val validWaitingForOpponent = MutableStateFlow(false)

    val availableTeams = MutableStateFlow<List<TeamInfo>>(emptyList())
    val selectedTeam = MutableStateFlow<TeamInfo?>(null)
    val gameName = MutableStateFlow("Game-${Random.nextInt(10_000)}")
    val port = MutableStateFlow<Int?>(8080)
    val canCreateGame = MutableStateFlow<Boolean>(false)
    val loadingTeams: MutableStateFlow<Boolean> = MutableStateFlow(true)

    init {
        sidebarEntries.addAll(workflow.getStartingSidebarEntries())
        menuViewModel.backgroundContext.launch {
            networkAdapter.clientState.collect { newState ->
                workflow.handleClientStateChange(newState)
            }
        }
        // Listen to changes in the Host's selected team. It can arrive (or
        // change) at any point before the game starts: it comes in the initial
        // game sync, again as a `TeamJoinedMessage`, and once more if the Host
        // goes back and picks a different team.
        menuViewModel.backgroundContext.launch {
            networkAdapter.homeTeam.collect { hostTeam ->
                val hostTeamId = hostTeam?.model?.id
                selectTeamModel.markTeamUnavailable(hostTeamId)
                // The Client may already have picked the team the Host just claimed. Drop it, so
                // they cannot continue with a game where the same team plays both sides (which
                // the engine will refuse).
                if (hostTeamId != null && selectedTeam.value?.teamId == hostTeamId) {
                    resetSelectedTeam()
                }
            }
        }
        menuViewModel.backgroundContext.launch {
            networkAdapter.serverErrors.collect { error ->
                when (error) {
                    is InvalidTeamServerError -> {
                        // `teamSelectionDone()` moves to the next page before the server has
                        // accepted the team, so on a rejection we have to walk that back and let
                        // the coach pick again. This normally means the Host claimed the team
                        // while we were choosing it.
                        resetSelectedTeam()
                        goBackToPage(1)
                        menuViewModel.showErrorDialog(
                            title = "Team not available",
                            message = error.message,
                        )
                    }
                    else -> {
                        // TOOD Should we also offer a "Report Issue" button here?
                        menuViewModel.showErrorDialog(
                            title = "The host rejected the request",
                            message = "[${error.errorCode.code}] ${error.message}",
                        )
                    }
                }
            }
        }
        menuViewModel.backgroundContext.launch {
            networkAdapter.connectionState.collect {
                when (it) {
                    Connected -> { /* Do nothing */ }
                    Connecting ->  { /* Do nothing */ }
                    is Disconnected -> {
                        // Server was closed by the Host
                        if (it.reason.code == JervisExitCode.SERVER_CLOSING.code && gameViewModel != null && !serverStoppedHandled) {
                            serverStoppedHandled = true
                            gameViewModel?.markTeamDisconnected(TeamActionMode.HOME_TEAM)
                            menuViewModel.showErrorDialog(
                                title = "Server stopped",
                                message = "The Server has stopped and the game will exit.",
                                onDismiss = {
                                    menuViewModel.hideErrorDialog()
                                    gameViewModel?.onDispose()
                                    menuViewModel.navigatorContext.launch {
                                        navigator.pop()
                                        navigator.pop()
                                    }
                                },
                                dismissButtonText = "Ok",
                                secondaryAction = { menuViewModel.showSaveGameDialog() },
                                secondaryButtonText = "Save Game"
                            )
                        } else if (it.reason.code == JervisExitCode.SERVER_CLOSING.code || it.reason.code == JervisExitCode.GAME_NOT_ACCEPTED.code) {
                            resetSelectedTeam()
                            workflow.handleClientStateChange(P2PClientState.JOIN_SERVER)
                        }
                    }
                }
            }
        }
    }

    private suspend fun getTeamInfo(teamFile: JervisTeamFile, team: Team): TeamInfo {
        val logoSize = LogoSize.SMALL
        val teamLogo = ICON_FACTORY.loadRosterIcon(
            team.id,
            teamFile.team.teamLogo ?: teamFile.roster.logo,
            logoSize
        )
        return TeamInfo(
            teamId = team.id,
            teamName = team.name,
            version = team.version,
            type = team.type,
            teamRoster = team.roster.name,
            teamValue = team.teamValue,
            rerolls = team.rerolls.size,
            logo = teamLogo,
            teamData = ModelRef(team.id, team)
        )
    }

    fun setSelectedTeam(team: TeamInfo?) {
        if (team == null || selectedTeam.value == team) {
            selectedTeam.value = null
            canCreateGame.value = false
        } else {
            selectedTeam.value = team
            canCreateGame.value = true
        }
    }

    fun setTeam(team: TeamInfo?) {
        if (team == null) {
            selectedTeam.value = null
            canCreateGame.value = false
        } else {
            selectedTeam.value = team
            canCreateGame.value = true
        }
    }

    fun hostJoinStarted() {
        _isConnectingToHost.value = true
        gotoNextPage(1)
    }

    fun hostJoinFailed(message: String) {
        joinHostModel.reset(message)
        if (!_isConnectingToHost.value) return
        _isConnectingToHost.value = false
        networkAdapter.cancelJoin()
        goBackToPage(0)
    }

    fun teamSelectionDone() {
        val team = selectedTeam.value ?: error("Team is not selected")
        // Should anything be saved here
        gotoNextPage(2)
        screenModelScope.launch {
            networkAdapter.teamSelected(team)
        }
    }

    // Called when user either Accept or Declines the game
    fun userAcceptGame(gameAccepted: Boolean) {
        menuViewModel.navigatorContext.launch {
            if (gameAccepted) {
                networkAdapter.gameAccepted(true)
                workflow.handleClientStateChange(P2PClientState.ACCEPTED_GAME)
            } else {
                networkAdapter.gameAccepted(false)
                resetSelectedTeam()
                workflow.handleClientStateChange(P2PClientState.JOIN_SERVER)
            }
        }
    }

    private fun gotoNextPage(nextPage: Int) {
        val currentPage = currentPage.value
        if (currentPage == 0 && nextPage == 2) {
            sidebarEntries[currentPage] = sidebarEntries[currentPage].copy(state = SidebarEntryState.DONE_AVAILABLE)
            sidebarEntries[1] = sidebarEntries[1].copy(state = SidebarEntryState.DONE_NOT_AVAILABLE)
        } else {
            sidebarEntries[currentPage] = sidebarEntries[currentPage].copy(state = SidebarEntryState.DONE_AVAILABLE)
        }
        sidebarEntries[nextPage] = sidebarEntries[nextPage].copy(state = SidebarEntryState.ACTIVE)
        this.currentPage.value = nextPage
    }

    // Reaching the "Start Game" screen prevents going back using the sidebar menu. Lock the steps
    // behind us, so the only way out is using the "Reject" or "Accept" buttons, which will notify
    // the Host.
    private fun lockCompletedSteps() {
        if (stateBeforeLockingSteps != null) return
        stateBeforeLockingSteps = sidebarEntries.map { it.state }
        for (index in 0 until currentPage.value) {
            sidebarEntries[index] = sidebarEntries[index].copy(state = SidebarEntryState.DONE_NOT_AVAILABLE)
        }
    }

    // Put the side menu back the way it was before "Start Game" locked it. The states are restored
    // rather than recomputed, because a step can legitimately have been unavailable beforehand.
    private fun unlockCompletedSteps() {
        val previousStates = stateBeforeLockingSteps ?: return
        stateBeforeLockingSteps = null
        previousStates.forEachIndexed { index, state ->
            sidebarEntries[index] = sidebarEntries[index].copy(state = state)
        }
    }

    private fun goBackToPage(previousPage: Int) {
        unlockCompletedSteps()
        sidebarEntries[previousPage] = sidebarEntries[previousPage].copy(state = SidebarEntryState.ACTIVE)
        for (index in previousPage + 1..currentPage.value) {
            sidebarEntries[index] = sidebarEntries[index].copy(state = SidebarEntryState.NOT_READY)
        }
        currentPage.value = previousPage
    }

    private fun initializeGameModel() {
        val rules = networkAdapter.rules!!
        val homeTeam = networkAdapter.homeTeam.value!!.model
        homeTeam.coach = networkAdapter.homeCoach.value!!
        val awayTeam = networkAdapter.awayTeam.value!!.model
        awayTeam.coach = networkAdapter.awayCoach.value!!
        val game = Game(rules, homeTeam, awayTeam)
        val gameController = GameEngineController(game, networkAdapter.initialActions)

        val homeActionProvider = RemoteActionProvider(
            clientMode = TeamActionMode.AWAY_TEAM,
            controller = gameController,
        )

        val awayActionProvider = when (joinHostModel.coachSetupModel.coachType.value) {
            HUMAN -> ManualActionProvider(
                gameController,
                menuViewModel,
                TeamActionMode.AWAY_TEAM,
                GameSettings(gameRules = rules),
            )
            // For now, we only support the Random AI player, so create it directly
            COMPUTER -> RandomActionProvider(TeamActionMode.AWAY_TEAM, gameController).also { it.startActionProvider() }
        }

        val actionProvider = P2PActionProvider(
            gameController,
            GameSettings(gameRules = rules),
            homeActionProvider,
            awayActionProvider,
            networkAdapter
        )

        gameViewModel = GameScreenModel(
            uiClientType = UiGameClientType.P2P_CLIENT,
            uiMode = TeamActionMode.AWAY_TEAM,
            gameController = gameController,
            homeTeam = gameController.state.homeTeam,
            awayTeam = gameController.state.awayTeam,
            actionProvider = actionProvider,
            mode = Manual(TeamActionMode.AWAY_TEAM),
            menuViewModel = menuViewModel,
            onEngineInitialized = {
                menuViewModel.controller = gameController
                menuViewModel.navigatorContext.launch {
                    networkAdapter.sendGameStarted()
                }
            },
            onGameStopped = {
                menuViewModel.backgroundContext.launch {
                    networkAdapter.close()
                }
            }
        ).also {
            it.waitForOpponent()
        }
        // Pushed on top rather than replacing this screen, so the "game was rejected" paths have
        // somewhere to come back to. See the same call in `P2PHostScreenModel`.
        navigator.push(GameScreen(menuViewModel, gameViewModel!!))
    }

    private fun prepareTeamSelection() {
        selectTeamModel.initializeTeamList(networkAdapter.rules!!)
        // The `homeTeam` collector in `init` keeps this up to date while the page is open, but it
        // only fires on changes. Re-assert it here so opening the page always reflects the Host's
        // current team, whatever happened on the way in.
        selectTeamModel.markTeamUnavailable(networkAdapter.homeTeam.value?.model?.id)
    }

    // Called when either pressing "Join" or "Continue" from the "Join Host" screen.
    fun userJoinOrContinue() {
        if (networkAdapter.connectionState.value == Connected) {
            if (networkAdapter.clientState.value == P2PClientState.ACCEPT_GAME ||
                networkAdapter.clientState.value == P2PClientState.RUN_GAME
            ) {
                workflow.handleClientStateChange(P2PClientState.ACCEPT_GAME)
            } else if (selectedTeam.value != null) {
                workflow.handleClientStateChange(P2PClientState.ACCEPT_GAME)
            } else if (networkAdapter.homeTeam.value != null) {
                workflow.handleClientStateChange(P2PClientState.SELECT_TEAM)
            } else {
                // Otherwise we are connected to a server the Host has not joined yet, which is
                // possible (but unlikely) because the game exists from the moment the server starts
                // listening. Moving on now would show a team list where the Host's team is not
                // marked as taken, so stay put. The server asks us to pick once the Host and its
                // team are there.
            }
        } else {
            joinHostModel.clientJoinGame()
        }
    }

    private fun resetSelectedTeam() {
        selectedTeam.value = null
        canCreateGame.value = false
        selectTeamModel.reset()
    }

    override fun onDispose() {
        menuViewModel.backgroundContext.launch {
            if (networkAdapter.clientState.value != P2PClientState.RUN_GAME) {
                networkAdapter.close()
            }
        }
    }

    private inner class Workflow() {
        // Must be single-threaded to serialize state updates
        private val stateChangeScope = CoroutineScope(singleThreadDispatcher("P2PClientScreenThread"))

        // A single-threaded dispatcher only serializes the steps of a transition, not the whole
        // transition. `changeState` suspends (starting a server, joining it, loading a game), and
        // while it is suspended the thread happily runs the next queued transition, which then
        // reads a `currentState` that has not been updated yet and repeats work the first one is
        // still doing. This mutex makes a transition atomic with respect to the others.
        private val stateChangeMutex = Mutex()
        private var currentState = P2PClientState.START
        fun handleClientStateChange(newState: P2PClientState) {
            stateChangeScope.launch {
                // A failed state change must never take down the app. `stateChangeScope` has no
                // parent to report to, so anything escaping here would surface as an uncaught
                // coroutine exception. `initializeGameModel()` in particular can fail on state
                // the Host sent us, e.g. two teams that share player ids.
                try {
                    stateChangeMutex.withLock {
                        changeState(newState)
                    }
                } catch (ex: CancellationException) {
                    throw ex
                } catch (ex: Throwable) {
                    LOG.e { "[P2PClientScreen] Failed state change: $currentState -> $newState\n${ex.stackTraceToString()}" }
                    when (_isConnectingToHost.value) {
                        true -> hostJoinFailed("Failed to join host: ${ex.message ?: ex::class.simpleName}")
                        false -> menuViewModel.showErrorDialog(
                            title = "Could not start the game",
                            message = ex.message,
                            error = ex,
                        )
                    }
                }
            }
        }

        private suspend fun changeState(newState: P2PClientState) {
            LOG.d { "[P2PClientScreen] state change: $currentState -> $newState" }
            if (newState == currentState) return
            when (currentState) {
                P2PClientState.START -> checkState(newState, P2PClientState.JOIN_SERVER)
                P2PClientState.JOIN_SERVER -> {
                    when (newState) {
                        P2PClientState.SELECT_TEAM -> {
                            prepareTeamSelection()
                            _isConnectingToHost.value = false
                            if (currentPage.value != 1) {
                                gotoNextPage(1)
                            }
                        }
                        P2PClientState.ACCEPT_GAME -> {
                            _isConnectingToHost.value = false
                            gotoNextPage(2)
                            lockCompletedSteps()
                        }
                        else -> unsupportedStateChange(newState)
                    }
                }
                P2PClientState.SELECT_TEAM -> {
                    when (newState) {
                        P2PClientState.JOIN_SERVER -> {
                            // Either initiated by Client or Host killed the server
                            // resetTeamAndNetworkIfNeeded()
                            goBackToPage(0)
                        }
                        P2PClientState.ACCEPT_GAME -> {
                            gotoNextPage(2)
                            lockCompletedSteps()
                        }
                        else -> unsupportedStateChange(newState)
                    }
                }
                P2PClientState.ACCEPT_GAME -> {
                    when (newState) {
                        P2PClientState.JOIN_SERVER -> {
                            // Either Client Or Host rejected the game,
                            // or Host killed the server.
                            // resetTeamAndNetworkIfNeeded()
                            goBackToPage(0)
                        }
                        P2PClientState.ACCEPTED_GAME -> {
                            // Called from `userAcceptGame()`
                            // networkAdapter.gameAccepted(true)
                            initializeGameModel()
                        }
                        else -> unsupportedStateChange(newState)
                    }
                }
                P2PClientState.ACCEPTED_GAME -> {
                    when (newState) {
                        P2PClientState.JOIN_SERVER -> {
                            // Either Client Or Host rejected the game,
                            // or Host killed the server.
                            navigator.pop()
                            goBackToPage(0)
                        }
                        P2PClientState.RUN_GAME -> {
                            // Should trigger next step on the loading screen
                            gameViewModel!!.gameAcceptedByAllPlayers()
                        }
                        else -> unsupportedStateChange(newState)
                    }
                }
                P2PClientState.RUN_GAME -> {
                    when (newState) {
                        P2PClientState.JOIN_SERVER -> {
                            // erver was killed while the game is running
                            // TODO Figure out how to handle this. Probably show disconnect
                            //  info in the Game UI.
                        }
                        else -> unsupportedStateChange(newState)
                    }
                }
                P2PClientState.CLOSE_GAME -> TODO()
                P2PClientState.DONE -> TODO()
            }
            currentState = newState
        }

        fun getStartingSidebarEntries(): List<SidebarEntry> {
            return listOf(
                SidebarEntry(
                    name = "1. Join Host",
                    state = SidebarEntryState.ACTIVE,
                    onClick = { workflow.handleClientStateChange(P2PClientState.JOIN_SERVER) },
                ),
                SidebarEntry(
                    name = "2. Select Team",
                    onClick = { workflow.handleClientStateChange(P2PClientState.SELECT_TEAM) },
                ),
                SidebarEntry(
                    name = "3. Start Game",
                    onClick = { workflow.handleClientStateChange(P2PClientState.ACCEPT_GAME) },
                )
            )
        }

        private fun checkState(newState: P2PClientState, expectedState: P2PClientState) {
            if (newState != expectedState) {
                error("Unsupported state change: $currentState -> $newState")
            }
        }

        private fun unsupportedStateChange(newState: P2PClientState) {
            error("Unsupported state change (from file): $currentState -> $newState")
        }
    }
}
