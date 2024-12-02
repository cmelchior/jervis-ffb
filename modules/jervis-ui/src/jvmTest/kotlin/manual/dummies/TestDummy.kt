package manual.dummies

import com.jervisffb.engine.GameController
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.utils.createDefaultGameState
import com.jervisffb.ui.UiGameController
import com.jervisffb.ui.screen.GameScreenModel
import com.jervisffb.ui.screen.Manual
import com.jervisffb.ui.viewmodel.FieldViewModel
import com.jervisffb.ui.viewmodel.MenuViewModel

object TestDummy {
    val menuViewModel = MenuViewModel()
    val gameModel = GameScreenModel(Manual, menuViewModel)
    val state = createDefaultGameState(StandardBB2020Rules)
    val controller = GameController(StandardBB2020Rules, state)
    val uiController = UiGameController(Manual, controller, menuViewModel, emptyList())
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
