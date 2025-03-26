package com.jervisffb.ui.menu.hotseat

import androidx.compose.runtime.snapshots.SnapshotStateList
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialize.GameFileData
import com.jervisffb.ui.game.LocalActionProvider
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.RandomActionProvider
import com.jervisffb.ui.game.view.SidebarEntry
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.GameScreen
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.Manual
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.components.TeamInfo
import com.jervisffb.ui.menu.components.coach.CoachType
import com.jervisffb.ui.menu.components.setup.ConfigType
import com.jervisffb.ui.menu.components.starting.StartGameComponentModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel class for setting up and starting a Hotseat games. This view model is responsible
 * for controlling the entire flow of setting up and selecting players, up until
 * running the game.
 */
class HotseatScreenModel(private val navigator: Navigator, private val menuViewModel: MenuViewModel) : ScreenModel {

    val sidebarEntries: SnapshotStateList<SidebarEntry> = SnapshotStateList()

    // Which page are currently being shown
    val totalPages = 4
    val currentPage = MutableStateFlow(0)

    // Page 1: Setup Game
    val setupGameModel = SetupHotseatGameScreenModel(menuViewModel, this)
    var saveGameData: GameFileData? = null
    var rules: Rules? = null

    // Page 2: Select Home Team
    val selectHomeTeamModel: SelectHotseatTeamScreenModel = SelectHotseatTeamScreenModel(
        menuViewModel,
        this,
        {
            selectHomeTeamModel.selectedTeam.value?.teamId?.let { teamSelectedByOtherCoach ->
                selectAwayTeamModel.makeTeamUnavailable(teamSelectedByOtherCoach)
            }
            selectedHomeTeam.value = selectHomeTeamModel.selectedTeam.value
            homeTeamSelectionDone()
        }
    )
    val selectedHomeTeam = MutableStateFlow<TeamInfo?>(null)

    // Page 3: Select Away Team
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

    init {
        val startEntries = listOf(
            SidebarEntry(
                name = "1. Configure Game",
                enabled = true,
                active = true,
            ),
            SidebarEntry(name = "2. Home Team"),
            SidebarEntry(name = "3. Away Team"),
            SidebarEntry(name = "4. Start Game")
        )
        sidebarEntries.addAll(startEntries)
    }

    fun gameSetupDone() {
        menuViewModel.navigatorContext.launch {
            if (isLoadingGame()) {
                saveGameData = setupGameModel.gameConfigModel.loadFileModel.gameFile ?: error("Game file is not loaded")
                val homeTeam = saveGameData!!.homeTeam
                val awayTeam = saveGameData!!.awayTeam
                if (!IconFactory.hasLogo(homeTeam.id)) {
                    IconFactory.saveLogo(homeTeam.id, homeTeam.teamLogo ?: homeTeam.roster.rosterLogo!!)
                }
                if (!IconFactory.hasLogo(awayTeam.id)) {
                    IconFactory.saveLogo(awayTeam.id, awayTeam.teamLogo ?: awayTeam.roster.rosterLogo!!)
                }
                selectedHomeTeam.value = TeamInfo(
                    teamId = homeTeam.id,
                    teamName = homeTeam.name,
                    teamRoster = homeTeam.roster.name,
                    teamValue = homeTeam.teamValue,
                    rerolls = homeTeam.rerolls.size,
                    logo = IconFactory.getLogo(homeTeam.id),
                    teamData = homeTeam
                )
                selectedAwayTeam.value = TeamInfo(
                    teamId = awayTeam.id,
                    teamName = awayTeam.name,
                    teamRoster = awayTeam.roster.name,
                    teamValue = awayTeam.teamValue,
                    rerolls = awayTeam.rerolls.size,
                    logo = IconFactory.getLogo(awayTeam.id),
                    teamData = awayTeam
                )
                sidebarEntries[0] = sidebarEntries[0].copy(active = false, onClick = { goBackToPage(0) })
                sidebarEntries[1] = sidebarEntries[1].copy(active = false, enabled = false, onClick = null)
                sidebarEntries[2] = sidebarEntries[2].copy(active = false, enabled = false, onClick = null)
                sidebarEntries[3] = sidebarEntries[3].copy(active = true, enabled = true, onClick = { startGame() })
                currentPage.value = 3
            } else {
                rules = setupGameModel.createRules()
                sidebarEntries[0] = sidebarEntries[0].copy(active = false, onClick = { goBackToPage(0) })
                sidebarEntries[1] = sidebarEntries[1].copy(active = true, enabled = true, onClick = { homeTeamSelectionDone() })
                currentPage.value = 1
            }
        }
    }

