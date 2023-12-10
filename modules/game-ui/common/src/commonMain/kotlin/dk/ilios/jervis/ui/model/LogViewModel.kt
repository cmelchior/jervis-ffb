package dk.ilios.jervis.ui.model

import dk.ilios.jervis.controller.AddEntry
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.controller.RemoveEntry
import dk.ilios.jervis.logs.LogCategory
import dk.ilios.jervis.logs.LogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LogViewModel(val controller: GameController) {
    val logStateMachine = true
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