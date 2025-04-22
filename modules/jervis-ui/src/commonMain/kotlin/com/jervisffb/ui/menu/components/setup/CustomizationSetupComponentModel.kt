package com.jervisffb.ui.menu.components.setup

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.FoulActionBehavior
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.utils.DropdownEntryWithValue
import com.jervisffb.ui.menu.utils.InputFieldDataWithValue
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * This component model is responsible for all the UI control needed to configure the the more
 * advanced customization options under the "Customizations" tab.
 */
class CustomizationSetupComponentModel(initialRulesBuilder: Rules.Builder, private val menuViewModel: MenuViewModel) : ScreenModel {

    var rulesBuilder = initialRulesBuilder
    val isSetupValid: MutableStateFlow<Boolean> = MutableStateFlow(true)

    val diceRollEntries = listOf(
        DropdownEntryWithValue("Roll on Server", DiceRollOwner.ROLL_ON_SERVER, true),
        DropdownEntryWithValue("Roll/Select on Client", DiceRollOwner.ROLL_ON_CLIENT, true),
    )

    val undoActionsEntries = listOf(
        DropdownEntryWithValue("None", UndoActionBehavior.NOT_ALLOWED, true),
        DropdownEntryWithValue("Only Non-Random Actions", UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS, true),
        DropdownEntryWithValue("All", UndoActionBehavior.ALLOWED, true),
    )

    val foulActionBehavior = listOf(
        DropdownEntryWithValue("Strict", FoulActionBehavior.STRICT, true),
        DropdownEntryWithValue("FUMBBL-compatible", FoulActionBehavior.FUMBBL, true),
    )

    val kickingPlayerBehavior = listOf(
        DropdownEntryWithValue("Strict", KickingPlayerBehavior.STRICT, true),
        DropdownEntryWithValue("FUMBBL-compatible", KickingPlayerBehavior.FUMBBL, true),
    )

    val intMatcher = Regex("^\\d*$")


    val fieldWidth = MutableStateFlow(InputFieldDataWithValue("Field Width", rulesBuilder.fieldWidth.toString(), rulesBuilder.fieldWidth, isError = false))
    val fieldHeight = MutableStateFlow(InputFieldDataWithValue("Field Height", rulesBuilder.fieldHeight.toString(), rulesBuilder.fieldHeight, isError = false))
    val endZone = MutableStateFlow(InputFieldDataWithValue("Endzone", rulesBuilder.endZone.toString(), rulesBuilder.endZone, isError = false))
    val lineOfScrimmageHome = MutableStateFlow(InputFieldDataWithValue("Line of Scrimmage (Home)", rulesBuilder.lineOfScrimmageHome.toString(), rulesBuilder.lineOfScrimmageHome, isError = false))
    val lineOfScrimmageAway = MutableStateFlow(InputFieldDataWithValue("Line of Scrimmage (Away)", rulesBuilder.lineOfScrimmageAway.toString(), rulesBuilder.lineOfScrimmageAway, isError = false))
    val wideZone = MutableStateFlow(InputFieldDataWithValue("Wide Zone", rulesBuilder.wideZone.toString(), rulesBuilder.wideZone, isError = false))
    val maxPlayersOnField = MutableStateFlow(InputFieldDataWithValue("Max Players On Field", rulesBuilder.maxPlayersOnField.toString(), rulesBuilder.maxPlayersOnField, isError = false))
    val maxPlayersInWideZone = MutableStateFlow(InputFieldDataWithValue("Max Players In Wide Zone", rulesBuilder.maxPlayersInWideZone.toString(), rulesBuilder.maxPlayersInWideZone, isError = false))
    val halfs = MutableStateFlow(InputFieldDataWithValue("Halfs Pr. Game", rulesBuilder.halfsPrGame.toString(), rulesBuilder.halfsPrGame, isError = false))
    val turnsPrHalf = MutableStateFlow(InputFieldDataWithValue("Turns Pr. Half", rulesBuilder.turnsPrHalf.toString(), rulesBuilder.turnsPrHalf, isError = false))
    val selectedDiceRollBehavior = MutableStateFlow(diceRollEntries.first { it.value == DiceRollOwner.ROLL_ON_SERVER })
    val selectedUndoActionBehavior = MutableStateFlow(undoActionsEntries.first { it.value == UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS })
    val selectedFoulActionBehavior = MutableStateFlow(foulActionBehavior.first { it.value == FoulActionBehavior.STRICT })
    val selectedKickingPlayerBehavior = MutableStateFlow(kickingPlayerBehavior.first { it.value == KickingPlayerBehavior.STRICT })

