package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiPlayerCard
import kotlinx.coroutines.flow.Flow
import org.pushingpixels.artemis.drawTextOnPath

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerStatsCard(flow: Flow<UiPlayerCard?>) {
    val playerData by flow.collectAsState(null)
    playerData?.let { player ->
        Box(modifier = Modifier
            .fillMaxWidth()
            .onPointerEvent(PointerEventType.Enter) { /* Swallow it */ }
            .onPointerEvent(PointerEventType.Exit) { /* Swallow it */ },
            contentAlignment = Alignment.TopStart
        ) {
            Image(
                painter = BitmapPainter(IconFactory.getPlayerDetailOverlay()),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )

            // Side bar content
            Column(modifier = Modifier.fillMaxWidth()) {
                // Blue Square with player information
                Column(
                    modifier =
                        Modifier
                            .aspectRatio(145f / 213f) // Size of blue square
                            .fillMaxSize(),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .weight(1f),
                    ) {
                        // Player name
                        Text(
                            modifier = Modifier.padding(4.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = player.model.name ?: "",
                            color = Color.White,
                            maxLines = 1,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            overflow = TextOverflow.Ellipsis,
                        )

                        // Image, type / number
                        Row(
                            modifier =
                                Modifier
                                    .padding(start = 8.dp, end = 8.dp)
                                    .fillMaxSize(),
                        ) {
                            Image(
                                modifier = Modifier.aspectRatio(95f / 147f).fillMaxSize(),
                                painter = BitmapPainter(IconFactory.getPlayerImage(player.model.id)),
                                contentDescription = "",
                                contentScale = ContentScale.Fit,
                            )

                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                // A path with three quad Bezier segments
                                val path = androidx.compose.ui.graphics.Path()
                                path.moveTo(this.size.width, this.size.height - 5)
                                path.lineTo(size.width, 5f)

                                val name = player.model.position.titleSingular.takeDot(10)
                                drawTextOnPath(
                                    text = "$name #${player.model.number.value}",
                                    textSize = 14.sp.toDp(),
                                    isEmboldened = true,
                                    path = path,
                                    offset = Offset(0.dp.toPx(), 0.0f),
                                    textAlign = TextAlign.Start,
                                    paint =
                                        Paint().also {
                                            it.color = Color.White
                                            it.style = PaintingStyle.Fill
                                        },
                                )
                            }
                        }
                    }

                    // Stat boxes
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
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
}
