package com.jervisffb.ui.game.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.common.inducements.BiasedRefereeInducement
import com.jervisffb.engine.common.inducements.BiasedRefereesInducementGroup
import com.jervisffb.engine.common.inducements.InducementGroupCommon
import com.jervisffb.engine.common.inducements.InducementTypeCommon
import com.jervisffb.engine.common.inducements.InfamousCoachingStaffInducement
import com.jervisffb.engine.common.inducements.InfamousCoachingStaffsInducementGroup
import com.jervisffb.engine.common.inducements.SimpleInducement
import com.jervisffb.engine.common.inducements.StarPlayerInducement
import com.jervisffb.engine.common.inducements.StarPlayersInducementGroup
import com.jervisffb.engine.common.inducements.WizardInducement
import com.jervisffb.engine.common.inducements.WizardsInducementGroup
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.inducements.settings.InducementGroup
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.inducements.settings.TeamPlayerInducement
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.sprites.SpriteSource
import com.jervisffb.engine.utils.dedupSkillsByType
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_icon_menu_minus
import com.jervisffb.shared.generated.resources.jervis_icon_menu_plus
import com.jervisffb.ui.ICON_FACTORY
import com.jervisffb.ui.formatCurrency
import com.jervisffb.ui.game.dialogs.BuyInducementsDialog
import com.jervisffb.ui.game.dialogs.BuyInducementsViewModel
import com.jervisffb.ui.game.dialogs.CartEntryView
import com.jervisffb.ui.game.dialogs.DialogSize
import com.jervisffb.ui.game.dialogs.GroupItemView
import com.jervisffb.ui.game.dialogs.MERCENARY_NAMES
import com.jervisffb.ui.game.model.GuardedAction
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.NumberChangeButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.viewmodel.DialogsViewModel
import com.jervisffb.ui.menu.BackNavigationHandler
import com.jervisffb.ui.menu.OnBackPress
import com.jervisffb.ui.menu.components.JervisTooltipArea
import com.jervisffb.ui.menu.components.JervisTooltipPlacement
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyInducementsDialog(
    dialog: BuyInducementsDialog,
    dialogsVm: DialogsViewModel,
) {
    val vm = remember(dialog) { BuyInducementsViewModel(dialogsVm.screenViewModel.uiState.gameController, dialog) }
    val isHomeTeam = dialog.team.isHomeTeam()
    val teamColor = if (isHomeTeam) JervisTheme.homeTeamColor else JervisTheme.awayTeamColor

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val mainListState = rememberLazyListState()
    var lastTouchedGroup by remember { mutableStateOf<InducementType?>(null) }

    LaunchedEffect(vm.cartEntries.size) {
        val type = lastTouchedGroup ?: return@LaunchedEffect
        // Mercenary Add closes the drawer immediately; wait until it's fully closed
        // so the LazyColumn's scroll request is not swallowed by the animation.
        snapshotFlow { drawerState.currentValue }.first { it == DrawerValue.Closed }
        // Wait one frame so LazyListState.layoutInfo reflects the post-mutation layout.
        androidx.compose.runtime.withFrameNanos { }
        val targetIndex = addRowIndexFor(vm, type)
        if (targetIndex < 0) return@LaunchedEffect
        // If the target isn't currently laid out, scroll it into view first so we
        // have accurate offset info to fine-tune with.
        if (mainListState.layoutInfo.visibleItemsInfo.none { it.index == targetIndex }) {
            mainListState.animateScrollToItem(targetIndex)
            androidx.compose.runtime.withFrameNanos { }
        }
        val info = mainListState.layoutInfo
        val target = info.visibleItemsInfo.find { it.index == targetIndex } ?: return@LaunchedEffect
        val below = target.offset + target.size - info.viewportEndOffset
        val above = info.viewportStartOffset - target.offset
        when {
            below > 0 -> mainListState.animateScrollBy(below.toFloat())
            above > 0 -> mainListState.animateScrollBy(-above.toFloat())
            // else: fully visible — no scroll needed
        }
    }

    LaunchedEffect(vm.activeDrawerGroup) {
        if (vm.activeDrawerGroup != null) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }
    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed && vm.activeDrawerGroup != null) {
            vm.closeDrawer()
        }
    }

    DisposableEffect(vm) {
        val callback = OnBackPress {
            if (vm.activeDrawerGroup != null) {
                vm.closeDrawer()
            }
            // Consume event. The dialog is non-dismissible and must never let
            // the game menu open while it is showing.
            true
        }
        BackNavigationHandler.register(callback)
        onDispose { BackNavigationHandler.unregister(callback) }
    }

    BasicAlertDialog(
        modifier = Modifier
            .width(DialogSize.LARGE)
            .fillMaxHeight(0.8f)
            .border(6.dp, teamColor),
        onDismissRequest = { /* Cannot be dismissed */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .paperBackground(),
        ) {
            ModalNavigationDrawer(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                drawerState = drawerState,
                gesturesEnabled = drawerState.isOpen,
                drawerContent = {
                    InducementDrawerContent(
                        vm = vm,
                        activeType = vm.activeDrawerGroup,
                        teamColor = teamColor,
                        isHomeTeam = isHomeTeam,
                    )
                },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(16.dp),
                ) {
                    HeaderBudgetRow(
                        pettyCash = dialog.pettyCash,
                        treasury = dialog.treasury,
                        dialogColor = teamColor,
                    )
                    Spacer(Modifier.height(8.dp))
                    TitleBorder(teamColor)
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        state = mainListState,
                    ) {
                        itemsIndexed(vm.simpleInducements) { index, inducement ->
                            SimpleInducementRow(
                                rowNo = index,
                                inducement = inducement,
                                count = vm.simpleCount(inducement.type),
                                canAffordMore = vm.canAfford(inducement),
                                teamColor = teamColor,
                                onIncrease = {
                                    lastTouchedGroup = null
                                    vm.changeSimpleCount(inducement, +1)
                                },
                                onDecrease = {
                                    lastTouchedGroup = null
                                    vm.changeSimpleCount(inducement, -1)
                                },
                            )
                        }

                        vm.groupInducements.forEach { group ->
                            item(key = "group-${group.type}") {
                                InducementGroupHeader(titleWithRange(group.name, group.max), teamColor)
                            }
                            val cartItems = vm.groupItemsInCart(group)
                            val isFull = vm.isGroupFull(group)
                            if (cartItems.isNotEmpty()) {
                                item(key = "group-header-${group.type}") {
                                    PositionTableHeader(
                                        teamColor = teamColor,
                                        showAction = true,
                                        showStats = group is StarPlayersInducementGroup,
                                        skillsLabel = run {
                                            val title = when {
                                                group is InducementGroupCommon -> {
                                                    when (group) {
                                                        is StarPlayersInducementGroup -> "Skills & Traits"
                                                        is BiasedRefereesInducementGroup,
                                                        is InfamousCoachingStaffsInducementGroup,
                                                        is WizardsInducementGroup -> "Special Abilities"
                                                    }
                                                }
                                                else -> error("Unsupported group type: $group")
                                            }
                                            title
                                        }
                                    )
                                }
                                itemsIndexed(cartItems, key = { _, item -> "cart-${item.key}" }) { index, item ->
                                    GroupCartRow(
                                        rowNo = index,
                                        item = item,
                                        teamColor = teamColor,
                                        isHomeTeam = isHomeTeam,
                                        onRemove = { vm.toggleGroupItem(item) },
                                    )
                                }
                            }
                            if (!isFull) {
                                item(key = "add-${group.type}") {
                                    AddGroupRow(
                                        enabled = true,
                                        teamColor = teamColor,
                                        label = "Add ${singularGroupName(group)}",
                                        onAdd = {
                                            lastTouchedGroup = group.type
                                            vm.openDrawer(group.type)
                                        },
                                    )
                                }
                            }
                        }

                        vm.mercenaryInducement?.let { mercSettings ->
                            item(key = "group-mercenary") {
                                InducementGroupHeader(titleWithRange(mercSettings.name, mercSettings.max), teamColor)
                            }
                            val mercs = vm.mercenaries
                            val mercFull = vm.isMercenaryLimitReached()
                            if (mercs.isNotEmpty()) {
                                item(key = "group-header-mercenary") {
                                    PositionTableHeader(
                                        teamColor = teamColor,
                                        showAction = true,
                                        showStats = true,
                                    )
                                }
                                itemsIndexed(mercs, key = { _, entry -> "cart-merc-${entry.id}" }) { index, entry ->
                                    PositionTableRow(
                                        rowNo = index,
                                        position = entry.position,
                                        positionSkillList = entry.positionalSkillList,
                                        extraSkillList = entry.extraSkillList,
                                        displayName = entry.displayName,
                                        isSelected = false,
                                        teamColor = teamColor,
                                        isHomeTeam = isHomeTeam,
                                        onClick = null,
                                        trailing = {
                                            NumberChangeButton(
                                                icon = Res.drawable.jervis_icon_menu_minus,
                                                description = "Remove ${entry.displayName}",
                                                onClick = { vm.removeMercenary(entry.id) },
                                                buttonColor = teamColor,
                                            )
                                        },
                                        costOverride = entry.price,
                                        subLabel = entry.position.titleSingular,
                                    )
                                }
                            }
                            if (!mercFull) {
                                item(key = "add-mercenary") {
                                    AddGroupRow(
                                        enabled = true,
                                        teamColor = teamColor,
                                        label = "Add ${singularGroupName(mercSettings)}",
                                        onAdd = {
                                            lastTouchedGroup = InducementTypeCommon.STANDARD_MERCENARY_PLAYERS
                                            vm.openDrawer(InducementTypeCommon.STANDARD_MERCENARY_PLAYERS)
                                        },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    TitleBorder(teamColor)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val onClickCancelCallback = remember(vm.gameController, dialog.nextActionId) {
                            GuardedAction(vm.gameController, dialog.nextActionId) { id ->
                                dialogsVm.userActionSelected(id, Cancel)
                            }
                        }
                        JervisButton(
                            text = "Cancel",
                            onClick = onClickCancelCallback,
                            buttonColor = teamColor,
                        )
                        val badgesState = rememberLazyListState()
                        LaunchedEffect(vm.cartEntries) {
                            if (vm.cartEntries.isNotEmpty()) {
                                badgesState.scrollToItem(vm.cartEntries.lastIndex)
                            }
                        }
                        LazyRow(
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            state = badgesState,
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            items(vm.cartEntries, key = { it.key }) { entry ->
                                PurchasedInducementBadge(entry = entry, isHomeTeam = isHomeTeam, teamColor = teamColor)
                            }
                        }
                        val onClickBuyCallback = remember(vm.gameController, dialog.nextActionId) {
                            GuardedAction(vm.gameController, dialog.nextActionId) { id ->
                                dialogsVm.userActionSelected(id, vm.submit())
                            }
                        }
                        JervisButton(
                            text = if (vm.totalPrice <= 0) "Buy" else "Buy (${formatCurrency(vm.totalPrice)})",
                            onClick = onClickBuyCallback,
                            enabled = vm.canBuy,
                            buttonColor = teamColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderBudgetRow(pettyCash: Int, treasury: Int, dialogColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Buy Inducements".uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = dialogColor,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            BudgetField("Petty Cash", pettyCash, dialogColor)
            BudgetField("Treasury", treasury, dialogColor)
        }
    }
}

@Composable
private fun BudgetField(label: String, value: Int, dialogColor: Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = dialogColor,
        )
        Text(
            text = formatCurrency(value),
            fontSize = 20.sp,
            lineHeight = 1.em,
            fontWeight = FontWeight.Bold,
            color = JervisTheme.contentTextColor,
        )
    }
}

@Composable
private fun SimpleInducementRow(
    rowNo: Int,
    inducement: SimpleInducement,
    count: Int,
    canAffordMore: Boolean,
    teamColor: Color,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = if (rowNo % 2 == 0) Color.Transparent else JervisTheme.rulebookPaperMediumDark)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(positionIconColWidth).padding(2.dp),
            contentAlignment = Alignment.Center,
        ) {
            val icon = ICON_FACTORY.getInducementIcon(inducement.type)
            if (icon != null) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = inducement.name,
                    modifier = Modifier.size(36.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(teamColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = inducement.name.firstOrNull()?.uppercase() ?: "?",
                        color = JervisTheme.white,
                        fontFamily = JervisTheme.fontFamily(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = titleWithRange(inducement.name, inducement.max),
            color = JervisTheme.contentTextColor,
            fontSize = 14.sp,
        )
        Text(
            modifier = Modifier.width(positionCostColWidth),
            text = formatCurrency(inducement.defaultPrice),
            color = JervisTheme.contentTextColor,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(8.dp))
        NumberChangeButton(
            icon = Res.drawable.jervis_icon_menu_plus,
            description = "Add ${inducement.name}",
            onClick = onIncrease,
            enabled = count < inducement.max && canAffordMore,
            buttonColor = teamColor,
        )
        Text(
            modifier = Modifier.width(48.dp),
            text = count.toString(),
            color = JervisTheme.contentTextColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        NumberChangeButton(
            icon = Res.drawable.jervis_icon_menu_minus,
            description = "Remove ${inducement.name}",
            onClick = onDecrease,
            enabled = count > 0,
            buttonColor = teamColor,
        )
    }
}

@Composable
private fun InducementGroupHeader(name: String, dialogColor: Color) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = name,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = dialogColor,
        )
    }
    TitleBorder(dialogColor)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun GroupItemRow(
    rowNo: Int,
    item: GroupItemView,
    teamColor: Color,
    isHomeTeam: Boolean,
    isSelected: Boolean,
    canAdd: Boolean,
    onToggle: () -> Unit,
) {
    InducementRowContainer(rowNo = rowNo) {
        InducementIcon(item.key.type, name = item.name, iconSource = item.iconSource, isHomeTeam = isHomeTeam, teamColor = teamColor)
        Spacer(Modifier.width(12.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = item.name,
            color = JervisTheme.contentTextColor,
            fontSize = 15.sp,
        )
        Text(
            modifier = Modifier.width(80.dp),
            text = formatCurrency(item.price),
            color = JervisTheme.contentTextColor,
            fontSize = 15.sp,
            textAlign = TextAlign.End,
        )
        Spacer(Modifier.width(8.dp))
        if (isSelected) {
            NumberChangeButton(
                icon = Res.drawable.jervis_icon_menu_minus,
                description = "Remove ${item.name}",
                onClick = onToggle,
                buttonColor = teamColor,
            )
        } else {
            NumberChangeButton(
                icon = Res.drawable.jervis_icon_menu_plus,
                description = "Add ${item.name}",
                onClick = onToggle,
                enabled = canAdd,
                buttonColor = teamColor,
            )
        }
    }
}

@Composable
private fun AddGroupRow(enabled: Boolean, teamColor: Color, label: String? = null, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (label != null) {
            Spacer(Modifier.width(positionIconColWidth))
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = JervisTheme.contentTextColor,
            )
        }
        Spacer(Modifier.weight(1f))
        NumberChangeButton(
            icon = Res.drawable.jervis_icon_menu_plus,
            description = label ?: "Add",
            onClick = onAdd,
            enabled = enabled,
            buttonColor = teamColor,
        )
    }
}

/**
 * Compute the LazyColumn item index of the "Add" row for the given inducement group,
 * mirroring the structure of the main dialog's LazyColumn. Falls back to the last
 * cart item's index when the group is full (Add row removed), so we can still
 * scroll the just-added entry into view. Returns -1 if the group isn't present.
 */
private fun addRowIndexFor(vm: BuyInducementsViewModel, type: InducementType): Int {
    var index = vm.simpleInducements.size
    vm.groupInducements.forEach { group ->
        index++ // group header
        val cartItems = vm.groupItemsInCart(group)
        val lastCartIndex = if (cartItems.isNotEmpty()) {
            index++ // table header
            val lastIdx = index + cartItems.size - 1
            index += cartItems.size
            lastIdx
        } else -1
        if (!vm.isGroupFull(group)) {
            if (group.type == type) return index
            index++ // add row
        } else if (group.type == type) {
            return lastCartIndex
        }
    }
    vm.mercenaryInducement?.let {
        index++ // group header
        val mercs = vm.mercenaries
        val lastMercIndex = if (mercs.isNotEmpty()) {
            index++ // table header
            val lastIdx = index + mercs.size - 1
            index += mercs.size
            lastIdx
        } else -1
        if (type == InducementTypeCommon.STANDARD_MERCENARY_PLAYERS) {
            return if (!vm.isMercenaryLimitReached()) index else lastMercIndex
        }
    }
    return -1
}

private fun singularGroupName(group: TeamPlayerInducement<*>): String {
    return "Mercenary"
}

private fun singularGroupName(group: InducementGroup<*, *, *>): String {
    return when {
        group is InducementGroupCommon<*, *, *> -> {
            when (group) {
                is BiasedRefereesInducementGroup -> "Biased Referee"
                is InfamousCoachingStaffsInducementGroup -> "Infamous Coaching Staff"
                is StarPlayersInducementGroup -> "Star Player"
                is WizardsInducementGroup -> "Wizard"
            }
        }
        else -> error("Unknown group type: $group")
    }
}

@Composable
private fun InducementRowContainer(rowNo: Int, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = if (rowNo % 2 == 0) Color.Transparent else JervisTheme.rulebookPaperMediumDark)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        content()
    }
}

