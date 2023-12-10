package dk.ilios.jervis.logs

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
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
