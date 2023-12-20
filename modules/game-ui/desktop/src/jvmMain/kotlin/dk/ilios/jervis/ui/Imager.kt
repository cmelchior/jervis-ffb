package dk.ilios.jervis.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.*
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.teamBuilder
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
                val team1: Team = teamBuilder {
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
                val team2: Team = teamBuilder {
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
                val state = Game(team1, team2)
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
