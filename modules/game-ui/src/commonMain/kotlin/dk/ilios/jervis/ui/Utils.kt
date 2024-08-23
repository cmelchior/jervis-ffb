package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import dk.ilios.bloodbowl.ui.game_ui.generated.resources.Res
import dk.ilios.jervis.utils.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.skia.Image

@Composable
fun painterResource(resource: String): Painter {
    return runBlocking {
        val image = Res.loadImage(resource)
        BitmapPainter(image)
    }
}

@OptIn(ExperimentalResourceApi::class)
internal fun Res.loadImage(path: String): ImageBitmap {
    return runBlocking {
        Image.makeFromEncoded(readBytes("drawable/$path")).toComposeImageBitmap()
    }
}

fun ImageBitmap.getSubImage(x: Int, y: Int, width: Int, height: Int): ImageBitmap {
    val newImageBitmap = ImageBitmap(width, height)
    val canvas = Canvas(newImageBitmap)
    canvas.drawImageRect(
        image = this,
        srcOffset = IntOffset(x, y),
        srcSize = IntSize(width, height),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(width, height),
        paint = Paint() // .apply { colorFilter = ColorFilter.tint(androidx.compose.ui.graphics.Color.Unspecified) },
    )

    return newImageBitmap
}
