package com.jervisffb.ui.game.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.actions.AddPlayerKeyword
import com.jervisffb.engine.actions.AddPlayerSkill
import com.jervisffb.engine.actions.ChangePlayerBaseStat
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RemovePlayerKeyword
import com.jervisffb.engine.actions.RemovePlayerSkill
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillValue
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_minus
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_plus
import com.jervisffb.ui.game.model.UiKeywordData
import com.jervisffb.ui.game.model.UiPlayerCard
import com.jervisffb.ui.game.model.UiSkillData
import com.jervisffb.ui.game.view.JervisTheme.rulebookBlue
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.components.JervisDialogHeader
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.jsp
import com.jervisffb.ui.utils.onClickWithSmallDragControl
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerEditorCard(
    gameModel: GameScreenModel,
    player: UiPlayerCard
) {
    var updateTrigger by remember { mutableStateOf(0) }
    val borderSize = 6.jdp

    LaunchedEffect(gameModel.uiState.devActionHandled) {
        gameModel.uiState.devActionHandled.collect {
            updateTrigger += 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onPointerEvent(PointerEventType.Enter) { /* Swallow it */ }
            .onPointerEvent(PointerEventType.Exit) { /* Swallow it */ }
        ,
    ) {
        Row() {
            BoxWithConstraints(
                modifier = Modifier.aspectRatio(555f/1362f),
            ) {
                PlayerStatsCard(flowOf(player))
            }
            PlayerEditor(gameModel, player, updateTrigger, borderSize, { action ->
                gameModel.actionProvider.userActionSelected(action)
            })
        }
    }
}

@Composable
fun rememberSkillSelectorOverlay(): suspend (player: Player, title: String, skillData: UiSkillData) -> Skill<*>? {
    var visible by remember { mutableStateOf(false) }
    var currentPlayer by remember { mutableStateOf<Player?>(null) }
    var currentTitle by remember { mutableStateOf("") }
    var currentOptions: UiSkillData? by remember { mutableStateOf(null) }
    var resume: ((Skill<*>?) -> Unit)? by remember { mutableStateOf(null) }
    val color = when (currentPlayer?.isOnHomeTeam() == true) {
        true -> JervisTheme.homeTeamColor
        false -> JervisTheme.awayTeamColor
    }
    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .paperBackground()
                .padding(top = 24.dp)
            ,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                TitleBorder(color)
                JervisDialogHeader(currentTitle, color)
                TitleBorder(color)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    when (val values = currentOptions?.options) {
                        is UiPlayerCard.IntOptions -> {
                            values.options.forEach { value ->
                                SkillValueButton(
                                    text = value.toString(),
                                    onClick = { resume?.invoke(
                                        currentOptions!!.factory.createSkill(currentPlayer!!, SkillValue.Int(value), Duration.PERMANENT)
                                    ) },
                                    color = color
                                )
                            }
                        }
                        is UiPlayerCard.KeywordOptions -> {
                            values.options.forEach { keyword ->
                                SkillValueButton(
                                    text = keyword.description,
                                    onClick = { resume?.invoke(
                                        currentOptions!!.factory.createSkill(currentPlayer!!, SkillValue.Keyword(keyword), Duration.PERMANENT)
                                    )},
                                    color
                                )
                            }
                        }
                        null -> error("No skill options available for $currentTitle")
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    JervisButton(
                        text = "Cancel",
                        onClick = { resume?.invoke(null) },
                        buttonColor = color,
                    )
                }
            }
        }
    }

    return remember {
        { player: Player, title, options->
            kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                currentPlayer = player
                currentTitle = title
                currentOptions = options
                resume = { chosen ->
                    if (cont.isActive) {
                        cont.resume(chosen) { _, _, _ ->
                            /* Do nothing on cancellation */
                        }
                    }
                    visible = false
                    resume = null
                }
                visible = true
            }
        }
    }
}

