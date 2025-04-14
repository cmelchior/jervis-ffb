package com.jervisffb.ui.menu

import cafe.adriel.voyager.core.screen.Screen

abstract class BaseScreen() : Screen {

    protected var onBackPressed: (() -> Boolean)? = null

    fun onBackPressed(): Boolean {
        return onBackPressed?.invoke() ?: true
    }
}
