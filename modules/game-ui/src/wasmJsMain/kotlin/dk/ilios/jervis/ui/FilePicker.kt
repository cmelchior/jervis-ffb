package dk.ilios.jervis.ui

import okio.Path

actual fun filePicker(
    dialogTitle: String,
    selectedFile: String?,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
    onFileSelected: (Path) -> Unit,
) {
    TODO() // How to do a file picker in Wasm web?
}
