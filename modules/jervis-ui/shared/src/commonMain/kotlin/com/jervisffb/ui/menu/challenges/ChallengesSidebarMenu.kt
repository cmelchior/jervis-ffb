package com.jervisffb.ui.menu.challenges

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_icon_trophy_disabled
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.SidebarEntryState
import com.jervisffb.ui.game.view.utils.paperBackgroundWithLine
import com.jervisffb.ui.menu.challenges.data.ChallengeRow
import org.jetbrains.compose.resources.painterResource

private const val TROPHY_INLINE_CONTENT_ID = "challenge-sidebar-trophy"

data class ChallengeSidebarEntry(
    val name: String,
    val state: SidebarEntryState = SidebarEntryState.NOT_READY,
    val isSolved: Boolean,
    val alternativeBackground: Boolean,
    val challenge: ChallengeRow,
    val onClick: (() -> Unit),
)

@Composable
fun ChallengeSidebarMenu(
    modifier: Modifier = Modifier,
    entries: List<ChallengeSidebarEntry>,
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
            ChallengeSidebarButton(
                text = entry.name,
                isSolved = entry.isSolved,
                selected = (entry.state == SidebarEntryState.ACTIVE),
                onClick = entry.onClick
            )
        }
    }
}

@Composable
private fun ChallengeSidebarButton(
    text: String,
    isSolved: Boolean,
    selected: Boolean = false,
    onClick: () -> Unit = { }
) {
    val backgroundColor = if (selected) JervisTheme.white.copy(alpha = 0.3f) else JervisTheme.white.copy(alpha = 0.1f)
    val textColor = JervisTheme.white
    val label = buildAnnotatedString {

        if (isSolved) {
            appendInlineContent(TROPHY_INLINE_CONTENT_ID, "Trophy")
            append(" ")
        }
        append(text.uppercase())
    }
    val inlineContent = if (isSolved) {
        mapOf(
            TROPHY_INLINE_CONTENT_ID to InlineTextContent(
                placeholder = Placeholder(
                    width = 14.sp,
                    height = 12.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline,
                ),
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(Res.drawable.jervis_icon_trophy_disabled),
                    contentDescription = "Solved",
                    colorFilter = ColorFilter.tint(JervisTheme.white),
                    contentScale = ContentScale.Fit,
                )
            },
        )
    } else {
        emptyMap()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(start = 6.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = label,
            inlineContent = inlineContent,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = textColor
        )

    }
}
