package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.GameLimitReachedBehaviour
import com.jervisffb.engine.OutOfTimeBehaviour
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.rules.Rules
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.utils.DropdownEntryWithValue
import com.jervisffb.ui.menu.utils.InputFieldDataWithValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class TimerPreset {
    HARD_LIMIT,
    CHESS_CLOCK,
    BB_CLOCK,
    CUSTOM,
}

val presets = listOf(
    DropdownEntryWithValue("Hard Limit", TimerPreset.HARD_LIMIT),
    DropdownEntryWithValue("Chess Clock", TimerPreset.CHESS_CLOCK),
    DropdownEntryWithValue("BB Clock", TimerPreset.BB_CLOCK),
    DropdownEntryWithValue("Custom", TimerPreset.CUSTOM, false),
)

val outOfTimeEntries = listOf(
    DropdownEntryWithValue("None", OutOfTimeBehaviour.NONE),
    DropdownEntryWithValue("Show Warning", OutOfTimeBehaviour.SHOW_WARNING),
    DropdownEntryWithValue("Opponent Can Call Timeout", OutOfTimeBehaviour.OPPONENT_CALL_TIMEOUT),
    DropdownEntryWithValue("Automatic Timeout", OutOfTimeBehaviour.AUTOMATIC_TIMEOUT),
)

val gameLimitEntries = listOf(
    DropdownEntryWithValue("None", GameLimitReachedBehaviour.NONE),
    DropdownEntryWithValue("Only Roll over / Stand Up", GameLimitReachedBehaviour.ROLL_OVER_STAND_UP),
    DropdownEntryWithValue("End New Turn Immediately", GameLimitReachedBehaviour.AUTOMATIC_END_TURN),
    DropdownEntryWithValue("Forfeit Game", GameLimitReachedBehaviour.FORFEIT_GAME),
)

/**
 * View controller for the timers setup component. This component is responsible for all the UI control needed
 * to configure the timer settings for a game.
 */
class SetupTimersComponentModel(initialRulesBuilder: Rules.Builder, private val menuViewModel: MenuViewModel) : ScreenModel {

    var rulesBuilder = initialRulesBuilder
    val isSetupValid: MutableStateFlow<Boolean> = MutableStateFlow(true)

    // Backing data (used to create timer setting)
    val customPreset = presets.first { it.value == TimerPreset.CUSTOM }
    val selectedPresetData = MutableStateFlow(presets.first { it.value == TimerPreset.BB_CLOCK })
    val outOfTimeLimitData = MutableStateFlow(outOfTimeEntries.first())
    val gameLimitReachedData = MutableStateFlow(gameLimitEntries.first())

    // UI Data
    val timersEnabled = MutableStateFlow(false)
    val selectedPreset: StateFlow<DropdownEntryWithValue<TimerPreset>> = selectedPresetData

    val normalGameLimit: MutableStateFlow<InputFieldDataWithValue<Duration?>> = MutableStateFlow(InputFieldDataWithValue("Game Time", "", null, false))
    val normalGameBuffer: MutableStateFlow<InputFieldDataWithValue<Duration>> = MutableStateFlow(InputFieldDataWithValue("Game Buffer", "", Duration.ZERO, false))
    val overtimeExtraLimit: MutableStateFlow<InputFieldDataWithValue<Duration>> = MutableStateFlow(InputFieldDataWithValue("Extra Overtime Time", "", Duration.ZERO, false))
    val overtimeExtraBuffer: MutableStateFlow<InputFieldDataWithValue<Duration>> = MutableStateFlow(InputFieldDataWithValue("Extra Overtime Buffer", "", Duration.ZERO, false))

    val outOfTimeLimit: StateFlow<DropdownEntryWithValue<OutOfTimeBehaviour>> = outOfTimeLimitData
    val gameLimitReached: StateFlow<DropdownEntryWithValue<GameLimitReachedBehaviour>> = gameLimitReachedData

    val setupUseBuffer = MutableStateFlow(false)
    val setupFreeTime: MutableStateFlow<InputFieldDataWithValue<Duration>> = MutableStateFlow(InputFieldDataWithValue("Free Time", "", Duration.ZERO, false))
    val setupMaxTime: MutableStateFlow<InputFieldDataWithValue<Duration?>> = MutableStateFlow(InputFieldDataWithValue("Max Time", "", null, false))

    val teamTurnUseBuffer = MutableStateFlow(false)
    val teamTurnFreeTime: MutableStateFlow<InputFieldDataWithValue<Duration>> = MutableStateFlow(InputFieldDataWithValue("Free Time", "", Duration.ZERO, false))
    val teamTurnMaxTime: MutableStateFlow<InputFieldDataWithValue<Duration?>> = MutableStateFlow(InputFieldDataWithValue("Max Time", "", null, false))

