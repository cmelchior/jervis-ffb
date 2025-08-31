package com.jervisffb.ui.menu.components.teamselector

import co.touchlab.kermit.Logger.Companion.e
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.engine.serialize.SerializedTeam
import com.jervisffb.fumbbl.web.FumbblApi
import com.jervisffb.tourplay.TourPlayApi
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreenModel
import com.jervisffb.ui.menu.components.TeamInfo
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * View controller for the team selector component. This component is responsible for all the UI control needed
 * to select and import a team for a game.
 *
 * @see [SelectTeamComponent]
 */
class SelectTeamComponentModel(
    val menuViewModel: MenuViewModel,
    private val getCoach: () -> Coach,
    private val onTeamSelected: (TeamInfo?) -> Unit,
    private val onTeamImported: (TeamInfo) -> Unit = { _ -> /* Do nothing */ },
) : JervisScreenModel {

    companion object {
        val LOG = jervisLogger()
    }

    val fumbblApi = FumbblApi()
    val tourplayApi = TourPlayApi()

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
        menuViewModel.backgroundContext.launch {
            val teams =  CacheManager.loadTeams().mapNotNull { teamFile ->
                try {
                    val teamData = teamFile.team
                    val unknownCoach = Coach(CoachId("Unknown"), "TemporaryCoach")
                    val team = SerializedTeam.deserialize(rules, teamData, unknownCoach)
                    getTeamInfo(teamFile, team)
                } catch (ex: Exception) {
                    // How to handle teams not being able to load?
                    LOG.e("Failed to load team: ${ex.message}")
                    null
                }
            }
            teams
                .filter { it.type == rules.gameType }
                .let {
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
            type = team.type,
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
            selectedTeam.value = team.also {
                it.teamData?.coach = getCoach()
            }
            onTeamSelected(team)
        }
    }

    fun loadFumbblTeamFromNetwork(
        teamId: String,
        onSuccess: () -> Unit,
        onError: (String, Throwable?) -> Unit,
    ) {
        val teamId = teamId.toLongOrNull()
        if (teamId == null) {
            onError("Team ID does not look like a valid number", null)
            return
        }
        menuViewModel.backgroundContext.launch {
            try {
                val rules = rules!!
                val loadResult = fumbblApi.loadTeam(teamId, rules)
                if (loadResult.isFailure) {
                    onError("Could not load team", loadResult.exceptionOrNull())
                } else {
                    val teamFile = loadResult.getOrThrow()
                    val team = SerializedTeam.deserialize(rules, teamFile.team, Coach.UNKNOWN)
                    CacheManager.saveTeam(teamFile)
                    val teamInfo = getTeamInfo(teamFile, team)
                    addNewTeam(teamInfo)
                    onTeamImported(teamInfo)
                    onSuccess()
                }
            } catch (e: Exception) {
                LOG.w { "Failed to load team:\n${e.stackTraceToString()}" }
                val errorMessage = if (e.message?.isNotBlank() == true) {
                    "Could not load team - ${e.message}"
                } else {
                    "Could not load team due to an unknown error"
                }
                onError(errorMessage, e)
            }
        }
    }

    fun loadTourPlayTeamFromNetwork(
        teamId: String,
        onSuccess: () -> Unit,
        onError: (String, Throwable?) -> Unit,
    ) {
        val teamId = teamId.toLongOrNull()
        if (teamId == null) {
            onError("Team ID does not look like a valid number", null)
            return
        }
        menuViewModel.backgroundContext.launch {
            try {
                val rules = rules!!
                val rosterResult = tourplayApi.loadRoster(teamId, rules)
                if (rosterResult.isFailure) {
                    onError("Could not load roster", rosterResult.exceptionOrNull())
                } else {
                    val teamFile = rosterResult.getOrThrow()
                    val team = SerializedTeam.deserialize(rules, teamFile.team, Coach.UNKNOWN)
                    CacheManager.saveTeam(teamFile)
                    val teamInfo = getTeamInfo(teamFile, team)
                    addNewTeam(teamInfo)
                    onTeamImported(teamInfo)
                    onSuccess()
                }
            } catch (e: Exception) {
                LOG.w { "Failed to load team:\n${e.stackTraceToString()}" }
                val errorMessage = if (e.message != null) {
                    "Could not load team - ${e.message}"
                } else {
                    "Could not load team due to an unknown error"
                }
                onError(errorMessage, e)
            }
        }
    }

    fun makeTeamUnavailable(team: TeamId) {
        unavailableTeam.value = team
    }

    fun addNewTeam(teamInfo: TeamInfo) {
        availableTeams.value = (availableTeams.value.filter { it.teamId != teamInfo.teamId } + teamInfo).sortedBy { it.teamName }
    }
}
