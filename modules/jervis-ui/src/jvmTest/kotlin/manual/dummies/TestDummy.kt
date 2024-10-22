package manual.dummies

import com.jervisffb.engine.GameController
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.ui.screen.GameScreenModel
import com.jervisffb.ui.screen.Manual
import com.jervisffb.ui.viewmodel.FieldViewModel
import com.jervisffb.ui.viewmodel.MenuViewModel
import com.jervisffb.ui.userinput.UiActionFactory
import com.jervisffb.engine.utils.createDefaultGameState
import kotlinx.coroutines.CoroutineScope

object TestDummy {
    val menuViewModel = MenuViewModel()
    val gameModel = GameScreenModel(Manual, menuViewModel)
    val uiActionFactory =
        object : UiActionFactory(gameModel) {
            init {
//                _fieldActions.tryEmit(WaitingForUserInput)
            }

            override suspend fun start(scope: CoroutineScope) { /* Do nothing */ }
        }
    val state = createDefaultGameState(StandardBB2020Rules)
    val controller = GameController(StandardBB2020Rules, state)
    val fieldVieModel by lazy { FieldViewModel(controller, uiActionFactory, state.field, gameModel.hoverPlayerFlow) }
    val leftSidebar by lazy {
        com.jervisffb.ui.viewmodel.SidebarViewModel(
            uiActionFactory,
            state.homeTeam,
            gameModel.hoverPlayerFlow
        )
    }
    val rightSidebar by lazy {
        com.jervisffb.ui.viewmodel.SidebarViewModel(
            uiActionFactory,
            state.awayTeam,
            gameModel.hoverPlayerFlow
        )
    }
}
