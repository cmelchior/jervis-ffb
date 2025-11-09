package com.jervisffb.ui.game.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.PlayerStatusEffect
import com.jervisffb.engine.model.SkillValue
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_minus
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_plus
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiKeywordData
import com.jervisffb.ui.game.model.UiPlayerCard
import com.jervisffb.ui.game.model.UiSkillData
import com.jervisffb.ui.game.view.JervisTheme.rulebookBlue
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.menu.components.JervisDialogHeader
import com.jervisffb.ui.utils.darken
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.jsp
import com.jervisffb.ui.utils.lighten
import com.jervisffb.ui.utils.onClickWithSmallDragControl
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerEditorCard(player: UiPlayerCard) {
    var updateTrigger by remember { mutableStateOf(0) }
    val teamColor = when (player.model.isOnHomeTeam() == true) {
        true -> JervisTheme.homeTeamColor
        false -> JervisTheme.awayTeamColor
    }

    val lightTeamColor = teamColor.lighten(0.15f)
    val darkTeamColor = teamColor.darken(0.25f)
    val darkerTeamColor = teamColor.darken(0.5f)
    val innerBorderColor = JervisTheme.white
    val borderSize = 6.jdp
    val bigBorderSize = 8.jdp

    val statboxLabelColor = darkerTeamColor
    val statboxContentColor = darkTeamColor

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onPointerEvent(PointerEventType.Enter) { /* Swallow it */ }
            .onPointerEvent(PointerEventType.Exit) { /* Swallow it */ }
        ,
//        shape = RectangleShape,
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        colors = CardDefaults.cardColors(containerColor = darkerTeamColor),
//        border = BorderStroke(width = bigBorderSize, color = teamColor),
    ) {
        Row() {
            PlayerStatsAndImage(player, updateTrigger, bigBorderSize, teamColor, borderSize, statboxLabelColor, statboxContentColor, lightTeamColor, innerBorderColor)
            PlayerEditor(player, updateTrigger, borderSize, innerBorderColor, { updateTrigger += 1})
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
    val color = JervisTheme.rulebookBlue
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
                                    color = JervisTheme.rulebookBlue
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
                                    JervisTheme.rulebookBlue
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
                    JervisButton(text = "Cancel", onClick = { resume?.invoke(null) })
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
                        cont.resume(chosen) { cause, _, context ->
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
private fun PlayerStatsAndImage(
    player: UiPlayerCard,
    updateTrigger: Int,
    bigBorderSize: Dp,
    teamColor: Color,
    borderSize: Dp,
    statboxLabelColor: Color,
    statboxContentColor: Color,
    lightTeamColor: Color,
    innerBorderColor: Color
) {
    key(updateTrigger) {
        val statWidthFactor = 0.3f
        val imageWidthFactor = 0.7f

        BoxWithConstraints(
            modifier = Modifier.aspectRatio(555f/1362f),
        ) {
            val boxWidth = maxWidth
            val portraitHeight = (boxWidth - bigBorderSize * 2) * imageWidthFactor * 147f/95f

            // Stats and image
            Column(
                modifier = Modifier
                    .paperBackground(teamColor) // Background color for the entire card
                    .padding(bigBorderSize),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .heightIn(min = (portraitHeight - (borderSize*3))/4f)
                    ,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(statWidthFactor)
                            .padding(end = borderSize)
                            .fillMaxHeight()
                            .background(teamColor)
                        ,
                        verticalArrangement = Arrangement.spacedBy(borderSize),
                    ) {
                        StatBox(
                            Modifier.fillMaxSize(),
                            "MV",
                            player.model.move.toString(),
                            statboxLabelColor,
                            statboxContentColor,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(0.7f)
                            .fillMaxSize()
                            .paperBackground(lightTeamColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Player type
                        val fontSize = 26.jsp
                        val type = remember(player.model.position.titleSingular) {
                            val originalTitle = player.model.position.titleSingular
                            val splitTitle = originalTitle.split(" ")
                            // We want to have the common case of two words in the title be split
                            // across two lines as it looks better as the chance of getting too
                            // close to the sides increases. This heuristic can probably be improved
                            // in the future.
                            if (splitTitle.size == 2) {
                                "${splitTitle[0]}\n${splitTitle[1]}"
                            } else {
                                originalTitle
                            }
                        }
                        Text(
                            modifier = Modifier.padding(2.jdp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = type,
                            fontFamily = JervisTheme.fontFamily(),
                            style = TextStyle.Default.copy(
                                shadow = Shadow(Color.Black, Offset(0f, 2f), 2f),
                            ),
                            color = Color.White,
                            fontSize = fontSize,
                            letterSpacing = 1.jsp,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(borderSize))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(portraitHeight)
                    ,
                ) {
                    Column(
                        modifier = Modifier.weight(statWidthFactor).fillMaxSize().padding(end = borderSize),
                        verticalArrangement = Arrangement.spacedBy(borderSize),
                    ) {
                        val model = player.model
                        val modifier = Modifier.weight(1f)
                        StatBox(modifier, "ST", model.strength.toString(), statboxLabelColor, statboxContentColor)
                        StatBox(modifier, "AG", "${model.agility}+", statboxLabelColor, statboxContentColor)
                        StatBox(modifier, "PA", if (model.passing == null) "-" else "${model.passing}+", statboxLabelColor, statboxContentColor)
                        StatBox(modifier, "AV", "${model.armorValue}+", statboxLabelColor, statboxContentColor)
                    }
                    Box(
                        modifier = Modifier
                            .weight(imageWidthFactor)
                            .fillMaxSize()
                            .border(
                                width = borderSize,
                                color = innerBorderColor,
                            )
                        ,
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Image(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RectangleShape)
                                .graphicsLayer {
                                    scaleX = 1.05f
                                    scaleY = 1.05f
                                }
                            ,
                            // .aspectRatio(95f / 147f).fillMaxWidth(),
                            bitmap = IconFactory.getPlayerPortrait(player.model.id),
                            filterQuality = FilterQuality.None,
                            contentDescription = "Image of ${player.model.name}",
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.Center,
                        )

                        Box(
                            modifier = Modifier
                                .padding(bottom = borderSize)
                                .background(Color.Black.copy(0.0f))
                                .padding(
                                    start = borderSize + 3.dp,
                                    end = borderSize + 3.dp,
                                    bottom = 4.jdp,
                                    top = 4.jdp
                                )
                            ,
                            contentAlignment = Alignment.Center,
                        ) {
                            PlayerName(player.model.name, borderSize)
                        }
                    }
                }

                // Player level
                Row(
                    modifier = Modifier
                        .padding(top = bigBorderSize, bottom = bigBorderSize)
                        .fillMaxWidth(),
                ) {
                    val fontSize = 16.jsp
                    Text(
                        textAlign = TextAlign.Start,
                        text = player.model.level.description,
                        fontFamily = JervisTheme.fontFamily(),
                        style = TextStyle.Default.copy(
                            shadow = Shadow(Color.Black, Offset(0f, 2f), 2f),
                        ),
                        color = Color.White,
                        maxLines = 1,
                        fontSize = fontSize,
                        letterSpacing = 1.jsp,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        textAlign = TextAlign.Start,
                        text = "${player.model.starPlayerPoints} SPP",
                        fontFamily = JervisTheme.fontFamily(),
                        style = TextStyle.Default.copy(
                            shadow = Shadow(Color.Black, Offset(0f, 2f), 2f),
                        ),
                        color = Color.White,
                        maxLines = 1,
                        fontSize = fontSize,
                        letterSpacing = 1.jsp,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Skill content
                Column(
                    modifier = Modifier
                        .paperBackground()
                        .border(borderSize, innerBorderColor)
                        .fillMaxSize()
                    ,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(borderSize*2)
                        ,
                    ) {
                        val skillFontSize = 16.jsp
                        val skills = player.model.skills
                        if (skills.isEmpty()) {
                            Text(
                                modifier = Modifier.padding(0.jdp).fillMaxWidth(),
                                fontSize = skillFontSize,
                                color = JervisTheme.contentTextColor,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic,
                                text = "No Skills"
                            )
                        } else {
                            player.model.skills.forEach {
                                Text(
                                    fontSize = skillFontSize,
                                    lineHeight = 1.5.em,
                                    color = JervisTheme.contentTextColor,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(0.jdp).fillMaxWidth(),
                                    text = it.name + if (it.compulsory) "*" else "",
                                    textDecoration = if (it.used) TextDecoration.LineThrough else TextDecoration.None,
                                )
                            }
                        }
                    }
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(borderSize*2)
                        ,
                        horizontalArrangement = Arrangement.spacedBy(borderSize, Alignment.End),
                        verticalArrangement = Arrangement.spacedBy(borderSize),
                    ) {
                        when (player.model.state) {
                            PlayerState.PRONE -> {
                                StatusText("Prone", JervisTheme.statusProne)
                            }
                            PlayerState.STUNNED -> {
                                StatusText("Stunned", JervisTheme.statusStunned)
                            }
                            PlayerState.STUNNED_OWN_TURN -> {
                                StatusText("Stunned", JervisTheme.statusStunned)
                            }
                            else -> { /* Do nothing */ }
                        }
                        player.model.statusEffects.forEach { statusEffect ->
                            when (statusEffect) {
                                PlayerStatusEffect.ROOTED -> {
                                    StatusText("Rooted", JervisTheme.statusRooted)
                                }
                                PlayerStatusEffect.DISTRACTED -> {
                                    StatusText("Distracted", JervisTheme.statusDistracted)
                                }
                                PlayerStatusEffect.CHOMPED -> {
                                    StatusText("Chomped", JervisTheme.statusChomped)
                                }
                                PlayerStatusEffect.EYE_GOUGE -> {
                                    StatusText("Eye Gouge", JervisTheme.statusEyeGouge)
                                }
                                PlayerStatusEffect.DODGY_SNACK -> {
                                    StatusText("Dodgy Snack", JervisTheme.statusDodgySnack)
                                }
                                PlayerStatusEffect.HYPNO_GAZED -> {
                                    // Ignore
                                }
                            }
                        }
                        player.model.keywords.forEach { keyword ->
                            StatusText(
                                text = keyword.description,
                                textColor = JervisTheme.white,
                                backgroundColor = if (player.model.isOnHomeTeam()) JervisTheme.rulebookRed else rulebookBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerEditor(player: UiPlayerCard, updateTrigger: Int, borderSize: Dp, innerBorderColor: Color, notifyUpdate: () -> Unit) {
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
                0 -> SkillSelectorTab(player, updateTrigger, borderSize, notifyUpdate) { skillValueSelectorVisible ->
                    userScrollEnabled = !skillValueSelectorVisible
                }
                1 -> StatsSelectorTab(player, updateTrigger, notifyUpdate)
                2 -> KeywordsSelectorTab(player, updateTrigger, borderSize, notifyUpdate)
            }
        }
    }
}


@Composable
private fun SkillSelectorTab(
    player: UiPlayerCard,
    updateTrigger: Int,
    borderSize: Dp,
    notifyUpdate: () -> Unit,
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
                            SelectSkillButton(player.model, skill, notifyUpdate) { data, onNewSkill ->
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
    player: UiPlayerCard,
    updateTrigger: Int,
    notifyUpdate: () -> Unit
) {
    var baseMove by remember(player, updateTrigger) { mutableStateOf(player.model.baseMove.toString()) }
    var baseStrength by remember(player, updateTrigger) { mutableStateOf(player.model.baseStrength.toString()) }
    var baseAgility by remember(player, updateTrigger) { mutableStateOf("${player.model.baseAgility}+") }
    var basePassing by remember(player, updateTrigger) { mutableStateOf(player.model.basePassing?.let { "$it+" } ?: "-") }
    var baseArmour by remember(player, updateTrigger) { mutableStateOf("${player.model.baseArmorValue}+") }

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
                    model.baseMove -= 1
                    rules.updatePlayerStat(model, StatModifier.Type.MA)
                    baseMove = model.baseMove.toString()
                    notifyUpdate()
                }
            },
            onIncreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.move < rules.moveRange.last) {
                    model.baseMove += 1
                    rules.updatePlayerStat(model, StatModifier.Type.MA)
                    baseMove = model.baseMove.toString()
                    notifyUpdate()
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
                    model.baseStrength -= 1
                    rules.updatePlayerStat(model, StatModifier.Type.ST)
                    baseStrength = model.baseStrength.toString()
                    notifyUpdate()
                }
            },
            onIncreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.strength < rules.strengthRange.last) {
                    model.baseStrength += 1
                    rules.updatePlayerStat(model, StatModifier.Type.ST)
                    baseStrength = model.baseStrength.toString()
                    notifyUpdate()
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
                    model.baseAgility -= 1
                    rules.updatePlayerStat(model, StatModifier.Type.AG)
                    baseAgility = "${model.baseAgility}+"
                    notifyUpdate()
                }
            },
            onIncreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.agility < rules.agilityRange.last) {
                    model.baseAgility += 1
                    rules.updatePlayerStat(model, StatModifier.Type.AG)
                    baseAgility = "${model.baseAgility}+"
                    notifyUpdate()
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
                val rules = model.team.game.rules
                if (model.passing == rules.passingRange.first) {
                    model.basePassing = null
                    rules.updatePlayerStat(model, StatModifier.Type.PA)
                    basePassing = "-"
                    notifyUpdate()
                } else if (model.passing != null && model.passing!! > rules.passingRange.first) {
                    model.basePassing = model.passing!! - 1
                    rules.updatePlayerStat(model, StatModifier.Type.PA)
                    basePassing = "${model.basePassing}+"
                    notifyUpdate()
                }
            },
            onIncreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.basePassing == null) {
                    model.basePassing = rules.passingRange.first
                    rules.updatePlayerStat(model, StatModifier.Type.PA)
                    basePassing = model.basePassing.toString()
                    notifyUpdate()
                } else if (model.passing!! < rules.passingRange.last) {
                    model.basePassing = (model.basePassing!! + 1)
                    rules.updatePlayerStat(model, StatModifier.Type.PA)
                    basePassing = "${model.basePassing}+"
                    notifyUpdate()
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
                    model.baseArmorValue -= 1
                    rules.updatePlayerStat(model, StatModifier.Type.AV)
                    baseArmour = "${model.baseArmorValue}+"
                    notifyUpdate()
                }
            },
            onIncreaseValue = {
                val model = player.model
                val rules = model.team.game.rules
                if (model.armorValue < rules.armorValueRange.last) {
                    model.baseArmorValue += 1
                    rules.updatePlayerStat(model, StatModifier.Type.AV)
                    baseArmour = "${model.baseArmorValue}+"
                    notifyUpdate()
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
    notifyUpdate: () -> Unit,
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
                    SelectKeywordButton(player.model, keyword, color, notifyUpdate)
                }
            }
        }
    }
}

