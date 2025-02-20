package com.jervisffb.ui.menu.p2p

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.fumbbl.web.FumbblApi
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreenModel
import com.jervisffb.ui.menu.p2p.host.TeamInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel class for the Team Selector subscreen. This is not a full screen,
 * but is a part of a flow when starting either Peer-to-Peer, Hotseat or AI
 * games.
 */
class TeamSelectorScreenModel(
    private val menuViewModel: MenuViewModel,
    private val getCoach: () -> Coach,
    private val onTeamSelected: (TeamInfo?) -> Unit
) : JervisScreenModel {

    val availableTeams = MutableStateFlow<List<TeamInfo>>(emptyList())
    val selectedTeam = MutableStateFlow<TeamInfo?>(null)
    val loadingTeams: MutableStateFlow<Boolean> = MutableStateFlow(true)

    init {
        loadTeamList()
    }

    fun reset() {
        selectedTeam.value = null
    }

    private fun loadTeamList() {
        menuViewModel.navigatorContext.launch {
            CacheManager.loadTeams().map { teamFile ->
                val team = teamFile.team
                team.coach = getCoach()
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

    fun setSelectedTeam(team: TeamInfo?) {
        if (team == null || selectedTeam.value == team) {
            selectedTeam.value = null
            onTeamSelected(null)
        } else {
            selectedTeam.value = team
            onTeamSelected(team)
        }
    }

    fun loadTeamFromNetwork(
        teamId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val team = teamId.toIntOrNull() ?: error("Do something here")
        menuViewModel.navigatorContext.launch {
            try {
                val teamFile = FumbblApi().loadTeam(team, StandardBB2020Rules())
                CacheManager.saveTeam(teamFile)
                val teamInfo = getTeamInfo(teamFile, teamFile.team)
                availableTeams.value = (availableTeams.value.filter { it.teamId != teamInfo.teamId } + teamInfo).sortedBy { it.teamName }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}
