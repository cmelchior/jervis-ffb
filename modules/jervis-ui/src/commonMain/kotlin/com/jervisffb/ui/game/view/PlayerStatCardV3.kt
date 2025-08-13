package com.jervisffb.ui.game.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.adamglin.composeshadow.dropShadow
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.ui.dropShadow
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiPlayerCard
import com.jervisffb.ui.game.view.utils.PixelatedImage
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.menu.intro.loadJervisFont
import com.jervisffb.ui.utils.darken
import kotlinx.coroutines.flow.Flow
import org.pushingpixels.artemis.drawTextOnPath

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerStatsCardV3(flow: Flow<UiPlayerCard?>) {
    val playerData by flow.collectAsState(null)
    val font = loadJervisFont()
    val teamColor = when (playerData?.model?.isOnHomeTeam() == true) {
        true -> JervisTheme.homeTeamColor
        false -> JervisTheme.awayTeamColor
    }
    playerData?.let { player ->
        Column(modifier = Modifier
            .fillMaxWidth()
            .onPointerEvent(PointerEventType.Enter) { /* Swallow it */ }
            .onPointerEvent(PointerEventType.Exit) { /* Swallow it */ },
            // contentAlignment = Alignment.TopStart
        ) {
//            Image(
//                painter = BitmapPainter(IconFactory.getPlayerDetailOverlay(player.model.isOnHomeTeam())),
//                contentDescription = null,
//                contentScale = ContentScale.Fit,
//                modifier = Modifier.fillMaxSize(),
//            )
            // Side bar content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .dropShadow(shape = RectangleShape, color = Color.Black, offsetX = 0.dp, offsetY = 4.dp, blur = 4.dp)
//                    .background(color = teamColor)
                    // .dropShadow(color = Color.Black, offsetX = 0.dp, offsetY = 4.dp, blurRadius = 4.dp)
                    .paperBackground(color = teamColor)
                    .border(4.dp, teamColor.darken(0.5f))
                    .zIndex(1f)
            ) {

                // Blue Square with player information
                Column(
                    modifier =
                    Modifier
                    // .aspectRatio(145f / 213f) // Size of blue square
                    // .fillMaxSize(),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .weight(1f),
                    ) {
                        // Player name
                        Text(
                            modifier = Modifier.padding(start = 8.dp, top = 12.dp, end = 4.dp, bottom = 4.dp).fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            text = player.model.name,
                            fontFamily = JervisTheme.fontFamily(),
                            style = TextStyle.Default.copy(
                                shadow = Shadow(Color.Black, Offset(2f, 2f), 2f),
                            ),
                            color = Color.White,
                            maxLines = 1,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp,
                            overflow = TextOverflow.Ellipsis,
                        )

                        // Image, type / number
                        Row(
                            modifier =
                                Modifier
                                    // .padding(start = 24.dp, end = 8.dp)
                                    .fillMaxSize(),
                        ) {
                            PixelatedImage(
                                modifier = Modifier
                                    .aspectRatio(95f / 147f)
                                    .clip(RectangleShape)
                                    .graphicsLayer {
                                        scaleX = 1.05f
                                        scaleY = 1.05f
                                    }
                                ,
                                painter = BitmapPainter(IconFactory.getPlayerPortrait(player.model.id)),
                                pixelSize = 4f,
                            )
//                            Image(
//                                modifier = Modifier
//                                    .aspectRatio(95f / 147f)
//                                    .fillMaxSize()
//                                    .clip(RectangleShape)
//                                    .graphicsLayer {
//                                        scaleX = 1.1f
//                                        scaleY = 1.1f
//                                    }
//                                ,
//                                painter = BitmapPainter(IconFactory.getPlayerPortrait(player.model.id)),
//                                contentDescription = "",
//                                alignment = Alignment.Center,
//                                contentScale = ContentScale.Crop,
//                            )

                            val font = loadJervisFont()
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                val fontSize = 14.sp
                                val lineX = this.size.width - 8.dp.toPx()
                                val path = androidx.compose.ui.graphics.Path()
                                path.moveTo(lineX, this.size.height - 6.dp.toPx())
                                path.lineTo(lineX, 0.dp.toPx())

                                val name = player.model.position.titleSingular
                                drawTextOnPath(
                                    text = "$name #${player.model.number.value}",
                                    textSize = fontSize.toDp(),
                                    isEmboldened = false,
                                    path = path,
                                    offset = Offset(0.dp.toPx(), 0.dp.toPx()),
                                    textAlign = TextAlign.Start,
                                    paint =
                                        Paint().also {
                                            it.color = Color.White
                                            it.style = PaintingStyle.Fill
                                        },
                                    font = font,
                                    letterSpacing = 1.sp,
                                    shadow = Shadow(Color.Black, Offset(2f, 2f), 2f)
                                )
                            }
                        }
                    }

                    // Stat boxes
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                        // horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        val model = player.model
                        val modifier = Modifier.weight(1f).aspectRatio(52f / 58f)
                        StatBox(modifier, "MV", model.move.toString())
                        StatBox(modifier, "ST", model.strength.toString())
                        StatBox(modifier, "AG", "${model.agility}+")
                        StatBox(modifier, "PA", if (model.passing == null) "-" else "${model.passing}+")
                        StatBox(modifier, "AV", "${model.armorValue}+")
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .paperBackground()
                    .zIndex(0f)

                // .border(4.dp, JervisTheme.rulebookPaperDark)
            ) {
                Text(
                    modifier = Modifier.padding(4.dp).fillMaxWidth(),
                    text = "${player.model.starPlayerPoints} ${player.model.level.name}",
                    textAlign = TextAlign.Center,
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    player.model.skills.forEach {
                        Text(
                            modifier = Modifier.padding(4.dp).fillMaxWidth(),
                            text = it.name + if (it.compulsory) "*" else "",
                            textDecoration = if (it.used) TextDecoration.LineThrough else TextDecoration.None,
                        )
                    }
                }
            }
        }
    }
}