    val responseUseBuffer = MutableStateFlow(false)
    val responseFreeTime: MutableStateFlow<InputFieldDataWithValue<Duration>> = MutableStateFlow(InputFieldDataWithValue("Free Time", "", Duration.ZERO, false))
    val responseMaxTime: MutableStateFlow<InputFieldDataWithValue<Duration?>> = MutableStateFlow(InputFieldDataWithValue("Max Time", "", null, false))

    init {
        updatePreset(selectedPreset.value)
    }

    fun updateTimersEnabled(enabled: Boolean) {
        timersEnabled.value = enabled
    }

    fun updatePreset(preset: DropdownEntryWithValue<TimerPreset>) {
        selectedPresetData.value = preset
        updatePreset(preset.value)
    }

    fun updateNormalGameTimeLimit(value: String, updatePreset: Boolean = true) {
        updateDurationWithNullEntry(value, true, "None", "Game Limit", normalGameLimit)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateNormalGameBuffer(value: String, updatePreset: Boolean = true) {
        updateDurationEntry(value, "Game Buffer", normalGameBuffer)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateOvertimeExtraLimit(value: String, updatePreset: Boolean = true) {
        updateDurationEntry(value,"Extra Overtime Game Time", overtimeExtraLimit)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateOvertimeExtraBuffer(value: String, updatePreset: Boolean = true) {
        updateDurationEntry(value, "Extra Overtime Buffer", overtimeExtraBuffer)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateGameLimitReachedBehaviour(behaviour: DropdownEntryWithValue<GameLimitReachedBehaviour>, updatePreset: Boolean = true) {
        gameLimitReachedData.value = behaviour
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateOutOfTimeBehaviour(behaviour: DropdownEntryWithValue<OutOfTimeBehaviour>, updatePreset: Boolean = true) {
        outOfTimeLimitData.value = behaviour
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateSetupUseBuffer(value: Boolean, updatePreset: Boolean = true) {
        setupUseBuffer.value = value
        updateSetupMaxTime(if (value) setupMaxTime.value.value else "", updatePreset)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateSetupFreeTime(value: String, updatePreset: Boolean = true) {
        updateDurationEntry(value, "Free Time", setupFreeTime)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateSetupMaxTime(value: String, updatePreset: Boolean = true) {
        updateDurationWithNullEntry(value, setupUseBuffer.value, "Game Limit", "Max Time", setupMaxTime)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateTeamTurnUseBuffer(value: Boolean, updatePreset: Boolean = true) {
        teamTurnUseBuffer.value = value
        updateTeamTurnMaxTime(teamTurnMaxTime.value.value, updatePreset)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateTeamTurnFreeTime(value: String, updatePreset: Boolean = true) {
        updateDurationEntry(value, "Free Time", teamTurnFreeTime)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateTeamTurnMaxTime(value: String, updatePreset: Boolean = true) {
        updateDurationWithNullEntry(value, teamTurnUseBuffer.value, "Game Limit", "Max Time", teamTurnMaxTime)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateResponseUseBuffer(value: Boolean, updatePreset: Boolean = true) {
        responseUseBuffer.value = value
        updateResponseMaxTime(responseMaxTime.value.value, updatePreset)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateResponseFreeTime(value: String, updatePreset: Boolean = true) {
        updateDurationEntry(value, "Free Time", responseFreeTime)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateResponseMaxTime(value: String, updatePreset: Boolean = true) {
        updateDurationWithNullEntry(value, responseUseBuffer.value, "Game Limit", "Max Time", responseMaxTime)
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun buildTimerSettings(): TimerSettings {
        return TimerSettings(
            timersEnabled = timersEnabled.value,

            gameLimit = normalGameLimit.value.underlyingValue,
            gameBuffer = normalGameBuffer.value.underlyingValue ?: Duration.ZERO,
            extraOvertimeLimit = overtimeExtraLimit.value.underlyingValue ?: Duration.ZERO,
            extraOvertimeBuffer =overtimeExtraBuffer.value.underlyingValue ?: Duration.ZERO,

            outOfTimeBehaviour = outOfTimeLimitData.value.value,
            gameLimitReached = gameLimitReached.value.value,

            setupUseBuffer = setupUseBuffer.value,
            setupFreeTime = setupFreeTime.value.underlyingValue ?: Duration.ZERO,
            setupMaxTime = setupMaxTime.value.underlyingValue,

            turnUseBuffer = teamTurnUseBuffer.value,
            turnFreeTime = teamTurnFreeTime.value.underlyingValue ?: Duration.ZERO,
            turnMaxTime = teamTurnMaxTime.value.underlyingValue,

            outOfTurnResponseUseBuffer = responseUseBuffer.value,
            outOfTurnResponseFreeTime = responseFreeTime.value.underlyingValue ?: Duration.ZERO,
            outOfTurnResponseMaxTime = responseMaxTime.value.underlyingValue,
        )
    }

    private fun updatePreset(preset: TimerPreset) {
        // If already selected preset is Custom, we should probably save it, so it can be restored if
        // Custom is re-selected.
        val presetData = when (preset) {
            TimerPreset.HARD_LIMIT -> TimerSettings.HARD_LIMIT
            TimerPreset.CHESS_CLOCK -> TimerSettings.CHESS_CLOCK
            TimerPreset.BB_CLOCK -> TimerSettings.BB_CLOCK
            TimerPreset.CUSTOM -> TimerSettings()
        }
        updatePreset(presetData)
    }

    private fun updatePreset(preset: TimerSettings) {
        updateNormalGameTimeLimit(preset.gameLimit?.toString() ?: "", updatePreset = false)
        updateNormalGameBuffer(preset.gameBuffer.toString(), updatePreset = false)
        updateOvertimeExtraLimit(preset.extraOvertimeLimit.toString(), updatePreset = false)
        updateOvertimeExtraBuffer(preset.extraOvertimeBuffer.toString(), updatePreset = false)

        updateOutOfTimeBehaviour(outOfTimeEntries.first { it.value == preset.outOfTimeBehaviour }, updatePreset = false)
        updateGameLimitReachedBehaviour(gameLimitEntries.first { it.value == preset.gameLimitReached }, updatePreset = false)

        updateSetupUseBuffer(preset.setupUseBuffer, updatePreset = false)
        updateSetupFreeTime(preset.setupFreeTime.toString(), updatePreset = false)
        updateSetupMaxTime(preset.setupMaxTime?.toString() ?: "", updatePreset = false)

        updateTeamTurnUseBuffer(preset.turnUseBuffer, updatePreset = false)
        updateTeamTurnFreeTime(preset.turnFreeTime.toString(), updatePreset = false)
        updateTeamTurnMaxTime(preset.turnMaxTime?.toString() ?: "", updatePreset = false)

        updateResponseUseBuffer(preset.outOfTurnResponseUseBuffer, updatePreset = false)
        updateResponseFreeTime(preset.outOfTurnResponseFreeTime.toString(), updatePreset = false)
        updateResponseMaxTime(preset.outOfTurnResponseMaxTime?.toString() ?: "", updatePreset = false)
    }

    private fun normalizeDurationString(value: String): String {
        return value.trim()
    }

    private fun parseDuration(value: String): Result<Duration?> {
        if (value.isBlank()) return Result.success(null)
        val updatedValue = if (!value.endsWith("s", ignoreCase = true) && !value.endsWith("m", ignoreCase = true)) {
            "${value}s"
        } else {
            value
        }

        return try {
            Result.success(Duration.parse(updatedValue))
        } catch (ex: IllegalArgumentException) {
            Result.failure(ex)
        }
    }

    private fun updateDurationWithNullEntry(
        value: String,
        enabled: Boolean,
        nullDescription: String,
        label: String,
        flow: MutableStateFlow<InputFieldDataWithValue<Duration?>>
    ) {
        val normalizedValue: String = normalizeDurationString(value)
        val duration = parseDuration(normalizedValue)
        val underlyingDuration = duration.getOrNull()?.inWholeSeconds?.seconds

        val labelDescription = if (duration.isFailure) {
            "Unknown"
        } else {
            when {
                !enabled -> "N/A"
                underlyingDuration == null -> nullDescription
                else -> mapNullDuration(underlyingDuration)

            }
        }
        val labelWithValue = "$label ($labelDescription)"

        val result = InputFieldDataWithValue<Duration?>(
            label = labelWithValue,
            value = value,
            underlyingValue = underlyingDuration,
            isError = duration.isFailure
        )
        flow.value = result
        isSetupValid.value = !result.isError
    }

    private fun updateDurationEntry(value: String, label: String, flow: MutableStateFlow<InputFieldDataWithValue<Duration>>) {
        val normalizedValue: String = normalizeDurationString(value)
        val duration = parseDuration(normalizedValue)
        val underlyingDuration = duration.getOrNull()?.inWholeSeconds?.seconds ?: Duration.ZERO

        val labelDescription = if (duration.isFailure) {
            "Unknown"
        } else {
            mapNullDuration(underlyingDuration)
        }
        val labelWithValue = "$label ($labelDescription)"

        val result = InputFieldDataWithValue(
            label = labelWithValue,
            value = value,
            underlyingValue = underlyingDuration,
            isError = duration.isFailure
        )
        flow.value = result
        isSetupValid.value = !result.isError
    }

    private fun mapNullDuration(duration: Duration?): String {
        if (duration == null) return ""
        return duration.toString()
    }

    fun updateRulesBuilder(ruleBuilder: Rules.Builder) {
        this.rulesBuilder = ruleBuilder
        TODO()
    }
}
