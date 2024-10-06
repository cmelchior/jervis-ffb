package com.jervisffb.ui

import okio.Path

actual fun filePicker(
    dialogTitle: String,
    selectedFile: String?,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
    onFileSelected: (Path) -> Unit,
) {
    // Do nothing
}
