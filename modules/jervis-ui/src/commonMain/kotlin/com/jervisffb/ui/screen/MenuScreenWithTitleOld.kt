package com.jervisffb.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.menu_background
import com.jervisffb.ui.dropShadow
import com.jervisffb.ui.view.JervisTheme
import org.jetbrains.compose.resources.imageResource

@Composable
fun MenuScreenWithTitleOld(title: String, content: @Composable BoxScope.() -> Unit) {
    val topPadding = 24.dp
    val fontSize = 30.sp

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.TopStart,
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = imageResource(Res.drawable.menu_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.9f
        )
        Column {
            Row {
                Box(
                    modifier = Modifier
                        .padding(top = topPadding)
                        .dropShadow(
                            color = Color.Black,
                            blurRadius = 4.dp
                        )
                        .background(JervisTheme.contentBackgroundColor)
                        .drawBehind {
                            val strokeWidth = 8.dp.toPx()
                            val halfStrokeWidth = strokeWidth / 2
                            val color = JervisTheme.awayTeamColor

                            // Top border
                            drawLine(
                                color = color,
                                start = Offset(0f, halfStrokeWidth),
                                end = Offset(size.width, halfStrokeWidth),
                                strokeWidth = strokeWidth
                            )

                            // Right border
                            drawLine(
                                color = color,
                                start = Offset(size.width - halfStrokeWidth, 0f),
                                end = Offset(size.width - halfStrokeWidth, size.height),
                                strokeWidth = strokeWidth
                            )

                            // Bottom border
                            drawLine(
                                color = color,
                                start = Offset(0f, size.height - halfStrokeWidth),
                                end = Offset(size.width, size.height - halfStrokeWidth),
                                strokeWidth = strokeWidth
                            )
                        }
                ) {
                    Text(
                        modifier = Modifier.padding(start = 32.dp, top = 8.dp, end = 32.dp, bottom = 4.dp),
                        text = title.uppercase(),
                        maxLines = 1,
                        lineHeight = 1.sp,
                        color = JervisTheme.contentTextColor,
                        fontSize = fontSize,
                        fontFamily = JervisTheme.fontFamily()
                    )

                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .padding(top = topPadding)
                        .dropShadow(
                            color = Color.Black,
                            blurRadius = 4.dp
                        )
                        .background(JervisTheme.contentBackgroundColor)
                        .drawBehind {
                            val strokeWidth = 8.dp.toPx()
                            val halfStrokeWidth = strokeWidth / 2
                            val color = JervisTheme.awayTeamColor

                            // Top border
                            drawLine(
                                color = color,
                                start = Offset(0f, halfStrokeWidth),
                                end = Offset(size.width, halfStrokeWidth),
                                strokeWidth = strokeWidth
                            )

                            // Right border
                            drawLine(
                                color = color,
                                start = Offset(halfStrokeWidth, 0f),
                                end = Offset(halfStrokeWidth, size.height),
                                strokeWidth = strokeWidth
                            )

                            // Bottom border
                            drawLine(
                                color = color,
                                start = Offset(0f, size.height - halfStrokeWidth),
                                end = Offset(size.width, size.height - halfStrokeWidth),
                                strokeWidth = strokeWidth
                            )
                        }
                ) {
                    Text(
                        modifier = Modifier.padding(start = 32.dp, top = 8.dp, end = 32.dp, bottom = 4.dp),
                        text = "BACK",
                        maxLines = 1,
                        lineHeight = 1.sp,
                        color = JervisTheme.contentTextColor,
                        fontSize = fontSize,
                        fontFamily = JervisTheme.fontFamily()
                    )

                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}
