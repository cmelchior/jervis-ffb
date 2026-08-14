package com.jervisffb.ui.game.view

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.ui.game.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.game.viewmodel.ChallengeSessionViewModel

/**
 * Challenge Panel: Being shown in the bottom-right corner during challenge
 * attempts.
 *
 * It hosts two tabs: The challenge goals and rules, so they are easily
 * accessible during the attempt, and the normal action selector, so actions
 * without a dedicated UI control are still reachable while playing a challenge.
 */
@Composable
fun ChallengePanel(
    vm: ChallengeSessionViewModel,
    actions: ActionSelectorViewModel,
    hovered: Boolean,
    challengeScrollState: ScrollState,
    actionsScrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    var tabIndex by remember { mutableStateOf(0) }
    val inputs: List<GameAction> by remember(actions.availableActions) { actions.availableActions }.collectAsState(emptyList())

    // An action without a dedicated UI control blocks the engine, so keep the tabs visible while
    // any are pending, even when not hovering. The count in the title says where to look.
    val showTabs = hovered || inputs.isNotEmpty()
    // The number of tabs must stay fixed. The indicator indexes into the tabs it was given, so a
    // tab that came and went would require `tabIndex` to be clamped in the same frame. Changing
    // the titles is fine.
    val tabs = listOf(
        "Challenge Info",
        if (inputs.isEmpty()) "Extra Actions" else "Extra Actions (${inputs.size})",
    )

    // Select the action tab when a later update produces extra actions. The initial snapshot is
    // intentionally skipped by [ActionSelectorViewModel.availableActionUpdates] so a challenge
    // always starts on its information tab, while its initial actions remain visible.
    LaunchedEffect(actions.availableActionUpdates) {
        actions.availableActionUpdates.collect { updatedInputs ->
            if (updatedInputs.isNotEmpty()) {
                tabIndex = 1
            }
        }
    }

    // Once the last pending action is gone the tabs are hidden again.
    LaunchedEffect(inputs.isEmpty()) {
        if (inputs.isEmpty()) {
            tabIndex = 0
        }
    }

    Column(modifier = modifier) {
        // Same idea as `LogViewer`: The tabs are only shown while they are relevant, so a
        // collapsed panel spends all of its height on content.
        if (showTabs) {
            PrimaryTabRow(
                modifier = Modifier.fillMaxWidth().height(36.dp),
                selectedTabIndex = tabIndex,
                containerColor = Color.Transparent,
                contentColor = JervisTheme.white,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabIndex, matchContentSize = false),
                        color = JervisTheme.white,
                    )
                },
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = (tabIndex == index)
                    val interactionSource = remember { MutableInteractionSource() }
                    val isHovered by interactionSource.collectIsHoveredAsState()
                    Tab(
                        modifier = Modifier.background(
                            when {
                                isSelected -> JervisTheme.white.copy(0.2f)
                                else -> Color.Transparent
                            }
                        ),
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                color = JervisTheme.white
                            )
                        },
                        selected = isSelected,
                        onClick = { tabIndex = index },
                        interactionSource = interactionSource,
                    )
                }
            }
        }
        when (tabIndex) {
            0 -> ChallengeInfo(vm, challengeScrollState, Modifier.fillMaxSize())
            // `ActionSelector` applies the modifier it is given twice, so pass `fillMaxSize()`
            // to make it behave exactly like it does inside `ExpandableActionPanel`.
            1 -> {
                ActionSelector(
                    actions = inputs,
                    modifier = Modifier.fillMaxSize(),
                    scrollState = actionsScrollState,
                    showEmptyMessage = true
                ) { action ->
                    actions.actionSelected(action)
                }
            }
        }
    }
}

/**
 * Show the goals and rules of the challenge being attempted. We might want to extend this
 * with more information in the future.
 */
@Composable
private fun ChallengeInfo(
    vm: ChallengeSessionViewModel,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = vm.challenge.name.uppercase(),
            color = JervisTheme.white,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Goal: ${vm.challenge.goal.description}",
            color = JervisTheme.white,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        vm.challenge.goal.modifiers.forEach { goalModifier ->
            Text(
                text = "•  ${goalModifier.description}",
                color = JervisTheme.white.copy(alpha = 0.7f),
                fontSize = 14.sp,
            )
        }
        Text(
            text = "Rules",
            color = JervisTheme.white,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        vm.challenge.rules.forEach { rule ->
            Text(
                text = "•  ${rule.description}",
                color = JervisTheme.white.copy(alpha = 0.7f),
                fontSize = 14.sp,
            )
        }
    }
}
