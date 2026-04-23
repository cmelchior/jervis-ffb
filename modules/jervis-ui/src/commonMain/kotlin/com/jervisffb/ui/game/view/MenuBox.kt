package com.jervisffb.ui.game.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.utils.jdp

@Composable
fun RowScope.MenuBox(label: String, onClick: () -> Unit, enabled: Boolean = true) {

    var modifier = Modifier
        .fillMaxHeight(0.9f)
        .weight(9f/36f,  false)
        .aspectRatio(1f)

    modifier = if (enabled) {
        modifier.background(color = JervisTheme.rulebookBlue).clickable { onClick() }
    } else {
        modifier.background(color = JervisTheme.rulebookDisabled)
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd,

    ) {
        // Same scaling as the frontpage boxes, which is slightly smaller and
        // uses a 0.1 factor.
        val fontSize = ((maxWidth.value) * 0.08722f).sp
        Text(
            modifier = Modifier.padding(16.jdp),
            text = label.uppercase(),
            textAlign = TextAlign.End,
            maxLines = 1,
            color = JervisTheme.buttonTextColor,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RowScope.
    SplitMenuBox(
        labelTop: String,
        onClickTop: (() -> Unit)?,
        labelMiddle: String,
        onClickMiddle: () -> Unit,
        labelBottom: String? = null,
        onClickBottom: (() -> Unit)? = null,
        p2pHostAvailable: Boolean,
        aspectRatio: Float = 0.67f,
    ) {

    val spaceBetweenButtons = 32.jdp
    // Same scaling as Frontpage and "Hotseat" box. Adjusted for a slightly
    // different box size.
    // We should probably calculate this dynamically from the difference in width,
    // but this is a good enough approximation for now.
    val fontScaleFactor = 0.13015f

    var hostLabel by remember { mutableStateOf(labelMiddle) }

    Column(
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .weight(9f/36f,  false)
            .aspectRatio(1f)
    ) {
        Column(modifier = Modifier.aspectRatio(aspectRatio).fillMaxSize()) {
            val topBgColor = if (onClickTop == null) {
                JervisTheme.rulebookDisabled
            } else {
                JervisTheme.rulebookBlue
            }
            BoxWithConstraints(
                modifier = Modifier.background(color = topBgColor).weight(1f).fillMaxSize().let { if (onClickTop != null) it.clickable { onClickTop() } else it },
                contentAlignment = Alignment.BottomEnd,
            ) {
                val fontSize = (maxWidth * fontScaleFactor).value.sp
                Text(
                    modifier = Modifier.padding(16.jdp),
                    text = labelTop.uppercase(),
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    color = JervisTheme.buttonTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    style = LocalTextStyle.current.copy(
                        lineHeight = 1.0.em,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Bottom,
                            trim = LineHeightStyle.Trim.LastLineBottom
                        ),
                    ),
                )
            }
            Spacer(modifier = Modifier.height(spaceBetweenButtons))

            val bgColor = if (!p2pHostAvailable) {
                JervisTheme.rulebookDisabled
            } else {
                JervisTheme.rulebookBlue
            }

            BoxWithConstraints(
                modifier = Modifier
                    .background(bgColor)
                    .weight(1f)
                    .fillMaxSize()
                    .onPointerEvent(PointerEventType.Enter) {
                        if (!p2pHostAvailable) {
                            hostLabel = "Requires\nDesktop/iOS Client"
                        }
                    }
                    .onPointerEvent(PointerEventType.Exit) {
                        if (!p2pHostAvailable) {
                            hostLabel = labelMiddle
                        }
                    }
                    .let { if (p2pHostAvailable) it.clickable { onClickMiddle() } else it }
                ,
                contentAlignment = Alignment.BottomEnd,
            ) {
                val fontSize = (maxWidth * fontScaleFactor).value.sp
                Text(
                    modifier = Modifier.padding(16.jdp),
                    text = hostLabel.uppercase(),
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    color = JervisTheme.buttonTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    style = LocalTextStyle.current.copy(
                        lineHeight = 1.0.em,
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Bottom,
                            trim = LineHeightStyle.Trim.LastLineBottom
                        ),
                    ),
                )
            }

            if (labelBottom != null) {
                val bgColor = if (onClickBottom == null) {
                    JervisTheme.rulebookDisabled
                } else {
                    JervisTheme.rulebookBlue
                }
                Spacer(modifier = Modifier.height(spaceBetweenButtons))
                BoxWithConstraints(
                    modifier = Modifier.background(bgColor).weight(1f).fillMaxSize().let { if (onClickBottom != null) it.clickable { onClickBottom() } else it },
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    val fontSize = (maxWidth * fontScaleFactor).value.sp
                    Text(
                        modifier = Modifier.padding(16.jdp),
                        text = labelBottom.uppercase(),
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        color = JervisTheme.buttonTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
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
    }
}

