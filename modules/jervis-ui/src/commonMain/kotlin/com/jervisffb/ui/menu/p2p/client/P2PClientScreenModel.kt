package com.jervisffb.ui.menu.p2p.client

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.P2PActionProvider
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.GameScreen
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.Manual
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.components.TeamInfo
import com.jervisffb.ui.menu.p2p.P2PClientGameController
import com.jervisffb.ui.menu.p2p.SelectP2PTeamScreenModel
import com.jervisffb.ui.menu.p2p.StartP2PGameScreenModel
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
    val selectTeamModel: SelectP2PTeamScreenModel = SelectP2PTeamScreenModel(
        menuViewModel = menuViewModel,
        getCoach = { joinHostModel.getCoach()!! },
        onTeamSelected = { teamSelected ->
            selectedTeam.value = teamSelected
            canCreateGame.value = (teamSelected != null)
        },
        getRules = { controller.rules ?: error("Rules are not loaded yet") }
    )

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
                            TeamActionMode.AWAY_TEAM,
                            GameSettings(gameRules = rules),
                        )
                        val awayActionProvider = ManualActionProvider(
                            gameController,
                            menuViewModel,
                            TeamActionMode.AWAY_TEAM,
                            GameSettings(gameRules = rules),
                        )

                        val actionProvider = P2PActionProvider(
                            gameController,
                            GameSettings(gameRules = rules),
                            homeActionProvider,
                            awayActionProvider,
                            controller
                        )

                        val model = GameScreenModel(
                            gameController,
                            gameController.state.homeTeam,
                            gameController.state.awayTeam,
                            actionProvider,
                            mode = Manual(TeamActionMode.AWAY_TEAM),
                            menuViewModel = menuViewModel,
                        ) {
                            menuViewModel.controller = gameController
                            menuViewModel.navigatorContext.launch {
                                controller.sendGameStarted()
                            }
                        }
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
                selectTeamModel.componentModel.reset()
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
