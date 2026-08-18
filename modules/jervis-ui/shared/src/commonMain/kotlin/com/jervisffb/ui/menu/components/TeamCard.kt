package com.jervisffb.ui.menu.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.formatCurrency
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.menu.utils.JervisTooltip

/**
 * Component responsible for showing a "Team Card" when selecting teams for
 * a game. The Team Card contains the most important high-level information:
 *
 * - Team Name
 * - Team Logo
 * - CTV
 * - Rerolls
 * - Race
 */
@Composable
fun TeamCard(
    name: String,
    race: String,
    teamValue: Int,
    rerolls: Int,
    logo: ImageBitmap,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    emptyTeam: Boolean = false,
    highlight: String = "",
    highlightColor: Color = JervisTheme.brightYellow,
    onClick: (() -> Unit)?,
) {
    val borderWidth = if (isSelected || !isEnabled) 3.dp else 0.dp
    val borderColor = if (isSelected || !isEnabled) JervisTheme.rulebookRed else Color.Transparent
    Box(
        modifier = modifier
            .size(width = 300.dp, height = 150.dp)
            .alpha(if (isEnabled) 1f else 0.3f)
            .background(JervisTheme.rulebookPaperMediumDark.copy(alpha = 0.5f))
            .border(width = borderWidth, color = borderColor)
            .let { if (onClick != null && isEnabled) it.clickable(!emptyTeam, onClick = onClick) else it }
        ,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val color = JervisTheme.rulebookRed
                TitleBorder(color)
                Box(
                    modifier = Modifier.fillMaxWidth().background(color),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
                        text = name.highlightMatches(highlight, highlightColor = highlightColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = JervisTheme.white
                    )
                }
                TitleBorder(color)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 4.dp)) {
                    Text(
                        text = formatCurrency(teamValue),
                        fontSize = 14.sp,
                        color = JervisTheme.contentTextColor
                    )
                    Text(
                        text = "$rerolls RR",
                        fontSize = 14.sp,
                        color = JervisTheme.contentTextColor
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                JervisTooltip(tooltip = race) {
                    Box(
                        modifier = Modifier.aspectRatio(1f).fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ShadowImage(
                            modifier = Modifier.padding(8.dp),
                            painter = logo,
                            contentDescription = null,
                            shadowColor = highlightColor,
                            shadowEnabled = highlight.isNotBlank() && race.contains(highlight, ignoreCase = true),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShadowImage(
    painter: ImageBitmap,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shadowEnabled: Boolean = true,
    shadowOffset: DpOffset = DpOffset(0.dp, 0.dp),
    shadowColor: Color = JervisTheme.brightYellow,
) {
    val density = LocalDensity.current

    Box(modifier) {
        // Shadow
        if (shadowEnabled) {
            Image(
                bitmap = painter,
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    shadowColor,
                    blendMode = BlendMode.SrcIn,
                ),
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        translationX = with(density) { shadowOffset.x.toPx() }
                        translationY = with(density) { shadowOffset.y.toPx() }
                        scaleX = 1.05f
                        scaleY = 1.05f
                    },
            )
        }

        // Actual image
        Image(
            bitmap = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
        )
    }
}

private fun String.highlightMatches(
    filter: String,
    highlightColor: Color = JervisTheme.brightYellow,
): AnnotatedString {
    val displayName = uppercase()
    return when (filter.isBlank()) {
        true -> AnnotatedString(displayName)
        false -> buildAnnotatedString {
            append(displayName)
            var searchStart = 0
            while (searchStart < displayName.length) {
                val matchStart = displayName.indexOf(filter, startIndex = searchStart, ignoreCase = true)
                when (matchStart) {
                    -1 -> break
                    else -> {
                        addStyle(
                            style = SpanStyle(
                                color = JervisTheme.contentTextColor,
                                background = highlightColor,
                            ),
                            start = matchStart,
                            end = matchStart + filter.length,
                        )
                        searchStart = matchStart + filter.length
                    }
                }
            }
        }
    }
}
