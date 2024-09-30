package dk.ilios.jervis

import androidx.compose.runtime.Composable
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.StandardBB2020Rules
import dk.ilios.jervis.ui.App
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import dk.ilios.jervis.utils.createDefaultGameState
import dk.ilios.jervis.utils.createRandomAction
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skiko.toImage
import java.awt.image.BufferedImage
import java.io.File

/**
 * Easy entry point for taking snapshots. Used by Kotlin Notebooks.
 */
object Imager {
    fun appScreenshot(
        state: Game,
        width: Int,
        height: Int,
    ): BufferedImage {
        return renderScreenshot(width, height) {
            val actionProvider: (
                Game,
                List<ActionDescriptor>,
            ) -> Any = { state: Game, availableActions: List<ActionDescriptor> ->
                if (availableActions.first() == ContinueWhenReady) {
                    Continue
                }
            }
            val actionRequestChannel =
                Channel<Pair<GameController, List<ActionDescriptor>>>(
                    capacity = 1,
                    onBufferOverflow = BufferOverflow.SUSPEND,
                )
            val actionSelectedChannel = Channel<GameAction>(1, onBufferOverflow = BufferOverflow.SUSPEND)
//            val controller = GameController(BB2020Rules, state, actionProvider as ((GameController, List<ActionDescriptor>) -> GameAction))
            val controller = GameController(StandardBB2020Rules, state)
            App(MenuViewModel()) // controller, actionRequestChannel, actionSelectedChannel)
        }
    }

    /**
     * Take a screenshot of a specific screen.
     */
    fun dummyAppScreenshot(
        width: Int,
        height: Int,
    ): BufferedImage {
        return renderScreenshot(width, height) {
            val rules = StandardBB2020Rules
            val state = createDefaultGameState(rules)
            val actionRequestChannel =
                Channel<Pair<GameController, List<ActionDescriptor>>>(
                    capacity = 1,
                    onBufferOverflow = BufferOverflow.SUSPEND,
                )
            val actionSelectedChannel = Channel<GameAction>(1, onBufferOverflow = BufferOverflow.SUSPEND)
            val actionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
                createRandomAction(state, availableActions)
            }
//            val controller = GameController(rules, state, actionProvider)
            val controller = GameController(rules, state)
            App(MenuViewModel()) // controller, actionRequestChannel, actionSelectedChannel)
        }
    }

    /**
     * Generic render function. Unfortunately it does not look it is possible to use
     * Composables directly from Kotlin Notebook, so instead we need to add a helper
     * method for each use case.
     */
    @OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
    private fun renderScreenshot(
        width: Int,
        height: Int,
        renderView: @Composable () -> Unit,
    ): BufferedImage {
        TODO()
//        lateinit var image: BufferedImage
//        runDesktopComposeUiTest(width, height) {
//            androidx.compose.ui.test.SkikoComposeUiTest.setContent {
//                renderView()
//            }
//            val screenshot: Bitmap = this.captureToImage().asSkiaBitmap()
//            image = screenshot.toBufferedImage()
//        }
//        return image
    }

    /**
     * Optionally save the screenshot to a file
     */
    fun saveScreenshot(
        image: BufferedImage,
        name: String,
    ): File {
        val file = File.createTempFile(name, ".png")
        val img: Data? = image.toImage().encodeToData(EncodedImageFormat.PNG)
        file.outputStream().use { stream ->
            stream.write(img!!.bytes)
        }
        return file
    }
}
