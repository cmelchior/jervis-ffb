package manual.dummies

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.HotSeatGameRunner
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.test.createDefaultGameState
import com.jervisffb.ui.UiGameController
import com.jervisffb.ui.screen.GameScreenModel
import com.jervisffb.ui.screen.Manual
import com.jervisffb.ui.screen.TeamActionMode
import com.jervisffb.ui.viewmodel.FieldViewModel
import com.jervisffb.ui.viewmodel.MenuViewModel

object TestDummy {
    val menuViewModel = MenuViewModel()
    val gameModel = GameScreenModel(null, null,Manual(TeamActionMode.ALL_TEAMS), menuViewModel)
    val state = createDefaultGameState(StandardBB2020Rules())
    val controller = GameEngineController(state)
    val runner = HotSeatGameRunner(controller.rules, state.homeTeam, state.awayTeam)
    val uiController = UiGameController(Manual(TeamActionMode.ALL_TEAMS), runner, menuViewModel, emptyList())
    val fieldVieModel by lazy { FieldViewModel(uiController, gameModel.hoverPlayerFlow) }
    val leftSidebar by lazy {
        com.jervisffb.ui.viewmodel.SidebarViewModel(
            uiController,
            state.homeTeam,
            gameModel.hoverPlayerFlow
        )
    }
    val rightSidebar by lazy {
        com.jervisffb.ui.viewmodel.SidebarViewModel(
            uiController,
            state.awayTeam,
            gameModel.hoverPlayerFlow
        )
    }
}
