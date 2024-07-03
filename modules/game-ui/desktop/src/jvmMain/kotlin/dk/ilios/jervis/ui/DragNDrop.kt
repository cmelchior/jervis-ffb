package dk.ilios.jervis.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication

//fun main() = singleWindowApplication {
//    var offset by remember { mutableStateOf(Offset.Zero) }
//    Box(modifier = Modifier.border(1.dp, Color.Red)) {
//        Image(
//            painter = painterResource("icons/actions/block.gif"),
//            contentDescription = "Draggable image",
//            modifier = Modifier
//                .pointerInput(Unit) {
//                    detectDragGestures { change, dragAmount ->
//                        offset += dragAmount
//                        change.consume()
//                    }
//                }
//                .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
//                .size(100.dp)
//        )
//    }
//}
//import androidx.compose.foundation.draganddrop.dragAndDropSource
//fun main() = singleWindowApplication {
//    var offset by remember { mutableStateOf(Offset(100f, 100f)) } // Set initial offset (100, 100)
//    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
//
//    Box(modifier = Modifier.pointerInput(Unit) {
//        detectDragGestures(onDragStart = {
//            // Capture the offset at the start of the drag to allow for smooth dragging
//            dragStartOffset = offset
//        }, onDrag = { change, dragAmount ->
//            val newOffset = Offset(
//                x = dragStartOffset.x + dragAmount.x,
//                y = dragStartOffset.y + dragAmount.y
//            )
//            offset = newOffset
//            change.consume()
//        })
//    }) {
//        Image(
//            painter = painterResource("icons/actions/block.gif"),
//            contentDescription = "Draggable image",
//            modifier = Modifier
//                .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
//                .size(100.dp)
//        )
//    }
//}