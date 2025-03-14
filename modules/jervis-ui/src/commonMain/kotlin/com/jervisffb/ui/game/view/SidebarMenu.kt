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

data class SidebarEntry(
    val name: String,
    val enabled: Boolean = false,
    val active: Boolean = false,
    val onClick: (() -> Unit)? = null,
)

@Composable
fun SidebarMenu(
    entries: SnapshotStateList<SidebarEntry>,
    currentPage: Int,
) {
    Column(
        modifier = Modifier.paperBackgroundWithLine(JervisTheme.rulebookBlue)
            .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 8.dp),
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.2f))
        Spacer(modifier = Modifier.height(16.dp))
        entries.forEachIndexed { index, entry ->
            val selected = (index == currentPage)
            val isPrevious = (index < currentPage)
            // val clickHandler: () -> Unit = if (isPrevious) ({ onClick(index) }) else ({ })
            SidebarEntry(
                text = entry.name.uppercase(),
                onClick = entry.onClick,
                selected = entry.active,
                enabled = entry.enabled // selected || isPrevious,
            )
        }
        Spacer(modifier = Modifier.fillMaxHeight(0.20f))
    }
}
