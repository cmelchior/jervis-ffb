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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import com.jervisffb.ui.menu.intro.loadJervisFont
import com.jervisffb.ui.utils.darken
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.jsp
import com.jervisffb.ui.utils.lighten
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerStatsCardV2(flow: Flow<UiPlayerCard?>) {
    val playerData by flow.collectAsState(null)
    val font = loadJervisFont()
    val teamColor = when (playerData?.model?.isOnHomeTeam() == true) {
        true -> JervisTheme.homeTeamColor
        false -> JervisTheme.awayTeamColor
    }
    val lightTeamColor = teamColor.lighten(0.25f)
    val darkTeamColor = teamColor.darken(0.25f)
    val darkerTeamColor = teamColor.darken(0.5f)
    val borderColor = darkerTeamColor
    val innerBorderColor = JervisTheme.white
    val borderSize = 6.jdp
    val bigBorderSize = 8.jdp

    playerData?.let { player ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .onPointerEvent(PointerEventType.Enter) { /* Swallow it */ }
                .onPointerEvent(PointerEventType.Exit) { /* Swallow it */ }
            ,
            shape = RectangleShape, //RoundedCornerShape(8.dp),
            elevation = 4.dp,
            backgroundColor = darkerTeamColor,
            // border = BorderStroke(width = bigBorderSize, color = teamColor),
        ) {
            BoxWithConstraints {
                val boxWidth = minWidth
                val portraitHeight = (boxWidth - bigBorderSize * 2) * 0.7f * 147f/95f

                // Stats and image
                Column(
                    modifier = Modifier.paperBackground(darkerTeamColor).padding(bigBorderSize),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.3f)
                                .padding(end = borderSize)
                                .fillMaxHeight()
                                .background(teamColor)
                            ,
                            verticalArrangement = Arrangement.spacedBy(borderSize),
                        ) {
                            StatBoxV3(Modifier.fillMaxSize(), "MV", player.model.move.toString(), teamColor, boxWidth)
                        }
                        Box(
                            modifier = Modifier.weight(0.7f).fillMaxSize().paperBackground(lightTeamColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Player type
                            val fontSize = 20.jsp
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
                            modifier = Modifier.weight(0.3f).fillMaxSize().padding(end = borderSize),
                            verticalArrangement = Arrangement.spacedBy(borderSize),
                        ) {
                            val model = player.model
                            val modifier = Modifier.weight(1f)
                            StatBoxV3(modifier, "ST", model.strength.toString(), teamColor, boxWidth)
                            StatBoxV3(modifier, "AG", "${model.agility}+", teamColor, boxWidth)
                            StatBoxV3(modifier, "PA", if (model.passing == null) "-" else "${model.passing}+", teamColor, boxWidth)
                            StatBoxV3(modifier, "AV", "${model.armorValue}+", teamColor, boxWidth)
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
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
                                PlayerName(player.model.name, borderSize, boxWidth)
                            }
                        }
                    }

                    // Player level
                    Row(
                        modifier = Modifier.padding(top = bigBorderSize, bottom = bigBorderSize).fillMaxWidth(),
                    ) {
                        Text(
                            textAlign = TextAlign.Start,
                            text = player.model.level.description,
                            fontFamily = JervisTheme.fontFamily(),
                            style = TextStyle.Default.copy(
                                shadow = Shadow(Color.Black, Offset(0f, 2f), 2f),
                            ),
                            color = Color.White,
                            maxLines = 1,
                            fontSize = 14.jsp,
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
                            fontSize = 14.jsp,
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
                            val skills = player.model.skills
                            if (skills.isEmpty()) {
                                Text(
                                    modifier = Modifier.padding(0.jdp).fillMaxWidth(),
                                    fontSize = 14.jsp,
                                    color = JervisTheme.contentTextColor,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.SemiBold,
                                    fontStyle = FontStyle.Italic,
                                    text = "No Skills"
                                )
                            } else {
                                player.model.skills.forEach {
                                    Text(
                                        fontSize = 14.jsp,
                                        lineHeight = 1.5.em,
                                        color = JervisTheme.contentTextColor,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(0.dp).fillMaxWidth(),
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
private fun BoxScope.PlayerName(name: String, borderSize: Dp, boxWidth: Dp) {
    // Because Compose does not support drop shadow on Outlined Text
    // we fake it by first blurring the outline and then render the rest

    val fontScale =  (16 / 210f) // Reference value (16.sp / 210.dp)
    val shadowScale = (8 / 210f) // Reference value (8f / 210.dp)
    val outlineScale = (6 / 210f) // Reference value (6f / 210.dp)

    // Outline not supported by Compose yet, so fake it
    val playerNameStyle = MaterialTheme.typography.body1.copy(
        textAlign = TextAlign.Center,
        fontFamily = JervisTheme.fontFamily(),
        fontSize = (boxWidth.value * fontScale).sp,
        lineHeight = 1.4.em,
        letterSpacing = 1.sp,
    )
    val fontOutlineSize = with(LocalDensity.current) { (outlineScale * boxWidth.value).dp.toPx() }

    // Drop shadow
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                renderEffect = BlurEffect(shadowScale * boxWidth.value, shadowScale * boxWidth.value, TileMode.Clamp)
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

@Composable
private fun StatBoxV2(
    modifier: Modifier,
    title: String,
    value: String,
    teamColor: Color,
    darkerTeamColor: Color,
) {
    Box(
        modifier = modifier // .aspectRatio(1.618f)
    ) {
        Row {
            Box(
                modifier = Modifier.width(24.dp).fillMaxHeight().background(darkerTeamColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier,
                    text = title,
                    fontSize = 8.sp,
                    lineHeight = 1.em,
                    maxLines = 1,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
            Box(
                modifier = Modifier.fillMaxSize().background(teamColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier,
                    text = value,
                    fontSize = 12.sp,
                    lineHeight = 1.em,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    color = JervisTheme.white,
                    textAlign = TextAlign.Center,

                )
            }
        }
    }
}

@Composable
private fun StatBoxV3(
    modifier: Modifier,
    title: String,
    value: String,
    backgroundColor: Color,
    boxWidth: Dp,
) {
    val fontScaleSmall = 0.05714285714 // Experimental value (12.sp / 210.dp)
    val fontScaleBig = 0.1047619048 // Experimental value (22.sp / / 210.dp)
    val paddingScale = 0.014285714f // Experimental value ( 4.dp / 210.dp)
    val shadowScale = 0.00952381f // Experimental value ( 2 / 210)
    Column(
        modifier = modifier.fillMaxWidth().background(backgroundColor).padding(boxWidth * paddingScale),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(bottom = boxWidth * paddingScale),
            text = title,
            fontSize = (boxWidth.value * fontScaleSmall).sp,
            lineHeight = 1.em,
            letterSpacing = 1.sp,
            maxLines = 1,
            fontWeight = FontWeight.Light,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier,
            text = value,
            fontSize = (boxWidth.value * fontScaleBig).sp,
            lineHeight = 1.em,
            maxLines = 1,
            letterSpacing = 2.sp,
            fontFamily = JervisTheme.fontFamily(),
            color = JervisTheme.white,
            textAlign = TextAlign.Center,
            style = TextStyle.Default.copy(
                shadow = Shadow(JervisTheme.black, Offset(shadowScale * boxWidth.value, shadowScale * boxWidth.value), shadowScale * boxWidth.value),
            ),
        )
    }
}
