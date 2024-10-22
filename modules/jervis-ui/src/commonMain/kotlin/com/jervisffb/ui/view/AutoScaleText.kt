package com.jervisffb.ui.view

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.TextStyle

// Credit: https://stackoverflow.com/questions/63971569/androidautosizetexttype-in-jetpack-compose
@Composable
fun AutoSizeText(
    text: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    var scaledTextStyle: TextStyle by remember { mutableStateOf(textStyle) }
    var readyToDraw: Boolean by remember { mutableStateOf(false) }

    Text(
        text,
        modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        style = scaledTextStyle,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                scaledTextStyle =
                    scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.9)
            } else {
                readyToDraw = true
            }
        },
    )
}
