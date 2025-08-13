package com.jervisffb.ui.game.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import com.jervisffb.ui.game.viewmodel.FieldViewData
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.game.viewmodel.GameStatusViewModel
import com.jervisffb.ui.game.viewmodel.LogViewModel
import com.jervisffb.ui.game.viewmodel.RandomActionsControllerViewModel
import com.jervisffb.ui.game.viewmodel.ReplayControllerViewModel
import com.jervisffb.ui.game.viewmodel.SidebarViewModel
import com.jervisffb.ui.menu.GameScreenModel

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
) {
    val aspectRation = (145f+145f+782f)/452f
    val fieldPositionData: FieldViewData by screenModel.fieldViewData.collectAsState() // remember { mutableStateOf(FieldViewData(IntSize.Zero, IntOffset.Zero)) }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        GameStatusV2(gameStatusController, modifier = Modifier)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp)
                .aspectRatio(aspectRation)
            ,
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(145f).align(Alignment.Top)) {
                SidebarV2(leftDugout, Modifier)
            }
            Column(
                modifier = Modifier.weight(782f).align(Alignment.Top),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Field(
                    modifier = Modifier.onGloballyPositioned {
                        field.updateFieldOffSet(it)
                    },
                    field
                )
                // ReplayController(replayController, actionSelector, modifier = Modifier.height(48.dp))
            }
            Column(modifier = Modifier.weight(145f).align(Alignment.Top)) {
                SidebarV2(rightDugout, Modifier)
            }
        }
        Row(modifier = Modifier.padding(start = 24.dp, end = 24.dp)) {
            LogViewer(logs, modifier = Modifier.weight(1f).background(Color.White.copy(alpha = 0.1f)).fillMaxSize())
            Spacer(modifier = Modifier.width(24.dp))
            // Divider(color = Color.LightGray, modifier = Modifier.fillMaxHeight().width(1.dp))
            Column(modifier = Modifier.weight(1f).background(Color.White.copy(alpha = 0.1f)).fillMaxSize()) {
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
    Dialogs(field, fieldPositionData, dialogsViewModel)
}

