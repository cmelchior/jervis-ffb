package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialize.FILE_EXTENSION_GAME_FILE
import com.jervisffb.engine.serialize.GameFileData
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.utils.readFile
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
        menuViewModel.navigatorContext.launch {
            readFile(
                extensionFilterDescription = "Jervis Game Files (.${FILE_EXTENSION_GAME_FILE})",
                extensionFilterFileType = FILE_EXTENSION_GAME_FILE
            ) { path, loadResult ->
                println(path)
                println(loadResult)
                fileError.value = ""
                filePath.value = if (path != null) path.toString() else ""
                loadResult
                    .onSuccess { fileContent ->
                        // Just silently ignore `null` values as it indicates the dialog was closed
                        if (path != null) {
                            JervisSerialization.loadFromFileContent(fileContent)
                                .onSuccess { gameFile ->
                                    this@LoadFileComponentModel.gameFile = gameFile
                                    isSetupValid.value = true
                                }
                                .onFailure { error ->
                                    fileError.value = "Error reading file - ${error.message}"
                                }
                        }
                    }
                    .onFailure { error ->
                        fileError.value = "Error reading file - ${error.message}"
                    }
            }
        }
    }
}