@Composable
private fun InducementIcon(
    type: InducementType,
    name: String,
    iconSource: SpriteSource?,
    isHomeTeam: Boolean,
    teamColor: Color,
) {
    RenderInducementIcon(
        type = type,
        name = name,
        iconSource = iconSource,
        isHomeTeam = isHomeTeam,
        teamColor = teamColor,
        size = 40.dp,
        letterSize = 18.sp,
    )
}

@Composable
private fun RenderInducementIcon(
    type: InducementType,
    name: String,
    iconSource: SpriteSource?,
    isHomeTeam: Boolean,
    teamColor: Color,
    size: Dp,
    letterSize: androidx.compose.ui.unit.TextUnit,
) {
    if (iconSource != null) {
        var image by remember(iconSource, isHomeTeam) { mutableStateOf<ImageBitmap?>(null) }
        LaunchedEffect(iconSource, isHomeTeam) {
            image = ICON_FACTORY.loadPlayerIcon(iconSource, isHomeTeam)
        }
        val bitmap = image
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = name,
                modifier = Modifier.size(size),
            )
            return
        }
    }
    val icon = ICON_FACTORY.getInducementIcon(type)
    if (icon != null) {
        Box(
            modifier = Modifier.size(size).clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = name,
                modifier = Modifier.size(size),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(teamColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                color = JervisTheme.white,
                fontWeight = FontWeight.Bold,
                fontSize = letterSize,
            )
        }
    }
}

