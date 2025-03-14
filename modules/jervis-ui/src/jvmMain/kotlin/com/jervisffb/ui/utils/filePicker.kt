package com.jervisffb.ui.utils

import okio.Path
import okio.Path.Companion.toPath
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileView

actual fun filePicker(
    type: FilePickerType,
    dialogTitle: String,
    selectedFile: String?,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
    onFileSelected: (Path) -> Unit,
) {
    val fileChooser = JFileChooser()
    val fileFilter = FileNameExtensionFilter(extensionFilterDescription, extensionFilterFileType)
    fileChooser.approveButtonText = "Select"
    fileChooser.dialogTitle = dialogTitle
    if (selectedFile != null) {
        fileChooser.selectedFile = File(selectedFile)
    }
    fileChooser.isMultiSelectionEnabled = false
    fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
    fileChooser.isFileHidingEnabled = true
    fileChooser.addChoosableFileFilter(fileFilter)
    fileChooser.setFileView(object : FileView() {
        override fun isTraversable(file: File): Boolean = file.isDirectory()
        override fun getName(file: File): String? {
            if (fileFilter.accept(file)) {
                return file.getName() // Show name only for matching files
            }
            return null // Hide non-matching files
        }
    })
    fileChooser.isAcceptAllFileFilterUsed = false
    val userSelection = when (type) {
        FilePickerType.OPEN -> fileChooser.showOpenDialog(null)
        FilePickerType.SAVE -> fileChooser.showSaveDialog(null)
    }
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        val fileToSave = fileChooser.selectedFile
        onFileSelected(fileToSave.absolutePath.toString().toPath())
    }
}