@Composable
private fun PlayerEditor(
    vm: GameScreenModel,
    player: UiPlayerCard,
    updateTrigger: Int,
    borderSize: Dp,
    handleAction: (GameAction) -> Unit
) {
    val tabs = listOf("Skills", "Stats", "Keywords")
    val pagerStateTop = rememberPagerState(initialPage = 0) { tabs.size }
    val coroutineScope = rememberCoroutineScope()
    var userScrollEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .paperBackground()
            .padding(start = 16.dp, top = 24.dp, end = 24.dp, bottom = 24.dp)
        ,
    ) {
        TitleBorder(color = player.color)
        PrimaryTabRow(
            modifier = Modifier.fillMaxWidth().height(36.dp),
            containerColor = Color.Transparent,
            contentColor = player.color,
            selectedTabIndex = 0,
            indicator = { },
            divider = @Composable { /* None */ },
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = (pagerStateTop.currentPage == index)
                Tab(
                    modifier = Modifier
                        .background(
                            if (isSelected) player.color else Color.Transparent,
                        ),
                    selected = isSelected,
                    onClick = {
                        if (userScrollEnabled) {
                            coroutineScope.launch {
                                pagerStateTop.animateScrollToPage(index)
                            }
                        }
                    },
                    text = {
                        val fontColor = if (isSelected) {
                            JervisTheme.white
                        } else {
                            player.color
                        }
                        Text(
                            text = title.uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = fontColor,
                            fontSize = 16.sp
                        )
                    },
                    selectedContentColor = player.color,
                    unselectedContentColor = JervisTheme.white,
                )
            }
        }
        TitleBorder(color = player.color)
        HorizontalPager(
            modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
            state = pagerStateTop,
            userScrollEnabled = userScrollEnabled,
        ) { page ->
            when (page) {
                0 -> SkillSelectorTab(player, updateTrigger, borderSize, handleAction) { skillValueSelectorVisible ->
                    userScrollEnabled = !skillValueSelectorVisible
                }
                1 -> StatsSelectorTab(vm, player, updateTrigger, handleAction)
                2 -> KeywordsSelectorTab(player, updateTrigger, borderSize, handleAction)
            }
        }
    }
}


