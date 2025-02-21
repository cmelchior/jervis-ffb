package manual.dummies

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.test.createDefaultGameState
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.runner.HotSeatGameRunner
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.Manual
import com.jervisffb.ui.menu.TeamActionMode

object TestDummy {
    val menuViewModel = MenuViewModel()
    val state = createDefaultGameState(StandardBB2020Rules())
    val homeActionProvider = ManualActionProvider(menuViewModel, TeamActionMode.HOME_TEAM)
    val awayActionProvider = ManualActionProvider(menuViewModel, TeamActionMode.AWAY_TEAM)
    val controller = GameEngineController(state)
    val runner = HotSeatGameRunner(controller.rules, state.homeTeam, state.awayTeam)

    val gameModel = GameScreenModel(
        state.homeTeam,
        homeActionProvider,
        state.awayTeam,
        awayActionProvider,
        Manual(TeamActionMode.ALL_TEAMS),
        menuViewModel,
        runner
    )
    val uiController = UiGameController(Manual(TeamActionMode.ALL_TEAMS), runner, homeActionProvider, awayActionProvider, menuViewModel, emptyList())
    val fieldVieModel by lazy { FieldViewModel(uiController, gameModel.hoverPlayerFlow) }
    val leftSidebar by lazy {
        SidebarViewModel(
            uiController,
            state.homeTeam,
            gameModel.hoverPlayerFlow
        )
    }
    val rightSidebar by lazy {
        SidebarViewModel(
            uiController,
            state.awayTeam,
            gameModel.hoverPlayerFlow
        )
    }
}
