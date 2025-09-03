package com.jervisffb.ui.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.jervis.generated.MenuSection
import com.jervis.generated.getGameSettingsMenu
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_arrow_down
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_arrow_right
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.JervisDialogHeader
import com.jervisffb.ui.menu.components.SimpleSwitch
import com.jervisffb.ui.utils.applyIf
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun GameMenuDrawer(
    drawerState: DrawerState,
    menuViewModel: MenuViewModel,
    showExitDialog: (Boolean) -> Unit,
    showMenuDrawer: (Boolean) -> Unit,
) {
    val uiState: UiGameSnapshot? by menuViewModel.uiState.uiStateFlow.collectAsState(null)
    var secondLevelItems: MenuSection? by remember { mutableStateOf(null) }

    // Close submenus when the drawer is closing
    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen) {
            secondLevelItems = null
        }
    }

    Row {
        // First level, this is a mix of generated and custom menus
        Column(modifier = Modifier
            .zIndex(1f)
            .fillMaxWidth(0.35f)
            .fillMaxHeight()
            .paperBackground()
            .drawWithCache {
                val strokeWidth = 8.dp.toPx()
                val x = size.width - strokeWidth / 2
                onDrawBehind {
                    drawLine(
                        color = JervisTheme.rulebookRed,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            }
        ) {
            MenuTitleBar(
                modifier = Modifier.fillMaxWidth().height(116.dp),
                title = "Game Menu",
                fontSize = 32.dp,
                textPaddingLeft = 16.dp,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DrawerSectionHeader("Game", topPadding = 0.dp)
                DrawerButton("Save Game") { menuViewModel.showSaveGameDialog(includeDebugState = false) }

                DrawerSectionHeader("Developer Tools")
                val currentNodeDescription: String = remember(uiState) {
                    menuViewModel.uiState?.gameController?.let { it ->
                        with(it) {
                            val procedure = currentProcedure() ?: return@let "null"
                            val currentNode = currentNode() ?: return@let "${procedure.name()}[<null>]"
                            "${procedure.name()}[${currentNode.name()}]"
                        }
                    } ?: "Unknown"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 0.dp, bottom = 4.dp)
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Current Node:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JervisTheme.contentTextColor,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = currentNodeDescription,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = JervisTheme.contentTextColor,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .paperBackground(JervisTheme.rulebookPaperMediumDark.copy(alpha = 0.1f))
                        .padding(4.dp)
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val actionOwner = remember(uiState) {
                        uiState?.actionOwner?.name ?: "Both"
                    }
                    Text(
                        text = "Action Owner:",
                        fontSize = 14.sp,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        color = JervisTheme.contentTextColor,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = actionOwner,
                        fontSize = 14.sp,
                        overflow = TextOverflow.Ellipsis,
                        color = JervisTheme.contentTextColor,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                    ,
                ) {
                    Text(
                        modifier = Modifier.padding(end = 4.dp),
                        text = "Last Action Error:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JervisTheme.contentTextColor,
                    )
                    val error = remember(uiState) { menuViewModel.lastActionException }
                    Text(
                        text = error?.message ?: "None",
                        fontSize = 14.sp,
                        fontStyle = if (error != null) FontStyle.Normal else FontStyle.Italic,
                        color = JervisTheme.contentTextColor,
                    )
                }
                DrawerButton("Dump Game State to File") { menuViewModel.showSaveGameDialog(includeDebugState = true) }
                DrawerButton("Report Issue") {
                    menuViewModel.showReportIssueDialog(
                        title = "",
                        body = "",
                        error = null,
                        gameState = menuViewModel.controller
                    )
                }
                DrawerSectionHeader("Settings")
                val generatedMenu = getGameSettingsMenu()
                generatedMenu.sections.forEachIndexed { index, section ->
                    DrawerMenuGroupHeader(
                        title = section.label,
                        isSelected = (secondLevelItems == section),
                        if (index % 2 == 1) JervisTheme.rulebookPaperMediumDark.copy(alpha = 0.1f) else Color.Transparent,
                        onClick = {
                            if (secondLevelItems == null) {
                                // No menu open, open the new one
                                secondLevelItems = section
                            } else if (secondLevelItems == section) {
                                // Menu already open, just close it
                                secondLevelItems = null
                            } else if (secondLevelItems != section) {
                                // Another menu open, just replace it
                                secondLevelItems = section
                            }
                        },
                    )
                }
            }
            Box(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)) {
                JervisButton(
                    "Exit Game",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showMenuDrawer(false)
                        showExitDialog(true)
                    }
                )
            }
        }

        // 2nd level menus
        AnimatedVisibility(
            visible = (secondLevelItems != null),
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth } ,
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth } ,
                animationSpec = tween(180, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                modifier = Modifier
                    .zIndex(0f)
                    .fillMaxWidth(0.5f) // Of remaining screen size
                    .fillMaxHeight()
                    .paperBackground()
                    .onClick(
                        onClick = {
                            secondLevelItems = null
                        }
                    )
                    .drawWithCache {
                        val strokeWidth = 8.dp.toPx()
                        val x = size.width - strokeWidth / 2
                        onDrawBehind {
                            drawLine(
                                color = JervisTheme.rulebookRed,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
                    }
                    .padding(start = 16.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
            ) {
                secondLevelItems?.let { menuGroup ->
                    DrawerSectionHeader(menuGroup.label, topPadding = 0.dp)
                    menuGroup.items.forEachIndexed { index, item ->
                        val itemValue by SETTINGS_MANAGER.observeBooleanKey(item.key, item.value).collectAsState(item.value)
                        // The "onClick" effect needs to be below the Switch to avoid making it too dim
                        Box(modifier = Modifier
                            .applyIf(index % 2 == 1) {
                                paperBackground(JervisTheme.rulebookPaperMediumDark.copy(alpha = 0.1f))
                            }
                            .pointerInput(itemValue) {
                                // Prevent mouse events on menu items to reach the Drawer
                                // as clicks here will close it.
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        event.changes.forEach { it.consume() }
                                    }
                                }
                            }
                        ) {
                            SimpleSwitch(
                                label = item.label,
                                isSelected = itemValue,
                                innerPadding = 4.dp,
                                onSelected = { selected ->
                                    SETTINGS_MANAGER[item.key] = selected
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Temporary composable for "normal" buttons in the NavigationDrawer. Using the normal blue hovering ones
 * seems a bit off.
 */
@Composable
private fun DrawerButton(text: String, onClick: () -> Unit) {
    JervisButton(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        onClick = onClick,
        buttonColor = JervisTheme.rulebookRed,
        shape = RectangleShape,
    )
}

@Composable
private fun DrawerSectionHeader(title: String, topPadding: Dp = 24.dp, bottomPadding: Dp = 8.dp) {
    Spacer(modifier = Modifier.height(topPadding))
    JervisDialogHeader(title, JervisTheme.rulebookRed)
    TitleBorder(JervisTheme.rulebookRed)
    Spacer(modifier = Modifier.height(bottomPadding))
}

@Composable
private fun DrawerMenuGroupHeader(
    title: String,
    isSelected: Boolean,
    background: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .applyIf(background != Color.Transparent) {
                paperBackground(background)
            }
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(4.dp)
        ,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = JervisTheme.rulebookRed
        )
        Spacer(modifier = Modifier.weight(1f))
        val img = when (isSelected) {
            false -> Res.drawable.jervis_icon_menu_arrow_right
            true -> Res.drawable.jervis_icon_menu_arrow_down
        }
        Image(
            modifier = Modifier.size(40.dp).aspectRatio(1f),
            painter = painterResource(img),
            contentDescription = when (isSelected) {
                false -> "Open $title submenu"
                true -> "Close $title submenu"
            },
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(JervisTheme.rulebookRed)

        )
    }
}
