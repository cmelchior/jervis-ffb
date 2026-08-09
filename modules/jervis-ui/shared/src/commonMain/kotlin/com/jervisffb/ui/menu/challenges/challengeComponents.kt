package com.jervisffb.ui.menu.challenges

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.challenge.ChallengeCategory
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.challenge_default_pitch
import com.jervisffb.shared.generated.resources.jervis_icon_thumbs_up_large
import com.jervisffb.shared.generated.resources.jervis_icon_thumbs_up_large_selected
import com.jervisffb.shared.generated.resources.jervis_icon_trophy
import com.jervisffb.shared.generated.resources.jervis_icon_trophy_1st
import com.jervisffb.shared.generated.resources.jervis_icon_trophy_disabled
import com.jervisffb.shared.generated.resources.jervis_star_selected
import com.jervisffb.shared.generated.resources.jervis_star_unselected
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.pitch.Pitch
import com.jervisffb.ui.game.view.pitch.calculatePitchSizeData
import com.jervisffb.ui.game.viewmodel.PitchViewModel
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.challenges.data.ChallengeUserState
import com.jervisffb.ui.menu.utils.JervisTooltip
import com.jervisffb.ui.utils.pixelSize
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * This file contains various Compose components used across the Challenge List
 * and Challenge Detail screens.
 */

/**
 * A star used to mark a challenge as a favorite. Colored when selected, grayed out when not.
 */
@Composable
fun FavoriteStar(
    isFavorite: Boolean,
    onToggle: () -> Unit,
) {
    val icon = when (isFavorite) {
        true -> Res.drawable.jervis_star_selected
        false -> Res.drawable.jervis_star_unselected
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(2.dp))
            .clickable(onClick = { onToggle() })
        ,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(icon),
            alpha = if (isFavorite) 1f else 0.8f,
            contentDescription = if (isFavorite) "Favorite Challenge Star" else "Not-Favorite Challenge Star",
        )
    }

}

/**
 * A trophy that fades in when a challenge is solved and out when it is not.
 */
@Composable
fun SolvedTrophy(
    state: ChallengeUserState
) {
    val alpha by animateFloatAsState(if (state.isSolved()) 1f else 0.4f)
    val icon = when (state.solved) {
        ChallengeUserState.SolvedState.UNSOLVED -> Res.drawable.jervis_icon_trophy_disabled
        ChallengeUserState.SolvedState.SOLVED -> Res.drawable.jervis_icon_trophy
        ChallengeUserState.SolvedState.BEST_IN_CLASS -> Res.drawable.jervis_icon_trophy_1st
    }
    val description = when (state.solved) {
        ChallengeUserState.SolvedState.UNSOLVED -> "Not Completed"
        ChallengeUserState.SolvedState.SOLVED -> buildString {
            append("Completed")
            if (state.score != null) {
                append(" ")
                append(state.getFormattedDate())
            }
        }
        ChallengeUserState.SolvedState.BEST_IN_CLASS -> buildString {
            when (state.score is ChallengeScore.CompletionOnly) {
                true -> append("Completed")
                false -> append("Completed as Best in Class")
            }
            if (state.score != null) {
                append(" ")
                append(state.getFormattedDate())
            }
        }
    }
    JervisTooltip(
        tooltip = description
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(icon),
                alpha = alpha,
                contentScale = ContentScale.None,
                contentDescription = description,
            )
        }
    }
}

/**
 * A "thumbs up" used to control voting. Will use a colored icon when selected, a more dimmed one
 * when not.
 */
@Composable
fun VoteControl(
    voted: Boolean,
    communityScore: Int,
    onVote: (Boolean) -> Unit,
    contentColor: Color,
    showCounter: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showCounter) {
            Text(
                text = communityScore.toString(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = contentColor,
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = { onVote(!voted) })
            ,
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.size(28.dp),
                painter = painterResource(when (voted) {
                    true -> Res.drawable.jervis_icon_thumbs_up_large_selected
                    false -> Res.drawable.jervis_icon_thumbs_up_large
                }),
                alpha = if (voted) 1f else 0.8f,
                contentDescription = "+$communityScore Votes",
            )
        }
    }
}

