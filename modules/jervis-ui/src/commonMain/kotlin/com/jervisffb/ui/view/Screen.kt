package com.jervisffb.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.viewmodel.DialogsViewModel
import com.jervisffb.ui.viewmodel.FieldViewModel
import com.jervisffb.ui.viewmodel.GameStatusViewModel
import com.jervisffb.ui.viewmodel.LogViewModel
import com.jervisffb.ui.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.viewmodel.ReplayControllerViewModel


@Composable
fun Screen(
    field: FieldViewModel,
    leftDugout: com.jervisffb.ui.viewmodel.SidebarViewModel,
    rightDugout: com.jervisffb.ui.viewmodel.SidebarViewModel,
    gameStatusController: GameStatusViewModel,
    replayActionsBar: ReplayControllerViewModel? = null,
    randomActionsBar: RandomActionsControllerViewModel? = null,
    unknownActions: ActionSelectorViewModel,
    logs: LogViewModel,
    dialogsViewModel: DialogsViewModel,
) {
    Dialogs(dialogsViewModel)
    val aspectRation = (145f+145f+782f)/690f
    Row(modifier = Modifier.aspectRatio(aspectRation).fillMaxSize()) {
        Column(modifier = Modifier.weight(145f).align(Alignment.Top)) {
            Sidebar(leftDugout, Modifier)
        }
        Column(modifier = Modifier.weight(782f).align(Alignment.Top)) {
            Field(field, Modifier.aspectRatio(field.aspectRatio))
            GameStatus(gameStatusController, modifier = Modifier.aspectRatio(782f/32f).fillMaxSize())
            // ReplayController(replayController, actionSelector, modifier = Modifier.height(48.dp))
            Row(modifier = Modifier.fillMaxSize()) {
                LogViewer(logs, modifier = Modifier.weight(1f).fillMaxSize())
                Divider(color = Color.LightGray, modifier = Modifier.fillMaxHeight().width(1.dp))
                Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                    if (replayActionsBar != null) {
                        ReplayCommandBar(replayActionsBar, modifier = Modifier)
                    }
                    if (randomActionsBar != null) {
                        RandomCommandBar(randomActionsBar, modifier = Modifier)
                    }
                    ActionSelector(unknownActions, modifier = Modifier.fillMaxSize())
                }

            }
        }
        Column(modifier = Modifier.weight(145f).align(Alignment.Top)) {
            Sidebar(rightDugout, Modifier)
        }
    }
}