    private fun isLoadingGame(): Boolean {
        val selectedGameTab = setupGameModel.gameConfigModel.selectedGameTab.value
        return setupGameModel.gameConfigModel.tabs[selectedGameTab].type == ConfigType.FROM_FILE
    }

    fun homeTeamSelectionDone() {
        sidebarEntries[1] = sidebarEntries[1].copy(enabled = true, active = false, onClick = { goBackToPage(1) })
        sidebarEntries[2] = sidebarEntries[2].copy(enabled = true, active = true, onClick = null)
        currentPage.value = 2
    }

    fun awayTeamSelectionDone() {
        sidebarEntries[2] = sidebarEntries[2].copy(enabled = true, active = false, onClick = { goBackToPage(2) })
        sidebarEntries[3] = sidebarEntries[3].copy(enabled = true, active = true, onClick = null)
        currentPage.value = 3
    }

    fun goBackToPage(previousPage: Int) {
        if (previousPage >= currentPage.value) {
            error("It is only allowed to go back: $previousPage")
        }
        sidebarEntries[previousPage] = sidebarEntries[previousPage].copy(active = true, onClick = null)
        for (index in previousPage + 1 .. currentPage.value) {
            sidebarEntries[index] = sidebarEntries[index].copy(active = false, enabled = false, onClick = null)
        }
        currentPage.value = previousPage
    }

    fun startGame() {
        // TODO If one of the teams are controlled by an AI, we should probably modify the UI and treat it as a remote client,
        // ie., not show UI controls for it.
        val homeTeam = selectedHomeTeam.value?.teamData ?: error("Home team is not selected")
        val awayTeam = selectedAwayTeam.value?.teamData ?: error("Away team is not selected")

        val rules = setupGameModel.createRules()
        homeTeam.coach = Coach(CoachId("1"), selectHomeTeamModel.setupCoachModel.coachName.value)
        awayTeam.coach = Coach(CoachId("2"), selectAwayTeamModel.setupCoachModel.coachName.value)
        val game = Game(rules, homeTeam, awayTeam, Field.Companion.createForRuleset(rules))
        val gameController = GameEngineController(game, saveGameData?.actions ?: emptyList())

        val homeActionProvider = when (selectHomeTeamModel.setupCoachModel.playerType.value) {
            CoachType.HUMAN -> ManualActionProvider(
                gameController,
                menuViewModel,
                TeamActionMode.HOME_TEAM,
                GameSettings(gameRules = rules, isHotseatGame = true),
            )
            // For now, we only support the Random AI player, so create it directly
            CoachType.COMPUTER -> RandomActionProvider(TeamActionMode.HOME_TEAM, gameController, true).also { it.startActionProvider() }
        }

        val awayActionProvider = when (selectAwayTeamModel.setupCoachModel.playerType.value) {
            CoachType.HUMAN -> ManualActionProvider(
                gameController,
                menuViewModel,
                TeamActionMode.AWAY_TEAM,
                GameSettings(gameRules = rules, isHotseatGame = true),
            )
            // For now, we only support the Random AI player, so create it directly
            CoachType.COMPUTER -> RandomActionProvider(TeamActionMode.AWAY_TEAM, gameController, true).also { it.startActionProvider() }
        }

        val actionProvider = LocalActionProvider(
            gameController,
            GameSettings(gameRules = rules),
            homeActionProvider,
            awayActionProvider
        )

        val model = GameScreenModel(
            TeamActionMode.ALL_TEAMS,
            gameController,
            gameController.state.homeTeam,
            gameController.state.awayTeam,
            actionProvider,
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
