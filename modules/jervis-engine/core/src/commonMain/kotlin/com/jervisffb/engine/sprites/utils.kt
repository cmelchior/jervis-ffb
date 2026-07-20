package com.jervisffb.engine.sprites

fun normalizeFumbblIconPath(path: String): String {
    var relativePath = path
    // All fumbbl icons are in /i/*, but it looks like some of the REST APIs only return the id and not the
    // full path.
    relativePath = if (relativePath.startsWith("/")) relativePath.removeSuffix("/") else relativePath
    if (!relativePath.startsWith("i/")) {
        relativePath = "i/$relativePath"
    }
    return relativePath
}
