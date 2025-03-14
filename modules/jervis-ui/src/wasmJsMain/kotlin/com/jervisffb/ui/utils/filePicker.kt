package com.jervisffb.ui.utils

import okio.Path

actual fun filePicker(
    type: FilePickerType,
    dialogTitle: String,
    selectedFile: String?,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
    onFileSelected: (Path) -> Unit,
) {
    // Do nothing for now. Figure out how to open a file dialog on the web
}
