package dk.ilios.jervis.ui

import okio.Path
import okio.Path.Companion.toPath
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual fun FilePicker(
    dialogTitle: String,
    selectedFile: String?,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
    onFileSelected: (Path) -> Unit
) {
    val fileChooser = JFileChooser()
    fileChooser.dialogTitle = dialogTitle
    if (selectedFile != null) {
        fileChooser.selectedFile = File(selectedFile)
    }
    fileChooser.fileFilter = FileNameExtensionFilter(extensionFilterDescription, extensionFilterFileType)
    val userSelection = fileChooser.showSaveDialog(null)
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        val fileToSave = fileChooser.selectedFile
        onFileSelected(fileToSave.absolutePath.toString().toPath())
    }
}
