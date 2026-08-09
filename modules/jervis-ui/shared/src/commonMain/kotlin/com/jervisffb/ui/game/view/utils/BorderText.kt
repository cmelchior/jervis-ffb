package com.jervisffb.ui.game.view.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.view.JervisTheme


// Outlines are not supported by Compose yet, so fake it by overlaying two texts.
@Composable
fun BorderText(
    modifier: Modifier = Modifier,
    text: String,
    fontFamily: FontFamily = JervisTheme.pixelFontHeaderFamily(),
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    lineHeight: TextUnit = 1.em,
    color: Color = JervisTheme.white,
    borderThickness: Float = 10f,
    shadowOffset: Float = 4f,
) {
    BorderText(
        modifier = modifier,
        text = buildAnnotatedString { append(text) },
        fontFamily = fontFamily,
        fontSize = fontSize,
        fontWeight = fontWeight,
        lineHeight = lineHeight,
        color = color,
        borderThickness = borderThickness,
        shadowOffset = shadowOffset
    )
}

@Composable
fun BorderText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    fontFamily: FontFamily = JervisTheme.pixelFontHeaderFamily(),
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    lineHeight: TextUnit = 1.em,
    color: Color = JervisTheme.white,
    borderThickness: Float = 10f,
    shadowOffset: Float = 4f,
) {
    val borderText = text.mapAnnotations { annotation ->
        val spanStyle = annotation.item as? SpanStyle
        if (spanStyle != null) {
            AnnotatedString.Range(
                item = spanStyle.copy(color = JervisTheme.black),
                start = annotation.start,
                end = annotation.end,
                tag = annotation.tag,
            )
        } else {
            annotation
        }
    }

    Box(
        modifier = modifier
    ) {
        Text(
            text = borderText,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = fontFamily,
                color = JervisTheme.black,
                fontWeight = fontWeight,
                fontSize = fontSize,
                lineHeight = lineHeight,
                drawStyle = Stroke(
                    miter = borderThickness,
                    width = borderThickness,
                    join = StrokeJoin.Round
                )
            ),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = Shadow(
                    color = JervisTheme.black,
                    offset = Offset(shadowOffset, shadowOffset),
                    blurRadius = 0f
                ),
                color = color,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                fontSize = fontSize,
                lineHeight = lineHeight,
            ),
        )
    }
}
