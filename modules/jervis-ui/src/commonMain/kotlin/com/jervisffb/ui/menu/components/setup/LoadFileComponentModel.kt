package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialize.FILE_EXTENSION_GAME_FILE
import com.jervisffb.engine.serialize.GameFileData
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.utils.FilePickerType
import com.jervisffb.ui.utils.filePicker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * View controller responsible for loading a previous game file during setting up the game.
 */
class LoadFileComponentModel(initialRulesBuilder: Rules.Builder, private val menuViewModel: MenuViewModel) : ScreenModel {

    val isSetupValid: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val fileError: MutableStateFlow<String> = MutableStateFlow("")
    val filePath: MutableStateFlow<String> = MutableStateFlow("")
    // Reference to the GameFileData
    var gameFile: GameFileData? = null

    fun openFileDialog() {
        filePicker(
            type = FilePickerType.OPEN,
            dialogTitle = "Select Save File",
            selectedFile = null,
            extensionFilterDescription = "Jervis Game Files (.${FILE_EXTENSION_GAME_FILE})",
            extensionFilterFileType = FILE_EXTENSION_GAME_FILE
        ) { path ->
            filePath.value = path.toString()
            menuViewModel.navigatorContext.launch {
                JervisSerialization.loadFromFile(path)
                    .onSuccess { gameFile ->
                        this@LoadFileComponentModel.gameFile = gameFile
                        isSetupValid.value = true
                    }
                    .onFailure { error ->
                        fileError.value = error.message ?: "Unknown error reading file."
                    }
            }
        }
    }
}
