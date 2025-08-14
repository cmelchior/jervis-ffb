package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.model.CoachType
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2020.procedures.GameDrive
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.ReplayActionProvider
import com.jervisffb.ui.menu.TeamActionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class GameProgress(
    val half: Int,
    val drive: Int,
    val turnMax: Int,
    val homeTeam: String,
    val homeTeamTurn: Int,
    val awayTeam: String,
    val awayTeamTurn: Int,
    val homeTeamScore: Int = 0,
    val awayTeamScore: Int = 0,
    val centerBadgeText: String = "",
    val centerBadgeAction: (() -> Unit)? = null,
    val badgeSubButtons: List<ButtonData> = emptyList(),
)

class GameStatusViewModel(val controller: UiGameController) {
    fun progress(): Flow<GameProgress> {
        return controller.uiStateFlow.map { uiSnapshot ->
            val game = uiSnapshot.game
            val rules = game.rules
            val actionButtons = createBadgeActions(uiSnapshot)
            GameProgress(
                game.halfNo,
                game.driveNo,
                if (game.halfNo > rules.halfsPrGame) rules.turnsInExtraTime else rules.turnsPrHalf,
                game.homeTeam.name,
                game.homeTeam.turnMarker,
                game.awayTeam.name,
                game.awayTeam.turnMarker,
                game.homeScore,
                game.awayScore,
                uiSnapshot.centerBadgeText,
                uiSnapshot.centerBadgeAction,
                actionButtons
            )
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

    private fun isTeamHumanAndControlledByClient(team: Team, mustBeHomeTeam: Boolean): Boolean {
        if (team.coach.type != CoachType.HUMAN) return false
        return when (mustBeHomeTeam) {
            true -> team.isHomeTeam()
            false -> !team.isHomeTeam()
        }
    }
}
