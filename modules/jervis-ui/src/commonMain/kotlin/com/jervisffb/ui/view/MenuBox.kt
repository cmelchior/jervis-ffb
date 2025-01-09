package com.jervisffb.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun RowScope.MenuBox(label: String, onClick: () -> Unit, enabled: Boolean = true, frontPage: Boolean = false) {

    var modifier = Modifier
        .padding(if (frontPage) 24.dp else 18.dp)
        .fillMaxHeight()
        .weight(1f,  false)
        .aspectRatio(1f)

    modifier = if (enabled) {
        modifier.background(color = Color.Red).clickable { onClick() }
    } else {
        modifier.background(color = Color.Gray)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd,

        ) {
        Text(
            modifier = Modifier.padding(16.dp).offset(y = 16.dp),
            text = label.uppercase(),
            textAlign = TextAlign.End,
            maxLines = 1,
            color = JervisTheme.buttonTextColor,
            fontSize = if (frontPage) 48.sp else 40.sp,
            fontFamily = JervisTheme.fontFamily(),
            style = LocalTextStyle.current.copy(
                lineHeight = 0.9.em,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Bottom,
                    trim = LineHeightStyle.Trim.LastLineBottom
                ),
            ),
        )
    }
}
