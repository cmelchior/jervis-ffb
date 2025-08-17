package com.jervisffb.ui.menu.fumbbl

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.menu.components.JervisCircularProgressIndicator

/**
 * Just for demo purposes for now
 */
@Composable
fun ColumnScope.GameStatusRowType3(textColor: Color = JervisTheme.contentTextColor, backgroundColor: Color = JervisTheme.rulebookPaperMediumDark) {
    val imageSize = 75.dp
    val fontSize = 14.sp
    val spaceBetweenTeamAndCoach = 4.dp

    var khorneLogo: ImageBitmap? by remember { mutableStateOf(null) }
    var skavenLogo: ImageBitmap? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        khorneLogo = IconFactory.loadRosterIcon(TeamId("khorne"), SingleSprite.embedded("jervis/roster/logo_khorne_small.png"), LogoSize.SMALL)
        skavenLogo = IconFactory.loadRosterIcon(TeamId("skaven"), SingleSprite.embedded("jervis/roster/logo_skaven_small.png"), LogoSize.SMALL)
    }
    Row(
        modifier = Modifier.background(backgroundColor).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    text = "Rotgut Reavers",
                    textAlign = TextAlign.End,
                    color = textColor,
                    fontSize = fontSize,
                    lineHeight = 1.0.em,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Text(
                    text = "Khorne",
                    textAlign = TextAlign.End,
                    lineHeight = 1.0.em,
                    fontSize = 12.sp,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(spaceBetweenTeamAndCoach))
                Text(
                    text = "(Rookie) Steelclaw",
                    fontSize = fontSize,
                    textAlign = TextAlign.End,
                    color = textColor,
                )
                Text(
                    text = "CTV 1.000K",
                    fontSize = fontSize,
                    lineHeight = 1.2.em,
                    textAlign = TextAlign.End,
                    color = textColor,
                )
            }
            Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                if (khorneLogo != null) {
                    Image(
                        modifier = Modifier.width(imageSize).height(imageSize).aspectRatio(1f),
                        bitmap = khorneLogo!!,
                        contentDescription = "",
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
        }
        Box(modifier = Modifier.width(110.dp), contentAlignment = Alignment.Center) {
            JervisCircularProgressIndicator(0.2f)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.weight(1f).offset(x = -2.dp),
                    text = "3",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.End,
                    lineHeight = 1f.em,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(80.dp))
                Text(
                    modifier = Modifier.weight(1f),
                    text = "1",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    fontSize = 24.sp,
                    lineHeight = 1f.em,
                    maxLines = 1
                )
            }
            Text(
                text = "h2t7",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 1f.em,
                maxLines = 1
            )
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                if (skavenLogo != null) {
                    Image(
                        modifier = Modifier.width(imageSize).height(imageSize).aspectRatio(1f),
                        bitmap = skavenLogo!!,
                        contentDescription = "",
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Snikt’s Endzone Slashers",
                    textAlign = TextAlign.End,
                    lineHeight = 1.0.em,
                    color = textColor,
                    fontSize = fontSize,
                    maxLines = 1
                )
                Text(
                    text = "Skaven",
                    lineHeight = 1.0.em,
                    fontSize = 12.sp,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(spaceBetweenTeamAndCoach))
                Text(
                    fontSize = fontSize,
                    text = "Skitter (Star)",
                    color = textColor,
                )
                Text(
                    fontSize = fontSize,
                    lineHeight = 1.2.em,
                    text = "CTV 1.000K",
                    color = textColor,
                )
            }
        }
    }
}