@Composable
private fun StatusText(text: String, backgroundColor: Color, textColor: Color = JervisTheme.contentTextColor) {
    Text(
        text = text,
        modifier = Modifier.background(backgroundColor).padding(4.jdp),
        fontSize = 14.jsp,
        lineHeight = 1.0.em,
        color = textColor,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun BoxScope.PlayerName(name: String, borderSize: Dp) {
    // Because Compose does not support drop shadow on Outlined Text
    // we fake it by first blurring the outline and then render the rest
    val playerNameStyle = MaterialTheme.typography.bodySmall.copy(
        textAlign = TextAlign.Center,
        fontFamily = JervisTheme.fontFamily(),
        fontSize = 20.jsp,
        lineHeight = 1.4.em,
        letterSpacing = 1.sp,
    )
    val fontOutlineSize = with(LocalDensity.current) { 6.jdp.toPx() }

    // Drop shadow
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                renderEffect = BlurEffect(2.jdp.toPx(), 2.jdp.toPx(), TileMode.Clamp)
            }
        ,
        text = name,
        overflow = TextOverflow.Ellipsis,
        style = playerNameStyle.copy(
            color = JervisTheme.black,
            drawStyle = Stroke(
                miter = fontOutlineSize,
                width = fontOutlineSize,
                join = StrokeJoin.Round
            )
        ),
    )

    // Outline
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = name,
        overflow = TextOverflow.Ellipsis,
        style = playerNameStyle.copy(
            color = JervisTheme.white,
            drawStyle = Stroke(
                miter = 2f, // fontOutlineSize,
                width = fontOutlineSize,
                join = StrokeJoin.Miter,
                cap = StrokeCap.Butt,

            )
        ),
    )
    // Real text
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = name,
        overflow = TextOverflow.Ellipsis,
        style = playerNameStyle.copy(
            color = JervisTheme.black,
        )
    )
}

