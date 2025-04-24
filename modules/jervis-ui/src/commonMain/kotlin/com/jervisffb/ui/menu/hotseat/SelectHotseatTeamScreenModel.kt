package com.jervisffb.ui.menu.hotseat

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.TeamInfo
import com.jervisffb.ui.menu.components.coach.CoachSetupComponentModel
import com.jervisffb.ui.menu.components.teamselector.SelectTeamComponentModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * View model for controlling the "Select <Home/Away> Team" screen, that is the 2nd and 3rd step in the "Hotseat Game" flow.
 */
class SelectHotseatTeamScreenModel(
    private val menuViewModel: MenuViewModel,
    private val parentModel: HotseatScreenModel,
    private val onNextScreen: () -> Unit,
    private val onTeamImported: (TeamInfo) -> Unit = { _ -> /* Do nothing */ } ,
) : ScreenModel {
    val rules: Rules?
        get() = parentModel.rules
    val setupCoachModel = CoachSetupComponentModel(menuViewModel)
    val selectedTeam = MutableStateFlow<TeamInfo?>(null)
    val isValidTeamSelection: Flow<Boolean> = selectedTeam.combine(setupCoachModel.coachName) { selectedTeam, coachName ->
        selectedTeam != null && coachName.isNotBlank()
    }

    @OptIn(ExperimentalUuidApi::class)
    val teamSelectorModel = SelectTeamComponentModel(
        menuViewModel,
        {
            val coach = Coach(CoachId(Uuid.random().toHexString()), setupCoachModel.coachName.value, setupCoachModel.coachType.value)
            coach
        },
        { teamSelected ->
            selectedTeam.value = teamSelected
        },
        onTeamImported
    )

    fun initializeTeamSelector(rules: Rules) {
        teamSelectorModel.initialize(rules)
    }

    fun makeTeamUnavailable(team: TeamId) {
        teamSelectorModel.makeTeamUnavailable(team)
    }

    fun teamSelectionDone() {
        onNextScreen()
    }

    fun addNewTeam(teamInfo: TeamInfo) {
        teamSelectorModel.addNewTeam(teamInfo)
    }
}