/**
 * A small colored label showing a challenge's [ChallengeCategory].
 */
@Composable
fun CategoryChip(category: ChallengeCategory, modifier: Modifier = Modifier) {
    val background = categoryColor(category)
    Text(
        modifier = modifier
            .background(background, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        text = category.label.uppercase(),
        lineHeight = 1.em,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = categoryTextColor(category),
    )
}


/**
 * Show the starting state of the challenge on the pitch.
 *
 * [screenModel] is a game built only to be looked at, and is null while one is
 * still being put together. Building it can mean reading a position off disk,
 * hence the placeholder.
 *
 * [pitchWidth] and [pitchHeight] have to be known before the game is, because
 * the frame is sized to the pitch that is coming rather than to an aspect
 * ratio. Every challenge so far uses the standard pitch; a ruleset with a
 * different one (BB7) would need to say so up front to avoid a jump.
 *
 * Only the pitch is drawn; dugouts are a separate part of the game screen.
 */
@Composable
fun ChallengeScreenshot(
    screenModel: GameScreenModel?,
    pitchWidth: Int = 26,
    pitchHeight: Int = 15,
    // How long the pitch takes to fade in once it has something to show.
    previewFadeInMs: Int = 300,
    // Border drawn around the pitch, and the chalk margin inside it. Has to match
    // what is handed to `Pitch` below, or the placeholder and the real pitch end up
    // different sizes.
    previewBorder: Dp = 2.dp,
    // The minimum amount of time the loading indicator is shown for, regardless
    // of when it was shown.
    minimumShowTime: Duration = 1.seconds,
    // When shown, the Progress Indicator will fade in over this duration.
    progressFadeIn: Duration = 300.milliseconds,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
        ,
        contentAlignment = Alignment.Center
    ) {
        // Pitch squares are whole pixels, so the pitch is a little narrower than
        // the space it is given and its height does not follow from the aspect
        // ratio. Sizing the frame with the same calculation the pitch uses is
        // what stops it resizing when the real one arrives.
        val density = LocalDensity.current
        val pitchSize = remember(maxWidth, density, pitchWidth, pitchHeight) {
            calculatePitchSizeData(
                availableWidth = maxWidth,
                density = density,
                pitchWidth = pitchWidth,
                pitchHeight = pitchHeight,
                borderBrushSize = previewBorder,
                // Matches PitchDetails.NICE, which is what the pitch falls back
                // to until the game reports the weather.
                drawPitchMarkers = true,
            )
        }

        // Whole-pixel squares leave the pitch a little narrower than the space
        // available. Rather than change the grid, draw it at its natural size
        // and scale the result up to close the gap. The factor is at most a few
        // percent, and exactly 1 whenever the width happens to divide evenly.
        val availableWidthPx = with(density) { maxWidth.roundToPx() }
        val scale = availableWidthPx.toFloat() / pitchSize.totalPitchWidthPx
        val scaledHeightPx = (pitchSize.totalPitchHeightPx * scale).roundToInt()

        Box(
            modifier = Modifier
                // Background and border live out here so they are not scaled;
                // the border stays a crisp PREVIEW_BORDER at the true full width.
                .pixelSize(IntSize(availableWidthPx, scaledHeightPx))
                .background(JervisTheme.rulebookPaperMediumDark.copy(alpha = 0.5f))
                .border(previewBorder, Color.Black)
            ,
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    // Measured at the exact pitch size, so `Pitch` below still floors
                    // to the same square size the placeholder was laid out for, then
                    // scaled about its centre to cover the frame.
                    .pixelSize(IntSize(pitchSize.totalPitchWidthPx, pitchSize.totalPitchHeightPx))
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                ,
                contentAlignment = Alignment.Center
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(Res.drawable.challenge_default_pitch),
                    contentDescription = "Pitch acting as placeholder while game state is loading",
                    // Same scaling as BackgroundImageLayer, which is what draws this
                    // very image once the game is running. The pitch is wider than
                    // the artwork, so stretching to fit would squash it vertically.
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                )
                when (screenModel) {
                    null -> { /* Keep showing the placeholder pitch. */ }
                    else -> {
                        val pitchViewModel = remember(screenModel) {
                            PitchViewModel(screenModel, screenModel.uiState, screenModel.hoverPlayerFlow)
                        }

                        // The game is handed over as soon as it has been started, which
                        // is before its loop has produced anything to draw. Waiting for
                        // the first snapshot means the fade starts on a pitch that
                        // already has its players, rather than on an empty field with
                        // everyone appearing halfway through.
                        val snapshot by remember(pitchViewModel) { pitchViewModel.observeSnapshot() }.collectAsState(null)
                        val pitchAlpha by animateFloatAsState(
                            targetValue = if (snapshot != null) 1f else 0f,
                            animationSpec = tween(previewFadeInMs),
                        )
                        // Not sure if it is worth having a loader. For short loading times it just looks like flicker
                        //    if (pitchAlpha < 1f) {
                        //        CircularProgressIndicator(
                        //            modifier = Modifier.alpha(1f - pitchAlpha),
                        //            color = JervisTheme.rulebookRed,
                        //        )
                        //    }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(pitchAlpha)
                                // The pitch is a live game underneath. Intercept all non-scroll events
                                // to prevent accidentally triggering in-game events.
                                .pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            if (event.type != PointerEventType.Scroll) {
                                                event.changes.forEach { it.consume() }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Pitch(Modifier, pitchViewModel, borderBrushSize = previewBorder)
                        }
                    }
                }
                DelayedChallengeLoadingIndicator(
                    isLoading = screenModel == null,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    minimumShowTime = minimumShowTime,
                    progressFadeIn = progressFadeIn,
                ) {
                    LoadingIndicator()
                }
            }
        }
    }
}

