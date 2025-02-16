package com.jervisffb.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun RowScope.MenuBox(label: String, onClick: () -> Unit, enabled: Boolean = true, frontPage: Boolean = false) {

    var modifier = Modifier
        .padding(if (frontPage) 0.dp else 16.dp)
        .fillMaxHeight(0.9f)
        .weight(9f/36f,  false)
        .aspectRatio(1f)

    modifier = if (enabled) {
        modifier.background(color = JervisTheme.rulebookBlue).clickable { onClick() }
    } else {
        modifier.background(color = JervisTheme.rulebookDisabled)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd,

        ) {
        Text(
            modifier = Modifier.padding(16.dp),//.offset(y = 16.dp),
            text = label.uppercase(),
            textAlign = TextAlign.End,
            maxLines = 1,
            color = JervisTheme.buttonTextColor,
            fontWeight = FontWeight.Bold,
            fontSize = if (frontPage) 36.sp else 32.sp,
            style = LocalTextStyle.current.copy(
                lineHeight = 1.0.em,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Bottom,
                    trim = LineHeightStyle.Trim.LastLineBottom
                ),
            ),
        )
    }
}

@Composable
fun RowScope.SplitMenuBox(
    labelTop: String,
    onClickTop: () -> Unit,
    labelBottom: String,
    onClickBottom: () -> Unit,
    p2pHostAvaiable: Boolean,
) {

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxHeight(0.9f)
            .weight(9f/36f,  false)
            .aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier.background(color = JervisTheme.rulebookBlue).weight(1f).fillMaxSize().clickable { onClickTop() },
            contentAlignment = Alignment.BottomEnd,
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = labelTop.uppercase(),
                textAlign = TextAlign.End,
                maxLines = 2,
                color = JervisTheme.buttonTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                style = LocalTextStyle.current.copy(
                    lineHeight = 1.0.em,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Bottom,
                        trim = LineHeightStyle.Trim.LastLineBottom
                    ),
                ),
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        val bgColor = if (!p2pHostAvaiable) {
            JervisTheme.rulebookDisabled
        } else {
            JervisTheme.rulebookBlue
        }

        Box(
            modifier = Modifier.background(bgColor).weight(1f).fillMaxSize().let { if (p2pHostAvaiable) it.clickable { onClickBottom() } else it },
            contentAlignment = Alignment.BottomEnd,
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = labelBottom.uppercase(),
                textAlign = TextAlign.End,
                maxLines = 2,
                color = JervisTheme.buttonTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                style = LocalTextStyle.current.copy(
                    lineHeight = 1.0.em,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Bottom,
                        trim = LineHeightStyle.Trim.LastLineBottom
                    ),
                ),
            )
        }
    }
}

