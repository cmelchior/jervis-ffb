package com.jervisffb.fumbbl.net.adapter

//import org.reflections.Reflections
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.model.Game
import com.jervisffb.fumbbl.net.ModelChangeProcessor
import com.jervisffb.fumbbl.net.adapter.impl.AbortActionMapper
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.utils.ReflectionUtils
import kotlin.reflect.KClass

/**
 * Map all server commands into the equivalent [JervisAction] by using all configured
 * [CommandActionMapper]s.
 */
class MapperChain constructor(jervisGame: Game, fumbblGame: FumbblGame, checkCommands: Boolean) {
    private val jervisGame: Game
    private val fumbblGame: FumbblGame
    private val checkCommands: Boolean
    private val jervisGameController = GameEngineController(jervisGame)
    private val mappers: List<CommandActionMapper>

    fun getMappersInPackage(): List<KClass<*>> {
        return ReflectionUtils.getTypesInPackage(
            packageName = "com.jervisffb.fumbbl.net.adapter.impl",
            type = CommandActionMapper::class
        )
    }

    init {
        this.jervisGame = jervisGame
        this.fumbblGame = fumbblGame
        this.checkCommands = checkCommands
        val classes = getMappersInPackage()
        val loadedMappers = classes
            .filter { it.qualifiedName?.contains("AbortActionMapper") == false }
            .filter { ReflectionUtils.isSubclassOf(it, CommandActionMapper::class) }
            .map {
                try {
                    ReflectionUtils.objectInstance(it) as CommandActionMapper
                } catch (ex: Exception) {
                    throw IllegalStateException("Failed to instantiate mapper ${it.qualifiedName}", ex)
                }
            }
        mappers = listOf(AbortActionMapper) + loadedMappers
    }

    fun process(commands: List<ServerCommandModelSync>): List<JervisActionHolder> {
        jervisGameController.startManualMode()
        val actions = mutableListOf<JervisActionHolder>()
        val processedCommands = mutableListOf<ServerCommandModelSync>()
        var i = 0
        while (i < commands.size) {
            val serverCommand: ServerCommandModelSync = commands[i]
            print("Processing CommandNr: ${serverCommand.commandNr}")

            // Map CommandModelSync changes to Jervis actions using all configured mappers
            val mapper = mappers.firstOrNull { it: CommandActionMapper ->
                it.isApplicable(fumbblGame, serverCommand, processedCommands)
            }
            println(" - Using: ${ReflectionUtils.simpleClassName(mapper) ?: "<none>" }")
            if (mapper != null) {
                val newActions = mutableListOf<JervisActionHolder>()
                mapper.mapServerCommand(fumbblGame, jervisGame, serverCommand, processedCommands, actions, newActions)
                newActions.forEach { action: JervisActionHolder ->
                    if (checkCommands && action !is OptionalJervisAction) {
                        if (jervisGameController.currentProcedure()?.currentNode() != action.expectedNode) {
                            val errorMessage = """
                                Processing CommandNr ${serverCommand.commandNr} failed.
                                Using mapper: ${ReflectionUtils.simpleClassName(mapper)}
                                Current node: ${jervisGameController.state.stack.currentNode()::class.qualifiedName}
                                Expected node: ${action.expectedNode::class.qualifiedName}
                            """.trimIndent()
                            throw IllegalStateException(errorMessage)
                        }
                    }
                    // Progress Jervis Game in cadence with the commands.
                    if (checkCommands) {
                        val jervisAction = when (action) {
                            is CalculatedJervisAction -> {
                                action.actionFunc(jervisGame, jervisGameController.rules)
                            }
                            is JervisAction -> action.action
                            is OptionalJervisAction -> if (jervisGameController.currentNode() == action.expectedNode) {
                                action.action
                            } else {
                                null
                            }
                        }
                        try {
                            if (jervisAction != null) {
                                jervisGameController.handleAction(jervisAction)
                            }
                        } catch (ex: Exception) {
                            println("Processed up to: ${serverCommand.commandNr}")
                            throw ex
                        }
                    }
                }
                actions.addAll(newActions)
            } else {
                reportNotHandled(serverCommand)
            }
            processedCommands.add(serverCommand)

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
