package com.jervisffb.ui.menu.p2p.client

import androidx.compose.runtime.snapshots.SnapshotStateList
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.messages.P2PClientState
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
import com.jervisffb.ui.menu.p2p.Connected
import com.jervisffb.ui.menu.p2p.Connecting
import com.jervisffb.ui.menu.p2p.Disconnected
import com.jervisffb.ui.menu.p2p.P2PClientNetworkAdapter
import com.jervisffb.ui.menu.p2p.SelectP2PTeamScreenModel
import com.jervisffb.ui.menu.p2p.StartP2PGameScreenModel
import com.jervisffb.ui.menu.p2p.host.P2PHostScreenModel.Companion.LOG
import com.jervisffb.utils.singleThreadDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel class for the P2P Join screen. This view model is responsible
 * for controlling the entire flow of joining the host, selecting the team up
 * until running the actual game.
 */
class P2PClientScreenModel(private val navigator: Navigator, private val menuViewModel: MenuViewModel) : ScreenModel {

    // Handles all state transitions
    private val workflow: Workflow = Workflow()

    val sidebarEntries: SnapshotStateList<SidebarEntry> = SnapshotStateList()

    // Adapter responsible for mapping network events to events that can be handled by the UI
    val networkAdapter = P2PClientNetworkAdapter()

    // Which page are currently being shown
    val totalPages = 3
    val currentPage = MutableStateFlow(0) // 0-indexed

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
        menuViewModel.backgroundContext.launch {
            networkAdapter.connectionState.collect {
                when (it) {
                    Connected -> { /* Do nothing */ }
                    Connecting ->  { /* Do nothing */ }
                    is Disconnected -> {
                        // Server was closed by the Host
                        // Error messages will be handled by the JoinHostScreenModel
                        if (it.reason.code == JervisExitCode.SERVER_CLOSING.code || it.reason.code == JervisExitCode.GAME_NOT_ACCEPTED.code) {
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
        val teamLogo = IconFactory.loadRosterIcon(
            team.id,
            teamFile.team.teamLogo ?: teamFile.roster.logo,
            logoSize
        )
        return TeamInfo(
            teamId = team.id,
            teamName = team.name,
            type = team.type,
            teamRoster = team.roster.name,
            teamValue = team.teamValue,
            rerolls = team.rerolls.size,
            logo = teamLogo,
            teamData = team
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

    // Called when the Client has succcesfully connected to the Host server.
    fun hostJoinedDone() {
        workflow.handleClientStateChange(P2PClientState.SELECT_TEAM)
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
            sidebarEntries[currentPage] = sidebarEntries[currentPage].copy(state = SideBarEntryState.DONE_AVAILABLE)
            sidebarEntries[1] = sidebarEntries[1].copy(state = SideBarEntryState.DONE_NOT_AVAILABLE)
        } else {
            sidebarEntries[currentPage] = sidebarEntries[currentPage].copy(state = SideBarEntryState.DONE_AVAILABLE)
        }
        sidebarEntries[nextPage] = sidebarEntries[nextPage].copy(state = SideBarEntryState.ACTIVE)
        this.currentPage.value = nextPage
    }

    private fun goBackToPage(previousPage: Int) {
        sidebarEntries[previousPage] = sidebarEntries[previousPage].copy(state = SideBarEntryState.ACTIVE)
        for (index in previousPage + 1..currentPage.value) {
            sidebarEntries[index] = sidebarEntries[index].copy(state = SideBarEntryState.NOT_READY)
        }
        currentPage.value = previousPage
    }

    private fun initializeGameModel() {
        val rules = networkAdapter.rules!!
        val homeTeam = networkAdapter.homeTeam.value!!
        homeTeam.coach = networkAdapter.homeCoach.value!!
        val awayTeam = networkAdapter.awayTeam.value!!
        awayTeam.coach = networkAdapter.awayCoach.value!!
        val game = Game(rules, homeTeam, awayTeam, Field.Companion.createForRuleset(rules))
        val gameController = GameEngineController(game, networkAdapter.initialActions)

        val homeActionProvider = RemoteActionProvider(
            clientMode = TeamActionMode.AWAY_TEAM,
            controller = gameController,
        )

        val awayActionProvider = when (joinHostModel.coachSetupModel.playerType.value) {
            CoachType.HUMAN -> ManualActionProvider(
                gameController,
                menuViewModel,
                TeamActionMode.AWAY_TEAM,
                GameSettings(gameRules = rules),
            )
            // For now, we only support the Random AI player, so create it directly
            CoachType.COMPUTER -> RandomActionProvider(TeamActionMode.AWAY_TEAM, gameController).also { it.startActionProvider() }
        }

        val actionProvider = P2PActionProvider(
            gameController,
            GameSettings(gameRules = rules),
            homeActionProvider,
            awayActionProvider,
            networkAdapter
        )

        gameViewModel = GameScreenModel(
            TeamActionMode.AWAY_TEAM,
            gameController,
            gameController.state.homeTeam,
            gameController.state.awayTeam,
            actionProvider,
            mode = Manual(TeamActionMode.AWAY_TEAM),
            menuViewModel = menuViewModel,
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

    private fun prepareTeamSelection() {
        selectTeamModel.initializeTeamList(networkAdapter.rules!!)
        networkAdapter.homeTeam.value?.id?.let { teamSelectedByOtherCoach ->
            selectTeamModel.markTeamUnavailable(teamSelectedByOtherCoach)
        }
    }

    // Called when either pressing "Join" or "Continue" from the "Join Host" screen.
    fun userJoinOrContinue() {
        if (networkAdapter.connectionState.value == Connected) {
            if (selectedTeam.value != null) {
                workflow.handleClientStateChange(P2PClientState.ACCEPT_GAME)
            } else {
                workflow.handleClientStateChange(P2PClientState.SELECT_TEAM)
            }
        } else {
            joinHostModel.clientJoinGame()
        }
    }

    private fun resetSelectedTeam() {
        selectedTeam.value = null
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
        private var currentState = P2PClientState.START
        fun handleClientStateChange(newState: P2PClientState) {
            stateChangeScope.launch {
                LOG.d { "[P2PClientScreen] state change: $currentState -> $newState" }
                if (newState == currentState) return@launch
                when (currentState) {
                    P2PClientState.START -> checkState(newState, P2PClientState.JOIN_SERVER)
                    P2PClientState.JOIN_SERVER -> {
                        when (newState) {
                            P2PClientState.SELECT_TEAM -> {
                                prepareTeamSelection()
                                gotoNextPage(1)
                            }
                            P2PClientState.ACCEPT_GAME -> {
                                gotoNextPage(2)
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
                                // Should
                                gotoNextPage(2)
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
        }

        fun getStartingSidebarEntries(): List<SidebarEntry> {
            return listOf(
                SidebarEntry(
                    name = "1. Join Host",
                    state = SideBarEntryState.ACTIVE,
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
