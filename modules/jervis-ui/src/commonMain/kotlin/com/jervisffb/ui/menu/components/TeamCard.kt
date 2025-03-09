package com.jervisffb.ui.menu.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.TitleBorder

@Composable
fun RowScope.TeamCard(
    name: String,
    teamValue: Int,
    rerolls: Int,
    logo: ImageBitmap,
    isSelected: Boolean = false,
    emptyTeam: Boolean = false,
    onClick: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .width(300.dp)
            .background(JervisTheme.rulebookPaperMediumDark.copy(alpha = 0.5f))
            .border(width = if (isSelected) 3.dp else 0.dp, color = if (isSelected) JervisTheme.rulebookRed else Color.Transparent)
            .let { if (onClick != null) it.clickable(!emptyTeam, onClick = onClick) else it }
        ,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(), //.padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 0.dp),
//                verticalAlignment = Alignment.Top,
            ) {
                val color = JervisTheme.rulebookRed
//                val color = if (isSelected) JervisTheme.rulebookGreen else JervisTheme.rulebookRed
//                Divider(
//                    modifier = Modifier
//                        .padding(bottom = 2.dp)
//                        .wrapContentWidth()
//                        .height(2.dp)
//                    ,
//                    color = JervisTheme.rulebookBlue,
//                )
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
//                        color = if (isSelected) JervisTheme.rulebookOrange else JervisTheme.white
                        color = JervisTheme.white
                    )
                }
                TitleBorder(color)
//
//
//                BoxHeader(name, color = JervisTheme.rulebookRed)
//                Text(
//                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 10.dp),
//                    textAlign = TextAlign.Start,
//                    text = name,
//                    color = JervisTheme.accentContentBackgroundColor,
//                    fontWeight = FontWeight.Bold,
//                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 4.dp)) {
                    val adjustedTv = teamValue / 1_000
                    Text(text = "$adjustedTv K", color = JervisTheme.contentTextColor)
                    Text("$rerolls RR", color = JervisTheme.contentTextColor)
                }
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    modifier = Modifier.padding(8.dp),
                    bitmap = logo,
                    contentDescription = null,
                    contentScale = ContentScale.Inside,
                )
            }
//            Divider(
//                modifier = Modifier
//                    .padding(top = 2.dp)
//                    .wrapContentWidth()
//                    .height(2.dp)
//                ,
//                color = JervisTheme.rulebookBlue,
//            )
        }
    }
}
