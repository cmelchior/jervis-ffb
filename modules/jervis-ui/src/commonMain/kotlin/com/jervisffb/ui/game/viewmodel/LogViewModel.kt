package com.jervisffb.ui.game.viewmodel

import com.jervisffb.engine.AddEntry
import com.jervisffb.engine.RemoveEntry
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LogViewModel(val uiState: UiGameController) {

    companion object {
        val LOG = jervisLogger()
    }

    val showDebugLogs: Boolean = true
    val state = uiState.state
    val controller = uiState.gameRunner.controller
    val logsCache = mutableListOf<LogEntry>()
    val debugLogsCache = mutableListOf<LogEntry>()
    val debugLogs: Flow<List<LogEntry>> =
        controller.logsEvents.map {
            when (it) {
                is AddEntry -> {
                    if (it.log.category == LogCategory.STATE_MACHINE) {
                        debugLogsCache.add(it.log)
                    }
                }
                is RemoveEntry -> {
                    if (it.log.category != LogCategory.STATE_MACHINE) {
                        if (debugLogsCache.isNotEmpty()) {
                            debugLogsCache.removeLast()
                        }
                    }
                }
            }
            debugLogsCache.map { it }
        }
    val logs: Flow<List<LogEntry>> =
        controller.logsEvents.map {
            LOG.d { it.toString() }
            when (it) {
                is AddEntry -> {
                    if (it.log.category != LogCategory.STATE_MACHINE) {
                        logsCache.add(it.log)
                    }
                }
                is RemoveEntry -> {
                    if (it.log.category != LogCategory.STATE_MACHINE) {
                        if (logsCache.isNotEmpty()) {
                            logsCache.removeLast()
                        }
                    }
                }
            }
            logsCache.map { it }
        }
}
