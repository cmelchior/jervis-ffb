package com.jervisffb.ui.game.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.utils.paperBackgroundWithLine
import com.jervisffb.ui.menu.SidebarEntry

enum class SidebarEntryState {
    NOT_READY, // Not available4
    DONE_NOT_AVAILABLE, // Is "done"
    DONE_AVAILABLE,
    ACTIVE,
}

data class SidebarEntry(
    val name: String,
    val state: SidebarEntryState = SidebarEntryState.NOT_READY,
    val alternativeBackground: Boolean = false,
    val onClick: (() -> Unit),
)

@Composable
fun SidebarMenu(
    entries: SnapshotStateList<SidebarEntry>,
) {
    Column(
        modifier = Modifier
            .paperBackgroundWithLine(JervisTheme.rulebookBlue)
            .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 8.dp)
        ,
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        entries.forEach { entry ->
            SidebarEntry(
                text = entry.name.uppercase(),
                state = entry.state,
                alternativeBackground = entry.alternativeBackground,
                onClick = entry.onClick,
            )
        }
        Spacer(modifier = Modifier.fillMaxHeight(0.20f))
    }
}
