package com.jervisffb.ui.utils

import com.jervisffb.engine.serialize.FILE_EXTENSION_GAME_FILE
import okio.Path


enum class FilePickerType {
    OPEN,
    SAVE,
}

expect fun saveFile(
    dialogTitle: String,
    fileName: String,
    fileContent: String,
    extensionFilterDescription: String = "Jervis Game Files (.${FILE_EXTENSION_GAME_FILE})",
    extensionFilterFileType: String = FILE_EXTENSION_GAME_FILE
)

// Only support loading string files for now
expect fun readFile(
    dialogTitle: String = "Select Save File",
    extensionFilterDescription: String = "Jervis Game Files (.${FILE_EXTENSION_GAME_FILE})",
    extensionFilterFileType: String = FILE_EXTENSION_GAME_FILE,
    // If `null` file load was canceled by the user
    onLoad: (Path?, Result<String>) -> Unit,
)
