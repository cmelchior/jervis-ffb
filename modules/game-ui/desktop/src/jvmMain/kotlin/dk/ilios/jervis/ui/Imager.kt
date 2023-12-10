package dk.ilios.jervis.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.*
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.utils.createRandomAction
import org.jetbrains.skia.*
import java.io.File

@Composable
fun Test() {
    Text("Hello")
}

object Imager {
    @OptIn(ExperimentalTestApi::class)
    fun renderScreenshot(width: Int, height: Int): File {
        val file = File("test-screenshot.png")
        runDesktopComposeUiTest(width, height) {
            setContent {
                val rules = BB2020Rules
                val p1 = Player()
                val p2 = Player()
                val state = Game(p1, p1)
                val actionProvider = { state: Game, availableActions: List<ActionDescriptor> ->
                    createRandomAction(state, availableActions)
                }
                val controller = GameController(rules, state, actionProvider)
                App(controller)
            }
            val screenshot: Bitmap = this.captureToImage().asSkiaBitmap()
            val img: Data? = Image.makeFromBitmap(screenshot).encodeToData(EncodedImageFormat.PNG)
            file.outputStream().use { stream ->
                stream.write(img!!.bytes)
            }
        }
        return file
    }
}



//fun saveScreenshot(screenshot: Bitmap, file: File) {
//    val pngData: Data = screenshot.encodeToData(EncodedImageFormat.PNG)
//    val pngBytes: ByteBuffer = pngData.toByteBuffer()
//
//    try {
//        val path: Path = Path.of("output.png")
//        val channel: ByteChannel = Files.newByteChannel(
//            path,
//            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
//        )
//        channel.write(pngBytes.toByteBuffer())
//        channel.close()
//    } catch (e: IOException) {
//        println(e)
//    }
//}
