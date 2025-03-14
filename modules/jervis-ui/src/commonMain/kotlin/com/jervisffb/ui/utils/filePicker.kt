package com.jervisffb.ui.utils

import okio.Path


enum class FilePickerType {
    OPEN,
    SAVE,
}

expect fun filePicker(
    type: FilePickerType,
    dialogTitle: String,
    selectedFile: String?,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
    onFileSelected: (Path) -> Unit,
)
