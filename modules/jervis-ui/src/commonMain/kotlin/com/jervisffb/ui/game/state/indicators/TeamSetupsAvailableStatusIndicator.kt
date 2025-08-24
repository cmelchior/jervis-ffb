package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.CoachType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2020.procedures.GameDrive
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ReplayActionProvider
import com.jervisffb.ui.game.viewmodel.ButtonData
import com.jervisffb.ui.game.viewmodel.Setups
import com.jervisffb.ui.menu.TeamActionMode

/**
 * Setup "Team Setup" buttons for quickly selecting a team setup.
 *
 * TODO This is something between an "action button" and an "indicator". Not 100%
 *  sure this is the best place to add support for them yet. But having them deep
 *  inside the ViewModels was definitely annoying.
 */
object TeamSetupsAvailableStatusIndicator: FieldStatusIndicator {
    override fun decorate(
        uiSnapshot: UiGameSnapshot,
        node: ActionNode,
        state: Game,
        request: ActionRequest
    ) {
        val actions = createBadgeActions(uiSnapshot)
        uiSnapshot.gameStatus.badgeSubButtons.addAll(actions)
    }

    private fun isTeamHumanAndControlledByClient(team: Team, mustBeHomeTeam: Boolean): Boolean {
        if (team.coach.type != CoachType.HUMAN) return false
        return when (mustBeHomeTeam) {
            true -> team.isHomeTeam()
            false -> !team.isHomeTeam()
        }
    }

    private fun createBadgeActions(uiSnapshot: UiGameSnapshot): List<ButtonData> {
        // TODO Find a better way to detect game mode
        if (uiSnapshot.uiController.actionProvider is ReplayActionProvider) return emptyList()
        val state = uiSnapshot.uiController.state
        val buttons = mutableListOf<ButtonData>()

        // TODO Can both teams add actions here? I don't think so
        buttons.addAll(uiSnapshot.homeTeamActions)
        buttons.addAll(uiSnapshot.awayTeamActions)

        // Check if this team is during the setup phase. For now, we just hard-code a few examples
        // This is mostly for WASM, iOS as JVM has a proper menu bar. This should be reworked
        // once we add proper menu support on WASM/iOS.
        // Also, consider moving this logic into decorators somehow.
        val setupKickingTeam = uiSnapshot.stack.containsNode(GameDrive.SetupKickingTeam)
        val setupReceivingTeam = uiSnapshot.stack.containsNode(GameDrive.SetupReceivingTeam)
        val teamControlledByClient = when (uiSnapshot.uiController.uiMode) {
            TeamActionMode.HOME_TEAM -> (setupKickingTeam && isTeamHumanAndControlledByClient(state.kickingTeam, true)) || (setupReceivingTeam && isTeamHumanAndControlledByClient(state.receivingTeam, true))
            TeamActionMode.AWAY_TEAM -> (setupKickingTeam && isTeamHumanAndControlledByClient(state.kickingTeam, false)) || (setupReceivingTeam && isTeamHumanAndControlledByClient(state.receivingTeam, false))
            TeamActionMode.ALL_TEAMS -> (setupKickingTeam || setupReceivingTeam)
        }
        if (teamControlledByClient) {
            val availableSetups = Setups.getSetups(state.rules.gameType)
            availableSetups.forEach { setup ->
                buttons.add(ButtonData(setup.name, onClick = { uiSnapshot.uiController.menuViewModel.loadSetup(setup)}))
            }
        }
        return buttons
    }
}
