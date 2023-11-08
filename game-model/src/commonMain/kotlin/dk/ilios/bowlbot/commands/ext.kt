import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.CompositeCommand

fun compositeActionOf(vararg commands: Command): Command {
    return CompositeCommand.create {
        commands.forEach {
            add(it)
        }
    }
}
