package com.jervisffb.ui.menu.hotseat

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.RandomActionProvider
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.GameScreen
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.Manual
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.components.starting.StartGameComponentModel
import com.jervisffb.ui.menu.p2p.host.TeamInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel class for setting up and starting a Hotseat games. This view model is responsible
 * for controlling the entire flow of setting up and selecting players, up until
 * running the game.
 */
class HotseatScreenModel(private val navigator: Navigator, private val menuViewModel: MenuViewModel) : ScreenModel {

    // Which page are currently being shown
    val totalPages = 4
    val currentPage = MutableStateFlow(0)

    // Page 1: Setup Game
    val setupGameModel = SetupHotseatGameScreenModel(menuViewModel, this)

    // Page 2: Select Home Team
    val selectHomeTeamModel: SelectHotseatTeamScreenModel = SelectHotseatTeamScreenModel(
        menuViewModel,
        this,
        {
            selectedHomeTeam.value = selectHomeTeamModel.selectedTeam.value
            homeTeamSelectionDone()
        }
    )
    val selectedHomeTeam = MutableStateFlow<TeamInfo?>(null)

    // Page 3: Select Home Team
    val selectAwayTeamModel: SelectHotseatTeamScreenModel = SelectHotseatTeamScreenModel(
        menuViewModel,
        this,
        {
            selectedAwayTeam.value = selectAwayTeamModel.selectedTeam.value
            awayTeamSelectionDone()
        }
    )
    val selectedAwayTeam = MutableStateFlow<TeamInfo?>(null)

    // Page 4: Accept game
    val acceptGameModel = StartGameComponentModel(
        selectedHomeTeam.map { it?.teamData!! },
        selectedAwayTeam.map { it?.teamData!! },
        menuViewModel
    )

    private var gameScreenModel: GameScreenModel? = null

    fun gameSetupDone() {
        // Should anything be saved here?
        currentPage.value = 1
    }

    fun homeTeamSelectionDone() {
        currentPage.value = 2
    }

    fun awayTeamSelectionDone() {
        currentPage.value = 3
    }

    fun goBackToPage(previousPage: Int) {
        if (previousPage >= currentPage.value) {
            error("It is only allowed to go back: $previousPage")
        }
        currentPage.value = previousPage
    }

    fun startGame() {
        // TODO If one of the teams are controlled by an AI, we should probably modify the UI and treat it as a remote client,
        // ie., not show UI controls for it.
        val homeTeam = selectedHomeTeam.value?.teamData ?: error("Home team is not selected")
        val awayTeam = selectedAwayTeam.value?.teamData ?: error("Away team is not selected")

        val rules = StandardBB2020Rules()
        homeTeam.coach = Coach(CoachId("1"), selectHomeTeamModel.coachName.value)
        awayTeam.coach = Coach(CoachId("2"), selectAwayTeamModel.coachName.value)
        val game = Game(rules, homeTeam, awayTeam, Field.Companion.createForRuleset(rules))
        val gameController = GameEngineController(game)

        val homeActionProvider = when (selectHomeTeamModel.playerType.value) {
            PlayerType.HUMAN -> ManualActionProvider(
                gameController.state.homeTeam,
                gameController,
                menuViewModel,
                TeamActionMode.HOME_TEAM,
            )
            PlayerType.COMPUTER -> RandomActionProvider(homeTeam, gameController).also { it.startActionProvider() }
        }

        val awayActionProvider = when (selectAwayTeamModel.playerType.value) {
            PlayerType.HUMAN -> ManualActionProvider(
                gameController.state.awayTeam,
                gameController,
                menuViewModel,
                TeamActionMode.AWAY_TEAM,
            )
            PlayerType.COMPUTER -> RandomActionProvider(awayTeam, gameController).also { it.startActionProvider() }
        }


        val model = GameScreenModel(
            gameController,
            gameController.state.homeTeam,
            homeActionProvider,
            gameController.state.awayTeam,
            awayActionProvider,
            mode = Manual(TeamActionMode.ALL_TEAMS),
            menuViewModel = menuViewModel,
            onEngineInitialized = {
                menuViewModel.controller = gameController
                menuViewModel.navigatorContext.launch {
                    // TODO Send to AI controller?
                    // controller.sendGameStarted()
                }
            }
        )
        navigator.push(GameScreen(model))
    }
}
