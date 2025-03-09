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
    val maxPlayersOnField = MutableStateFlow(InputFieldDataWithValue("Max Players On Field", rulesBuilder.maxPlayersOnField.toString(), rulesBuilder.maxPlayersOnField, isError = false))
    val halfs = MutableStateFlow(InputFieldDataWithValue("Halfs Pr. Game", rulesBuilder.halfsPrGame.toString(), rulesBuilder.halfsPrGame, isError = false))
    val turnsPrHalf = MutableStateFlow(InputFieldDataWithValue("Turns Pr. Half", rulesBuilder.turnsPrHalf.toString(), rulesBuilder.turnsPrHalf, isError = false))
    val selectedDiceRollBehavior = MutableStateFlow(diceRollEntries.first { it.value == DiceRollOwner.ROLL_ON_SERVER })
    val selectedUndoActionBehavior = MutableStateFlow(undoActionsEntries.first { it.value == UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS })
    val selectedFoulActionBehavior = MutableStateFlow(foulActionBehavior.first { it.value == FoulActionBehavior.STRICT })
    val selectedKickingPlayerBehavior = MutableStateFlow(kickingPlayerBehavior.first { it.value == KickingPlayerBehavior.STRICT })

    fun updateFieldWidth(value: String) {
        updateIntEntry(value, fieldWidth)
    }

    fun updateFieldHeight(value: String) {
        updateIntEntry(value, fieldHeight)
    }

    fun updateMaxPlayersOnField(value: String) {
        updateIntEntry(value, maxPlayersOnField)
    }

    fun updateHalfs(value: String) {
        updateIntEntry(value, halfs)
    }

    fun updateTurnsPrHalf(value: String) {
        updateIntEntry(value, turnsPrHalf)
    }

    fun updateDiceRollBehavior(value: DropdownEntryWithValue<DiceRollOwner>) {
        selectedDiceRollBehavior.value = value
    }

    fun updateUndoActionBehavior(value: DropdownEntryWithValue<UndoActionBehavior>) {
        selectedUndoActionBehavior.value = value
    }

    fun updateFoulActionBehavior(it: DropdownEntryWithValue<FoulActionBehavior>) {
        selectedFoulActionBehavior.value = it
    }

    fun updateKickingPlayerBehavior(it: DropdownEntryWithValue<KickingPlayerBehavior>) {
        selectedKickingPlayerBehavior.value = it
    }

    private fun updateIntEntry(value: String, flow: MutableStateFlow<InputFieldDataWithValue<Int>>) {
        if (!value.trim().matches(intMatcher)) return // Only allow Numbers
        val underlyingValue = value.trim().toIntOrNull()
        val isError = (underlyingValue == null)
        val data = InputFieldDataWithValue(
            label = flow.value.label,
            value = value,
            underlyingValue = flow.value.underlyingValue,
            isError = isError
        )
        flow.value = data
        isSetupValid.value = !data.isError
    }

    fun updateRulesBuilder(rulesBuilder: Rules.Builder) {
        this.rulesBuilder = rulesBuilder
        TODO()
    }
}
