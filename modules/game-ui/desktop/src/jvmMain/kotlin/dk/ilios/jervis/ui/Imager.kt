package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.teamBuilder
import dk.ilios.jervis.utils.createRandomAction
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Data
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skiko.toBufferedImage
import org.jetbrains.skiko.toImage
import java.awt.image.BufferedImage
import java.io.File

/**
 * Easy entry point for taking snapshots. Used by Kotlin Notebooks.
 */
object Imager {

    fun appScreenshot(state: Game, width: Int, height: Int): BufferedImage {
        return renderScreenshot(width, height) {
            val actionProvider: (Game, List<ActionDescriptor>) -> Any = { state: Game, availableActions: List<ActionDescriptor> ->
                if (availableActions.first() == ContinueWhenReady) {
                    Continue
                }
            }
            val actionRequestChannel = Channel<Pair<GameController, List<ActionDescriptor>>>(capacity = 1, onBufferOverflow = BufferOverflow.SUSPEND)
            val actionSelectedChannel = Channel<GameAction>(1, onBufferOverflow = BufferOverflow.SUSPEND)
            val controller = GameController(BB2020Rules, state, actionProvider as ((GameController, List<ActionDescriptor>) -> GameAction))
            App(controller, actionRequestChannel, actionSelectedChannel)
        }
    }

    /**
     * Take a screenshot of a specific screen.
     */
    fun dummyAppScreenshot(width: Int, height: Int): BufferedImage {
        return renderScreenshot(width, height) {
            val rules = BB2020Rules
            val team1: Team = teamBuilder(HumanTeam) {
                coach = Coach("HomeCoach")
                name = "HomeTeam"
                addPlayer("Lineman-1", PlayerNo(1), HumanTeam.LINEMAN)
                addPlayer("Lineman-2", PlayerNo(2), HumanTeam.LINEMAN)
                addPlayer("Lineman-3", PlayerNo(3), HumanTeam.LINEMAN)
                addPlayer("Lineman-4", PlayerNo(4), HumanTeam.LINEMAN)
                addPlayer("Thrower-1", PlayerNo(5), HumanTeam.THROWER)
                addPlayer("Catcher-1", PlayerNo(6), HumanTeam.CATCHER)
                addPlayer("Catcher-2", PlayerNo(7), HumanTeam.CATCHER)
                addPlayer("Blitzer-1", PlayerNo(8), HumanTeam.BLITZER)
                addPlayer("Blitzer-2", PlayerNo(9), HumanTeam.BLITZER)
                addPlayer("Blitzer-3", PlayerNo(10), HumanTeam.BLITZER)
                addPlayer("Blitzer-4", PlayerNo(11), HumanTeam.BLITZER)
                reRolls = 4
                apothecary = true
            }
            val team2: Team = teamBuilder(HumanTeam) {
                coach = Coach("AwayCoach")
                name = "AwayTeam"
                addPlayer("Lineman-1", PlayerNo(1), HumanTeam.LINEMAN)
                addPlayer("Lineman-2", PlayerNo(2), HumanTeam.LINEMAN)
                addPlayer("Lineman-3", PlayerNo(3), HumanTeam.LINEMAN)
                addPlayer("Lineman-4", PlayerNo(4), HumanTeam.LINEMAN)
                addPlayer("Thrower-1", PlayerNo(5), HumanTeam.THROWER)
                addPlayer("Catcher-1", PlayerNo(6), HumanTeam.CATCHER)
                addPlayer("Catcher-2", PlayerNo(7), HumanTeam.CATCHER)
                addPlayer("Blitzer-1", PlayerNo(8), HumanTeam.BLITZER)
                addPlayer("Blitzer-2", PlayerNo(9), HumanTeam.BLITZER)
                addPlayer("Blitzer-3", PlayerNo(10), HumanTeam.BLITZER)
                addPlayer("Blitzer-4", PlayerNo(11), HumanTeam.BLITZER)
                reRolls = 4
                apothecary = true
            }
            val field = dk.ilios.jervis.model.Field.createForRuleset(rules)
            val state = Game(team1, team2, field)
            val actionRequestChannel = Channel<Pair<GameController, List<ActionDescriptor>>>(capacity = 1, onBufferOverflow = BufferOverflow.SUSPEND)
            val actionSelectedChannel = Channel<GameAction>(1, onBufferOverflow = BufferOverflow.SUSPEND)
            val actionProvider = { controller: GameController, availableActions: List<ActionDescriptor> ->
                createRandomAction(state, availableActions)
            }
            val controller = GameController(rules, state, actionProvider)
            App(controller, actionRequestChannel, actionSelectedChannel)
        }
    }

    /**
     * Generic render function. Unfortunately it does not look it is possible to use
     * Composables directly from Kotlin Notebook, so instead we need to add a helper
     * method for each use case.
     */
    @OptIn(ExperimentalTestApi::class)
    private fun renderScreenshot(width: Int, height: Int, renderView: @Composable () -> Unit): BufferedImage {
        lateinit var image: BufferedImage
        runDesktopComposeUiTest(width, height) {
            setContent {
                renderView()
            }
            val screenshot: Bitmap = this.captureToImage().asSkiaBitmap()
            image = screenshot.toBufferedImage()
        }
        return image
    }

    /**
     * Optionally save the screenshot to a file
     */
    fun saveScreenshot(image: BufferedImage, name: String): File {
        val file = File.createTempFile(name, ".png")
        val img: Data? = image.toImage().encodeToData(EncodedImageFormat.PNG)
        file.outputStream().use { stream ->
            stream.write(img!!.bytes)
        }
        return file
    }
}
