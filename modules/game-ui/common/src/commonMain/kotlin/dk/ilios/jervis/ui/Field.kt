package dk.ilios.jervis.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dk.ilios.jervis.model.FieldSquare
import dk.ilios.jervis.ui.images.IconFactory
import dk.ilios.jervis.ui.model.UiFieldSquare
import dk.ilios.jervis.ui.viewmodel.FieldDetails
import dk.ilios.jervis.ui.viewmodel.FieldViewModel
import dk.ilios.jervis.ui.viewmodel.Square

@Composable
fun Field(vm: FieldViewModel, modifier: Modifier) {
    val field: FieldDetails by vm.field().collectAsState()
    val highlightedSquare: Square? by vm.highlights().collectAsState()

    Box(modifier = modifier
        .fillMaxSize()
        .aspectRatio(vm.aspectRatio)
    ) {
        Image(
            painter = painterResource(field.resource),
            contentDescription = field.description,
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.TopStart)
        )
        Column(modifier = Modifier
            .fillMaxSize()
        ) {
            repeat(vm.height) { height: Int ->
                Row(modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                ) {
                    repeat(vm.width) { width ->
                        val boxModifier = Modifier.fillMaxSize().weight(1f)
                        FieldSquare(boxModifier, highlightedSquare, width, height, vm)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FieldSquare(
    boxModifier: Modifier,
    highlightedSquare: Square?,
    width: Int,
    height: Int,
    vm: FieldViewModel
) {
    val hover: Boolean = Square(width, height) == highlightedSquare
    val squareFlow = remember(width, height) { vm.observeSquare(width, height) }
    val square by squareFlow.collectAsState(initial = UiFieldSquare(FieldSquare(-1, -1)))
    val bgColor = when {
        hover -> Color.Cyan.copy(alpha = 0.25f)
        square.onSelected != null -> Color.Green.copy(alpha = 0.25f)
        else -> Color.Transparent
    }
    val boxWrapperModifier = boxModifier
        .fillMaxSize()
        .background(color = bgColor)
        .onPointerEvent(PointerEventType.Enter) {
            vm.hoverOver(Square(width, height))
        }
        .clickable {
            square.onSelected?.let {
                it()
            }
        }

    Box(modifier = boxWrapperModifier) {
        square.player?.let {
            Player(boxModifier, it)
        }
        square.ball?.let {
            Image(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                alignment = Alignment.Center,
                contentScale = ContentScale.FillBounds,
                bitmap = IconFactory.getBall().toComposeImageBitmap(),
                contentDescription = ""
            )
        }
    }
}
