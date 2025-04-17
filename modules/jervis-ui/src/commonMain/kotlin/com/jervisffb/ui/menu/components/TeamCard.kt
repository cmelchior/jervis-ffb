package com.jervisffb.ui.menu.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.formatCurrency
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.TitleBorder

@Composable
fun TeamCard(
    name: String,
    teamValue: Int,
    rerolls: Int,
    logo: ImageBitmap,
    isSelected: Boolean = false,
    isEnabled: Boolean = true,
    emptyTeam: Boolean = false,
    onClick: (() -> Unit)?
) {
    val borderWidth = if (isSelected || !isEnabled) 3.dp else 0.dp
    val borderColor = if (isSelected || !isEnabled) JervisTheme.rulebookRed else Color.Transparent
    Box(
        modifier = Modifier
            .width(300.dp)
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
                        text = name.uppercase(),
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
                Image(
                    modifier = Modifier.padding(8.dp),
                    bitmap = logo,
                    contentDescription = null,
                    contentScale = ContentScale.Inside,
                )
            }
        }
    }
}
