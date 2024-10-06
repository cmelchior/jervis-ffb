package com.jervisffb.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okio.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// These functions cannot be in the same file as an expect function,
// it seems to break the compiler

fun <T> runBlocking(block: suspend CoroutineScope.() -> T): T {
    return runBlocking(Dispatchers.Unconfined, block)
}

fun Path.readText(): String {
    return platformFileSystem.read(this) { readUtf8() }
}

fun Path.writeText(text: String) {
    platformFileSystem.write(this) { writeUtf8(text) }
}

val Path.isDirectory get() = (platformFileSystem.metadataOrNull(this)?.isDirectory == true)
val Path.isRegularFile get() = (platformFileSystem.metadataOrNull(this)?.isRegularFile == true)

@OptIn(ExperimentalContracts::class)
inline fun <reified T: Any> Any.checkType(): T {
    contract { returns() implies (this@checkType is T) }
    return this as T
}

