package com.jervisffb.ui.game.view
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_dice_roll
import com.jervisffb.ui.game.dialogs.ActionWheelInputDialog
import com.jervisffb.ui.game.dialogs.MultipleChoiceUserInputDialog
import com.jervisffb.ui.game.dialogs.SingleChoiceInputDialog
import com.jervisffb.ui.game.dialogs.circle.CoinMenuItem
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import com.jervisffb.ui.game.viewmodel.FieldViewData
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.menu.components.JervisDialog
import com.jervisffb.ui.menu.utils.JervisLogo
import com.jervisffb.ui.utils.jdp
import org.jetbrains.compose.resources.painterResource
import kotlin.math.hypot
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SingleSelectUserActionDialog(
    dialog: SingleChoiceInputDialog,
    vm: DialogsViewModel,
) {
    JervisDialog(
        title = dialog.title,
        icon = { JervisLogo() },
        width = dialog.width,
        draggable = dialog.moveable,
        backgroundScrim = false,
        centerOnField = vm.screenViewModel,
        dialogColor = if (dialog.owner?.isHomeTeam() ?: true) JervisTheme.rulebookRed else JervisTheme.rulebookBlue,
        content = { inputFieldTextColor, textColor ->
            Text(
                modifier = Modifier.weight(1f),
                text = dialog.message,
                color = textColor
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.padding(top = 16.dp, bottom = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    dialog.actionDescriptions.forEach { (action, description) ->
                        when (action) {
                            is CoinSideSelected -> {
                                CoinButton(
                                    modifier = Modifier.offset(y = 4.dp),
                                    coin = CoinMenuItem(
                                        value = action.component1(),
                                        parent = null,
                                        label = { description },
                                        enabled = true,
                                        onClick = { vm.userActionSelected(action) },
                                        startAnimationFrom = null
                                    ),
                                    onClick = { vm.userActionSelected(action) },
                                    dropShadow = false
                                )
                            }
                            is CoinTossResult -> {
                                CoinButton(
                                    modifier = Modifier.offset(y = 4.dp),
                                    coin = CoinMenuItem(
                                        value = action.result,
                                        parent = null,
                                        label = { description },
                                        enabled = true,
                                        onClick = { vm.userActionSelected(action) },
                                        startAnimationFrom = null
                                    ),
                                    onClick = { vm.userActionSelected(action) },
                                    dropShadow = false
                                )
                            }
                            is DBlockResult -> {

                            }
                            is DieResult -> {
                                DialogDiceButton(
                                    modifier = Modifier.offset(y = 4.dp),
                                    die = action,
                                    isSelected = false,
                                    onClick = { vm.userActionSelected(action) },
                                    useSelectedColorAsHover = true
                                )
                            }
                            else -> {
                                JervisButton(
                                    modifier = Modifier.offset(y = 8.dp),
                                    text = description,
                                    onClick = { vm.userActionSelected(action) },
                                    buttonColor = if (dialog.owner?.isAwayTeam() == true) JervisTheme.rulebookRed else JervisTheme.rulebookBlue,
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MultipleSelectUserActionDialog(
    dialog: MultipleChoiceUserInputDialog,
    vm: DialogsViewModel,
) {
    var showDialog by remember(dialog) { mutableStateOf(true) }
    if (showDialog) {
        val selectedRolls = remember(dialog) {
            mutableStateListOf<DieResult?>(*dialog.dice.map { vm.diceRollGenerator.rollDie(it.first) }.toTypedArray())
        }
        val result = DiceRollResults(selectedRolls.filterNotNull())
        val resultText = if (result.rolls.size < dialog.dice.size) null else dialog.result(result)
        val dialogColor = if (dialog.owner?.isHomeTeam() ?: true) JervisTheme.rulebookRed else JervisTheme.rulebookBlue
        val buttonColor = if (dialog.owner?.isHomeTeam() ?: true) JervisTheme.rulebookBlue else JervisTheme.rulebookRed
        JervisDialog(
            title = dialog.title,
            icon = {
                Image(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 20.dp, end = 20.dp),
                    painter = painterResource(Res.drawable.jervis_icon_menu_dice_roll),
                    contentDescription = "",
                    colorFilter = ColorFilter.tint(JervisTheme.white),
                )
            },
            width = dialog.width,
            draggable = dialog.movable,
            backgroundScrim = false,
            centerOnField = vm.screenViewModel,
            dialogColor = dialogColor,
            content = { inputFieldTextColor, textColor ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = dialog.message, color = textColor)
                    dialog.dice.forEachIndexed { i, el: Pair<Dice, List<DieResult>> ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            el.second.forEach { it: DieResult ->
                                val isSelected = remember(dialog) { derivedStateOf { selectedRolls[i] == it } }
                                when (it) {
                                    is DBlockResult -> {
                                        val buttonColors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected.value) MaterialTheme.colorScheme.primary else JervisTheme.diceBackground,
                                            )
                                        val text = it.blockResult.name
                                        Button(
                                            modifier = Modifier.weight(1f).aspectRatio(1.0f),
                                            onClick = { selectedRolls[i] = it },
                                            colors = buttonColors,
                                            contentPadding = PaddingValues(4.dp),
                                        ) {
                                            Image(
                                                modifier = Modifier.fillMaxSize(),
                                                bitmap = IconFactory.getDiceIcon(it),
                                                contentDescription = text,
                                                alignment = Alignment.Center,
                                                contentScale = ContentScale.Fit
                                            )
                                        }
                                    }

                                    else -> {
                                        DialogDiceButton(
                                            die = it,
                                            isSelected = (selectedRolls[i] == it),
                                            onClick = { selectedRolls[i] = it },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            buttons = {
                val buttonText by derivedStateOf { resultText ?: "Confirm" }
                JervisButton(
                    modifier = Modifier.offset(y = 8.dp),
                    text = buttonText,
                    onClick = {
                        showDialog = false
                        vm.userActionSelected(result)
                    },
                    buttonColor = buttonColor,
                    enabled = (selectedRolls.size == dialog.dice.size) && !selectedRolls.contains(null),
                )
            }
        )
    }
}

/**
 * Composable responsible for showing the Action Wheel. It will also calculate the rotation and offset needed
 * to place the wheel in a visible location (i.e. if at the edge of the Field, the wheel will be moved inside the field).
 *
 * TODO Reconsider this logic in light of the new game UI. It might be possible we have enough room to always show the wheel
 *  over the field square. But this might mean we need to disable some other events while it is showing to avoid accidental
 *  misclicks (like End Turn).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ActionWheelDialog(fieldVm: FieldViewModel, fieldData: FieldViewData, dialog: ActionWheelInputDialog, vm: DialogsViewModel) {
    val wheelViewModel = dialog.viewModel
    val ringSize = 250.jdp
    val boxSize = (hypot(ringSize.value, ringSize.value)).dp
    val ringSizePx = with(LocalDensity.current) { ringSize.toPx() }
    val boxWidthPx = with(LocalDensity.current) { boxSize.toPx() }
    var showTip by remember { mutableStateOf(false) }
    var tipRotationDegree by remember { mutableStateOf(0f) }
    val offset = remember(fieldData, dialog.viewModel.center) {
        if (dialog.viewModel.center == null) {
            IntOffset(
                x = (fieldData.offset.x + (fieldData.size.width / 2f) - boxWidthPx/2f).roundToInt(),
                y = (fieldData.offset.y + (fieldData.size.height / 2f) - boxWidthPx/2f).roundToInt(),
            )
        } else {
            val data = fieldData.calculateActionWheelPlacement(
                dialog,
                fieldVm,
                boxWidthPx,
                ringSizePx,
            )
            showTip = data.showTip
            tipRotationDegree = data.tipRotationDegree
            data.offset
        }
    }
    if (!dialog.viewModel.shown.value) return
    val dismissRequest = remember(wheelViewModel.hideOnClickedOutside, wheelViewModel.onMenuHidden) {
        { userDismissed: Boolean -> // `true` if the user clicked outside a button (to hide the wheel)
            if (userDismissed && dialog.viewModel.hideOnClickedOutside) {
                dialog.viewModel.shown.value = false
                wheelViewModel.hideWheel(false)
            }
        }
    }
    Popup(
        alignment = Alignment.TopStart,
        offset = offset,
        onDismissRequest = {
            dismissRequest(true)
        },
        properties = PopupProperties(
            focusable = true,
            clippingEnabled = true // Prevents being dragged outside the window bounds
        )
    ) {
        Box(
            // modifier = Modifier.offset { offset }
        ) {
            ActionWheelMenu(
                viewModel = wheelViewModel,
                ringSize = ringSize,
                showTip = showTip,
                tipRotationDegree = tipRotationDegree,
                onDismissRequest = dismissRequest,
            )
        }
    }
}
