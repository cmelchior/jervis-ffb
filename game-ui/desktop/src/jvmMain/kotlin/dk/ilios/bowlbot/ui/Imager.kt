package dk.ilios

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.*
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import dk.ilios.bowlbot.ui.App
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
                App()
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
