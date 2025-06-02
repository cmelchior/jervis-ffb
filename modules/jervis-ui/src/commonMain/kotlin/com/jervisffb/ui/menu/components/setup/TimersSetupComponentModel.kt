package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.GameLimitReachedBehaviour
import com.jervisffb.engine.OutOfTimeBehaviour
import com.jervisffb.engine.TimerPreset
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.rules.Rules
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.utils.DropdownEntryWithValue
import com.jervisffb.ui.menu.utils.InputFieldDataWithValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
    val setupActionTime: MutableStateFlow<InputFieldDataWithValue<Duration?>> = MutableStateFlow(InputFieldDataWithValue("Free Time", "", null, false))
    val setupFreeTime: MutableStateFlow<InputFieldDataWithValue<Duration>> = MutableStateFlow(InputFieldDataWithValue("Max Time", "", Duration.ZERO, false))

    val teamTurnUseBuffer = MutableStateFlow(false)
    val teamTurnActionTime: MutableStateFlow<InputFieldDataWithValue<Duration?>> = MutableStateFlow(InputFieldDataWithValue("Free Time", "", null, false))
    val teamTurnFreeTime: MutableStateFlow<InputFieldDataWithValue<Duration>> = MutableStateFlow(InputFieldDataWithValue("Max Time", "", Duration.ZERO, false))

    val responseUseBuffer = MutableStateFlow(false)
    val responseActionTime: MutableStateFlow<InputFieldDataWithValue<Duration?>> = MutableStateFlow(InputFieldDataWithValue("Free Time", "", null, false))
    val responseFreeTime: MutableStateFlow<InputFieldDataWithValue<Duration>> = MutableStateFlow(InputFieldDataWithValue("Max Time", "", Duration.ZERO, false))

    init {
        updateFromRulesBuilder(rulesBuilder)
    }

    fun updateTimersEnabled(enabled: Boolean) {
        timersEnabled.value = enabled
        rulesBuilder.timers.timersEnabled = enabled
    }

    fun updatePreset(preset: DropdownEntryWithValue<TimerPreset>) {
        selectedPresetData.value = preset
        updatePreset(preset.value)
    }

    fun updateNormalGameTimeLimit(value: String, updateUiPreset: Boolean = true) {
        updateDurationWithNullEntry(value, true, "None", "Game Limit", normalGameLimit) {
            rulesBuilder.timers.gameLimit = it
        }
        if (updateUiPreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateNormalGameBuffer(value: String, updateUiPreset: Boolean = true) {
        updateDurationEntry(value, "Game Buffer", normalGameBuffer) {
            rulesBuilder.timers.gameBuffer = it
        }
        if (updateUiPreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateOvertimeExtraLimit(value: String, updateUiPreset: Boolean = true) {
        updateDurationEntry(value,"Extra Overtime Game Time", overtimeExtraLimit) {
            rulesBuilder.timers.extraOvertimeLimit = it
        }
        if (updateUiPreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateOvertimeExtraBuffer(value: String, updateUiPreset: Boolean = true) {
        updateDurationEntry(value, "Extra Overtime Buffer", overtimeExtraBuffer) {
            rulesBuilder.timers.extraOvertimeBuffer = it
        }
        if (updateUiPreset) {
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

    fun updateSetupUseBuffer(value: Boolean, updateUiPreset: Boolean = true) {
        setupUseBuffer.value = value
        rulesBuilder.timers.setupUseBuffer = value
        updateSetupActionTime(setupActionTime.value.value, updateUiPreset)
        updateSetupFreeTime(if (value) setupFreeTime.value.value else "", updateUiPreset)
        if (updateUiPreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateSetupActionTime(value: String, updateUiPreset: Boolean = true) {
        updateDurationWithNullEntry(
            value,
            true,
            if (setupUseBuffer.value) "Buffer Limit" else "Game Limit",
            "Action Time",
            setupActionTime
        ) {
            rulesBuilder.timers.setupActionTime = it
        }
        if (updateUiPreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateSetupFreeTime(value: String, updateUiPreset: Boolean = true) {
        updateDurationEntry(
            value,
            "Free Time",
            setupFreeTime
        ) {
            rulesBuilder.timers.setupFreeTime = it
        }
        if (updateUiPreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateTeamTurnUseBuffer(value: Boolean, updateUiPreset: Boolean = true) {
        teamTurnUseBuffer.value = value
        rulesBuilder.timers.turnUseBuffer = value
        updateTeamTurnActionTime(teamTurnActionTime.value.value, updateUiPreset)
        updateTeamTurnFreeTime(teamTurnFreeTime.value.value, updateUiPreset)
        if (updateUiPreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateTeamTurnActionTime(value: String, updatePreset: Boolean = true) {
        updateDurationWithNullEntry(
            value,
            true,
            if (teamTurnUseBuffer.value) "Buffer Limit" else "Game Limit",
            "Action Time",
            teamTurnActionTime
        ) {
            rulesBuilder.timers.turnActionTime = it
        }
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateTeamTurnFreeTime(value: String, updatePreset: Boolean = true) {
        updateDurationEntry(
            value,
            "Free Time",
            teamTurnFreeTime
        ) {
            rulesBuilder.timers.turnFreeTime = it
        }
        if (updatePreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateResponseUseBuffer(value: Boolean, updateUiPreset: Boolean = true) {
        responseUseBuffer.value = value
        rulesBuilder.timers.outOfTurnResponseUseBuffer = value
        updateResponseActionTime(responseActionTime.value.value, updateUiPreset)
        updateResponseFreeTime(responseFreeTime.value.value, updateUiPreset)
        if (updateUiPreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateResponseActionTime(value: String, updateUiPreset: Boolean = true) {
        updateDurationWithNullEntry(value,
            true,
            if (responseUseBuffer.value) "Buffer Limit" else "Game Limit",
            "Action Time",
            responseActionTime
        ) {
            rulesBuilder.timers.outOfTurnResponseActionTime = it
        }
        if (updateUiPreset) {
            selectedPresetData.value = customPreset
        }
    }

    fun updateResponseFreeTime(value: String, updateUiPreset: Boolean = true) {
        updateDurationEntry(
            value,
            "Free Time",
            responseFreeTime
        ) {
            rulesBuilder.timers.outOfTurnResponseFreeTime = it
        }
        if (updateUiPreset) {
            selectedPresetData.value = customPreset
        }
    }

//    fun buildTimerSettings(): TimerSettings {
//        return TimerSettings(
//            timersEnabled = timersEnabled.value,
//
//            gameLimit = normalGameLimit.value.underlyingValue,
//            gameBuffer = normalGameBuffer.value.underlyingValue ?: Duration.ZERO,
//            extraOvertimeLimit = overtimeExtraLimit.value.underlyingValue ?: Duration.ZERO,
//            extraOvertimeBuffer =overtimeExtraBuffer.value.underlyingValue ?: Duration.ZERO,
//
//            outOfTimeBehaviour = outOfTimeLimitData.value.value,
//            gameLimitReached = gameLimitReached.value.value,
//
//            setupUseBuffer = setupUseBuffer.value,
//            setupFreeTime = setupFreeTime.value.underlyingValue ?: Duration.ZERO,
//            setupMaxTime = setupMaxTime.value.underlyingValue,
//
//            turnUseBuffer = teamTurnUseBuffer.value,
//            turnFreeTime = teamTurnFreeTime.value.underlyingValue ?: Duration.ZERO,
//            turnMaxTime = teamTurnMaxTime.value.underlyingValue,
//
//            outOfTurnResponseUseBuffer = responseUseBuffer.value,
//            outOfTurnResponseFreeTime = responseFreeTime.value.underlyingValue ?: Duration.ZERO,
//            outOfTurnResponseMaxTime = responseMaxTime.value.underlyingValue,
//        )
//    }

    private fun updatePreset(preset: TimerPreset) {
        // If already selected preset is Custom, we should probably save it, so it can be restored if
        // Custom is re-selected.
        val presetData = when (preset) {
            TimerPreset.HARD_LIMIT -> TimerSettings.HARD_LIMIT
            TimerPreset.CHESS_CLOCK -> TimerSettings.CHESS_CLOCK
            TimerPreset.BB_CLOCK -> TimerSettings.BB_CLOCK
            TimerPreset.CUSTOM -> TimerSettings()
        }
        rulesBuilder.timers = presetData.toBuilder()
        updatePreset(presetData)
    }

    private fun updatePreset(preset: TimerSettings) {
        updateNormalGameTimeLimit(preset.gameLimit?.toString() ?: "", updateUiPreset = false)
        updateNormalGameBuffer(preset.gameBuffer.toString(), updateUiPreset = false)
        updateOvertimeExtraLimit(preset.extraOvertimeLimit.toString(), updateUiPreset = false)
        updateOvertimeExtraBuffer(preset.extraOvertimeBuffer.toString(), updateUiPreset = false)

        updateOutOfTimeBehaviour(outOfTimeEntries.first { it.value == preset.outOfTimeBehaviour }, updatePreset = false)
        updateGameLimitReachedBehaviour(gameLimitEntries.first { it.value == preset.gameLimitReached }, updatePreset = false)

        updateSetupUseBuffer(preset.setupUseBuffer, updateUiPreset = false)
        updateSetupActionTime(preset.setupActionTime?.toString() ?: "", updateUiPreset = false)
        updateSetupFreeTime(preset.setupFreeTime.toString(), updateUiPreset = false)

        updateTeamTurnUseBuffer(preset.turnUseBuffer, updateUiPreset = false)
        updateTeamTurnActionTime(preset.turnActionTime?.toString() ?: "", updatePreset = false)
        updateTeamTurnFreeTime(preset.turnFreeTime.toString(), updatePreset = false)

        updateResponseUseBuffer(preset.outOfTurnResponseUseBuffer, updateUiPreset = false)
        updateResponseActionTime(preset.outOfTurnResponseActionTime?.toString() ?: "", updateUiPreset = false)
        updateResponseFreeTime(preset.outOfTurnResponseFreeTime.toString(), updateUiPreset = false)
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
        flow: MutableStateFlow<InputFieldDataWithValue<Duration?>>,
        onValueChange: (Duration?) -> Unit = { duration -> /* Do nothing */ }
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
        onValueChange(underlyingDuration)
    }

    private fun updateDurationEntry(
        value: String,
        label: String,
        flow: MutableStateFlow<InputFieldDataWithValue<Duration>>,
        onValueChange: (Duration) -> Unit = { duration ->  }
    ) {
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
        onValueChange(underlyingDuration)
    }

    private fun mapNullDuration(duration: Duration?): String {
        if (duration == null) return ""
        return duration.toString()
    }

    // Update configuration with data from the Rules.Builder
    fun updateFromRulesBuilder(rules: Rules.Builder) {
        this.rulesBuilder = rules
        // Only set the preset dialog, do not attempt to update any data based on it as we
        // want to use the timer settings from the rules builder.
        selectedPresetData.value = presets.first { it.value == rules.timers.preset }
        with(rules.timers) {
            // updateSetupUseBuffer / updateTeamTurnUseBuffer / updateResponseUseBuffer
            // Must be called after updating the action time / free time, otherwise
            // it reset these values before they have a chance to be applied.
            updateTimersEnabled(timersEnabled)
            updateNormalGameTimeLimit(gameLimit?.toString() ?: "", updateUiPreset = false)
            updateNormalGameBuffer(gameBuffer.toString(), updateUiPreset = false)
            updateOvertimeExtraLimit(extraOvertimeLimit.toString(), updateUiPreset = false)
            updateOvertimeExtraBuffer(extraOvertimeBuffer.toString(), updateUiPreset = false)
            updateOutOfTimeBehaviour(outOfTimeEntries.first { it.value == outOfTimeBehaviour }, updatePreset = false)
            updateGameLimitReachedBehaviour(gameLimitEntries.first { it.value == gameLimitReached }, updatePreset = false)
            updateSetupActionTime(setupActionTime?.toString() ?: "", updateUiPreset = false)
            updateSetupFreeTime(setupFreeTime.toString(), updateUiPreset = false)
            updateSetupUseBuffer(setupUseBuffer, updateUiPreset = false)
            updateTeamTurnActionTime(turnActionTime?.toString() ?: "", updatePreset = false)
            updateTeamTurnFreeTime(turnFreeTime.toString(), updatePreset = false)
            updateTeamTurnUseBuffer(turnUseBuffer, updateUiPreset = false)
            updateResponseActionTime(outOfTurnResponseActionTime?.toString() ?: "", updateUiPreset = false)
            updateResponseFreeTime(outOfTurnResponseFreeTime.toString(), updateUiPreset = false)
            updateResponseUseBuffer(outOfTurnResponseUseBuffer, updateUiPreset = false)
        }
    }
}