@Composable
private fun InducementDrawerContent(
    vm: BuyInducementsViewModel,
    activeType: InducementType?,
    teamColor: Color,
    isHomeTeam: Boolean,
) {
    val isMercenary = activeType == InducementTypeCommon.STANDARD_MERCENARY_PLAYERS
    val group = vm.groupInducements.firstOrNull { it.type == activeType }
    val title = when {
        isMercenary -> vm.mercenaryInducement?.let { titleWithRange(it.name, it.max) } ?: ""
        group != null -> titleWithRange(group.name, group.max)
        else -> ""
    }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(DialogSize.MEDIUM)
            .paperBackground()
            .drawWithCache {
                val strokeWidth = 8.dp.toPx()
                val x = size.width - strokeWidth / 2
                onDrawBehind {
                    drawLine(
                        color = teamColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = strokeWidth,
                    )
                }
            }
            .padding(16.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title.uppercase(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = teamColor,
        )
        Spacer(Modifier.height(8.dp))
        TitleBorder(teamColor)
        Spacer(Modifier.height(8.dp))

        when {
            isMercenary -> MercenaryBuilder(
                vm = vm,
                teamColor = teamColor,
                isHomeTeam = isHomeTeam,
            )
            group is StarPlayersInducementGroup -> StarPlayerTable(
                vm = vm,
                group = group,
                teamColor = teamColor,
                isHomeTeam = isHomeTeam,
            )
            group is WizardsInducementGroup ||
                group is BiasedRefereesInducementGroup ||
                group is InfamousCoachingStaffsInducementGroup -> AbilityGroupTable(
                vm = vm,
                group = group,
                teamColor = teamColor,
            )
            group != null -> {
                val items = vm.availableItemsInGroup(group)
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    itemsIndexed(items, key = { _, it -> "drawer-${it.key}" }) { index, item ->
                        val selected = vm.isInCart(item.key)
                        GroupItemRow(
                            rowNo = index,
                            item = item,
                            teamColor = teamColor,
                            isHomeTeam = isHomeTeam,
                            isSelected = selected,
                            canAdd = !vm.isGroupFull(group) && vm.canAfford(item.inducement),
                            onToggle = { vm.toggleGroupItem(item) },
                        )
                    }
                }
            }
        }
    }
}

