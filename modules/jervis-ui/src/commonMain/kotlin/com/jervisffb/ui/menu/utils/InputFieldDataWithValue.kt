package com.jervisffb.ui.menu.utils

data class InputFieldDataWithValue<T>(
    val label: String,
    val value: String,
    val underlyingValue: T?,
    val isError: Boolean
)
