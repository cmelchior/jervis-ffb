package com.jervisffb.ui.menu.p2p.host

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.ui.PROPERTIES_MANAGER
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.setup.SetupGameComponentModel
import com.jervisffb.utils.PROP_DEFAULT_COACH_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * View model for controlling the "Setup Game" screen, that is the first step in the "P2P Host" flow.
 */
class SetupGameScreenModel(private val menuViewModel: MenuViewModel, private val parentModel: P2PHostScreenModel) : ScreenModel {

    val setupGameScreenModel = SetupGameComponentModel(menuViewModel)

    val coachName = MutableStateFlow("")
    val gameName = MutableStateFlow("Game-${Random.nextInt(10_000)}")
    val port = MutableStateFlow<Int?>(8080)
    private val validGameMetadata = MutableStateFlow(false)
    val isSetupValid: Flow<Boolean> = combine(setupGameScreenModel.isSetupValid, validGameMetadata) {
        isSetupValid, validGameMetadata -> isSetupValid && validGameMetadata
    }

    init {
        setGameName("Game-${Random.nextInt(10_000)}")
        setPort(8080.toString())
        menuViewModel.navigatorContext.launch {
            PROPERTIES_MANAGER.getString(PROP_DEFAULT_COACH_NAME)?.let {
                updateCoachName(it)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun getCoach(): Coach? {
        val name = coachName.value
        return if (name.isNotBlank()) {
            Coach(CoachId(Uuid.random().toString()), name)
        } else {
            null
        }
    }

    fun setPort(port: String) {
        val newPort = port.toIntOrNull()
        this.port.value = newPort
        checkValidSetup()
    }

    private fun getLocalIp(): String {
        return "127.0.0.1"
    }

    private fun getPublicIp(): String {
        TODO()
    }

    fun updateCoachName(name: String) {
        coachName.value = name
        checkValidSetup()
    }

    private fun checkValidSetup() {
        var isValid = true
        isValid = isValid && gameName.value.isNotBlank()
        isValid = isValid && coachName.value.isNotBlank()
        isValid = isValid && (port.value.let {it != null && it in 1..65535 })
        validGameMetadata.value = isValid
    }

    fun setGameName(gameName: String) {
        this.gameName.value = gameName
        checkValidSetup()
    }

    fun gameSetupDone() {
        menuViewModel.navigatorContext.launch {
            PROPERTIES_MANAGER.setProperty(PROP_DEFAULT_COACH_NAME, coachName.value)
        }
        parentModel.gameSetupDone()
    }
}
