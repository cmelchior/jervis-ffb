package com.jervisffb.ui.menu.challenges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.SidebarEntryState
import com.jervisffb.ui.game.view.utils.paperBackgroundWithLine
import com.jervisffb.ui.menu.fumbbl.MenuSidebarButton

data class ChallengeSidebarEntry(
    val name: String,
    val state: SidebarEntryState = SidebarEntryState.NOT_READY,
    val alternativeBackground: Boolean = false,
    val challenge: ChallengeRow,
    val onClick: (() -> Unit),
)

@Composable
fun ChallengeSidebarMenu(
    modifier: Modifier = Modifier,
    entries: SnapshotStateList<ChallengeSidebarEntry>,
) {
    Column(
        modifier = modifier
            .paperBackgroundWithLine(JervisTheme.rulebookBlue)
            .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 32.dp)
            .verticalScroll(rememberScrollState())
        ,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        entries.forEach { entry ->
            MenuSidebarButton(
                text = entry.name,
                selected = (entry.state == SidebarEntryState.ACTIVE),
                onClick = entry.onClick
            )
        }
    }
}
