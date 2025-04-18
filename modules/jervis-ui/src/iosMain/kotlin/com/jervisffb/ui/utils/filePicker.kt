package com.jervisffb.ui.utils

import okio.Path

actual fun saveFile(
    dialogTitle: String,
    fileName: String,
    fileContent: String,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
) {
    // TODO
}

actual fun readFile(
    dialogTitle: String,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
    onLoad: (Path?, Result<String>) -> Unit,
) {
    // Do nothing for now.
    // Figure out how to open a file dialog on iOS
    onLoad(null, Result.success(""))
}

