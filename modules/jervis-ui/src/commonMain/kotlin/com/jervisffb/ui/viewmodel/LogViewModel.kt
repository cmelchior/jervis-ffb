package com.jervisffb.ui.viewmodel

import com.jervisffb.engine.AddEntry
import com.jervisffb.engine.RemoveEntry
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.ui.UiGameController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LogViewModel(val uiState: UiGameController) {
    val state = uiState.state
    val controller = uiState.controller
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
        //        logsCache.addAll(controller.state.logs.filter { it.category != LogCategory.STATE_MACHINE })
    }
}
