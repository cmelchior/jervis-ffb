package com.jervisffb.ui.menu.components.teamselector

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.engine.serialize.SerializedTeam
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
) : JervisScreenModel {

    var unavailableTeam = MutableStateFlow<TeamId?>(null)
    val availableTeams = MutableStateFlow<List<TeamInfo>>(emptyList())
    val selectedTeam = MutableStateFlow<TeamInfo?>(null)
    val loadingTeams: MutableStateFlow<Boolean> = MutableStateFlow(true)
    var rules: Rules? = null

    fun initialize(rules: Rules) {
        this.rules = rules
        loadTeamList(rules)
    }

    fun reset() {
        selectedTeam.value = null
    }

    private fun loadTeamList(rules: Rules) {
        menuViewModel.navigatorContext.launch {
            CacheManager.loadTeams().map { teamFile ->
                val teamData = teamFile.team
                val unknownCoach = Coach(CoachId("Unknown"), "TemporaryCoach")
                val team = SerializedTeam.deserialize(rules, teamData, unknownCoach)
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
        val teamId = teamId.toIntOrNull() ?: error("Do something here")
        menuViewModel.navigatorContext.launch {
            try {
                val rules = rules!!
                val teamFile = FumbblApi().loadTeam(teamId, rules)
                CacheManager.saveTeam(teamFile)
                val unknownCoach = Coach(CoachId("Unknown"), "TemporaryCoach")
                val team = SerializedTeam.deserialize(rules, teamFile.team, unknownCoach)
                val teamInfo = getTeamInfo(teamFile, team)
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
