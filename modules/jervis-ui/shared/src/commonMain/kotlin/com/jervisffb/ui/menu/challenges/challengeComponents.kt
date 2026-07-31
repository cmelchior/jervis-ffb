package com.jervisffb.ui.menu.challenges

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_icon_thumbs_up_large
import com.jervisffb.shared.generated.resources.jervis_icon_thumbs_up_large_selected
import com.jervisffb.shared.generated.resources.jervis_icon_trophy
import com.jervisffb.shared.generated.resources.jervis_icon_trophy_1st
import com.jervisffb.shared.generated.resources.jervis_icon_trophy_disabled
import com.jervisffb.shared.generated.resources.jervis_star_selected
import com.jervisffb.shared.generated.resources.jervis_star_unselected
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.menu.challenges.ChallengeUserState.SolvedState.*
import com.jervisffb.ui.menu.utils.JervisTooltip
import org.jetbrains.compose.resources.painterResource

/**
 * A star used to mark a challenge as a favorite. Filled + orange when favorite,
 * dimmed outline otherwise.
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
        UNSOLVED -> Res.drawable.jervis_icon_trophy_disabled
        SOLVED -> Res.drawable.jervis_icon_trophy
        BEST_IN_CLASS -> Res.drawable.jervis_icon_trophy_1st
    }
    val description = when (state.solved) {
        UNSOLVED -> "Unsolved"
        SOLVED -> buildString {
            append("Solved")
            if (state.solvedDate != null) {
                append(" ")
                append(state.getFormattedDate())
            }
        }
        BEST_IN_CLASS -> buildString {
            append("Solved as Best in Class")
            if (state.solvedDate != null) {
                append("  ")
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
 * Control
 * An up/down rating control. The community score is shown between the two buttons and the button
 * matching the user's current vote is highlighted. Tapping the highlighted direction again clears
 * the vote (handled by [ChallengeStore.setVote]).
 */
@Composable
fun RatingControl(
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
 * Show the starting state of the challenge on the pitch
 */
@Composable
fun ChallengeScreenshot() {
    Box(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .aspectRatio(26/15f)
            .fillMaxWidth()
            .background(JervisTheme.rulebookPaperMediumDark.copy(alpha = 0.5f))
            .border(2.dp, Color.Black)
        ,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Challenge Screenshot",
            color = JervisTheme.contentTextColor
        )

    }
}

fun categoryColor(category: ChallengeCategory): Color = when (category) {
    ChallengeCategory.CROWD_SURFING -> JervisTheme.rulebookRed
    ChallengeCategory.BLOCKING -> JervisTheme.rulebookBlue
    ChallengeCategory.SCORING -> JervisTheme.rulebookGreen
    ChallengeCategory.BREAK_THE_CAGE -> JervisTheme.rulebookOrange
    ChallengeCategory.ONE_TURN_TOUCHDOWNS -> JervisTheme.rulebookPurple
}

fun categoryTextColor(category: ChallengeCategory): Color {
    return when (category) {
        ChallengeCategory.BLOCKING,
        ChallengeCategory.CROWD_SURFING,
        ChallengeCategory.ONE_TURN_TOUCHDOWNS,
        ChallengeCategory.SCORING -> JervisTheme.white
        ChallengeCategory.BREAK_THE_CAGE -> JervisTheme.black
    }
}
