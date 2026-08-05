package com.jervisffb.ui.game.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.viewmodel.ActionSelectorViewModel
import com.jervisffb.ui.game.viewmodel.ChallengeSessionViewModel
import com.jervisffb.ui.game.viewmodel.PanelBackground
import com.jervisffb.ui.utils.jdp
import kotlinx.coroutines.launch

/**
 * Panel used while playing a challenge. It works like [ExpandableActionPanel], but hosts both
 * the challenge info and the action selector as tabs, so both are reachable during the attempt.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ExpandableChallengePanel(
    challengeSession: ChallengeSessionViewModel,
    actions: ActionSelectorViewModel,
    background: PanelBackground,
    collapsedHeight: Dp,
    modifier: Modifier,
    expandedHeight: Dp = 300.jdp,
    animateHeightChangeMs: Int = 200
) {
    val expansionEnabled = collapsedHeight < expandedHeight
    var hovered by remember { mutableStateOf(false) }
    val isExpanded = hovered && expansionEnabled
    // Hoisted out of the tabs. Switching tab removes the other one from the composition, which
    // would otherwise drop its scroll position, and this panel also needs them when collapsing.
    val challengeScrollState = rememberScrollState()
    val actionsScrollState = rememberScrollState()

    // Animate only on expand/collapse toggle; snap immediately on window resize.
    val heightAnimatable = remember { Animatable(collapsedHeight.value) }
    LaunchedEffect(isExpanded) {
        launch {
            if (!isExpanded) {
                // The collapsed panel should show the top of whichever tab is selected.
                challengeScrollState.scrollTo(0)
                actionsScrollState.scrollTo(0)
            }
            val target = if (isExpanded) expandedHeight.value else collapsedHeight.value
            heightAnimatable.animateTo(target, animationSpec = tween(animateHeightChangeMs))
        }
    }
    LaunchedEffect(collapsedHeight) {
        if (!isExpanded) {
            heightAnimatable.snapTo(collapsedHeight.value)
        }
    }
    val animatedHeight = heightAnimatable.value.dp

    Box(
        modifier = modifier
            .height(animatedHeight)
            .background(if (isExpanded) background.hoverColor else background.color)
            // Hover is tracked even when the panel cannot grow (a tall window already shows
            // everything), since it is also what reveals the tabs.
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
        ,
        contentAlignment = Alignment.BottomStart,
    ) {
        ChallengePanel(
            vm = challengeSession,
            actions = actions,
            hovered = hovered,
            challengeScrollState = challengeScrollState,
            actionsScrollState = actionsScrollState,
            modifier = Modifier.fillMaxSize(),
        )
        if (expansionEnabled) {
            // Keep this column non-clickable, so it doesn't swallow clicks on the rightmost
            // part of the tab row.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(48.dp)
                    .fillMaxHeight()
                ,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    fontFamily = JervisTheme.extendedDefaultFontFamily(),
                    text = if (isExpanded) "▼" else "▲",
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}
