package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun App() {
    Navigator(IntroScreen())
}
//fun App(
//    controller: GameController,
//    actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
//    actionSelectedChannel: Channel<GameAction>,
//    fumbbl: FumbblReplayAdapter? = null
//) {
//    Screen(
//        FieldViewModel(controller.state.field),
//        SidebarViewModel(controller.state.homeTeam),
//        SidebarViewModel(controller.state.awayTeam),
//        GameStatusViewModel(controller),
//        ReplayViewModel(controller),
//        ActionSelectorViewModel(controller, actionRequestChannel, actionSelectedChannel, fumbbl),
//        LogViewModel(controller),
//    )
//}
