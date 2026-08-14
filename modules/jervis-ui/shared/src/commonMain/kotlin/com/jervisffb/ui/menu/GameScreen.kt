package com.jervisffb.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervis.generated.SettingsKeys
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.state.ReplayController
import com.jervisffb.ui.game.view.GameScreen
import com.jervisffb.ui.game.view.LoadingScreen
import com.jervisffb.ui.game.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.game.viewmodel.ChallengeSessionViewModel
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import com.jervisffb.ui.game.viewmodel.GameStatusViewModel
import com.jervisffb.ui.game.viewmodel.LogViewModel
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.game.viewmodel.PitchDetails
import com.jervisffb.ui.game.viewmodel.PitchViewModel
import com.jervisffb.ui.game.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.game.viewmodel.ReplayControllerViewModel
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import com.jervisffb.ui.menu.challenges.ChallengeGameFactory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class GameDrawerActionButton(
    val label: String,
    val onClick: () -> Unit,
)

class GameScreen(val menuViewModel: MenuViewModel, val viewModel: GameScreenModel) : Screen {
    override val key: ScreenKey = "GameScreen"

    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        JervisScreen(menuViewModel) {
            LoadingScreen(viewModel) {

                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val drawerScope = rememberCoroutineScope()
                var showExitDialog by remember { mutableStateOf(false) }

                // Reset and exit live up here rather than with the rest of the
                // challenge UI, because the game menu drawer offers them too.
                val navigator = LocalNavigator.currentOrThrow
                val challengeMode = viewModel.mode as? ChallengeGame

                // Configure "action"-buttons displayed at the bottom of the game menu drawer.
                val (drawerActionButtons, challengeActionButtons) = remember(viewModel, challengeMode, navigator) {
                    when (challengeMode != null) {
                        true -> {
                            val onReset: () -> Unit = {
                                viewModel.onDispose()
                                ChallengeGameFactory.restart(navigator, menuViewModel, challengeMode.challenge)
                            }
                            val onExit: () -> Unit = {
                                viewModel.onDispose()
                                navigator.pop()
                            }
                            listOf(
                                GameDrawerActionButton("Reset Challenge", onReset),
                                GameDrawerActionButton("Exit Challenge", onExit)
                            ) to ChallengeMenuActions(onReset = onReset, onExit = onExit)
                        }
                        false -> {
                            listOf(
                                GameDrawerActionButton("Exit Game") {
                                    showExitDialog = true
                                },
                            ) to null
                        }
                    }
                }

                LifecycleEffectOnce {
                    val callback = object : OnBackPress {
                        override fun onBackPressed(): Boolean {
                            if (drawerState.isOpen) {
                                drawerScope.launch {
                                    drawerState.close()
                                }
                            } else {
                                drawerScope.launch {
                                    drawerState.open()
                                }
                            }
                            return true
                        }
                    }
                    BackNavigationHandler.register(callback)
                    onDispose {
                        BackNavigationHandler.unregister(callback)
                    }
                }

                ModalNavigationDrawer(
                    modifier = Modifier.fillMaxSize(),
                    drawerState = drawerState,
                    drawerContent = {
                        GameMenuDrawer(
                            drawerState = drawerState,
                            menuViewModel = menuViewModel,
                            gameScreenModel = viewModel,
                            showMenuDrawer = { visible ->
                                drawerScope.launch {
                                    drawerState.snapTo(if (visible) DrawerValue.Open else DrawerValue.Closed)
                                }
                            },
                            actionButtons = drawerActionButtons,
                        )
                    }
                ) {
                    // Screen content which the Navigation Drawer can move over
                    Box(
                        modifier = Modifier.fillMaxSize(), //.stoneBackground(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        val useWeatherBackground by SETTINGS_MANAGER.observeBooleanKey(SettingsKeys.JERVIS_UI_USE_PITCH_WEATHER_AS_GAME_BACKGROUND_VALUE, false).collectAsState(false)
                        val currentWeather by viewModel.pitchBackground.collectAsState(PitchDetails.NICE)
                        val backgroundImage = remember(useWeatherBackground, currentWeather) {
                            if (!useWeatherBackground) {
                                PitchDetails.NICE
                            } else {
                                currentWeather
                            }
                        }
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            bitmap = IconFactory.getPitch(backgroundImage),
                            contentDescription = "",
                            contentScale = ContentScale.FillBounds,
                        )
                        GameScreenContent(
                            menuViewModel,
                            viewModel,
                            onSettingsClick = {
                                drawerScope.launch {
                                    if (drawerState.isOpen) {
                                        drawerState.close()
                                    } else {
                                        drawerState.open()
                                    }
                                }
                            },
                            challengeMode = challengeMode,
                            challengeActions = challengeActionButtons,
                        )
                    }
                }

                if (showExitDialog) {
                    ExitGameDialogComponent(viewModel, { showExitDialog = false })
                }
            }
        }
    }
}

@Composable
private fun GameScreenContent(
    menuViewModel: MenuViewModel,
    viewModel: GameScreenModel,
    onSettingsClick: () -> Unit,
    challengeMode: ChallengeGame?,
    challengeActions: ChallengeMenuActions?,
) {
    val pitchViewModel = remember(viewModel) {
        PitchViewModel(
            viewModel,
            viewModel.uiState,
            viewModel.hoverPlayerFlow,
        )
    }

    // Only present while playing a challenge; drives the challenge panel and
    // the outcome box.
    val challengeSession = remember(viewModel, challengeMode) {
        challengeMode?.let {
            ChallengeSessionViewModel(viewModel, it.challenge)
        }
    }

    LaunchedEffect(pitchViewModel) {
        launch {
            pitchViewModel.actionWheelViewModel.start()
        }
        launch {
            pitchViewModel.contextActionWheelViewModel.start()
        }
    }

    if (challengeSession != null) {
        LaunchedEffect(challengeSession) {
            viewModel.uiState.uiStateFlow
                .distinctUntilChanged { old, new ->
                    old.delta?.id == new.delta?.id && old.delta?.reversed == new.delta?.reversed
                }
                .collect { snapshot ->
                    challengeSession.onSnapshot(snapshot.game, snapshot.delta!!)
                }
        }
    }

    GameScreen(
        viewModel,
        pitchViewModel,
        SidebarViewModel(
            viewModel,
            viewModel.menuViewModel,
            viewModel.uiState,
            viewModel.sharedPitchData,
            viewModel.homeTeam,
            viewModel.hoverPlayerFlow,
        ),
        SidebarViewModel(
            viewModel,
            viewModel.menuViewModel,
            viewModel.uiState,
            viewModel.sharedPitchData,
            viewModel.awayTeam,
            viewModel.hoverPlayerFlow,
        ),
        GameStatusViewModel(viewModel, viewModel.sharedPitchData, viewModel.uiState),
        if (viewModel.mode is Replay) ReplayControllerViewModel(viewModel.actionProvider as ReplayController) else null,
        if (viewModel.mode is Random) RandomActionsControllerViewModel(viewModel.uiState, viewModel) else null,
        ActionSelectorViewModel(viewModel.uiState),
        LogViewModel(viewModel.uiState),
        DialogsViewModel(viewModel, viewModel.uiState),
        onSettingsClick,
        challengeSession = challengeSession,
        onChallengeReset = challengeActions?.onReset ?: {},
        onChallengeExit = challengeActions?.onExit ?: {},
    )
}
