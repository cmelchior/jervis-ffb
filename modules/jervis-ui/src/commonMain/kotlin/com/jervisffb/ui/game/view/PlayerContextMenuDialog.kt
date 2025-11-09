package com.jervisffb.ui.game.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.ui.game.dialogs.DialogSize
import com.jervisffb.ui.game.model.UiPlayerCard
import com.jervisffb.ui.menu.GameScreenModel

/**
 * This file contains the "Player Context Menu". It is displayed when
 * right-clicking a player.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerContextMenuDialog(
    vm: GameScreenModel,
    player: UiPlayerCard
) {
    val borderColor = when (player.model.isOnHomeTeam()) {
        true -> JervisTheme.homeTeamColor
        false -> JervisTheme.awayTeamColor
    }
    BasicAlertDialog(
        modifier = Modifier
            .width(DialogSize.LARGE)
            .fillMaxHeight(0.7f)
            .border(6.dp, borderColor)
        ,
        onDismissRequest = { vm.hidePlayerContextMenu() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            PlayerEditorCard(player)
        }
    }
}
