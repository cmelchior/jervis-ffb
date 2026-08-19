package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asComposeShader
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.skiaShader
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.ui.ICON_FACTORY
import com.jervisffb.ui.game.UiFocusStyle
import com.jervisffb.ui.game.model.UiPitchPlayer
import com.jervisffb.ui.game.view.pitch.SquarePointerEventType
import com.jervisffb.ui.game.view.pitch.jervisPointerEvent
import com.jervisffb.ui.game.viewmodel.UiPlayerTransientData
import com.jervisffb.ui.markings.PlayerMarking
import com.jervisffb.ui.markings.PlayerMarkingTeam
import com.jervisffb.ui.markings.PlayerMarkingType
import com.jervisffb.ui.markings.PlayerMarkingsSettings
import com.jervisffb.ui.menu.ChallengeGame
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.Manual
import com.jervisffb.ui.menu.Random
import com.jervisffb.ui.menu.Replay
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.utils.applyIf
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.jsp
import com.jervisffb.ui.utils.toSkiaColor
import kotlinx.coroutines.flow.map
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import kotlin.math.min

// Background highlight effect for players that are available for selecting
// **Developer's Commentary:**
// I tried to investigate if it was possible to use shaders for this to make it more customizable.
// It probably is, but the APIs are really annoying to work with. If we get back to this at some
// point.https://www.pushing-pixels.org/2022/04/09/shader-based-render-effects-in-compose-desktop-with-skia.html
// is probably the best starting point.
//
// Keeping this around while experimenting with the Shader approach.
// This can be re-enabled using `modifier.graphicsLayer(renderEffect = ...)
//
// Maybe it is worth creating all the player images with borders up front during the initialization phase.
// This would prevent the excessive amount of Shader creation in this file, but it is unclear if it is
// worth it. Especially if the client is resized. Also, it will probably degrade the "sharpness" of the
// border if it ends up being a part of a scaled image.
private val playerAvailableDropShadowEffect: RenderEffect = ImageFilter.makeDropShadow(
    dx = 0.0f, dy = 0.0f, sigmaX = 5.0f, sigmaY = 5.0f,
    color = JervisTheme.darkYellow.toSkiaColor()
).asComposeRenderEffect()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Player(
    modifier: Modifier,
    screenModel: GameScreenModel,
    player: UiPitchPlayer,
    transientData: UiPlayerTransientData?,
    parentHandleClick: Boolean,
    contextMenuShowing: Boolean,
) {
    val isUsingFumblerooskiFlow = remember(player.id) {
        screenModel.uiState.uiDecorations.fumblerooskiEnabled.map { it?.id == player.id }
    }
    val isUsingFumblerooski by isUsingFumblerooskiFlow.collectAsState(false)
    val isMovePlayersFreelyMode by screenModel.isMovePlayersFreely.collectAsState()
    val playerImage = remember(player) { ICON_FACTORY.getPlayerIcon(player) }
    val ballImage = ICON_FACTORY.getHeldBallOverlay(isUsingFumblerooski)
    val playerMarkingsSettings by screenModel.playerMarkings.collectAsState()
    val playerMarkings = remember(player, playerMarkingsSettings, screenModel.mode) {
        playerMarkingsSettings.matchingMarkings(
            player = player,
            isOwnTeam = screenModel.isOwnTeam(player.isOnHomeTeam),
        )
    }
    val playerAlpha = if (player.hasActivated || player.isStunned) 0.5f else 1.0f
    var isTempSelected by player.isTemporarySelected
    var playerModifier: Modifier = modifier.aspectRatio(1f)

    if (player.isSelectable && !parentHandleClick) {
        playerModifier = playerModifier.clickable {
            player.selectedAction!!(screenModel, player)
        }
    }
    if (transientData?.onHover != null) {
        playerModifier =
            playerModifier.onPointerEvent(eventType = PointerEventType.Enter) {
                transientData.onHover.invoke()
            }
    }
    if (transientData?.onHoverExit != null) {
        playerModifier =
            playerModifier
                .onPointerEvent(eventType = PointerEventType.Exit) {
                    transientData.onHoverExit.invoke()
                }
                .applyIf(player.location is PitchCoordinate) {
                    jervisPointerEvent(event = SquarePointerEventType.SecondaryClickSquare, player.location as PitchCoordinate) {
                        transientData.onHoverExit.invoke()
                    }
                }
                .applyIf(player.location is Dogout) {
                    pointerInput(player.id, player.location) {
                        awaitPointerEventScope {
                            var down = false // Track press, so we can filter Release correctly
                            while (true) {
                                val e = awaitPointerEvent()
                                when (e.type) {
                                    PointerEventType.Press -> {
                                        down = e.buttons.isSecondaryPressed
                                    }
                                    PointerEventType.Release -> {
                                        if (down) {
                                            transientData.onSecondaryClick?.invoke()
                                        }
                                        down = false
                                    }
                                }
                            }
                        }
                    }
                }
    }

    Box(modifier = playerModifier) {
        PlayerImage(
            bitmap = playerImage,
            isSelectable = player.isSelectable,
            isTempSelected = isTempSelected,
            isActionWheelFocus = contextMenuShowing,
            isGoingDown = player.isGoingDown,
            isHighlighted = player.isHighlighted,
            isActiveFocus = player.focusStyle != null && player.isActive,
            focusStyle = player.focusStyle,
            isMoveFreelyMode = isMovePlayersFreelyMode,
            alpha = playerAlpha,
        )
        if (player.carriesBall) {
            Image(
                bitmap = ballImage,
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (player.isProne) {
            Image(
                bitmap = ICON_FACTORY.getProneDecoration(),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (player.isStunned) {
            Image(
                bitmap = ICON_FACTORY.getStunnedDecoration(),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (playerMarkings.isNotEmpty()) {
            PlayerMarkingText(playerMarkings, playerAlpha)
        }
    }
}

@Composable
private fun BoxScope.PlayerMarkingText(markings: List<String>, alpha: Float) {
    Text(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .offset(y = 6.jdp)
            .fillMaxWidth()
            .alpha(alpha),
        text = markings.joinToString(separator = ""),
        maxLines = 1,
        textAlign = TextAlign.Center,
        style = JervisTheme.pitchSquareTextStyle.copy(
            shadow = Shadow(
                color = JervisTheme.black,
                blurRadius = 4f,
                offset = Offset(0f, 0f),
            ),
            color = JervisTheme.brightYellow,
            fontSize = 15.jsp,
        ),
    )
}

// Returns `true` if the player is considerd to be on the "own" team with regard
// to markings. `null` is returnd in the case where this question is ambiguous.
internal fun GameScreenModel.isOwnTeam(playerIsHomeTeam: Boolean): Boolean? {
    return when (val gameMode = mode) {
        is Manual -> when (gameMode.actionMode) {
            TeamActionMode.HOME_TEAM -> playerIsHomeTeam
            TeamActionMode.AWAY_TEAM -> !playerIsHomeTeam
            TeamActionMode.ALL_TEAMS -> null
        }
        // "Own" team is not really defined in this context
        is ChallengeGame, Random, is Replay -> null
    }
}

private fun PlayerMarkingsSettings.matchingMarkings(
    player: UiPitchPlayer,
    isOwnTeam: Boolean?, // `null` if ambigious.
): List<String> {
    if (!enabled) return emptyList()
    player.id.let { id ->
        if (id in playerOverrides) {
            return listOf(playerOverrides.getValue(id)).filter(String::isNotBlank)
        }
    }
    return generatedMarkings(player.positionalSkills, player.gainedSkills, isOwnTeam)
}

internal fun PlayerMarkingsSettings.generatedMarkings(
    skillTypes: List<SkillType>,
    gainedSkillTypes: List<SkillType>,
    isOwnTeam: Boolean?, // `null` if ambigious.
): List<String> {
    val matchingMarkings = markings.filter { marking ->
        marking.enabled &&
            marking.text.isNotBlank() &&
            marking.team.matches(isOwnTeam) &&
            marking.skills.isNotEmpty() &&
            marking.skills.all { skill ->
                val playerSkills = when (marking.type) {
                    PlayerMarkingType.ALL -> skillTypes
                    PlayerMarkingType.GAINED -> gainedSkillTypes
                }
                playerSkills.any { it == skill }
            }
    }
    val skillOrder = skillTypes.mapIndexed { index, skill -> skill to index }.toMap()
    return matchingMarkings
        .sortedBy { marking -> marking.skills.minOf { skillOrder[it] ?: Int.MAX_VALUE } }
        .map(PlayerMarking::text)
}

// If the "own team" is ambiguous, return we will also apply markings. This mean
// that both teams will be marked, even if the setting is "Own" or "Opponent.
private fun PlayerMarkingTeam.matches(isOwnTeam: Boolean?): Boolean {
    return when (this) {
        PlayerMarkingTeam.BOTH -> true
        PlayerMarkingTeam.OWN -> isOwnTeam != false
        PlayerMarkingTeam.OPPONENT -> isOwnTeam != true
    }
}

// Shader code generated mostly by ChatGPT, so probably there is a better way to achieve
// this, but for now it seems to work.
// There are some issues with drawing outside the Canvas, which is why TileMode.Decal
// is set. There are trade-offs to this as it forces us to keep the entire player icon
// inside its "square". But going outside will also look messy in some cases. Probably
// something to experiment with.
// NOTE: This template has a %color% that must be replaced using string manipulation
// It is possible to provide arguments to shaders, but this is just a quick prototype.
private val playerBorderShaderTemplate = """
        uniform shader image;
        uniform vec2 resolution; // [widthPx, heightPx] 
        uniform vec2 scaleFactor; // [xScale, yScale]
        const float blurRadius = 2.0; // Loop initializers must be constant 
        
        vec4 blur(vec2 uv) {
            vec4 sum = vec4(0.0);
            float total = 0.0;
        
            for (float x = -blurRadius; x <= blurRadius; x++) {
                for (float y = -blurRadius; y <= blurRadius; y++) {
                    vec2 offset = vec2(x, y);
                    vec4 sample = image.eval(uv + offset);
                    
                    // Use alpha or brightness as the mask source
                    float maskValue = sample.a * 5; // Multiply with 5 to increase the opaqueness of the mask
                    float weight = exp(-0.5 * (x * x + y * y) / (blurRadius * blurRadius));
                    
                    sum += vec4(maskValue) * weight;
                    total += weight;
                }
            }
            
            return sum / total;
        }
        
        half4 main(vec2 fragCoord) {
            vec2 uv = fragCoord;
            vec4 original = image.eval(uv);
            vec4 blurredMask = blur(uv);
        
            // Green background with intensity from the blurred mask
            vec4 tint = %tintColor%
            vec4 greenEffect = tint * blurredMask;
        
            return greenEffect;
        }
""".trimIndent()

val playerSelectedBorderShader = playerBorderShaderTemplate.replace(
    oldValue = "%tintColor%",
    newValue = "vec4(56.0/255.0, 162.0/255.0, 59.0/255.0, 1.0); // JervisTheme.rulebookGreenAccent"
)
val playerDownBorderShader = playerBorderShaderTemplate.replace(
    oldValue = "%tintColor%",
    newValue = "vec4(198.0/255.0, 0.0/255.0, 0.0/255.0, 1.0); // JervisTheme.rulebookRed"
)
val playerTempSelectedShader = playerBorderShaderTemplate.replace(
    oldValue = "%tintColor%",
    newValue = "vec4(0.0/255.0, 119.0/255.0, 198.0/255.0, 1.0); // JervisTheme.rulebookBlue"
)

val playerInFocus = playerBorderShaderTemplate.replace(
    oldValue = "%tintColor%",
    newValue = "vec4(255.0/255.0, 190.0/255.0, 38.0/255.0, 1.0); // JervisTheme.orange"
    //    newValue = "vec4(190.0/255.0, 38.0/255.0, 255.0/255.0, 1.0); // JervisTheme.purple"
    //    newValue = "vec4(0.0/255.0, 119.0/255.0, 198.0/255.0, 1.0); // JervisTheme.rulebookBlue"
    //    newValue = "vec4(0.0/255.0, 0.0/255.0, 0.0/255.0, 1.0); // JervisTheme.rulebookBlue"
    //    newValue = "vec4(56.0/255.0, 162.0/255.0, 59.0/255.0, 1.0); // JervisTheme.rulebookGreenAccent"
    //    newValue = "vec4(255.0/255.0, 255.0/255.0, 255.0/255.0, 1.0); // JervisTheme.rulebookBlue"
)

// Custom rendering of Player images on a pitch square.
// Players that are available will render with a glowing border around them.
// Some of the icons seem to go all the way to the edge, which means the glow doesn't render correctly
// Maybe we need to draw the image to a slightly larger canvas before applying the blur. This requires
// more experimentation.
@Composable
fun PlayerImage(
    bitmap: ImageBitmap,
    isSelectable: Boolean,
    isTempSelected: Boolean,
    isActionWheelFocus: Boolean,
    isGoingDown: Boolean,
    isHighlighted: Boolean,
    isActiveFocus: Boolean = false,
    focusStyle: UiFocusStyle? = null,
    isMoveFreelyMode: Boolean = false,
    alpha: Float,
    filterQuality: FilterQuality = FilterQuality.Low,
) {
    // Use Decal to avoid artifacts at the edges. It would be nice if we could render the "glow" outside
    // the canvas. It seems possible when using renderEffects on the graphicsLayer. But will need
    // more investigation.
    val imageShader = remember(bitmap) { ImageShader(bitmap, TileMode.Decal, TileMode.Decal) }
    val playerBorderShader = when {
        isMoveFreelyMode -> playerSelectedBorderShader
        isActionWheelFocus || isHighlighted -> playerInFocus
        isTempSelected -> playerTempSelectedShader
        isActiveFocus -> playerSelectedBorderShader
        focusStyle != null -> playerDownBorderShader
        isGoingDown -> playerDownBorderShader
        else -> playerSelectedBorderShader
    }
    val runtimeEffect = RuntimeEffect.makeForShader(playerBorderShader)
    BoxWithConstraints(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxSize()
            .drawWithCache {
                if (!isSelectable && !isHighlighted && !isMoveFreelyMode && focusStyle == null /* && !isGoingDown && !isActionWheelFocus */) {
                    return@drawWithCache onDrawBehind { /* Do nothing */ }
                }
                val canvasWidth = size.width
                val canvasHeight = size.height
                val shaderWidth = bitmap.width.toFloat()
                val shaderHeight = bitmap.height.toFloat()

                // Calculate scale factors
                val scaleX = canvasWidth / shaderWidth
                val scaleY = canvasHeight / shaderHeight
                val scale = min(scaleX, scaleY) // Maintain aspect ratio

                // Feed data to the Shader
                val shader = RuntimeShaderBuilder(runtimeEffect).apply {
                    child("image", imageShader.skiaShader)
                    uniform("resolution", size.width, size.height)
                    uniform("scaleFactor", scaleX, scaleY)
                }.makeShader()
                val shaderBrush = ShaderBrush(shader.asComposeShader())

                // Draw the "glow" behind the real player image
                onDrawBehind {
                    withTransform({
                        scale(scale, scale, pivot = Offset.Zero)
                    }) {
                        drawRect(brush = shaderBrush, topLeft = Offset.Zero, size = size, alpha = alpha)
                    }
                }
            }

    ) {
        // Draw the original sharp image on top
        Image(
            bitmap = bitmap,
            contentDescription = null,
            // Try to keep as much of the "pixel" feel, while still
            // allowing dynamic scaling. We probably need to play around with
            // this setting.
            filterQuality = filterQuality,
            modifier = Modifier.aspectRatio(1f).fillMaxSize().alpha(alpha)
        )
    }
}