/**
 * Render a single stat box that is part of a Playe Stats Card.
 */
@Composable
private fun StatBox(
    modifier: Modifier,
    title: String,
    value: String,
    labelBackgroundColor: Color,
    backgroundColor: Color,
) {
    Column(
        modifier = modifier.fillMaxWidth().fillMaxHeight().background(backgroundColor),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(labelBackgroundColor)
                .wrapContentHeight(align = Alignment.CenterVertically)
            ,
            text = title,
            fontSize = 12.jsp,
            lineHeight = 1.em,
            letterSpacing = 1.jsp,
            maxLines = 1,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier
                .offset(y = (-1).jdp) // Offset to make the font look slightly more aligned
                .fillMaxHeight(0.65f)
                .fillMaxWidth()
                .background(backgroundColor)
                .wrapContentHeight(align = Alignment.CenterVertically)
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
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SelectSkillButton(
    player: Player,
    skill: UiSkillData,
    notifyUpdate: () -> Unit,
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
                    player.removeSkill(skill.existingSkill!!)
                    skillLabel = skill.factory.name
                    isActive = !isActive
                    notifyUpdate()
                }
                false -> {
                    if (skill.options?.options?.isNotEmpty() == true) {
                        selectSkillOption(skill) { generatedSkill ->
                            if (generatedSkill != null) {
                                player.addSkill(generatedSkill)
                                skillLabel = generatedSkill.name
                                skill.existingSkill = generatedSkill
                                notifyUpdate()
                                isActive = !isActive
                            }
                        }
                    } else {
                        player.addSkill(skill.factory.createSkill(player, null, Duration.PERMANENT))
                        notifyUpdate()
                        isActive = !isActive
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
private fun SelectKeywordButton(player: Player, keyword: UiKeywordData, color: Color, notifyUpdate: () -> Unit) {
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
            when (isActive) {
                true -> player.keywords.remove(keyword.keyword)
                false -> player.keywords.add(keyword.keyword)
            }
            isActive = !isActive
            notifyUpdate()
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