    fun updateFieldWidth(value: String) {
        updateIntEntry(value, fieldWidth)
        rulesBuilder.fieldWidth = fieldWidth.value.underlyingValue ?: -1
    }

    fun updateFieldHeight(value: String) {
        updateIntEntry(value, fieldHeight)
        rulesBuilder.fieldHeight = fieldHeight.value.underlyingValue ?: -1
    }

    fun updateEndZone(value: String) {
        updateIntEntry(value, endZone)
        rulesBuilder.endZone = endZone.value.underlyingValue ?: -1
    }

    fun updateLineOfScrimmageHome(value: String) {
        updateIntEntry(value, lineOfScrimmageHome)
        rulesBuilder.lineOfScrimmageHome = lineOfScrimmageHome.value.underlyingValue ?: -1
    }

    fun updateLineOfScrimmageAway(value: String) {
        updateIntEntry(value, lineOfScrimmageAway)
        rulesBuilder.lineOfScrimmageAway = lineOfScrimmageAway.value.underlyingValue ?: -1
    }

    fun updateWideZone(value: String) {
        updateIntEntry(value, wideZone)
        rulesBuilder.wideZone = wideZone.value.underlyingValue ?: -1
    }

    fun updateMaxPlayersOnField(value: String) {
        updateIntEntry(value, maxPlayersOnField)
        maxPlayersOnField.value.underlyingValue?.let {
            rulesBuilder.maxPlayersOnField = it
        }
    }

    fun updateMaxPlayersInWideZone(value: String) {
        updateIntEntry(value, maxPlayersInWideZone)
        maxPlayersInWideZone.value.underlyingValue?.let {
            rulesBuilder.maxPlayersInWideZone = it
        }
    }

    fun updateHalfs(value: String) {
        updateIntEntry(value, halfs)
        halfs.value.underlyingValue?.let {
            rulesBuilder.halfsPrGame = it
        }
    }

    fun updateTurnsPrHalf(value: String) {
        updateIntEntry(value, turnsPrHalf)
        turnsPrHalf.value.underlyingValue?.let {
            rulesBuilder.turnsPrHalf = it
        }
    }

    fun updateDiceRollBehavior(value: DropdownEntryWithValue<DiceRollOwner>) {
        selectedDiceRollBehavior.value = value
        rulesBuilder.diceRollsOwner = value.value
    }

    fun updateUndoActionBehavior(value: DropdownEntryWithValue<UndoActionBehavior>) {
        selectedUndoActionBehavior.value = value
        rulesBuilder.undoActionBehavior = value.value
    }

    fun updateFoulActionBehavior(it: DropdownEntryWithValue<FoulActionBehavior>) {
        selectedFoulActionBehavior.value = it
        rulesBuilder.foulActionBehavior = it.value
    }

    fun updateKickingPlayerBehavior(it: DropdownEntryWithValue<KickingPlayerBehavior>) {
        selectedKickingPlayerBehavior.value = it
        rulesBuilder.kickingPlayerBehavior = it.value
    }

    private fun updateIntEntry(value: String, flow: MutableStateFlow<InputFieldDataWithValue<Int>>) {
        if (!value.trim().matches(intMatcher)) return // Only allow Numbers
        val underlyingValue = value.trim().toIntOrNull()
        val isError = (underlyingValue == null)
        val data = InputFieldDataWithValue(
            label = flow.value.label,
            value = value,
            underlyingValue = underlyingValue,
            isError = isError
        )
        flow.value = data
        isSetupValid.value = !data.isError
    }

    fun updateRulesBuilder(rulesBuilder: Rules.Builder) {
        this.rulesBuilder = rulesBuilder

        updateFieldWidth(rulesBuilder.fieldWidth.toString())
        updateFieldHeight(rulesBuilder.fieldHeight.toString())
        updateMaxPlayersOnField(rulesBuilder.maxPlayersOnField.toString())

        updateHalfs(rulesBuilder.halfsPrGame.toString())
        updateTurnsPrHalf(rulesBuilder.turnsPrHalf.toString())

        updateDiceRollBehavior(diceRollEntries.first { it.value == rulesBuilder.diceRollsOwner })

        updateUndoActionBehavior(undoActionsEntries.first { it.value == rulesBuilder.undoActionBehavior })
        updateFoulActionBehavior(foulActionBehavior.first { it.value == rulesBuilder.foulActionBehavior })
        updateKickingPlayerBehavior(kickingPlayerBehavior.first { it.value == rulesBuilder.kickingPlayerBehavior })
    }
}
