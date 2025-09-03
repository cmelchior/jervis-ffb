package com.jervisffb.ui.menu.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.jervisffb.ui.dropShadow
import com.jervisffb.ui.game.dialogs.DialogSize
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.bannerBackground
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.viewmodel.FieldViewData
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.utils.applyIf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonNull.content
import kotlin.math.roundToInt

/**
 * This file contains Composables for creating Jervis themed dialogs
 */

/**
 * Customizable Jervis dialog with title, icon, icon and button row.
 *
 * As a default, this dialog does not close when pressing Escape. Override
 * [onDismissRequest] to enable this behavior.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun JervisDialog(
    title: String,
    icon: @Composable () -> Unit = { },
    width: Dp = DialogSize.MEDIUM,
    minHeight: Dp = 230.dp,
    draggable: Boolean = false,
    backgroundScrim: Boolean = false,
    // If set, will be used to center the popup over the field
    centerOnField: GameScreenModel? = null,
    // If `centerOnField` is `null`. This alignment is used instead.
    fallbackAlignment: Alignment = Alignment.Center,
    dialogColor: Color = JervisTheme.rulebookRed,
    content: @Composable ColumnScope.(@Composable (String) -> TextFieldColors, Color) -> Unit = { _, _  -> /* Do nothing */  },
    buttons: (@Composable RowScope.() -> Unit)? = null,
    onDismissRequest: () -> Unit = { /* Ignore ESC as dismiss */ },
) {
    var popupSize by remember { mutableStateOf(IntSize.Zero) }
    val fieldViewInfo: FieldViewData? by centerOnField?.fieldViewData?.collectAsState() ?: MutableStateFlow<FieldViewData?>(null).collectAsState()
    var popupOffset by remember(fieldViewInfo) {
        fieldViewInfo?.let { fvd ->
            mutableStateOf(
                IntOffset(
                    x = ((fvd.size.width - popupSize.width) / 2f).roundToInt() + fvd.offset.x,
                    y = ((fvd.size.height - popupSize.height) / 2f).roundToInt() + fvd.offset.y,
                )
            )
        } ?: mutableStateOf(IntOffset.Zero)
    }
    // Keep popup hidden until first layout pass
    var alpha by remember { mutableStateOf(0f) }
    // Needed to make room for the drop shadow
    val dialogPadding = 8.dp
    // Needed to adjust center of dialog
    val paddingPx = with(LocalDensity.current) { dialogPadding.roundToPx() }

    val textColor = JervisTheme.contentTextColor
    val buttonTextColor = JervisTheme.white
    val inputDialogColors: @Composable (String) -> TextFieldColors = { text: String ->
        TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = textColor,
            focusedLabelColor = dialogColor,
            focusedIndicatorColor = dialogColor,
            unfocusedLabelColor = if (text.isEmpty()) textColor.copy(alpha = 0.4f) else textColor,
            unfocusedIndicatorColor = textColor,
        )
    }
    // Background box for showing a Scrim and tracking the size of the window
    // so we can accurately position the popup in the center.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .applyIf(backgroundScrim) {
                background(color = Color.Black.copy(alpha = 0.5f))
            }
    )
    Popup(
        alignment = if (centerOnField == null) fallbackAlignment else Alignment.TopStart,
        offset = popupOffset,
        onDismissRequest = {
            onDismissRequest()
        },
        properties = PopupProperties(
            focusable = true,
            clippingEnabled = true // Prevents being dragged outside the window bounds
        )
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .dropShadow(JervisTheme.black.copy(0.5f), 0.dp, offsetY = 0.dp, blurRadius = 8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .alpha(alpha)
                    .heightIn(min = minHeight)
                    .width(width)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (draggable) {
                                popupOffset = IntOffset(
                                    x = (popupOffset.x + dragAmount.x).roundToInt(),
                                    y = (popupOffset.y + dragAmount.y).roundToInt()
                                )
                            }
                        }
                    }
                    .onSizeChanged {
                        popupSize = it
                        popupOffset = recalculateOffset(fieldViewInfo, popupSize, paddingPx)
                        alpha = 1f
                    }
                    .defaultMinSize(minHeight = 200.dp, minWidth = width)
                    .paperBackground(color = JervisTheme.rulebookPaper),
                shape = RectangleShape,
                border = BorderStroke(8.dp, color = dialogColor),
                color = JervisTheme.rulebookPaper,
                contentColor = textColor,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .paperBackground(JervisTheme.rulebookPaper)
                ) {
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .fillMaxHeight()
                            .padding(start = 24.dp)
                            .bannerBackground(bannerColor = dialogColor)
                    ) {
                        icon()
                    }
                    Column(
                        modifier = Modifier
                            .padding(start = 24.dp, top = 20.dp, end = 32.dp, bottom = 28.dp)
                            .fillMaxSize()
                    ) {
                        JervisDialogContent(
                            title = title,
                            dialogColor = dialogColor,
                            textColor = textColor,
                            inputDialogColors = inputDialogColors,
                            content = { content(this, inputDialogColors, textColor) },
                            buttons = buttons,
                        )
                    }
                }
            }
        }
    }
}

fun recalculateOffset(fieldViewInfo: FieldViewData?, popupSize: IntSize, paddingPx: Int): IntOffset {
    return fieldViewInfo?.let { fvd ->
        IntOffset(
            x = ((fvd.size.width - popupSize.width) / 2f).roundToInt() + fvd.offset.x - 2*paddingPx,
            y = ((fvd.size.height - popupSize.height) / 2f).roundToInt() + fvd.offset.y - 2*paddingPx,
        )
    } ?: IntOffset.Zero
}

/**
 * Dialog showing that something is still missing to be implemented
 */
@Composable
fun NotImplementYetDialog(title: String, onDismissRequest: () -> Unit) {
    JervisDialog(
        title,
        icon = { /* No icon yet */ },
        width = 650.dp,
        content = { _, textColor ->
            Text(
                text = "Not implemented yet",
                color = textColor
            )
        },
        buttons = {
            Spacer(modifier = Modifier.weight(1f))
            JervisButton(
                text = "Close",
                onClick = { onDismissRequest() },
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor
            )
        },
        onDismissRequest = onDismissRequest,
    )
}

@Composable
private fun ColumnScope.JervisDialogContent(
    title: String,
    dialogColor: Color,
    textColor: Color,
    inputDialogColors: @Composable (String) -> TextFieldColors,
    content: @Composable ColumnScope.() -> Unit = { /* Do nothing */ },
    buttons: (@Composable RowScope.() -> Unit)? = null,
) {
    JervisDialogHeader(title, dialogColor)
    TitleBorder(dialogColor)
    Column(modifier = Modifier.weight(1f).fillMaxSize().padding(top = 8.dp)) {
        content()
    }
    if (buttons != null) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            buttons()
        }
    }
}

@Composable
fun JervisDialogHeader(title: String, dialogColor: Color) {
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = title.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = dialogColor
        )
    }
}
