package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiPlayerCard
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.utils.darken
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.jsp
import com.jervisffb.ui.utils.lighten
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerStatsCard(flow: Flow<UiPlayerCard?>) {
    val playerData by flow.collectAsState(null)
    val teamColor = when (playerData?.model?.isOnHomeTeam() == true) {
        true -> JervisTheme.homeTeamColor
        false -> JervisTheme.awayTeamColor
    }
    val statWidthFactor = 0.3f
    val imageWidthFactor = 0.7f

    val lightTeamColor = teamColor.lighten(0.15f)
    val darkTeamColor = teamColor.darken(0.25f)
    val darkerTeamColor = teamColor.darken(0.5f)
    val innerBorderColor = JervisTheme.white
    val borderSize = 6.jdp
    val bigBorderSize = 8.jdp

    val statboxLabelColor = darkerTeamColor
    val statboxContentColor = darkTeamColor

    playerData?.let { player ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .onPointerEvent(PointerEventType.Enter) { /* Swallow it */ }
                .onPointerEvent(PointerEventType.Exit) { /* Swallow it */ }
            ,
            shape = RectangleShape, //RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = darkerTeamColor),
            // border = BorderStroke(width = bigBorderSize, color = teamColor),
        ) {
            BoxWithConstraints {
                val boxWidth = maxWidth
                val portraitHeight = (boxWidth - bigBorderSize * 2) * imageWidthFactor * 147f/95f

                // Stats and image
                Column(
                    modifier = Modifier
                        .paperBackground(teamColor) // Background color for the entire card
                        .padding(bigBorderSize),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .heightIn(min = (portraitHeight - (borderSize*3))/4f)
                        ,
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(statWidthFactor)
                                .padding(end = borderSize)
                                .fillMaxHeight()
                                .background(teamColor)
                            ,
                            verticalArrangement = Arrangement.spacedBy(borderSize),
                        ) {
                            StatBox(
                                Modifier.fillMaxSize(),
                                "MV",
                                player.model.move.toString(),
                                statboxLabelColor,
                                statboxContentColor,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .fillMaxSize()
                                .paperBackground(lightTeamColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Player type
                            val fontSize = 26.jsp
                            val type = remember(player.model.position.titleSingular) {
                                val originalTitle = player.model.position.titleSingular
                                val splitTitle = originalTitle.split(" ")
                                // We want to have the common case of two words in the title be split
                                // across two lines as it looks better as the chance of getting too
                                // close to the sides increases. This heuristic can probably be improved
                                // in the future.
                                if (splitTitle.size == 2) {
                                    "${splitTitle[0]}\n${splitTitle[1]}"
                                } else {
                                    originalTitle
                                }
                            }
                            Text(
                                modifier = Modifier.padding(2.jdp).fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = type,
                                fontFamily = JervisTheme.fontFamily(),
                                style = TextStyle.Default.copy(
                                    shadow = Shadow(Color.Black, Offset(0f, 2f), 2f),
                                ),
                                color = Color.White,
                                fontSize = fontSize,
                                letterSpacing = 1.jsp,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(borderSize))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(portraitHeight)
                        ,
                    ) {
                        Column(
                            modifier = Modifier.weight(statWidthFactor).fillMaxSize().padding(end = borderSize),
                            verticalArrangement = Arrangement.spacedBy(borderSize),
                        ) {
                            val model = player.model
                            val modifier = Modifier.weight(1f)
                            StatBox(modifier, "ST", model.strength.toString(), statboxLabelColor, statboxContentColor)
                            StatBox(modifier, "AG", "${model.agility}+", statboxLabelColor, statboxContentColor)
                            StatBox(modifier, "PA", if (model.passing == null) "-" else "${model.passing}+", statboxLabelColor, statboxContentColor)
                            StatBox(modifier, "AV", "${model.armorValue}+", statboxLabelColor, statboxContentColor)
                        }
                        Box(
                            modifier = Modifier
                                .weight(imageWidthFactor)
                                .fillMaxSize()
                                .border(
                                    width = borderSize,
                                    color = innerBorderColor,
                                )
                            ,
                            contentAlignment = Alignment.BottomCenter,
                        ) {
//                            PixelatedImage(
//                                modifier = Modifier
//                                    // .aspectRatio(95f / 147f)
//                                    .clip(RectangleShape)
//                                    .graphicsLayer {
//                                        scaleX = 1.05f
//                                        scaleY = 1.05f
//                                    }
//                                ,
//                                painter = BitmapPainter(IconFactory.getPlayerPortrait(player.model.id)),
//                                pixelSize = 2f,
//                            )
                            Image(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RectangleShape)
                                    .graphicsLayer {
                                        scaleX = 1.05f
                                        scaleY = 1.05f
                                    }
                                ,
                                // .aspectRatio(95f / 147f).fillMaxWidth(),
                                bitmap = IconFactory.getPlayerPortrait(player.model.id),
                                filterQuality = FilterQuality.None,
                                contentDescription = "Image of ${player.model.name}",
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.Center,
                            )

                            Box(
                                modifier = Modifier
                                    .padding(bottom = borderSize)
                                    .background(Color.Black.copy(0.0f))
                                    .padding(
                                        start = borderSize + 3.dp,
                                        end = borderSize + 3.dp,
                                        bottom = 4.jdp,
                                        top = 4.jdp
                                    )
                                ,
                                contentAlignment = Alignment.Center,
                            ) {
                                PlayerName(player.model.name, borderSize)
                            }
                        }
                    }

                    // Player level
                    Row(
                        modifier = Modifier
                            .padding(top = bigBorderSize, bottom = bigBorderSize)
                            .fillMaxWidth(),
                    ) {
                        val fontSize = 16.jsp
                        Text(
                            textAlign = TextAlign.Start,
                            text = player.model.level.description,
                            fontFamily = JervisTheme.fontFamily(),
                            style = TextStyle.Default.copy(
                                shadow = Shadow(Color.Black, Offset(0f, 2f), 2f),
                            ),
                            color = Color.White,
                            maxLines = 1,
                            fontSize = fontSize,
                            letterSpacing = 1.jsp,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            textAlign = TextAlign.Start,
                            text = "${player.model.starPlayerPoints} SPP",
                            fontFamily = JervisTheme.fontFamily(),
                            style = TextStyle.Default.copy(
                                shadow = Shadow(Color.Black, Offset(0f, 2f), 2f),
                            ),
                            color = Color.White,
                            maxLines = 1,
                            fontSize = fontSize,
                            letterSpacing = 1.jsp,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Skill content
                    Column(
                        modifier = Modifier
                            .paperBackground()
                            .border(borderSize, innerBorderColor)
                            .fillMaxSize()
                        ,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(borderSize*2),
                        ) {
                            val skillFontSize = 16.jsp
                            val skills = player.model.skills
                            if (skills.isEmpty()) {
                                Text(
                                    modifier = Modifier.padding(0.jdp).fillMaxWidth(),
                                    fontSize = skillFontSize,
                                    color = JervisTheme.contentTextColor,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Italic,
                                    text = "No Skills"
                                )
                            } else {
                                player.model.skills.forEach {
                                    Text(
                                        fontSize = skillFontSize,
                                        lineHeight = 1.5.em,
                                        color = JervisTheme.contentTextColor,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(0.jdp).fillMaxWidth(),
                                        text = it.name + if (it.compulsory) "*" else "",
                                        textDecoration = if (it.used) TextDecoration.LineThrough else TextDecoration.None,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.PlayerName(name: String, borderSize: Dp) {
    // Because Compose does not support drop shadow on Outlined Text
    // we fake it by first blurring the outline and then render the rest
    val playerNameStyle = MaterialTheme.typography.bodySmall.copy(
        textAlign = TextAlign.Center,
        fontFamily = JervisTheme.fontFamily(),
        fontSize = 20.jsp,
        lineHeight = 1.4.em,
        letterSpacing = 1.sp,
    )
    val fontOutlineSize = with(LocalDensity.current) { 6.jdp.toPx() }

    // Drop shadow
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                renderEffect = BlurEffect(2.jdp.toPx(), 2.jdp.toPx(), TileMode.Clamp)
            }
        ,
        text = name,
        overflow = TextOverflow.Ellipsis,
        style = playerNameStyle.copy(
            color = JervisTheme.black,
            drawStyle = Stroke(
                miter = fontOutlineSize,
                width = fontOutlineSize,
                join = StrokeJoin.Round
            )
        ),
    )

    // Outline
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = name,
        overflow = TextOverflow.Ellipsis,
        style = playerNameStyle.copy(
            color = JervisTheme.white,
            drawStyle = Stroke(
                miter = 2f, // fontOutlineSize,
                width = fontOutlineSize,
                join = StrokeJoin.Miter,
                cap = StrokeCap.Butt,

            )
        ),
    )
    // Real text
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = name,
        overflow = TextOverflow.Ellipsis,
        style = playerNameStyle.copy(
            color = JervisTheme.black,
        )
    )
}

/**
 * Render a single stat box that is part of a Playe Stats Card.
 */
@Composable
private fun StatBox(
    modifier: Modifier,
    title: String,
    value: String,
    labelBackgroundColor: Color,
    backgroundColor: Color,
) {
    Column(
        modifier = modifier.fillMaxWidth().fillMaxHeight().background(backgroundColor),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(labelBackgroundColor)
                .wrapContentHeight(align = Alignment.CenterVertically)
            ,
            text = title,
            fontSize = 12.jsp,
            lineHeight = 1.em,
            letterSpacing = 1.jsp,
            maxLines = 1,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier
                .offset(y = (-1).jdp) // Offset to make the font look slightly more aligned
                .fillMaxHeight(0.65f)
                .fillMaxWidth()
                .background(backgroundColor)
                .wrapContentHeight(align = Alignment.CenterVertically)
            ,
            text = value,
            fontSize = 30.jsp,
            lineHeight = 1.em,
            maxLines = 1,
            letterSpacing = 2.jsp,
            fontFamily = JervisTheme.fontFamily(),
            color = JervisTheme.white,
            textAlign = TextAlign.Center,
            style = TextStyle.Default.copy(
                shadow = Shadow(JervisTheme.black, Offset(2.jdp.value, 2.jdp.value), 2.jdp.value),
            ),
        )
    }
}