@Composable
private fun SkillSelectorTab(
    player: UiPlayerCard,
    updateTrigger: Int,
    borderSize: Dp,
    handleAction: (GameAction) -> Unit,
    onValueSelectorShown: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val showSkillOptionsSelector = rememberSkillSelectorOverlay()
    val skillSections = remember(player, updateTrigger) {
        player.getSkillSections()
    }
    Column(
        modifier = Modifier
            .padding(borderSize)
            .verticalScroll(rememberScrollState())
        ,
    ) {
        key(skillSections, updateTrigger) {
            skillSections.forEach { (category, skills) ->
                Column(
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    CategoryHeader(category, if (player.model.isOnHomeTeam()) JervisTheme.homeTeamColor else JervisTheme.awayTeamColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                        ,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        skills.forEach { skill ->
                            SelectSkillButton(player.model, skill, handleAction) { data, onNewSkill ->
                                scope.launch {
                                    onValueSelectorShown(true)
                                    val selected = showSkillOptionsSelector(
                                        player.model,
                                        "Select value for ${data.factory.name}",
                                        data
                                    )
                                    onValueSelectorShown(false)
                                    onNewSkill(selected) // null if canceled
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsSelectorTab(
    vm: GameScreenModel,
    player: UiPlayerCard,
    updateTrigger: Int,
    handleAction: (GameAction) -> Unit
) {
    var baseMove by remember(player, updateTrigger) { mutableStateOf(player.model.baseMove.toString()) }
    var baseStrength by remember(player, updateTrigger) { mutableStateOf(player.model.baseStrength.toString()) }
    var baseAgility by remember(player, updateTrigger) { mutableStateOf("${player.model.baseAgility}+") }
    var basePassing by remember(player, updateTrigger) { mutableStateOf(player.model.basePassing?.let { "$it+" } ?: "-") }
    var baseArmour by remember(player, updateTrigger) { mutableStateOf("${player.model.baseArmorValue}+") }

    LaunchedEffect(vm.uiState.devActionHandled) {
        vm.uiState.devActionHandled.collect {
            baseMove = player.model.baseMove.toString()
            baseStrength = player.model.baseStrength.toString()
            baseAgility = "${player.model.baseAgility}+"
            basePassing = player.model.basePassing?.let { "$it+" } ?: "-"
            baseArmour = "${player.model.baseArmorValue}+"
        }
    }

    @Composable
    fun StatRow(
        rowNo: Int,
        rowLabel: String,
        fieldLabel: String,
        value: String,
        onDecreaseValue: () -> Unit,
        onIncreaseValue: () -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .background(color = if (rowNo % 2 == 0) Color.Transparent else JervisTheme.rulebookPaperMediumDark)
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(rowLabel)
            Spacer(modifier = Modifier.weight(1f))
            StatChangeButton(
                icon = Res.drawable.jervis_icon_menu_minus,
                description = "-1 $fieldLabel",
                onClick = onDecreaseValue,
                buttonColor = player.color,
            )
            Text(
                modifier = Modifier
                    .width(72.dp)
                ,
                text = value,
                fontSize = 30.jsp,
                lineHeight = 1.em,
                maxLines = 1,
                letterSpacing = 2.jsp,
                fontFamily = JervisTheme.fontFamily(),
                color = JervisTheme.white,
                textAlign = TextAlign.Center,
                style = TextStyle.Default.copy(
                    shadow = Shadow(JervisTheme.black, Offset(2.jdp.value, 2.jdp.value), 2.jdp.value),
                ),
            )
            StatChangeButton(
                icon = Res.drawable.jervis_icon_menu_plus,
                description = "+1 $fieldLabel",
                onClick = onIncreaseValue,
                buttonColor = player.color,
            )
        }
    }

    // TODO All of these decrease/increase actions have race conditions as the model
    //  might not update in time. That is fine for now, as we probably need to refactor
    //  the flow of these dev events anyway.
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        StatRow(
            rowNo = 0,
            rowLabel = "Base Move",
            fieldLabel = "MV",
            value = baseMove,
            onDecreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.move > rules.moveRange.first) {
                    val action = ChangePlayerBaseStat(model.id, StatModifier.Type.MA, -1)
                    handleAction(action)
                }
            },
            onIncreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.move < rules.moveRange.last) {
                    val action = ChangePlayerBaseStat(model.id, StatModifier.Type.MA, +1)
                    handleAction(action)
                }
            },
        )
        StatRow(
            rowNo = 1,
            rowLabel = "Base Strength",
            fieldLabel = "ST",
            value = baseStrength,
            onDecreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.strength > rules.strengthRange.first) {
                    val action = ChangePlayerBaseStat(model.id, StatModifier.Type.ST, -1)
                    handleAction(action)
                }
            },
            onIncreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.strength < rules.strengthRange.last) {
                    val action = ChangePlayerBaseStat(model.id, StatModifier.Type.ST, +1)
                    handleAction(action)
                }
            },
        )
        StatRow(
            rowNo = 2,
            rowLabel = "Base Agility",
            fieldLabel = "AG",
            value = baseAgility,
            onDecreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.agility > rules.agilityRange.first) {
                    val action = ChangePlayerBaseStat(model.id, StatModifier.Type.AG, -1)
                    handleAction(action)
                }
            },
            onIncreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.agility < rules.agilityRange.last) {
                    val action = ChangePlayerBaseStat(model.id, StatModifier.Type.AG, +1)
                    handleAction(action)
                }
            },
        )
        StatRow(
            rowNo = 3,
            rowLabel = "Base Passing",
            fieldLabel = "PA",
            value = basePassing,
            onDecreaseValue = {
                val model = player.model
                val currentPassing = player.model.passing
                if (currentPassing != null) {
                    val action = ChangePlayerBaseStat(model.id, StatModifier.Type.PA, -1)
                    handleAction(action)
                }
            },
            onIncreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if ((model.passing ?: 0) < rules.agilityRange.last) {
                    val action = ChangePlayerBaseStat(model.id, StatModifier.Type.PA, +1)
                    handleAction(action)
                }
            },
        )
        StatRow(
            rowNo = 4,
            rowLabel = "Base Armour",
            fieldLabel = "AV",
            value = baseArmour,
            onDecreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.armorValue > rules.armorValueRange.first) {
                    val action = ChangePlayerBaseStat(model.id, StatModifier.Type.AV, -1)
                    handleAction(action)
                }
            },
            onIncreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.armorValue < rules.armorValueRange.last) {
                    val action = ChangePlayerBaseStat(model.id, StatModifier.Type.AV, +1)
                    handleAction(action)
                }
            },
        )
    }
}

