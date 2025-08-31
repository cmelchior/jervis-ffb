package com.jervisffb.ui.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.GameScreen
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.LoadingScreen
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import com.jervisffb.ui.game.viewmodel.Feature
import com.jervisffb.ui.game.viewmodel.FieldDetails
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.game.viewmodel.GameStatusViewModel
import com.jervisffb.ui.game.viewmodel.LogViewModel
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.game.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.game.viewmodel.ReplayControllerViewModel
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import com.jervisffb.ui.menu.components.JervisDialogHeader
import com.jervisffb.ui.menu.components.SimpleSwitch
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
                                drawerScope.launch { drawerState.close() }
                            } else {
                                drawerScope.launch { drawerState.open() }
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
                        GameDrawerContent(
                            viewModel = viewModel,
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
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            bitmap = IconFactory.getField(FieldDetails.NICE),
                            contentDescription = "",
                            contentScale = ContentScale.FillBounds,
                        )
                        val navigator = LocalNavigator.currentOrThrow
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
private fun ColumnScope.AutomatedOptionsSection(vm: MenuViewModel) {
    var rerollSuccessfulActions by remember { mutableStateOf(vm.isFeatureEnabled(Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS)) }
    var selectKickingPlayer by remember { mutableStateOf(vm.isFeatureEnabled(Feature.SELECT_KICKING_PLAYER)) }
    var autoEndPlayerAction by remember { mutableStateOf(vm.isFeatureEnabled(Feature.END_PLAYER_ACTION_IF_ONLY_OPTION)) }
    var selectBlockType by remember { mutableStateOf(vm.isFeatureEnabled(Feature.SELECT_BLOCK_TYPE_IF_ONLY_OPTION)) }
    var pushPlayerIntoCrowd by remember { mutableStateOf(vm.isFeatureEnabled(Feature.PUSH_PLAYER_INTO_CROWD)) }

    Row(modifier = Modifier.background(Color.Transparent)) {
        SimpleSwitch(
            label = "Keep successful dice rolls",
            isSelected = rerollSuccessfulActions,
            onSelected = { selected ->
                rerollSuccessfulActions = selected
                vm.toggleFeature(Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS, selected)
            }
        )
    }
    Row(modifier = Modifier.background(JervisTheme.rulebookPaperMediumDark)) {
        SimpleSwitch(
            label = "Select kicking player",
            isSelected = selectKickingPlayer,
            onSelected = { selected ->
                selectKickingPlayer = selected
                vm.toggleFeature(Feature.SELECT_KICKING_PLAYER, selected)
            }
        )
    }
    Row(modifier = Modifier.background(Color.Transparent)) {
        SimpleSwitch(
            label = "End action automatically",
            isSelected = autoEndPlayerAction,
            onSelected = { selected ->
                autoEndPlayerAction = selected
                vm.toggleFeature(Feature.END_PLAYER_ACTION_IF_ONLY_OPTION, selected)
            }
        )
    }
    Row(modifier = Modifier.background(JervisTheme.rulebookPaperMediumDark)) {
        SimpleSwitch(
            label = "Select standard Block when no other variants",
            isSelected = selectBlockType,
            onSelected = { selected ->
                selectBlockType = selected
                vm.toggleFeature(Feature.SELECT_BLOCK_TYPE_IF_ONLY_OPTION, selected)
            }
        )
    }
    Row(modifier = Modifier.background(Color.Transparent)) {
        SimpleSwitch(
            label = "Push Player into the Crowd when no other push directions",
            isSelected = pushPlayerIntoCrowd,
            onSelected = { selected ->
                pushPlayerIntoCrowd = selected
                vm.toggleFeature(Feature.PUSH_PLAYER_INTO_CROWD, selected)
            }
        )
    }
}

@Composable
private fun GameDrawerContent(
    viewModel: GameScreenModel,
    menuViewModel: MenuViewModel,
    showExitDialog: (Boolean) -> Unit,
    showMenuDrawer: (Boolean) -> Unit
) {

    val uiState: UiGameSnapshot? by menuViewModel.uiState.uiStateFlow.collectAsState(null)

    Column(modifier = Modifier
        .fillMaxWidth(0.35f)
        .fillMaxHeight()
        .paperBackground()
        .drawWithCache {
            val strokeWidth = 8.dp.toPx()
            val x = size.width - strokeWidth / 2
            onDrawBehind {
                drawLine(
                    color = JervisTheme.rulebookRed,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = strokeWidth
                )
            }
        }
    ) {
        MenuTitleBar(
            modifier = Modifier.fillMaxWidth().height(116.dp),
            title = "Game Menu",
            fontSize = 32.dp,
            textPaddingLeft = 16.dp,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            DrawerSectionHeader("Game", topPadding = 0.dp)
            DrawerButton("Save Game", onClick = { menuViewModel.showSaveGameDialog(includeDebugState = false) })

            DrawerSectionHeader("Developer Tools")
            val currentNodeDescription: String = remember(uiState) {
                uiState?.stack?.let {
                    with(it) {
                        val procedure = it.peepOrNull() ?: return@let "null"
                        val currentNode = it.currentProcedure()?.currentNode() ?: return@let "${procedure.name()}[<null>]"
                        "${procedure.name()}[${currentNode.name()}]"
                    }
                } ?: "Unknown"
            }
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text(
                    text = "Current Node:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = JervisTheme.contentTextColor,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = currentNodeDescription,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = JervisTheme.contentTextColor,
                )
            }
            Row(modifier = Modifier.fillMaxWidth().background(JervisTheme.rulebookPaperMediumDark).padding(vertical = 8.dp)) {
                val actionOwner = remember(uiState) {
                    uiState?.actionOwner?.name ?: "Both"
                }
                Text(
                    text = "Action Owner:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = JervisTheme.contentTextColor,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = actionOwner,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = JervisTheme.contentTextColor,
                )
            }
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(
                    modifier = Modifier.padding(end = 4.dp),
                    text = "Last Action Error:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = JervisTheme.contentTextColor,
                )
                val error = remember(uiState) { menuViewModel.lastActionException }
                Text(
                    text = error?.message ?: "None",
                    fontSize = 14.sp,
                    fontStyle = if (error != null) FontStyle.Normal else FontStyle.Italic,
                    color = JervisTheme.contentTextColor,
                )
            }
            DrawerButton("Dump Game State to File", onClick = { menuViewModel.showSaveGameDialog(includeDebugState = true) })
            if (menuViewModel.creditData.newIssueUrl.isNotEmpty()) {
                DrawerButton(
                    text = "Report Issue",
                    onClick = {
                        menuViewModel.showReportIssueDialog(
                            title = "",
                            body = "",
                            gameState = viewModel.uiState.gameController
                        )
                    }
                )
            }
            DrawerSectionHeader("Automated Actions")
            AutomatedOptionsSection(menuViewModel)
        }
        Box(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)) {
            JervisButton(
                "Exit Game",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showMenuDrawer(false)
                    showExitDialog(true)
                }
            )
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
            viewModel.homeTeam,
            viewModel.hoverPlayerFlow
        ),
        SidebarViewModel(
            viewModel.menuViewModel,
            viewModel.uiState,
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




/**
 * Temporary composable for "normal" buttons in the NavigationDrawer. Using the normal blue hovering ones
 * seems a bit off.
 */
@Composable
private fun ColumnScope.DrawerButton(text: String, onClick: () -> Unit) {
    JervisButton(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        onClick = onClick,
        buttonColor = JervisTheme.rulebookRed,
        shape = RectangleShape,
    )
}

@Composable
private fun ColumnScope.DrawerSectionHeader(title: String, topPadding: Dp = 24.dp, bottomPadding: Dp = 8.dp) {
    Spacer(modifier = Modifier.height(topPadding))
    JervisDialogHeader(title, JervisTheme.rulebookRed)
    TitleBorder(JervisTheme.rulebookRed)
    Spacer(modifier = Modifier.height(bottomPadding))
}
