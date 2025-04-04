package com.jervisffb.ui.menu.components.teamselector

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.fumbbl.web.FumbblApi
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreenModel
import com.jervisffb.ui.menu.components.TeamInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * View controller for the team selector component. This component is responsible for all the UI control needed
 * to select and import a team for a game.
 *
 * @see [SelectTeamComponent]
 */
class SelectTeamComponentModel(
    private val menuViewModel: MenuViewModel,
    private val getCoach: () -> Coach,
    private val onTeamSelected: (TeamInfo?) -> Unit,
    private val getRules: () -> Rules,
) : JervisScreenModel {

    var unavailableTeam = MutableStateFlow<TeamId?>(null)
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
                // team.coach = Coach(CoachId("Unknown", "TemporaryCoach"))
                getTeamInfo(teamFile, team)
            }.let {
                availableTeams.value = it.sortedBy { it.teamName }
            }
        }
    }

    private suspend fun getTeamInfo(teamFile: JervisTeamFile, team: Team): TeamInfo {
        val logo = IconFactory.loadRosterIcon(
            team.id,
            teamFile.team.teamLogo ?: teamFile.roster.logo,
            LogoSize.SMALL,
        )
        return TeamInfo(
            teamId = team.id,
            teamName = team.name,
            teamRoster = team.roster.name,
            teamValue = team.teamValue,
            rerolls = team.rerolls.size,
            logo = logo,
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
                val teamFile = FumbblApi().loadTeam(team, getRules())
                CacheManager.saveTeam(teamFile)
                val teamInfo = getTeamInfo(teamFile, teamFile.team)
                availableTeams.value = (availableTeams.value.filter { it.teamId != teamInfo.teamId } + teamInfo).sortedBy { it.teamName }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun makeTeamUnavailable(team: TeamId) {
        unavailableTeam.value = team
    }
}
