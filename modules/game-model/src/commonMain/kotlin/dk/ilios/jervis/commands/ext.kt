import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.CompositeCommand

fun compositeCommandOf(vararg commands: Command?): Command {
    return CompositeCommand.create {
        commands.forEach {
            it?.let { add(it) }
        }
    }
}
