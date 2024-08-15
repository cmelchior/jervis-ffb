package dk.ilios.jervis.fumbbl.adapter

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fumbbl.ModelChangeProcessor
import dk.ilios.jervis.fumbbl.adapter.impl.AbortActionMapper
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.BB2020Rules
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Map all server commands into the equivalent [JervisAction] by using all configured
 * [CommandActionMapper]s.
 */
class MapperChain(private val jervisGame: Game, private val fumbblGame: FumbblGame, private val checkCommands: Boolean = false) {

    private val jervisGameController = GameController(BB2020Rules, jervisGame)
    private val mappers: List<CommandActionMapper>

    fun getMappersInPackage(): List<KClass<*>> {
        val reflections = Reflections("dk.ilios.jervis.fumbbl.adapter.impl")
        return reflections.getSubTypesOf(CommandActionMapper::class.java).map { it.kotlin }.toList()
    }

    init {
        val classes = getMappersInPackage()
        val loadedMappers = classes
            .filter { it.qualifiedName?.contains("AbortActionMapper") == false }
            .filter { it.isSubclassOf(CommandActionMapper::class) }
            .map {
                try {
                    it.objectInstance as CommandActionMapper
                } catch (ex: Exception) {
                    throw IllegalStateException("Failed to instantiate mapper ${it.qualifiedName}", ex)
                }
            }
        mappers = listOf(AbortActionMapper) + loadedMappers
    }

    fun process(commands: List<ServerCommandModelSync>): List<JervisActionHolder> {
        jervisGameController.startManualMode()
        val actions = mutableListOf<JervisActionHolder>()
        val processedCommands = mutableSetOf<ServerCommandModelSync>()
        var i = 0
        while (i < commands.size) {
            val serverCommand: ServerCommandModelSync = commands[i]

            // Map CommandModelSync changes to Jervis actions using all configured mappers
            val mapper = mappers.firstOrNull { it: CommandActionMapper ->
                it.isApplicable(fumbblGame, serverCommand, processedCommands)
            }

            if (mapper != null) {
                val newActions = mutableListOf<JervisActionHolder>()
                mapper.mapServerCommand(fumbblGame, jervisGame, serverCommand, processedCommands, actions, newActions)
                newActions.forEach { action: JervisActionHolder ->
                    if (checkCommands) {
                        if (jervisGameController.currentProcedure()?.currentNode() != action.expectedNode) {
                            val serverCommandIndex = i
                            val errorMessage = """
                                Processing index $serverCommandIndex failed.
                                Current node: ${jervisGameController.stack.currentNode()::class.qualifiedName}
                                Expected node: ${action.expectedNode::class.qualifiedName}
                            """.trimIndent()
                            throw IllegalStateException(errorMessage)
                        }
                    }
                    // Progress Jervis Game in cadence with the commands.
                    if (checkCommands) {
                        val jervisAction = when(action) {
                            is CalculatedJervisAction -> {
                                action.actionFunc(jervisGame, jervisGameController.rules)
                            }
                            is JervisAction -> action.action
                        }
                        jervisGameController.processAction(jervisAction)
                    }
                }
                actions.addAll(newActions)
            } else {
                reportNotHandled(serverCommand)
            }

            // Then update the FUMBBL State model
            serverCommand.modelChangeList.forEach {
                if (!ModelChangeProcessor.apply(fumbblGame, it)) {
                    throw IllegalStateException("Failed at: $it")
                }
            }
            i += 1
        }

        return actions
    }

    private fun reportNotHandled(command: ServerCommandModelSync): List<JervisActionHolder> {
        println("Not handling: $command")
        return emptyList()
    }
}
