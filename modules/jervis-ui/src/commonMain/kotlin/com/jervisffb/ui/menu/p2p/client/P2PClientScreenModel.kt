package com.jervisffb.ui.menu.p2p.client

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.runner.SingleTeamNetworkGameRunner
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.GameScreen
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.Manual
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.p2p.P2PClientGameController
import com.jervisffb.ui.menu.p2p.StartP2PGameScreenModel
import com.jervisffb.ui.menu.p2p.TeamSelectorScreenModel
import com.jervisffb.ui.menu.p2p.host.TeamInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel class for the P2P Join screen. This view model is responsible
 * for controlling the entire flow of joining the host, selecting the team up
 * until running the actual game.
 */
class P2PClientScreenModel(private val navigator: Navigator, private val menuViewModel: MenuViewModel) : ScreenModel {

    // Central controller for the entire game lifecycle
    val controller = P2PClientGameController()

    // Which page are currently being shown
    val totalPages = 3
    val currentPage = MutableStateFlow(0) // 0-indexed
    var lastValidPage = 0

    // Page 1: Join Host
    val joinHostModel = JoinHostScreenModel(menuViewModel, this)

    // Page 2: Team selection
    val selectTeamModel: TeamSelectorScreenModel = TeamSelectorScreenModel(menuViewModel, { joinHostModel.getCoach()!! }) { teamSelected ->
        selectedTeam.value = teamSelected
        canCreateGame.value = (teamSelected != null)
    }

    // Page 3: Accept game and load resources
    val acceptGameModel = StartP2PGameScreenModel(controller, menuViewModel)

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
        loadTeamList()
        menuViewModel.navigatorContext.launch {
            controller.clientState.collect {
                // TODO We move state optimistically, so we probably need to check if things needs to be reset somehow.
                when (it) {
                    P2PClientState.START -> { /* Do nothing */ }
                    P2PClientState.JOIN_SERVER -> {
                        currentPage.value = 0
                    }
                    P2PClientState.SELECT_TEAM -> {
                        currentPage.value = 1
                    }
                    P2PClientState.ACCEPT_GAME -> {
                        currentPage.value = 2
                    }
                    P2PClientState.RUN_GAME -> {
                        val runner = SingleTeamNetworkGameRunner(
                            controller.awayTeam.value!!,
                            controller
                        ) { clientIndex, clientAction ->
                            println("User action: $clientIndex > ${controller.lastServerActionIndex}")
                            if (clientIndex > controller.lastServerActionIndex) {
                                menuViewModel.navigatorContext.launch {
                                    controller.sendActionToServer(clientIndex, clientAction)
                                }
                            }
                        }

                        val homeTeam = controller.homeTeam.value ?: error("Home team is not selected")
                        val homeActionProvider = ManualActionProvider(menuViewModel, TeamActionMode.HOME_TEAM)

                        val awayTeam = controller.awayTeam.value ?: error("Away team is not selected")
                        val awayActionProvider = ManualActionProvider(menuViewModel, TeamActionMode.HOME_TEAM)

                        val model = GameScreenModel(
                            homeTeam,
                            homeActionProvider,
                            awayTeam,
                            awayActionProvider,
                            mode = Manual(TeamActionMode.AWAY_TEAM),
                            menuViewModel = menuViewModel,
                            gameRunner = runner,
                            onEngineInitialized = {
                                menuViewModel.navigatorContext.launch {
                                    controller.sendGameStarted()
                                }
                            }
                        )
                        controller.runner = runner
                        navigator.push(GameScreen(model))
                        lastValidPage = 2
                    }
                    P2PClientState.CLOSE_GAME -> {}
                    P2PClientState.DONE -> {}
                }
            }
        }
    }

    private fun loadTeamList() {
        menuViewModel.navigatorContext.launch {
            CacheManager.loadTeams().map { teamFile ->
                val team = teamFile.team
                getTeamInfo(teamFile, team)
            }.let {
                availableTeams.value = it.sortedBy { it.teamName }
            }
        }
    }

    private suspend fun getTeamInfo(teamFile: JervisTeamFile, team: Team): TeamInfo {
        if (!IconFactory.hasLogo(team.id)) {
            IconFactory.saveLogo(team.id, teamFile.team.teamLogo ?: teamFile.roster.rosterLogo!!)
        }
        return TeamInfo(
            teamId = team.id,
            teamName = team.name,
            teamRoster = team.roster.name,
            teamValue = team.teamValue,
            rerolls = team.rerolls.size,
            logo = IconFactory.getLogo(team.id),
            teamData = team
        )
    }

    private fun getLocalIp(): String {
        return "127.0.0.1"
    }

    private fun getPublicIp(): String {
        TODO()
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

    fun hostJoinedDone() {
        // Move on from "Join Host" page
        lastValidPage = 1
        currentPage.value = 1
    }

    fun teamSelectionDone() {
        val team = selectedTeam.value ?: error("Team is not selected")
        // Should anything be saved here
        lastValidPage = 2
        currentPage.value = 2
        screenModelScope.launch {
            controller.teamSelected(team)
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
                selectedTeam.value = null
                canCreateGame.value = false
                joinHostModel.reset()
                selectTeamModel.reset()
                acceptGameModel.reset()
                lastValidPage = 0
                currentPage.value = 0
            }
        }
    }

    override fun onDispose() {
        screenModelScope.launch {
            controller.close()
        }
    }

}
