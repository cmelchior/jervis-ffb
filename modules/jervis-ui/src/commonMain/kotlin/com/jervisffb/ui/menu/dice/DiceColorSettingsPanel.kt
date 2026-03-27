package com.jervisffb.ui.menu.dice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.icons.DiceColor
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.D20Shape
import com.jervisffb.ui.game.view.utils.D6Shape
import com.jervisffb.ui.game.view.utils.D8Shape
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.utils.applyIf

/**
 * Second-level settings panel showing a dice color entry for every [DiceRollTypeConfig].
 * Entries with more than one available color are interactive, while single-color entries
 * are shown as read-only. Only one row can be expanded at a time; expanding a row auto-scrolls
 * it into view.
 */
@Composable
fun DiceColorSettingsPanel(config: DiceColorConfig) {
    var expandedRollType by remember { mutableStateOf<DiceRollType?>(null) }
    config.entries.forEachIndexed { index, entry ->
        DiceColorRow(
            config = config,
            entry = entry,
            rowIndex = index,
            isExpanded = expandedRollType == entry.rollType,
            onExpandToggle = {
                expandedRollType = if (expandedRollType == entry.rollType) null else entry.rollType
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DiceColorRow(
    config: DiceColorConfig,
    entry: DiceRollTypeConfig,
    rowIndex: Int,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
) {
    val description = ""
    val isConfigurable = (entry.availableColors.size > 1)
    val key = entry.settingsKey
    val storedValue by SETTINGS_MANAGER
        .observeStringKey(key, entry.defaultColor.name)
        .collectAsState(entry.defaultColor.name)
    val currentColor = remember(storedValue) { DiceColor.entries.find { it.name == storedValue } ?: entry.defaultColor }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(isExpanded) {
        if (isExpanded) bringIntoViewRequester.bringIntoView()
    }

    Column(
        modifier = Modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .defaultMinSize(minHeight = 48.dp)
            .applyIf(rowIndex % 2 == 1) {
                paperBackground(JervisTheme.rulebookPaperMediumDark.copy(alpha = 0.1f))
            }
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press) {
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.padding(end = 4.dp).weight(1f),
            ) {
                Text(
                    text = entry.label,
                    color = if (isConfigurable) JervisTheme.contentTextColor else JervisTheme.contentTextColor.copy(alpha = 0.6f),
                )
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = if (isConfigurable) JervisTheme.contentTextColor.copy(alpha = 0.7f) else JervisTheme.contentTextColor.copy(alpha = 0.35f),
                    )
                }
            }
            DiceIcon(
                entry = entry,
                color = currentColor,
                modifier = Modifier
                    .size(40.dp)
                    .alpha(if (isConfigurable) 1f else 0.5f)
                    .applyIf(isConfigurable) {
                        clickable { onExpandToggle() }
                    },
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                entry.availableColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .applyIf(color == currentColor) {
                                border(
                                    width = 2.dp,
                                    color = JervisTheme.rulebookRed,
                                    shape = when (entry.representativeDie) {
                                        is D2Result -> TODO("Shape not supported: ${entry.representativeDie}")
                                        is D3Result -> D6Shape
                                        is D4Result -> TODO("Shape not supported: ${entry.representativeDie}")
                                        is D6Result -> D6Shape
                                        is D8Result -> D8Shape
                                        is D12Result -> D20Shape
                                        is D16Result -> D20Shape
                                        is D20Result -> D20Shape
                                        is DBlockResult -> D6Shape
                                    }
                                )
                            }
                            .clickable {
                                SETTINGS_MANAGER[key] = color.name
                                onExpandToggle()
                            }
                    ) {
                        DiceIcon(entry = entry, color = color, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun DiceIcon(
    entry: DiceRollTypeConfig,
    color: DiceColor,
    modifier: Modifier = Modifier,
) {
    val bitmap = IconFactory.getDiceIcon(entry.representativeDie, color)
    Image(
        bitmap = bitmap,
        contentDescription = "${entry.label}: ${color.name}",
        modifier = modifier,
        contentScale = ContentScale.FillHeight,
        filterQuality = FilterQuality.High,
    )
}
