package com.jervisffb.ui.menu.p2p

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreenModel
import com.jervisffb.ui.menu.components.TeamInfo
import com.jervisffb.ui.menu.components.teamselector.SelectTeamComponentModel

/**
 * ViewModel class for the Team Selector subscreen. This is not a full screen,
 * but is a part of a flow when starting either Peer-to-Peer, Hotseat or AI
 * games.
 *
 * Right now, this model is just passing everything through to the [SelectTeamComponentModel].
 * This is mostly to future-proof it in case this screen will have additional responsibilities in
 * the future, and it also mirrors what e.g. [com.jervisffb.ui.menu.p2p.host.SetupGameScreenModel]
 * does.
 */
class SelectP2PTeamScreenModel(
    private val menuViewModel: MenuViewModel,
    private val getCoach: () -> Coach,
    private val onTeamSelected: (TeamInfo?) -> Unit,
    private val getRules: () -> Rules,
) : JervisScreenModel {
    val componentModel = SelectTeamComponentModel(menuViewModel, getCoach, onTeamSelected,  getRules)

    fun markTeamUnavailable(team: TeamId) {
        componentModel.makeTeamUnavailable(team)
    }
}
