package com.jervisffb.ui.menu.player_markings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.ui.PLAYER_MARKINGS_MANAGER
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.markings.PlayerMarking
import com.jervisffb.ui.markings.PlayerMarkingTeam
import com.jervisffb.ui.markings.PlayerMarkingType
import com.jervisffb.ui.markings.PlayerMarkingsSettings
import com.jervisffb.ui.menu.DrawerSectionHeader
import com.jervisffb.ui.menu.components.JervisDropDownMenu
import com.jervisffb.ui.menu.components.JervisSwitch
import com.jervisffb.ui.menu.utils.DropdownEntryWithValue
import com.jervisffb.ui.menu.utils.JervisTooltip

private data class SkillChoice(
    val type: SkillType,
    val name: String,
)

internal data class PlayerMarkingEditorState(
    val index: Int?,
    val marking: PlayerMarking,
)

private const val markingWeight = 0.18f
private const val typeWeight = 0.15f
private const val teamsWeight = 0.15f
private const val skillsWeight = 0.34f
private const val enabledWeight = 0.18f

@Composable
fun PlayerMarkingsSettingsPanel(
    onCreate: () -> Unit,
    onEdit: (Int, PlayerMarking) -> Unit,
) {
    val storedSettings by PLAYER_MARKINGS_MANAGER.observePlayerMarkings()
        .collectAsState(PLAYER_MARKINGS_MANAGER.getPlayerMarkings())

    PlayerMarkingsSettingsPanel(
        settings = storedSettings,
        onEnabledChanged = { enabled ->
            PLAYER_MARKINGS_MANAGER.setPlayerMarkings(storedSettings.copy(enabled = enabled))
        },
        onMarkingEnabledChanged = { index, enabled ->
            PLAYER_MARKINGS_MANAGER.setPlayerMarkings(
                storedSettings.copy(
                    markings = storedSettings.markings.mapIndexed { currentIndex, marking ->
                        if (currentIndex == index) marking.copy(enabled = enabled) else marking
                    },
                )
            )
        },
        onCreate = onCreate,
        onEdit = onEdit,
    )
}

@Composable
private fun PlayerMarkingsSettingsPanel(
    settings: PlayerMarkingsSettings,
    onEnabledChanged: (Boolean) -> Unit,
    onMarkingEnabledChanged: (Int, Boolean) -> Unit,
    onCreate: () -> Unit,
    onEdit: (Int, PlayerMarking) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            JervisButton(
                text = "Create",
                onClick = onCreate,
                buttonColor = JervisTheme.rulebookBlue,
            )
            Spacer(modifier = Modifier.weight(1f))
            JervisTooltip(
                tooltip = if (settings.enabled) "Click to disable" else "Click to Enable",
            ) {
                JervisSwitch(
                    enabled = true,
                    checked = settings.enabled,
                    onCheckedChange = onEnabledChanged,
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.alpha(if (settings.enabled) 1f else 0.5f)) {
            PlayerMarkingsHeader()
            settings.markings.forEachIndexed { index, marking ->
                PlayerMarkingRow(
                    index = index,
                    marking = marking,
                    onEnabledChanged = { enabled -> onMarkingEnabledChanged(index, enabled) },
                    onClick = { onEdit(index, marking) },
                )
            }
        }
    }
}

@Composable
private fun PlayerMarkingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .background(JervisTheme.rulebookRed)
            .padding(horizontal = 4.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MarkingCell("Marking", markingWeight, isHeader = true)
        MarkingCell("Type", typeWeight, isHeader = true)
        MarkingCell("Teams", teamsWeight, isHeader = true)
        MarkingCell("Skills", skillsWeight, isHeader = true)
        MarkingCell("Enabled", enabledWeight, isHeader = true, alignment = Alignment.End)
    }
}

@Composable
private fun PlayerMarkingRow(
    index: Int,
    marking: PlayerMarking,
    onEnabledChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                when (index % 2 == 1) {
                    true -> JervisTheme.rulebookPaperMediumDark
                    false -> Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MarkingCell(marking.text, markingWeight)
        MarkingCell(marking.type.displayName, typeWeight)
        MarkingCell(marking.team.displayName, teamsWeight)
        MarkingCell(marking.skills.joinToString { it.description }, skillsWeight)
        Box(
            modifier = Modifier.weight(enabledWeight),
            contentAlignment = Alignment.CenterEnd
        ) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                JervisSwitch(
                    enabled = true,
                    checked = marking.enabled,
                    onCheckedChange = onEnabledChanged,
                )
            }
        }
    }
}

