package com.jervisffb.ui.game.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_settings
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_undo
import com.jervisffb.ui.game.view.field.Field
import com.jervisffb.ui.game.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import com.jervisffb.ui.game.viewmodel.FieldDetails
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.game.viewmodel.GameStatusViewModel
import com.jervisffb.ui.game.viewmodel.LogViewModel
import com.jervisffb.ui.game.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.game.viewmodel.ReplayControllerViewModel
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import com.jervisffb.ui.keybinds.ClientShortcut
import com.jervisffb.ui.keybinds.KeyBindings
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.TopbarButton
import com.jervisffb.ui.utils.createPlatformButtonLabel
import com.jervisffb.ui.utils.jdp

@Composable
fun GameScreen(
    screenModel: GameScreenModel,
    field: FieldViewModel,
    leftDugout: SidebarViewModel,
    rightDugout: SidebarViewModel,
    gameStatusController: GameStatusViewModel,
    replayActionsBar: ReplayControllerViewModel? = null,
    randomActionsBar: RandomActionsControllerViewModel? = null,
    unknownActions: ActionSelectorViewModel,
    logs: LogViewModel,
    dialogsViewModel: DialogsViewModel,
    onSettingsClick: () -> Unit,
) {
    //val aspectRation = (145f+145f+782f)/452f
    val aspectRation = (550f+550f+2354f)/1362f
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TopBarGameStatus(gameStatusController, modifier = Modifier.padding(horizontal = 24.jdp))
        Spacer(modifier = Modifier.height(8.jdp))
        Row(
            modifier = Modifier
                .aspectRatio(aspectRation)
                // Field should be above the sidebar and bottom log viewers, so the Action Wheel
                // is the first thing that intercepts touch events if it overlaps with these sections.
                .zIndex(1f)
            ,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(550f /*145f*/).align(Alignment.Top)) {
                Sidebar(leftDugout, Modifier)
            }
            Column(
                // Make sure that field layers (including the Action Wheel) are placed above the sidebar
                modifier = Modifier.zIndex(1f).weight(/*782f*/ 2354f).align(Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Field(
                    Modifier,
                    field,
                    borderBrushSize = 3.dp
                )
                // ReplayController(replayController, actionSelector, modifier = Modifier.height(48.dp))
            }
            Column(modifier = Modifier.weight(550f /*145f*/).align(Alignment.Top)) {
                Sidebar(rightDugout, Modifier)
            }
        }
        Spacer(modifier = Modifier.height(24.jdp))
        Row(modifier = Modifier.padding(horizontal = 24.jdp)) {
            val backgroundColor by screenModel.logsBackgroundColor.collectAsState(FieldDetails.NICE.logBackground)
            LogViewer(logs, modifier = Modifier.weight(1f).background(backgroundColor).fillMaxSize())
            Spacer(modifier = Modifier.width(24.dp))
            // Divider(color = Color.LightGray, modifier = Modifier.fillMaxHeight().width(1.dp))
            Column(modifier = Modifier.weight(1f).background(backgroundColor).fillMaxSize()) {
                if (replayActionsBar != null) {
                    ReplayCommandBar(replayActionsBar, modifier = Modifier)
                }
                if (randomActionsBar != null) {
                    RandomCommandBar(randomActionsBar, modifier = Modifier)
                }
                ActionSelector(unknownActions, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(
                modifier = Modifier.width(48.dp).fillMaxHeight().background(backgroundColor),
                verticalArrangement = Arrangement.Bottom,
            ) {
                TopbarButton(
                    icon = Res.drawable.jervis_icon_menu_undo,
                    contentDescription = KeyBindings.createPlatformButtonLabel(ClientShortcut.UNDO),
                    onClick = {
                        screenModel.menuViewModel.undoAction()
                    }
                )
                TopbarButton(
                    icon = Res.drawable.jervis_icon_menu_settings,
                    contentDescription = KeyBindings.createPlatformButtonLabel(ClientShortcut.GAME_MENU),
                    onClick = onSettingsClick
                )
            }
        }
    }
    Dialogs(dialogsViewModel)
}

