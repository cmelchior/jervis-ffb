package com.jervisffb.ui.view

import okio.Path

expect fun filePicker(
    dialogTitle: String,
    selectedFile: String?,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
    onFileSelected: (Path) -> Unit,
)
