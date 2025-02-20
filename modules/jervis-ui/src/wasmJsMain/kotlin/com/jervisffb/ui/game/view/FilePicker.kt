package com.jervisffb.ui.game.view

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