@Composable
private fun KeywordsSelectorTab(
    player: UiPlayerCard,
    updateTrigger: Int,
    borderSize: Dp,
    handleAction: (GameAction) -> Unit,
) {
    val keywordsList = remember(player, updateTrigger) {
        player.getKeywords()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(borderSize)
            .verticalScroll(rememberScrollState())
        ,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        key(keywordsList, updateTrigger) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val color = if (player.model.isOnHomeTeam()) JervisTheme.homeTeamColor else JervisTheme.awayTeamColor
                keywordsList.forEach { keyword ->
                    SelectKeywordButton(player.model, keyword, color, handleAction)
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SelectSkillButton(
    player: Player,
    skill: UiSkillData,
    handleAction: (GameAction) -> Unit,
    selectSkillOption: (data: UiSkillData, onNewSkill: (Skill<*>?) -> Unit) -> Unit
) {
    val shape = RoundedCornerShape(0.dp)
    val teamColor = if (player.isOnHomeTeam()) JervisTheme.homeTeamColor else JervisTheme.awayTeamColor
    var skillLabel by remember(skill) { mutableStateOf(skill.name) }
    var isHover by remember { mutableStateOf(false) }
    var isActive by remember(skill) { mutableStateOf(skill.isEnabled) }
    val border: BorderStroke? = when (isActive) {
        false -> BorderStroke(4.dp, teamColor)
        true -> null
    }
    val containerColor = when {
        isActive -> teamColor
        isHover -> teamColor.copy(alpha = 0.25f)
        else -> Color.Transparent
    }
    val onClick = remember(skill) {
        {
            when (isActive) {
                true -> {
                    skill.existingSkill?.let { existingSkill ->
                        val action = RemovePlayerSkill(player.id, existingSkill.skillId)
                        skillLabel = skill.factory.name
                        isActive = !isActive
                        handleAction(action)
                    }
                    Unit
                }
                false -> {
                    if (skill.options?.options?.isNotEmpty() == true) {
                        selectSkillOption(skill) { generatedSkill ->
                            if (generatedSkill != null) {
                                val action = AddPlayerSkill(player.id, generatedSkill.skillId)
                                skillLabel = generatedSkill.name
                                skill.existingSkill = generatedSkill
                                isActive = !isActive
                                handleAction(action)
                            }
                        }
                    } else {
                        val skill = skill.factory.createSkill(player, null, Duration.PERMANENT)
                        val action = AddPlayerSkill(player.id, skill.skillId)
                        isActive = !isActive
                        handleAction(action)
                    }
                }
            }
        }
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Button(
            modifier = Modifier
                .onClickWithSmallDragControl(onClick = onClick)
                .onPointerEvent(PointerEventType.Enter) { isHover = true }
                .onPointerEvent(PointerEventType.Exit) { isHover = false }
            ,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                disabledContainerColor = JervisTheme.rulebookPaperMediumDark
            ),
            shape = shape,
            onClick = { /* Handled by modifier */ },
            border = border,
            enabled = true,
        ) {
            Text(
                text = skillLabel,
                fontSize = 12.sp,
                lineHeight = 1.em,
                fontWeight = FontWeight.Medium,
                color = if (isActive) JervisTheme.white else JervisTheme.contentTextColor,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SelectKeywordButton(player: Player, keyword: UiKeywordData, color: Color, handleAction: (GameAction) -> Unit) {
    val shape = RoundedCornerShape(0.dp)
    var isHover by remember { mutableStateOf(false) }
    var isActive by remember(keyword) { mutableStateOf(keyword.isEnabled) }
    val border: BorderStroke? = when (isActive) {
        false -> BorderStroke(4.dp, color)
        true -> null
    }
    val containerColor = when {
        isActive -> color
        isHover -> color.copy(alpha = 0.25f)
        else -> Color.Transparent
    }
    val onClick = remember(keyword) {
        {
            val action = when (isActive) {
                true -> RemovePlayerKeyword(player.id, keyword.keyword)
                false -> AddPlayerKeyword(player.id, keyword.keyword)
            }
            isActive = !isActive
            handleAction(action)
        }
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Button(
            modifier = Modifier
                .onClickWithSmallDragControl(onClick = onClick)
                .onPointerEvent(PointerEventType.Enter) { isHover = true }
                .onPointerEvent(PointerEventType.Exit) { isHover = false }
            ,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                disabledContainerColor = JervisTheme.rulebookPaperMediumDark
            ),
            shape = shape,
            onClick = { /* Handled by modifier */ },
            border = border,
            enabled = true,
        ) {
            Text(
                text = keyword.name,
                fontSize = 12.sp,
                lineHeight = 1.em,
                fontWeight = FontWeight.Medium,
                color = if (isActive) JervisTheme.white else JervisTheme.contentTextColor,
            )
        }
    }
}

@Composable
private fun CategoryHeader(text: String, color: Color = JervisTheme.rulebookRed) {
//    TitleBorder(color)
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
    }
    TitleBorder(color)
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SkillValueButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    isActive: Boolean = false,
) {
    var hovered by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(0.dp)

    val border: BorderStroke? = when (isActive) {
        false -> BorderStroke(4.dp, color)
        true -> null
    }
    val containerColor = when {
        isActive -> color
        hovered -> color.copy(alpha = 0.25f)
        else -> Color.Transparent
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Button(
            modifier = Modifier
                .onClickWithSmallDragControl(onClick = onClick)
                .onPointerEvent(PointerEventType.Enter) { hovered = true }
                .onPointerEvent(PointerEventType.Exit) { hovered = false }
            ,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hovered) containerColor else Color.Transparent,
                disabledContainerColor = JervisTheme.rulebookPaperMediumDark
            ),
            shape = shape,
            onClick = { /* Handled by modifier */ },
            border = border,
            enabled = true,
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                lineHeight = 1.em,
                fontWeight = FontWeight.Medium,
                color = if (isActive) JervisTheme.white else JervisTheme.contentTextColor,
            )
        }
    }
}

@Composable
private fun StatChangeButton(
    icon: DrawableResource,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonColor: Color = rulebookBlue,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    Button(
        modifier = modifier
            .size(48.dp)
            .onClickWithSmallDragControl(onClick = onClick)
        ,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor, disabledContainerColor = JervisTheme.rulebookPaperMediumDark),
        contentPadding = PaddingValues(0.dp),
        shape = shape,
        onClick = { /* Do nothing */ },
        enabled = enabled,
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painterResource(icon),
            contentDescription = description,
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(JervisTheme.white),
        )
    }
}
