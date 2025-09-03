package com.jervisffb.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import com.jervis.generated.SettingsKeys
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.GameScreen
import com.jervisffb.ui.game.view.LoadingScreen
import com.jervisffb.ui.game.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import com.jervisffb.ui.game.viewmodel.FieldDetails
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.game.viewmodel.GameStatusViewModel
import com.jervisffb.ui.game.viewmodel.LogViewModel
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.game.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.game.viewmodel.ReplayControllerViewModel
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import kotlinx.coroutines.launch

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
                            showExitDialog = { visible ->
                                showExitDialog = visible
                            },
                            showMenuDrawer = { visible ->
                                drawerScope.launch {
                                    drawerState.snapTo(if (visible) DrawerValue.Open else DrawerValue.Closed)
                                }
                            }
                        )
                    }
                ) {
                    // Screen content which the Navigation Drawer can move over
                    Box(
                        modifier = Modifier.fillMaxSize(), //.stoneBackground(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        val useWeatherBackground by SETTINGS_MANAGER.observeBooleanKey(SettingsKeys.JERVIS_UI_USE_PITCH_WEATHER_AS_GAME_BACKGROUND_VALUE, false).collectAsState(false)
                        val currentWeather by viewModel.fieldBackground.collectAsState(FieldDetails.NICE)
                        val backgroundImage = remember(useWeatherBackground, currentWeather) {
                            if (!useWeatherBackground) {
                                FieldDetails.NICE
                            } else {
                                currentWeather
                            }
                        }
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            bitmap = IconFactory.getField(backgroundImage),
                            contentDescription = "",
                            contentScale = ContentScale.FillBounds,
                        )
                        GameScreenContent(viewModel, {
                            drawerScope.launch {
                                if (drawerState.isOpen) {
                                    drawerState.close()
                                } else {
                                    drawerState.open()
                                }
                            }
                        })
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
private fun GameScreenContent(viewModel: GameScreenModel, onSettingsClick: () -> Unit) {
    GameScreen(
        viewModel,
        FieldViewModel(
            viewModel,
            viewModel.uiState,
            viewModel.hoverPlayerFlow,
        ),
        SidebarViewModel(
            viewModel.menuViewModel,
            viewModel.uiState,
            viewModel.sharedFieldData,
            viewModel.homeTeam,
            viewModel.hoverPlayerFlow
        ),
        SidebarViewModel(
            viewModel.menuViewModel,
            viewModel.uiState,
            viewModel.sharedFieldData,
            viewModel.awayTeam,
            viewModel.hoverPlayerFlow
        ),
        GameStatusViewModel(viewModel.uiState),
        if (viewModel.mode is Replay) ReplayControllerViewModel(viewModel.uiState, viewModel) else null,
        if (viewModel.mode is Random) RandomActionsControllerViewModel(viewModel.uiState, viewModel) else null,
        ActionSelectorViewModel(viewModel.uiState),
        LogViewModel(viewModel.uiState),
        DialogsViewModel(viewModel, viewModel.uiState),
        onSettingsClick
    )
}