@Composable
private fun RowScope.MarkingCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    Text(
        modifier = Modifier.weight(weight),
        text = text,
        fontWeight = if (isHeader) FontWeight.Medium else FontWeight.Normal,
        color = if (isHeader) JervisTheme.white else JervisTheme.contentTextColor,
        textAlign = when (alignment) {
            Alignment.End -> TextAlign.End
            Alignment.Center -> TextAlign.Center
            else -> TextAlign.Start
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun PlayerMarkingEditPanel(
    modifier: Modifier = Modifier,
    marking: PlayerMarking,
    onDismiss: () -> Unit,
    onSave: (PlayerMarking) -> Unit,
    onDelete: () -> Unit,
) {
    var draft by remember(marking) { mutableStateOf(marking) }
    val skillChoices = remember {
        val skillSettings = StandardBB2025Rules().skillSettings
        val categories = listOf(
            SkillCategory.AGILITY,
            SkillCategory.DEVIOUS,
            SkillCategory.GENERAL,
            SkillCategory.MUTATIONS,
            SkillCategory.PASSING,
            SkillCategory.STRENGTH,
            SkillCategory.TRAITS,
            SkillCategory.SPECIAL_RULES,
        )
        val seenSkills = mutableSetOf<SkillType>()
        categories.mapNotNull { category ->
            val choices = skillSettings.getAvailableSkills(category)
                .map { factory ->
                    SkillChoice(factory.type, factory.type.description)
                }
                .filter { seenSkills.add(it.type) }
                .sortedBy { it.name }
            choices.takeIf { it.isNotEmpty() }?.let { category to it }
        }
    }
    val typeEntries = remember {
        listOf(
            DropdownEntryWithValue("Positional + Gained", PlayerMarkingType.ALL),
            DropdownEntryWithValue("Gained Only", PlayerMarkingType.GAINED),
        )
    }
    val teamEntries = remember {
        listOf(
            DropdownEntryWithValue("Both", PlayerMarkingTeam.BOTH),
            DropdownEntryWithValue("Own", PlayerMarkingTeam.OWN),
            DropdownEntryWithValue("Opponent", PlayerMarkingTeam.OPPONENT),
        )
    }

    val textColor = JervisTheme.contentTextColor
    val inputColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedTextColor = textColor,
        focusedLabelColor = JervisTheme.rulebookRed,
        focusedIndicatorColor = JervisTheme.rulebookRed,
        unfocusedLabelColor = if (draft.text.isEmpty()) textColor.copy(alpha = 0.4f) else textColor,
        unfocusedIndicatorColor = textColor,
    )

    Column(
        modifier = modifier,
    ) {
        DrawerSectionHeader("Edit Player Marking", topPadding = 0.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            ) {
                Text(
                    text = "Skills: ",
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                Text(
                    text = draft.skills.joinToString { it.description }.ifBlank { "No skills selected" },
                    color = textColor,
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                value = draft.text,
                onValueChange = { draft = draft.copy(text = it) },
                label = { Text("Marking") },
                singleLine = true,
                colors = inputColors,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                JervisDropDownMenu(
                    title = "Skill Type",
                    entries = typeEntries,
                    selectedEntry = typeEntries.first { it.value == draft.type },
                    modifier = Modifier.weight(1f),
                    onSelected = { draft = draft.copy(type = it.value) },
                )
                JervisDropDownMenu(
                    title = "Affected Teams",
                    entries = teamEntries,
                    selectedEntry = teamEntries.first { it.value == draft.team },
                    modifier = Modifier.weight(1f),
                    onSelected = { draft = draft.copy(team = it.value) },
                )
            }
            skillChoices.forEach { (category, choices) ->
                Column(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
                    PlayerMarkingSkillCategoryHeader(category.description)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        choices.forEach { choice ->
                            var isHovered by remember(choice.type) { mutableStateOf(false) }
                            val isSelected = choice.type in draft.skills
                            val skillColor = JervisTheme.rulebookRed
                            val containerColor = when {
                                isSelected -> skillColor
                                isHovered -> skillColor.copy(alpha = 0.25f)
                                else -> Color.Transparent
                            }
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                                Button(
                                    modifier = Modifier
                                        .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                                        .onPointerEvent(PointerEventType.Exit) { isHovered = false },
                                    onClick = {
                                        val skills = if (isSelected) {
                                            draft.skills.filterNot { it == choice.type }
                                        } else {
                                            draft.skills + choice.type
                                        }
                                        draft = draft.copy(skills = skills)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = containerColor,
                                        disabledContainerColor = JervisTheme.rulebookPaperMediumDark,
                                    ),
                                    shape = RoundedCornerShape(0.dp),
                                    border = if (isSelected) null else BorderStroke(4.dp, skillColor),
                                ) {
                                    Text(
                                        text = choice.name,
                                        fontSize = 11.sp,
                                        color = if (isSelected) JervisTheme.white else JervisTheme.contentTextColor,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            JervisButton(
                text = "Cancel",
                onClick = onDismiss,
                buttonColor = JervisTheme.rulebookBlue,
            )
            Spacer(modifier = Modifier.width(8.dp))
            JervisButton(
                text = "Delete",
                onClick = onDelete,
                enabled = marking.text.isNotEmpty() || marking.skills.isNotEmpty(),
            )
            Spacer(modifier = Modifier.weight(1f))
            JervisButton(
                text = "Save",
                onClick = { onSave(draft) },
                enabled = draft.text.isNotBlank() && draft.skills.isNotEmpty(),
                buttonColor = JervisTheme.rulebookBlue,
            )
        }
    }
}

@Composable
private fun PlayerMarkingSkillCategoryHeader(text: String) {
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = JervisTheme.rulebookRed,
        )
    }
    TitleBorder(JervisTheme.rulebookRed)
}

private val PlayerMarkingType.displayName: String
    get() = when (this) {
        PlayerMarkingType.ALL -> "All"
        PlayerMarkingType.GAINED -> "Gained"
    }

private val PlayerMarkingTeam.displayName: String
    get() = when (this) {
        PlayerMarkingTeam.BOTH -> "Both"
        PlayerMarkingTeam.OWN -> "Own"
        PlayerMarkingTeam.OPPONENT -> "Opponent"
    }
