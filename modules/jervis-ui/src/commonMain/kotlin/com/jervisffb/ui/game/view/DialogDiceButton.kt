package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.ui.game.icons.DiceColor
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.utils.D6Shape
import com.jervisffb.ui.game.view.utils.D8Shape
import com.jervisffb.ui.utils.applyIf

/**
 * This file contains [DialogDiceButton] which is responsible for rendering dice
 * buttons in [com.jervisffb.ui.menu.components.JervisDialog] popups, but not
 * the Action Wheel.
 */

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DialogDiceButton(
    modifier: Modifier = Modifier,
    die: DieResult,
    isSelected: Boolean,
    onClick: () -> Unit,
    useSelectedColorAsHover: Boolean = false,
) {
    val buttonSize = IconFactory.getDiceSizeDp(die)
    Box(
        modifier = modifier.size(buttonSize),
        contentAlignment = Alignment.Center
    ) {
        var hover: Boolean by remember { mutableStateOf(false) }
        var colorFilter by remember { mutableStateOf<ColorFilter?>(null) }
        val bitmap = if (isSelected || (useSelectedColorAsHover && hover)) {
            val color = when (die) {
                is D3Result,
                is D6Result -> DiceColor.YELLOW
                else -> DiceColor.DEFAULT
            }
            IconFactory.getDiceIcon(die, color)
        } else {
            val color = when (die) {
                is D3Result,
                is D6Result -> DiceColor.BROWN
                else -> DiceColor.DEFAULT
            }
            IconFactory.getDiceIcon(die, color)
        }
        Image(
            bitmap = bitmap,
            contentDescription = die.value.toString(),
            modifier = Modifier.fillMaxSize()
                .applyIf(die is D6Result) {
                    clip(D6Shape)
                }
                .applyIf(die is D8Result) {
                    clip(D8Shape)
                }
                .applyIf(true) {
                    this
                        .onPointerEvent(PointerEventType.Enter) {
                            hover = true
                            if (!useSelectedColorAsHover) {
                                colorFilter = ColorFilter.tint(JervisTheme.black.copy(0.1f), BlendMode.Darken)
                            }
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            hover = false
                            if (!useSelectedColorAsHover) {
                                colorFilter = null
                            }
                        }
                        .onPointerEvent(PointerEventType.Press) {
                            onClick()
                        }
                }
            ,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
            colorFilter = colorFilter,
        )
    }
}
