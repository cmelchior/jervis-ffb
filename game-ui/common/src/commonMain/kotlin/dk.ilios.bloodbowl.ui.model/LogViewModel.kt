package dk.ilios.bloodbowl.ui.model

import dk.ilios.bowlbot.controller.AddEntry
import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.controller.RemoveEntry
import dk.ilios.bowlbot.logs.LogCategory
import dk.ilios.bowlbot.logs.LogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LogViewModel(val controller: GameController) {
    val logStateMachine = false
    val logsCache = mutableListOf<LogEntry>()
    val logs: Flow<List<LogEntry>> = controller.logsEvents.map {
        println(it)
        when (it) {
            is AddEntry -> {
                if (it.log.category != LogCategory.STATE_MACHINE || logStateMachine) {
                    logsCache.add(it.log)
                }
            }
            is RemoveEntry -> {
                if (it.log.category != LogCategory.STATE_MACHINE || logStateMachine) {
                    if (logsCache.isNotEmpty()) {
                        logsCache.removeLast()
                    }
                }
            }
        }
        logsCache.map { it }
    }

    init {
        logsCache.addAll(controller.logs)
    }
}