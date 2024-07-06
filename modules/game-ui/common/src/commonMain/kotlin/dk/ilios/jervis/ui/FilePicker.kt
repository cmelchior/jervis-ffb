package dk.ilios.jervis.ui

import okio.Path

expect fun FilePicker(
    dialogTitle: String,
    selectedFile: String?,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
    onFileSelected: (Path) -> Unit
)
