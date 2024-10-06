package com.jervisffb.ui.viewmodel

import com.jervisffb.engine.controller.AddEntry
import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.controller.RemoveEntry
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LogViewModel(val controller: GameController) {
    val logStateMachine = false
    val logsCache = mutableListOf<LogEntry>()
    val logs: Flow<List<LogEntry>> =
        controller.logsEvents.map {
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
