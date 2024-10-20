package com.jervisffb.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.ActionsRequest
import com.jervisffb.engine.GameController
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.ui.images.IconFactory
import com.jervisffb.ui.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.viewmodel.DialogsViewModel
import com.jervisffb.ui.viewmodel.FieldViewModel
import com.jervisffb.ui.viewmodel.GameStatusViewModel
import com.jervisffb.ui.viewmodel.LogViewModel
import com.jervisffb.ui.userinput.ManualModeUiActionFactory
import com.jervisffb.ui.viewmodel.MenuViewModel
import com.jervisffb.ui.userinput.RandomModeUiActionFactory
import com.jervisffb.ui.userinput.ReplayModeUiActionFactory
import com.jervisffb.ui.viewmodel.ReplayViewModel
import com.jervisffb.engine.utils.createDefaultGameState
import com.jervisffb.engine.utils.lizardMenAwayTeam
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow

class GameScreenModel(
    val mode: GameMode,
    val menuViewModel: MenuViewModel,
    private val injectedController: GameController? = null,
) : ScreenModel {
    val actionRequestChannel =
        Channel<Pair<GameController, ActionsRequest>>(capacity = 2, onBufferOverflow = BufferOverflow.SUSPEND)
    val actionSelectedChannel =
        Channel<GameAction>(capacity = Channel.Factory.RENDEZVOUS, onBufferOverflow = BufferOverflow.SUSPEND)
    val hoverPlayerFlow =
        MutableSharedFlow<Player?>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    lateinit var controller: GameController
    var fumbbl: FumbblReplayAdapter? = null
    val rules: StandardBB2020Rules = StandardBB2020Rules

    suspend fun initialize() {
        if (injectedController != null) {
            this.controller = injectedController
            fumbbl = null
        } else {
            when (mode) {
                Manual -> {
                    fumbbl = null
                    this.controller = GameController(rules, createDefaultGameState(rules, awayTeam = lizardMenAwayTeam()))
                }

                Random -> {
                    fumbbl = null
                    this.controller = GameController(rules, createDefaultGameState(rules))
                }

                is Replay -> {
                    fumbbl = FumbblReplayAdapter(mode.file, checkCommandsWhenLoading = false)
                    fumbbl!!.loadCommands()
                    this.controller = GameController(rules, fumbbl!!.getGame())
                }
            }
        }
        menuViewModel.controller = this.controller
        IconFactory.initialize(controller.state.homeTeam, controller.state.awayTeam)
    }
}

class GameScreen(val screenModel: GameScreenModel, private val actions: List<GameAction>) : Screen {
    @Composable
    override fun Content() {
        val uiActionFactory =
            when (screenModel.mode) {
                Manual -> ManualModeUiActionFactory(screenModel, actions)
                Random -> RandomModeUiActionFactory(screenModel)
                is Replay -> ReplayModeUiActionFactory(screenModel)
            }
        screenModel.menuViewModel.uiActionFactory = uiActionFactory
        Screen(
            FieldViewModel(
                screenModel.controller,
                uiActionFactory,
                screenModel.controller.state.field,
                screenModel.hoverPlayerFlow,
            ),
            com.jervisffb.ui.viewmodel.SidebarViewModel(
                uiActionFactory,
                screenModel.controller.state.homeTeam,
                screenModel.hoverPlayerFlow
            ),
            com.jervisffb.ui.viewmodel.SidebarViewModel(
                uiActionFactory,
                screenModel.controller.state.awayTeam,
                screenModel.hoverPlayerFlow
            ),
            GameStatusViewModel(screenModel.controller),
            ReplayViewModel(uiActionFactory, screenModel),
            ActionSelectorViewModel(uiActionFactory),
            LogViewModel(screenModel.controller),
            DialogsViewModel(uiActionFactory),
        )
    }
}
