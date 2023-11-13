package dk.ilios.bowlbot.logs

import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.model.Game
import kotlin.random.Random

abstract class LogEntry: Command {
    private val id: Long = Random.nextLong()
    abstract val category: LogCategory
    abstract val message: String

    override fun execute(state: Game, controller: GameController) {
        controller.addLog(this)
    }

    override fun undo(state: Game, controller: GameController) {
        controller.removeLog(this)
    }

    override fun toString(): String {
        return "${this::class.simpleName}(id=$id, category=$category, message='$message')"
    }


}
