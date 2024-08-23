package manual.dummies

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.ui.GameScreenModel
import dk.ilios.jervis.ui.Manual
import dk.ilios.jervis.ui.viewmodel.FieldViewModel
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import dk.ilios.jervis.ui.viewmodel.SidebarViewModel
import dk.ilios.jervis.ui.viewmodel.UiActionFactory
import dk.ilios.jervis.ui.viewmodel.WaitingForUserInput
import dk.ilios.jervis.utils.createDefaultGameState
import kotlinx.coroutines.CoroutineScope

object TestDummy {
    val menuViewModel = MenuViewModel()
    val gameModel = GameScreenModel(Manual, menuViewModel)
    val uiActionFactory =
        object : UiActionFactory(gameModel) {
            init {
                _fieldActions.tryEmit(WaitingForUserInput)
            }

            override suspend fun start(scope: CoroutineScope) { /* Do nothing */ }
        }
    val state = createDefaultGameState(BB2020Rules)
    val controller = GameController(BB2020Rules, state)
    val fieldVieModel by lazy { FieldViewModel(controller, uiActionFactory, state.field, gameModel.hoverPlayerFlow) }
    val leftSidebar by lazy { SidebarViewModel(uiActionFactory, state.homeTeam, gameModel.hoverPlayerFlow) }
    val rightSidebar by lazy { SidebarViewModel(uiActionFactory, state.awayTeam, gameModel.hoverPlayerFlow) }
}
