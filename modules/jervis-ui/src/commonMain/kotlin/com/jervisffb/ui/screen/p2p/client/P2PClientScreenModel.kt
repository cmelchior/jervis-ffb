package com.jervisffb.ui.screen.p2p.client

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.icons.IconFactory
import com.jervisffb.ui.screen.p2p.TeamSelectorScreenModel
import com.jervisffb.ui.screen.p2p.host.TeamInfo
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel class for the P2P Join screen. This view model is responsible
 * for controlling the entire flow of selecting the team and connecting to the
 * host, up until running the actual game.
 */
class P2PClientScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    // Which page are currently being shown
    val totalPages = 3
    val currentPage = MutableStateFlow(0)

    // Page 1: Team selection
    val selectTeamModel = TeamSelectorScreenModel(menuViewModel, { teamSelected ->
        canCreateGame.value = teamSelected
    })


    // Page 2: Join Host
    val joinHostModel = JoinHostScreenModel(menuViewModel)

    // Page 3: Accept game and load resources


    val validGameSetup = MutableStateFlow(true)
    val validTeamSelection = MutableStateFlow(false)
    val validWaitingForOpponent = MutableStateFlow(false)

    val availableTeams = MutableStateFlow<List<TeamInfo>>(emptyList())
    val selectedTeam = MutableStateFlow<TeamInfo?>(null)
    val gameName = MutableStateFlow("Game#${Random.nextInt(10_000)}")
    val port = MutableStateFlow<Int?>(8080)
    val canCreateGame = MutableStateFlow<Boolean>(false)
    val loadingTeams: MutableStateFlow<Boolean> = MutableStateFlow(true)

    init {
        loadTeamList()
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
            IconFactory.saveLogo(team.id, teamFile.uiData.teamLogo ?: teamFile.rosterUiData.rosterLogo!!)
        }
        return TeamInfo(
            teamId = team.id,
            teamName = team.name,
            teamRoster = team.roster.name,
            teamValue = team.teamValue,
            rerolls = team.rerolls.size,
            logo = IconFactory.getLogo(team.id),
        )
    }

    fun setPort(port: String) {
        val newPort = port.toIntOrNull()
        if (newPort == null) {
            this.port.value = null
            this.canCreateGame.value = false
        } else {
            this.port.value = newPort
            this.canCreateGame.value = newPort in 1..65535
        }
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

    fun gameSetupDone() {
        // Should anything be saved here?
        currentPage.value = 1
    }

    fun teamSelectionDone() {
        // Should anything be saved here
        currentPage.value = 1
    }

    fun goBackToPage(previousPage: Int) {
        if (previousPage >= currentPage.value) {
            error("It is only allowed to go back: $previousPage")
        }
        currentPage.value = previousPage
    }

    fun userAcceptGame(acceptGame: Boolean) {
        // TODO
    }

    fun clientRejectGame() {
        TODO()
    }

    fun clientAcceptGame() {
        TODO("Not yet implemented")
    }
}