@Composable
fun DelayedChallengeLoadingIndicator(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    minimumShowTime: Duration = 1.seconds,
    progressFadeIn: Duration = 300.milliseconds,
    content: @Composable () -> Unit,
) {
    var showLoadingIndicator by remember { mutableStateOf(false) }
    var loadingIndicatorShownAt by remember { mutableStateOf<TimeMark?>(null) }
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(400.milliseconds)
            showLoadingIndicator = true
            loadingIndicatorShownAt = TimeSource.Monotonic.markNow()
        } else if (showLoadingIndicator) {
            val elapsed = loadingIndicatorShownAt?.elapsedNow() ?: minimumShowTime
            val remaining = (minimumShowTime - elapsed).coerceAtLeast(0.milliseconds)
            delay(remaining)
            showLoadingIndicator = false
            loadingIndicatorShownAt = null
        }
    }
    AnimatedVisibility(
        visible = showLoadingIndicator,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(progressFadeIn.inWholeMilliseconds.toInt())),
        exit = ExitTransition.None,
    ) {
        content()
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = "Loading...",
            color = JervisTheme.white,
            fontSize = 12.sp,
        )
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = JervisTheme.white,
            trackColor = JervisTheme.white.copy(alpha = 0.25f),
        )
    }
}

private fun categoryColor(category: ChallengeCategory): Color = when (category) {
    ChallengeCategory.CROWD_SURFING -> JervisTheme.rulebookRed
    ChallengeCategory.BLOCKING -> JervisTheme.rulebookBlue
    ChallengeCategory.SCORING -> JervisTheme.rulebookGreen
    ChallengeCategory.BREAK_THE_CAGE -> JervisTheme.rulebookOrange
    ChallengeCategory.ONE_TURN_TOUCHDOWNS -> JervisTheme.rulebookPurple
}

private fun categoryTextColor(category: ChallengeCategory): Color {
    return when (category) {
        ChallengeCategory.BLOCKING,
        ChallengeCategory.CROWD_SURFING,
        ChallengeCategory.ONE_TURN_TOUCHDOWNS,
        ChallengeCategory.SCORING -> JervisTheme.white
        ChallengeCategory.BREAK_THE_CAGE -> JervisTheme.black
    }
}
