package com.jervisffb.ui.ext

import com.jervisffb.ui.userinput.WaitingForUserInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.scan

fun <T : Any?> MutableSharedFlow<T>.collectUntilWait(): Flow<List<T>> =
    this.scan<T, List<T>>(emptyList()) { list, element ->
        if (element == WaitingForUserInput) {
            emptyList()
        } else {
            list + element
        }
    }
