package com.jervisffb

import androidx.compose.runtime.Composable
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.App
import com.jervisffb.ui.viewmodel.MenuViewModel
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
                List<GameActionDescriptor>,
            ) -> Any = { state: Game, availableActions: List<GameActionDescriptor> ->
                if (availableActions.first() == ContinueWhenReady) {
                    Continue
                }
            }
            val actionRequestChannel =
                Channel<Pair<GameEngineController, List<GameActionDescriptor>>>(
                    capacity = 1,
                    onBufferOverflow = BufferOverflow.SUSPEND,
                )
            val actionSelectedChannel = Channel<GameAction>(1, onBufferOverflow = BufferOverflow.SUSPEND)
//            val controller = GameController(BB2020Rules, state, actionProvider as ((GameController, List<ActionDescriptor>) -> GameAction))
            val controller = GameEngineController(state)
            App(MenuViewModel()) // controller, actionRequestChannel, actionSelectedChannel)
        }
    }

//    /**
//     * Take a screenshot of a specific screen.
//     */
//    fun dummyAppScreenshot(
//        width: Int,
//        height: Int,
//    ): BufferedImage {
//        return renderScreenshot(width, height) {
//            val rules = StandardBB2020Rules()
//            val state = createDefaultGameState(rules)
//            val actionRequestChannel =
//                Channel<Pair<GameEngineController, List<GameActionDescriptor>>>(
//                    capacity = 1,
//                    onBufferOverflow = BufferOverflow.SUSPEND,
//                )
//            val actionSelectedChannel = Channel<GameAction>(1, onBufferOverflow = BufferOverflow.SUSPEND)
//            val actionProvider = { controller: GameEngineController, availableActions: List<GameActionDescriptor> ->
//                createRandomAction(state, availableActions)
//            }
////            val controller = GameController(rules, state, actionProvider)
//            val controller = GameEngineController(state)
//            App(MenuViewModel()) // controller, actionRequestChannel, actionSelectedChannel)
//        }
//    }

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