private fun titleWithRange(name: String, max: Int): String {
    return when {
        max <= 0 -> name
        max == Int.MAX_VALUE -> "(Unlimited) $name"
        else -> "(0-$max) $name"
    }
}

private val positionIconColWidth = 40.dp
private val positionNameColWidth = 140.dp
private val positionStatColWidth = 30.dp
private val positionCostColWidth = 60.dp

// View responsible for creating a new mercenary. This includes selecting a
// position and optional skill.
// TODO Respect limits for the type of mercenary
// TODO Make name configurable
@Composable
private fun ColumnScope.MercenaryBuilder(
    vm: BuyInducementsViewModel,
    teamColor: Color,
    isHomeTeam: Boolean,
) {
    val positionNames = remember(vm.mercenaryPositions) {
        vm.mercenaryPositions.associate { it.id to MERCENARY_NAMES.random() }
    }
    var selectedPositionId by remember { mutableStateOf<PositionId?>(null) }
    var selectedSkill by remember(selectedPositionId) { mutableStateOf<SkillId?>(null) }
    val selectedPosition = selectedPositionId?.let { id -> vm.mercenaryPositions.firstOrNull { it.id == id } }
    val skills = selectedPosition?.let { vm.primarySkillsFor(it) }.orEmpty()
    val canConfirm = selectedPosition != null &&
        vm.canAffordMercenary(selectedPosition, selectedSkill) &&
        !vm.isMercenaryLimitReached() &&
        !vm.isPositionLimitReached(selectedPosition)

    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
        PositionTableHeader(teamColor, showAction = false)
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            itemsIndexed(vm.mercenaryPositions, key = { _, p -> "merc-pos-${p.id.value}" }) { index, position ->
                val isRowSelected = selectedPositionId == position.id
                val extraSkill = if (isRowSelected) skills.firstOrNull { it.first == selectedSkill }?.first else null
                val rowCost = vm.mercenaryPrice(position, if (isRowSelected) selectedSkill else null)
                val dedupedSkills = dedupSkillsByType(position.skills, listOfNotNull(SkillType.LONER.idTarget(4), extraSkill))
                val positionAvailable = !vm.isPositionLimitReached(position)
                PositionTableRow(
                    rowNo = index,
                    position = position,
                    positionSkillList = dedupedSkills.positionalSkills,
                    extraSkillList = dedupedSkills.extraSkills,
                    displayName = position.titleSingular,
                    isSelected = isRowSelected,
                    teamColor = teamColor,
                    isHomeTeam = isHomeTeam,
                    onClick = if (positionAvailable) {
                        { selectedPositionId = position.id }
                    } else {
                        null
                    },
                    trailing = null,
                    costOverride = rowCost,
                    enabled = positionAvailable,
                )
            }
            if (selectedPosition != null && skills.isNotEmpty()) {
                item(key = "merc-skills-header") {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                        text = "Extra Skill (optional)".uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = teamColor,
                    )
                }
                item(key = "merc-skills-list") {
                    val positionSkillTypes = selectedPosition.skills.map { it.type }.toSet()
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        skills.forEach { (skillId, skillName) ->
                            val alreadyHas = skillId.type in positionSkillTypes
                            SkillChip(
                                name = skillName,
                                isSelected = (selectedSkill == skillId),
                                enabled = !alreadyHas,
                                teamColor = teamColor,
                                onClick = {
                                    selectedSkill = if (selectedSkill == skillId) null else skillId
                                },
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        TitleBorder(teamColor)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JervisButton(
                text = "Add",
                onClick = {
                    val pos = selectedPosition ?: return@JervisButton
                    val name = positionNames[pos.id].orEmpty().trim()
                    vm.addMercenary(pos, selectedSkill, name)
                    selectedPositionId = null
                    selectedSkill = null
                    vm.closeDrawer()
                },
                enabled = canConfirm,
                buttonColor = teamColor,
            )
        }
    }
}

@Composable
private fun ColumnScope.StarPlayerTable(
    vm: BuyInducementsViewModel,
    group: StarPlayersInducementGroup,
    teamColor: Color,
    isHomeTeam: Boolean,
) {
    val items = vm.availableItemsInGroup(group)
    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
        PositionTableHeader(teamColor, showAction = true)
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            itemsIndexed(items, key = { _, it -> "star-${it.key}" }) { index, item ->
                val star = item.inducement as? StarPlayerInducement ?: return@itemsIndexed
                val selected = vm.isInCart(item.key)
                val canAdd = !vm.isGroupFull(group) && vm.canAfford(item.inducement)
                PositionTableRow(
                    rowNo = index,
                    position = star.starPlayer,
                    positionSkillList = star.starPlayer.skills,
                    extraSkillList = emptyList(),
                    displayName = star.starPlayer.title,
                    isSelected = false,
                    teamColor = teamColor,
                    isHomeTeam = isHomeTeam,
                    onClick = null,
                    trailing = {
                        if (selected) {
                            NumberChangeButton(
                                icon = Res.drawable.jervis_icon_menu_minus,
                                description = "Remove ${star.starPlayer.title}",
                                onClick = { vm.toggleGroupItem(item) },
                                buttonColor = teamColor,
                            )
                        } else {
                            NumberChangeButton(
                                icon = Res.drawable.jervis_icon_menu_plus,
                                description = "Add ${star.starPlayer.title}",
                                onClick = { vm.toggleGroupItem(item) },
                                enabled = canAdd,
                                buttonColor = teamColor,
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun GroupCartRow(
    rowNo: Int,
    item: GroupItemView,
    teamColor: Color,
    isHomeTeam: Boolean,
    onRemove: () -> Unit,
) {
    val trailing: @Composable () -> Unit = {
        NumberChangeButton(
            icon = Res.drawable.jervis_icon_menu_minus,
            description = "Remove ${item.name}",
            onClick = onRemove,
            buttonColor = teamColor,
        )
    }
    when (val inducement = item.inducement) {
        is StarPlayerInducement -> PositionTableRow(
            rowNo = rowNo,
            position = inducement.starPlayer,
            positionSkillList = inducement.starPlayer.skills,
            extraSkillList = emptyList(),
            displayName = inducement.starPlayer.title,
            isSelected = false,
            teamColor = teamColor,
            isHomeTeam = isHomeTeam,
            onClick = null,
            trailing = trailing,
        )
        is WizardInducement,
        is BiasedRefereeInducement,
        is InfamousCoachingStaffInducement -> AbilityRow(
            rowNo = rowNo,
            name = item.name,
            abilities = abilitiesFor(inducement),
            cost = item.price,
            teamColor = teamColor,
            trailing = trailing,
        )
        else -> {
            // Fallback to the previous compact display for other group types.
            GroupItemRow(
                rowNo = rowNo,
                item = item,
                teamColor = teamColor,
                isHomeTeam = isHomeTeam,
                isSelected = true,
                canAdd = false,
                onToggle = onRemove,
            )
        }
    }
}

@Composable
private fun ColumnScope.AbilityGroupTable(
    vm: BuyInducementsViewModel,
    group: InducementGroup<*, *, *>,
    teamColor: Color,
) {
    val items = vm.availableItemsInGroup(group)
    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
        PositionTableHeader(
            teamColor,
            showAction = true,
            showStats = false,
            skillsLabel = "Special Abilities",
        )
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            itemsIndexed(items, key = { _, it -> "ability-${it.key}" }) { index, item ->
                val abilities = abilitiesFor(item.inducement)
                val selected = vm.isInCart(item.key)
                val canAdd = !vm.isGroupFull(group) && vm.canAfford(item.inducement)
                AbilityRow(
                    rowNo = index,
                    name = item.name,
                    abilities = abilities,
                    cost = item.price,
                    teamColor = teamColor,
                    trailing = {
                        if (selected) {
                            NumberChangeButton(
                                icon = Res.drawable.jervis_icon_menu_minus,
                                description = "Remove ${item.name}",
                                onClick = { vm.toggleGroupItem(item) },
                                buttonColor = teamColor,
                            )
                        } else {
                            NumberChangeButton(
                                icon = Res.drawable.jervis_icon_menu_plus,
                                description = "Add ${item.name}",
                                onClick = { vm.toggleGroupItem(item) },
                                enabled = canAdd,
                                buttonColor = teamColor,
                            )
                        }
                    },
                )
            }
        }
    }
}

private fun abilitiesFor(inducement: com.jervisffb.engine.model.inducements.settings.SingleInducement<*>): List<String> {
    return when (inducement) {
        is WizardInducement -> inducement.wizard.spells.map { it.name }
        is BiasedRefereeInducement -> inducement.referee.specialRules.map { it.description }
        is InfamousCoachingStaffInducement -> inducement.staff.specialRules.map { it.description }
        else -> emptyList()
    }
}

@Composable
private fun AbilityRow(
    rowNo: Int,
    name: String,
    abilities: List<String>,
    cost: Int,
    teamColor: Color,
    trailing: (@Composable () -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = if (rowNo % 2 == 0) Color.Transparent else JervisTheme.rulebookPaperMediumDark)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(positionIconColWidth)
                .padding(2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(teamColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "?",
                    color = JervisTheme.white,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            modifier = Modifier.width(positionNameColWidth),
            text = name,
            fontSize = 14.sp,
            lineHeight = 1.em,
            color = JervisTheme.contentTextColor,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = if (abilities.isEmpty()) "" else abilities.joinToString(", "),
            fontSize = 14.sp,
            lineHeight = 1.em,
            color = JervisTheme.contentTextColor,
        )
        Text(
            modifier = Modifier.width(positionCostColWidth),
            text = formatCurrency(cost),
            fontSize = 14.sp,
            color = JervisTheme.contentTextColor,
            textAlign = TextAlign.Center,
        )
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun PositionTableHeader(
    teamColor: Color,
    showAction: Boolean,
    showStats: Boolean = true,
    skillsLabel: String = "Skills & Traits",
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = teamColor)
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(positionIconColWidth)) // icon column
        Spacer(Modifier.width(8.dp))
        PositionTableHeaderText("Name", positionNameColWidth, center = false)
        if (showStats) {
            PositionTableHeaderText("Ma", positionStatColWidth)
            PositionTableHeaderText("St", positionStatColWidth)
            PositionTableHeaderText("Ag", positionStatColWidth)
            PositionTableHeaderText("Pa", positionStatColWidth)
            PositionTableHeaderText("Av", positionStatColWidth)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            modifier = Modifier.weight(1f),
            text = skillsLabel.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = JervisTheme.white,
        )
        PositionTableHeaderText("Cost", positionCostColWidth)
        if (showAction) {
            Spacer(Modifier.width(8.dp))
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun PositionTableHeaderText(text: String, width: Dp, center: Boolean = true) {
    Text(
        modifier = Modifier.width(width),
        text = text.uppercase(),
        fontSize = 12.sp,
        textAlign = if (center) TextAlign.Center else null,
        fontWeight = FontWeight.Medium,
        color = JervisTheme.white,
    )
}

/**
 * A TeamTable-like row rendering a [Position] with its stats, default skills and
 * cost. When [onClick] is provided the row is clickable and highlights when
 * [isSelected]. When [trailing] is provided it's rendered after the cost column.
 */
@Composable
private fun PositionTableRow(
    rowNo: Int,
    position: Position,
    positionSkillList: List<SkillId>,
    extraSkillList: List<SkillId>,
    displayName: String,
    isSelected: Boolean,
    teamColor: Color,
    isHomeTeam: Boolean,
    onClick: (() -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    costOverride: Int? = null,
    subLabel: String? = null,
    enabled: Boolean = true,
) {
    val gameVersion = GameVersion.BB2025 // Used to render skills
    val baseSkillNames = remember(position) {
        positionSkillList.map { it.toNiceString(gameVersion = gameVersion) }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .background(
                color = when {
                    isSelected -> teamColor.copy(alpha = 0.25f)
                    rowNo % 2 == 0 -> Color.Transparent
                    else -> JervisTheme.rulebookPaperMediumDark
                }
            )
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MercenaryPositionIcon(position, isHomeTeam, teamColor)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.width(positionNameColWidth)) {
            Text(
                text = displayName,
                fontSize = 14.sp,
                lineHeight = 1.em,
                color = JervisTheme.contentTextColor,
            )
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    fontSize = 10.sp,
                    lineHeight = 1.em,
                    color = JervisTheme.contentTextColor,
                )
            }
        }
        PositionStatText(position.move.toString())
        PositionStatText(position.strength.toString())
        PositionStatText("${position.agility}+")
        PositionStatText(if (position.passing != null) "${position.passing}+" else "-")
        PositionStatText("${position.armorValue}+")
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (baseSkillNames.isNotEmpty()) {
                Text(
                    text = baseSkillNames.joinToString(", "),
                    fontSize = 10.sp,
                    lineHeight = 1.em,
                    color = JervisTheme.contentTextColor,
                )
            }
            if (extraSkillList.isNotEmpty()) {
                Text(
                    text = extraSkillList.joinToString(", ") { it.toNiceString(gameVersion = gameVersion) },
                    fontSize = 14.sp,
                    lineHeight = 1.em,
                    color = JervisTheme.contentTextColor,
                )
            }
        }
        Text(
            modifier = Modifier.width(positionCostColWidth),
            text = formatCurrency(costOverride ?: position.cost),
            fontSize = 14.sp,
            color = JervisTheme.contentTextColor,
            textAlign = TextAlign.Center,
        )
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun MercenaryPositionIcon(position: Position, isHomeTeam: Boolean, teamColor: Color) {
    val iconSource = position.icon
    var bitmap by remember(iconSource, isHomeTeam) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(iconSource, isHomeTeam) {
        bitmap = iconSource?.let { ICON_FACTORY.loadPlayerIcon(it, isHomeTeam) }
    }
    Box(
        modifier = Modifier.width(positionIconColWidth).padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        val current = bitmap
        if (current != null) {
            Image(
                modifier = Modifier.aspectRatio(1f).fillMaxSize().graphicsLayer(scaleX = 2f, scaleY = 2f),
                bitmap = current,
                contentDescription = position.title,
                contentScale = ContentScale.None,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(teamColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = position.title.firstOrNull()?.uppercase() ?: "?",
                    color = JervisTheme.white,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun PositionStatText(text: String) {
    Text(
        modifier = Modifier.width(positionStatColWidth),
        text = text,
        fontSize = 14.sp,
        lineHeight = 1.em,
        textAlign = TextAlign.Center,
        color = JervisTheme.contentTextColor,
    )
}

@Composable
private fun SkillChip(
    name: String,
    isSelected: Boolean,
    enabled: Boolean,
    teamColor: Color,
    onClick: () -> Unit,
) {
    val border: BorderStroke? = if (isSelected) null else BorderStroke(4.dp, teamColor)
    val bg = if (isSelected) teamColor else Color.Transparent
    val textColor = if (isSelected) JervisTheme.white else JervisTheme.contentTextColor
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
    ) {
        Button(
            modifier = Modifier,
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = bg,
                contentColor = textColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = JervisTheme.contentTextColor.copy(alpha = 0.35f),
            ),
            border = if (enabled) border else BorderStroke(4.dp, teamColor.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(0.dp),
        ) {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) textColor else JervisTheme.contentTextColor.copy(alpha = 0.35f),
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PurchasedInducementBadge(entry: CartEntryView, isHomeTeam: Boolean, teamColor: Color) {
    val tooltipText = "${entry.count} × ${entry.tooltipName} — ${formatCurrency(entry.totalPrice)}"
    JervisTooltipArea(
        tooltip = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                color = JervisTheme.white.copy(alpha = 0.95f),
            ) {
                Text(tooltipText, Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
            }
        },
        delayMillis = 300,
        tooltipPlacement = JervisTooltipPlacement.CursorPoint(
            offset = DpOffset((-16).dp, 16.dp),
        ),
    ) {
        TeamFeature(
            value = entry.count,
            content = {
                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                    InducementBadgeIcon(entry.type, entry.name, entry.iconSource, isHomeTeam, teamColor)
                }
            },
            leftSide = false,
        )
    }
}

@Composable
private fun InducementBadgeIcon(
    type: InducementType,
    name: String,
    iconSource: SpriteSource?,
    isHomeTeam: Boolean,
    teamColor: Color,
) {
    RenderInducementIcon(
        type = type,
        name = name,
        iconSource = iconSource,
        isHomeTeam = isHomeTeam,
        teamColor = teamColor,
        size = 32.dp,
        letterSize = 14.sp,
    )
}
