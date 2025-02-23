package manual.dummies

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.test.createDefaultGameState
import com.jervisffb.ui.game.LocalActionProvider
import com.jervisffb.ui.game.UiGameController
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
    val controller = GameEngineController(state)
    val settings = GameSettings(timerSettings = TimerSettings(turnTimerEnabled = false), clientSelectedDiceRolls = true)
    val homeActionProvider = ManualActionProvider(controller, menuViewModel,TeamActionMode.HOME_TEAM, settings)
    val awayActionProvider = ManualActionProvider(controller, menuViewModel,  TeamActionMode.AWAY_TEAM, settings)

    val actionProvider = LocalActionProvider(
        controller,
        settings,
        homeActionProvider,
        awayActionProvider
    )

    val gameModel = GameScreenModel(
        controller,
        state.homeTeam,
        state.awayTeam,
        actionProvider,
        Manual(TeamActionMode.ALL_TEAMS),
        menuViewModel,
    )
    val uiController = UiGameController(controller, actionProvider, menuViewModel, emptyList())
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
