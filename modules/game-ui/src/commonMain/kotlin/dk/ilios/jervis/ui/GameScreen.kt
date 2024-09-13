package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.controller.ActionsRequest
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fumbbl.adapter.FumbblReplayAdapter
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.ui.images.IconFactory
import dk.ilios.jervis.ui.viewmodel.ActionSelectorViewModel
import dk.ilios.jervis.ui.viewmodel.DialogsViewModel
import dk.ilios.jervis.ui.viewmodel.FieldViewModel
import dk.ilios.jervis.ui.viewmodel.GameStatusViewModel
import dk.ilios.jervis.ui.viewmodel.LogViewModel
import dk.ilios.jervis.ui.viewmodel.ManualModeUiActionFactory
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import dk.ilios.jervis.ui.viewmodel.RandomModeUiActionFactory
import dk.ilios.jervis.ui.viewmodel.ReplayModeUiActionFactory
import dk.ilios.jervis.ui.viewmodel.ReplayViewModel
import dk.ilios.jervis.ui.viewmodel.SidebarViewModel
import dk.ilios.jervis.utils.createDefaultGameState
import dk.ilios.jervis.utils.lizardMenAwayTeam
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
    val rules: BB2020Rules = BB2020Rules

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
                    fumbbl = FumbblReplayAdapter(mode.file, true)
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
            SidebarViewModel(uiActionFactory, screenModel.controller.state.homeTeam, screenModel.hoverPlayerFlow),
            SidebarViewModel(uiActionFactory, screenModel.controller.state.awayTeam, screenModel.hoverPlayerFlow),
            GameStatusViewModel(screenModel.controller),
            ReplayViewModel(uiActionFactory, screenModel),
            ActionSelectorViewModel(uiActionFactory),
            LogViewModel(screenModel.controller),
            DialogsViewModel(uiActionFactory),
        )
    }
}
