package com.jervisffb.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.utils.runBlocking
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.staticCFunction
import platform.Foundation.NSSetUncaughtExceptionHandler
import platform.Foundation.NSUncaughtExceptionHandler
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
fun MainViewController(): UIViewController {
    runBlocking {
        initApplication()
    }
    return ComposeUIViewController {
        // See https://medium.com/@mohaberabi98/global-exception-handling-in-compose-multiplatform-with-1dfc069ffd5cs
        setUnhandledExceptionHook { exception ->
            IssueTracker.saveUncaughtException(exception)
            terminateWithUnhandledException(exception)
        }
        handleNSUncaughtException()
        val menuViewModel = MenuViewModel()
        App(menuViewModel)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun handleNSUncaughtException() {
    val handler: CPointer<NSUncaughtExceptionHandler> = staticCFunction { nsException ->
        val cause = Throwable(nsException?.reason)
        val throwable = Throwable(message = nsException?.name, cause)
        IssueTracker.saveUncaughtException(throwable)
    }
    NSSetUncaughtExceptionHandler(handler)
}
