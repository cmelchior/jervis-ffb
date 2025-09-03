package com.jervisffb.ui.menu.p2p.host

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.CoachType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.coach.CoachSetupComponentModel
import com.jervisffb.ui.menu.components.setup.GameConfigurationContainerComponentModel
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

    val gameSetupModel = GameConfigurationContainerComponentModel(menuViewModel)
    val coachSetupModel = CoachSetupComponentModel(menuViewModel) {
        checkValidSetup()
    }

    val gameName = MutableStateFlow("Game-${Random.nextInt(10_000)}")
    val port = MutableStateFlow<Int?>(8080)

    private val validGameMetadata = MutableStateFlow(false)
    val isSetupValid: Flow<Boolean> = gameSetupModel.isSetupValid
        .combine(validGameMetadata) { gameSetupValid, gameMetadataValid ->
            gameSetupValid && gameMetadataValid
        }

    init {
        setGameName("Game-${Random.nextInt(10_000)}")
        setPort(8080.toString())
        menuViewModel.navigatorContext.launch {
            SETTINGS_MANAGER.getStringOrNull(SettingsKeys.JERVIS_DEFAULT_HOST_COACH_NAME)?.let {
                coachSetupModel.updateCoachName(it)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun getCoach(): Coach? {
        val name = getCoachName()
        return if (name.isNotBlank()) {
            Coach(CoachId(Uuid.random().toString()), name, getCoachType())
        } else {
            null
        }
    }

    fun setPort(port: String) {
        val newPort = port.toIntOrNull()
        this.port.value = newPort
        checkValidSetup()
    }

    private fun checkValidSetup() {
        var isValid = true
        isValid = isValid && gameName.value.isNotBlank()
        isValid = isValid && getCoachName().isNotBlank()
        isValid = isValid && (port.value.let {it != null && it in 1..65535 })
        validGameMetadata.value = isValid
    }

    fun setGameName(gameName: String) {
        this.gameName.value = gameName
        checkValidSetup()
    }

    fun gameSetupDone() {
        menuViewModel.navigatorContext.launch {
            SETTINGS_MANAGER.set(SettingsKeys.JERVIS_DEFAULT_HOST_COACH_NAME, getCoachName())
        }
        parentModel.userAcceptedGameSetup()
    }

    private fun getCoachName(): String = coachSetupModel.coachName.value

    private fun getCoachType(): CoachType = coachSetupModel.coachType.value

    fun createRules(): Rules {
        return gameSetupModel.createRules()
    }
}
