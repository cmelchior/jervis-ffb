package com.jervisffb.ui.game.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Direction.Companion.BOTTOM
import com.jervisffb.engine.model.Direction.Companion.BOTTOM_LEFT
import com.jervisffb.engine.model.Direction.Companion.BOTTOM_RIGHT
import com.jervisffb.engine.model.Direction.Companion.LEFT
import com.jervisffb.engine.model.Direction.Companion.RIGHT
import com.jervisffb.engine.model.Direction.Companion.UP
import com.jervisffb.engine.model.Direction.Companion.UP_LEFT
import com.jervisffb.engine.model.Direction.Companion.UP_RIGHT
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteLocation
import com.jervisffb.engine.serialize.SpriteSheet
import com.jervisffb.engine.serialize.SpriteSource
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.icons_decorations_block1d
import com.jervisffb.jervis_ui.generated.resources.icons_decorations_block2d
import com.jervisffb.jervis_ui.generated.resources.icons_decorations_block2dagainst
import com.jervisffb.jervis_ui.generated.resources.icons_decorations_block3d
import com.jervisffb.jervis_ui.generated.resources.icons_decorations_block3dagainst
import com.jervisffb.jervis_ui.generated.resources.icons_decorations_block_away
import com.jervisffb.jervis_ui.generated.resources.icons_decorations_block_home
import com.jervisffb.jervis_ui.generated.resources.icons_decorations_holdball
import com.jervisffb.jervis_ui.generated.resources.icons_decorations_prone
import com.jervisffb.jervis_ui.generated.resources.icons_decorations_stunned
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_east
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_east_filled
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_north
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_north_filled
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_northeast
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_northeast_filled
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_northwest
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_northwest_filled
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_south
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_south_filled
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_southeast
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_southeast_filled
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_southwest
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_southwest_filled
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_west
import com.jervisffb.jervis_ui.generated.resources.icons_game_pb_west_filled
import com.jervisffb.jervis_ui.generated.resources.icons_game_sball_30x30
import com.jervisffb.jervis_ui.generated.resources.icons_scorebar_background_scorebar
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_player_detail_blue
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_player_detail_red
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_resource_blue
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_resource_red
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_turn_dice_status_blue
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_turn_dice_status_red
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_box_button
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_overlay_player_detail_blue_modified
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_overlay_player_detail_red_modified
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_turn_button
import com.jervisffb.jervis_ui.generated.resources.jervis_dogout
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_leader_reroll
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_team_reroll
import com.jervisffb.jervis_ui.generated.resources.jervis_inducement_apothercary
import com.jervisffb.jervis_ui.generated.resources.jervis_inducement_keg
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.game.model.UiPlayer
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.viewmodel.FieldDetails
import com.jervisffb.ui.loadFileAsImage
import com.jervisffb.ui.loadImage
import com.jervisffb.ui.utils.getSubImage
import com.jervisffb.ui.utils.scalePixels
import com.jervisffb.ui.utils.toImageBitmap
import com.jervisffb.ui.utils.toSkiaColor
import com.jervisffb.utils.canBeHost
import com.jervisffb.utils.getHttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import io.ktor.http.headers
import io.ktor.http.isSuccess
import okio.internal.commonToUtf8String
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Image
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.TextLine
import kotlin.math.floor

enum class DiceColor {
    DEFAULT,
    BROWN,
    WHITE,
    RED,
    BLUE,
    YELLOW,
    BLACK
}

/**
 * Enumerates the various types of actions that can appear on the Circular
 * Action Bar.
 */
enum class ActionIcon(val path: String) {

    // Generic actions
    CANCEL("jervis/actions/jervis_action_cancel.png"),
    CONFIRM("jervis/actions/jervis_action_confirm.png"),
    END_TURN("jervis/actions/jervis_action_cancel.png"),

    // Developer Actions
    ROLL_DICE("jervis/actions/jervis_action_roll_dice.png"),
    TEAM_REROLL("jervis/actions/jervis_action_team_reroll.png"),

    // Player Actions
    MOVE("jervis/actions/jervis_action_move.png"),
    BLOCK("jervis/actions/jervis_action_block.png"),
    BLITZ("jervis/actions/jervis_action_blitz.png"),
    FOUL("jervis/actions/jervis_action_foul.png"),
    PASS("jervis/actions/jervis_action_pass.png"),
    HANDOFF("jervis/actions/jervis_action_handoff.png"),
    THROW_TEAM_MATE("jervis/actions/jervis_action_pass.png"),

    // Move Actions
    STAND_UP("jervis/actions/jervis_action_move.png"),
    STAND_UP_AND_END("jervis/actions/jervis_action_move.png"),
    JUMP("jervis/actions/jervis_action_jump.png"),
    LEAP("jervis/actions/jervis_action_jump.png"),
    STAY("jervis/actions/jervis_action_cancel.png"),
    FOLLOW_UP("jervis/actions/jervis_action_move.png"),


    // Special Actions
    BALL_AND_CHAIN("jervis/actions/jervis_action_move.png"),
    BOMBARDIER("jervis/actions/jervis_action_pass.png"),
    BREATHE_FIRE("jervis/actions/jervis_action_blitz.png"),
    CHAINSAW("jervis/actions/jervis_action_block.png"),
    HYPNOTIC_GAZE("jervis/actions/jervis_action_block.png"),
    KICK_TEAM_MATE("jervis/actions/jervis_action_pass.png"),
    MULTIPLE_BLOCK("jervis/actions/jervis_action_block.png"),
    PROJECTILE_VOMIT("jervis/actions/jervis_action_block.png"),
    STAB("jervis/actions/jervis_action_block.png");
}


/**
 * Logo size options for the team/roster logos.
 */
enum class LogoSize {
    LARGE, // 600x600px
    SMALL // 200x200px
}

/**
 * Wrapper around extracted image data for a single player position
 * in the two positions supported.
 */
data class PlayerSprite(
    val default: ImageBitmap,
    val active: ImageBitmap,
)

/**
 * Main class responsible for handling all logic around fetching and storing
 * graphic assets.
 *
 * A lot of the methods in here are `suspend` functions due to how WASM loads
 * resources.
 */
object IconFactory {

    // Many of the assets are pixel-art, where we want to preserve as much or the
    // blockiness as possible. Use the scale factor to adjust the size of images
    // so they are close to the intended usage size. This will remove interpolation
    // artifacts for smaller adjustments to the size.
    val scaleFactor
        get() = density.density.toInt()
    private lateinit var density: Density

    private val cachedPlayers: MutableMap<Player, PlayerSprite> = mutableMapOf()
    // Map from resource "path" to loaded in-memory image
    private val cachedImages: MutableMap<String, ImageBitmap> = mutableMapOf()
    private val cachedPortraits: MutableMap<PlayerId, ImageBitmap> = mutableMapOf()
    private val cachedLargeLogos: MutableMap<TeamId, ImageBitmap> = mutableMapOf()
    private val cachedSmallLogos: MutableMap<TeamId, ImageBitmap> = mutableMapOf()
    private val cachedDice: MutableMap<DiceColor, MutableMap<DieResult, ImageBitmap>> = mutableMapOf()
    private val cachedCoin: MutableMap<Coin, ImageBitmap> = mutableMapOf()
    private val cachedActionIcons: MutableMap<ActionIcon, ImageBitmap> = mutableMapOf()
    private val cachedGeneratedPlayers: MutableMap<String, ImageBitmap> = mutableMapOf()

    // FUMBBL Mappings
    private val fumbblCache = mutableMapOf<String, Url>()

    private val httpClient = getHttpClient()

    /**
     * Loads the fumbbl ini file and prepare the mapping between local paths
     * and download URLs
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun initializeFumbblMapping() {
        val fileContent = Res.readBytes("files/fumbbl/icons.ini")
        val propertiesFile = fileContent.commonToUtf8String()
        propertiesFile.lines().forEach { line ->
            val parts = line.split("=")
            if (parts.size == 2) {
                val url = parts[0].replace("https\\", "https")
                val path = parts[1]
                fumbblCache[path] = Url(url)
            }
        }
    }

    // Load all image resources used.
    // It looks like we cannot lazy-load them due to how Compose Resources work on WasmJS
    // `Res.readBytes` is suspendable and runBlocking doesn't work on wasmJs, which makes
    // loading images in the middle of a Composable function quite a nightmare.
    // Instead, we preload all dynamic resources up front. This will probably result in slightly
    // higher memory usage, but it will probably not be problematic.
    suspend fun initialize(density: Density, homeTeam: Team, awayTeam: Team): Boolean {
        this.density = density
        FieldDetails.entries.forEach {
            saveFileIntoCache(it.resource)
        }
        initializeDiceMappings(scaleFactor)
        initializeGameActionIcons(scaleFactor)
        saveTeamPlayerImagesToCache(homeTeam)
        saveTeamPlayerImagesToCache(awayTeam)
        return true
    }

    private suspend fun saveImageIntoCache(path: String) {
        val image = Res.loadImage(path)
        cachedImages[path] = image
    }

    private suspend fun saveFileIntoCache(path: String) {
        val image = Res.loadFileAsImage(path)
        cachedImages[path] = image
    }

    private fun loadImageFromCache(path: String): ImageBitmap {
        return cachedImages[path] ?: error("Could not find: $path")
    }

    private suspend fun loadImageFromResources(
        path: String,
        cache: Boolean = true,
    ): ImageBitmap {
        if (cache && cachedImages.containsKey(path)) {
            return cachedImages[path]!!
        } else {
            try {
                val image = Res.loadFileAsImage(path)
                cachedImages[path] = image
                return image
            } catch (ex: Exception) {
                throw IllegalStateException("Problems loading: $path", ex)
            }
        }
    }

    // Create a player sprite. We create this as a normal player size, so 4x(30x30) = 120x30 px
    private fun generatePlayerSprite(letters: String): ImageBitmap {
        if (cachedGeneratedPlayers.contains(letters)) return cachedGeneratedPlayers[letters]!!
        // Prepare sprite sheet image
        val w = 120
        val h = 30
        val bitmap = Bitmap()
        bitmap.allocN32Pixels(w, h, false)
        val canvas = org.jetbrains.skia.Canvas(bitmap)

        // Load default system font
        val mgr = FontMgr.default
        val typeface = mgr.matchFamilyStyle(null, FontStyle.NORMAL)
            ?: mgr.legacyMakeTypeface("", FontStyle.NORMAL)
        val font = org.jetbrains.skia.Font(typeface, 14f).apply {
            this.isSubpixel = false
        }

        // Draw sprites
        val radius = 14f
        val centers = floatArrayOf(15f, 45f, 75f, 105f).map { x -> Offset(x, 15f) }

        val paint = org.jetbrains.skia.Paint().apply {
            color = JervisTheme.rulebookRed.toSkiaColor()
            isAntiAlias = false
        }
        val borderPaint = org.jetbrains.skia.Paint().apply {
            color = JervisTheme.black.toSkiaColor()
            mode = PaintMode.STROKE
            strokeWidth = 1f
            isAntiAlias = false
        }
        val textPaint = org.jetbrains.skia.Paint().apply {
            color = JervisTheme.white.toSkiaColor()
            isAntiAlias = false
        }

        val l = TextLine.make(letters, font)
        val ascent = l.ascent
        val descent = l.descent
        val leading = l.leading

        // Not 100% sure why this is correct. It feels like it is just by accident :thinking:
        // val baselineY = centers[0].y + (0.5f*centers[0].y + 0.5f*((ascent + descent) * 0.5f + leading * 0.5f));

        // Y-value is the same, so just use the first
        val baselineY = centers[0].y + (l.height - (descent + leading)) / 2f;

        // First two red
        paint.color = JervisTheme.rulebookRed.toSkiaColor()
        canvas.drawCircle(centers[0].x, centers[0].y, radius, paint)
        canvas.drawCircle(centers[0].x, centers[0].y, radius, borderPaint)
        val baseline1X = floor(centers[0].x - (l.width / 2f)) // Round down since rounding up makes it look more "off"
        canvas.drawTextLine(l, baseline1X, baselineY, textPaint)

        canvas.drawCircle(centers[1].x, centers[1].y, radius, paint)
        canvas.drawCircle(centers[1].x, centers[1].y, radius, borderPaint)
        val baseline2X = floor(centers[1].x - (l.width / 2f)) // Round down since rounding up makes it look more "off"
        canvas.drawTextLine(l,baseline2X, baselineY, textPaint)

        // Last two blue
        paint.color = JervisTheme.rulebookBlue.toSkiaColor()
        canvas.drawCircle(centers[2].x, centers[2].y, radius, paint)
        canvas.drawCircle(centers[2].x, centers[2].y, radius, borderPaint)
        val baseline3X = floor(centers[2].x - (l.width / 2f)) // Round down since rounding up makes it look more "off"
        canvas.drawTextLine(l,baseline3X, baselineY, textPaint)

        canvas.drawCircle(centers[3].x, centers[3].y, radius, paint)
        canvas.drawCircle(centers[3].x, centers[3].y, radius, borderPaint)
        val baseline4X = floor(centers[3].x - (l.width / 2f)) // Round down since rounding up makes it look more "off"
        canvas.drawTextLine(l,baseline4X, baselineY, textPaint)

        // Return generated sprite sheet
        canvas.close()
        val spriteSheet = Image.makeFromBitmap(bitmap).toComposeImageBitmap()
        cachedGeneratedPlayers[letters] = spriteSheet
        return spriteSheet
    }

    private suspend fun createPlayerSprite(player: Player, isHomeTeam: Boolean): PlayerSprite {
        val playerSprite = player.icon?.sprite ?: throw IllegalStateException("Cannot find sprite configured for: $player")
        val image = when (playerSprite.type) {
            SpriteLocation.EMBEDDED -> loadImageFromResources(playerSprite.resource)
            SpriteLocation.URL -> loadImageFromNetwork(Url(playerSprite.resource), false)!! // TODO Fallback image
            SpriteLocation.FUMBBL_INI -> loadImageFromFumbblIni(playerSprite.resource)!! // TODO Fallback image
            SpriteLocation.GENERATED -> generatePlayerSprite(letters = playerSprite.resource)
        }
        return when (val sprite = playerSprite) {
            is SingleSprite -> {
                PlayerSprite(image, image)
            }
            is SpriteSheet -> {
                extractSprites(image, sprite.variants, sprite.selectedIndex ?: 0, isHomeTeam)
            }
        }
    }

    private fun extractSprites(image: ImageBitmap, variants: Int?, selectedIndex: Int, onHomeTeam: Boolean): PlayerSprite {
        val spriteWidth = image.width / 4 // There are always 4 sprites pr line.
        val spriteHeight: Int = spriteWidth
        val lines = variants ?: (image.height / spriteHeight)
        val line = selectedIndex
        val homeDefaultX = 0
        val homeActiveX = spriteWidth
        val awayDefaultX = spriteWidth * 2
        val awayActiveX = spriteWidth * 3
        val homeDefault = image.getSubImage(homeDefaultX, line * spriteHeight, spriteWidth, spriteHeight)
        val homeActive = image.getSubImage(homeActiveX, line * spriteHeight, spriteWidth, spriteHeight)
        val awayDefault = image.getSubImage(awayDefaultX, line * spriteHeight, spriteWidth, spriteHeight)
        val awayActive = image.getSubImage(awayActiveX, line * spriteHeight, spriteWidth, spriteHeight)
        val homePlayer = PlayerSprite(homeDefault, homeActive)
        val awayPlayer = PlayerSprite(awayDefault, awayActive)
        return if (onHomeTeam) {
            homePlayer
        } else {
            awayPlayer
        }
    }

    private suspend fun loadImageFromNetwork(url: Url, useProxy: Boolean): ImageBitmap? {
        // User server proxy to bypass CORS restrictions but only on Web. JVM and iOS does not need this.
        // Right now we are just using the "canBeHost()" as an easy way to check for the Web target.
        // Probably need to find something better in the future.
        val callUrl = if (useProxy && !canBeHost()) {
            Url("https://jervis.ilios.dk/proxy.php?url=${url.toString().encodeURLParameter()}")
        } else {
            url
        }

        val cachedImage = CacheManager.getCachedImage(url)
        if (cachedImage != null) return cachedImage
        val result = httpClient.get(callUrl) {
            headers {
                // In some cases, gifs are returned even though the path is a png. Problem?
                accept(ContentType.Image.PNG)
                accept(ContentType.Image.GIF)
            }
        }
        return if (result.status.isSuccess()) {
            val bytes = result.readRawBytes()
            val image = Image.makeFromEncoded(bytes).toComposeImageBitmap()
            CacheManager.saveImage(url, image)
            image
        } else {
            null
        }
    }

    private suspend fun loadImageFromFumbblIni(path: String): ImageBitmap? {
        val url = fumbblCache[path] ?: error("Path not found in ini file: $path")
        // It looks like most images in the FUMBBL ini file are protected by CORS, so on
        // web platforms we need to use a proxy to load them.
        return loadImageFromNetwork(url, true)
    }

    private suspend fun initializeDiceMappings(scaleFactor: Int) {
        DiceColor.entries.forEach {
            cachedDice[it] = mutableMapOf()
        }

        // Block Dice
        DBlockResult.allOptions().forEach {
            val die = it.blockResult
            val typeAsFileName = die.name.lowercase().replace("_", "")
            val path = "jervis/dice/jervis_dblock_black_$typeAsFileName.png"
            cachedDice[DiceColor.DEFAULT]!![it] = loadImageFromResources(path).scalePixels(scaleFactor)
        }

        val d6sColors = listOf(
            DiceColor.BROWN to true,
            DiceColor.WHITE to false,
            DiceColor.RED to false,
            DiceColor.BLUE to false,
            DiceColor.YELLOW to false,
            DiceColor.BLACK to false,
        )

        // D3 (Use D6 images for now)
        d6sColors.forEach { (color, isDefault) ->
            D3Result.allOptions().forEach {
                val image = loadImageFromResources("jervis/dice/jervis_d6_${color.name.lowercase()}_${it.value}.png").scalePixels(scaleFactor)
                cachedDice[color]!![it] = image
                if (isDefault) {
                    cachedDice[DiceColor.DEFAULT]!![it] = image
                }
            }
        }

        d6sColors.forEach { (color, isDefault) ->
            D6Result.allOptions().forEach {
                val image = loadImageFromResources("jervis/dice/jervis_d6_${color.name.lowercase()}_${it.value}.png").scalePixels(scaleFactor)
                cachedDice[color]!![it] = image
                if (isDefault) {
                    cachedDice[DiceColor.DEFAULT]!![it] = image
                }
            }
        }

        // D8
        D8Result.allOptions().forEach {
            val image = loadImageFromResources("jervis/dice/jervis_d8_purple_${it.value}.png").scalePixels(scaleFactor)
            cachedDice[DiceColor.DEFAULT]!![it] = image
        }

        // D12
        D12Result.allOptions().forEach {
            val image = loadImageFromResources("jervis/dice/jervis_d20_green_${it.value}.png").scalePixels(scaleFactor)
            cachedDice[DiceColor.DEFAULT]!![it] = image
        }

        // D16
        D16Result.allOptions().forEach {
            val image = loadImageFromResources("jervis/dice/jervis_d20_green_${it.value}.png").scalePixels(scaleFactor)
            cachedDice[DiceColor.DEFAULT]!![it] = image
        }

        // D20
        D20Result.allOptions().forEach {
            val image = loadImageFromResources("jervis/dice/jervis_d20_green_${it.value}.png").scalePixels(scaleFactor)
            cachedDice[DiceColor.DEFAULT]!![it] = image
        }

        // Coins
        Coin.entries.forEach {
            val image = loadImageFromResources("jervis/dice/jervis_coin_${it.name.lowercase()}.png")
            cachedCoin[it] = image.scalePixels(IconFactory.scaleFactor)
        }
    }

    private suspend fun initializeGameActionIcons(scaleFactor: Int) {
        ActionIcon.entries.forEach {
            val resource = it.path
            val image = loadImageFromResources(resource, cache = false).scalePixels(scaleFactor)
            cachedActionIcons[it] = image
        }
    }

    private suspend fun saveTeamPlayerImagesToCache(team: Team) {
        team.forEach { player ->
            val playerSprite = createPlayerSprite(player, player.isOnHomeTeam())
            cachedPlayers[player] = playerSprite
            val portrait = player.icon?.portrait ?: SingleSprite.embedded("jervis/portraits/default_portrait.png")
            val portraitImage = when (portrait.type) {
                SpriteLocation.EMBEDDED -> loadImageFromResources(portrait.resource)
                SpriteLocation.URL -> loadImageFromNetwork(Url(portrait.resource), false)
                SpriteLocation.FUMBBL_INI -> loadImageFromFumbblIni(portrait.resource)
                SpriteLocation.GENERATED -> generatePlayerSprite(letters = portrait.resource)
            }
            cachedPortraits[player.id] = portraitImage!! // TODO Fix null value
        }
    }

    fun getPlayerIcon(player: UiPlayer): ImageBitmap {
        val isHomeTeam: Boolean = player.isOnHomeTeam
        val roster: Roster = player.model.team.roster
        val playerType: Position = player.position
        val isActive = player.isActive

        if (cachedPlayers.contains(player.model)) {
            return if (isActive) {
                cachedPlayers[player.model]!!.active
            } else {
                cachedPlayers[player.model]!!.default
            }
        } else {
            error("Could not find: $player")
        }
    }

    /**
     * Returns size of dice image for the current dice type in [androidx.compose.ui.unit.Dp].
     */
    fun getDiceSizeDp(die: DieResult): DpSize {
        val image = cachedDice[DiceColor.DEFAULT]?.get(die) ?: error("Could not find: $die")
        return DpSize(
            (image.width / density.density).dp,
            (image.height / density.density).dp
        )
    }

    /**
     * Returns size of dice image for the current dice type in pixels
     */
    fun getDiceSizePx(die: DieResult): Size {
        val image = cachedDice[DiceColor.DEFAULT]?.get(die) ?: error("Could not find: $die")
        return Size(image.width.toFloat(), image.height.toFloat())
    }

    @Composable
    fun getDiceIcon(die: DieResult, color: DiceColor = DiceColor.DEFAULT): ImageBitmap {
        return cachedDice[color]?.get(die) ?: error("Could not find die: $die [$color]")
    }

    @Composable
    fun getCoinIcon(coin: Coin): ImageBitmap {
        return cachedCoin[coin] ?: error("Could not find coin: $coin")
    }

    fun getCoinSizeDp(coin: Coin): DpSize {
        val image = cachedCoin[coin] ?: error("Could not find coin: $coin")
        return DpSize(
            (image.width / density.density).dp,
            (image.height / density.density).dp
        )
    }

    fun getActionIcon(action: ActionIcon): ImageBitmap {
        return cachedActionIcons[action] ?: error("Could not find action: $action")
    }

    @Composable
    fun getHeldBallOverlay(): ImageBitmap {
        return imageResource(Res.drawable.icons_decorations_holdball)
    }

    @Composable
    fun getBall(): ImageBitmap {
        return imageResource(Res.drawable.icons_game_sball_30x30)
    }

    @Composable
    fun getPlayerDetailOverlay(onHomeTeam: Boolean): ImageBitmap {
        return if (onHomeTeam) {
            imageResource(Res.drawable.icons_sidebar_overlay_player_detail_red_modified)
        } else {
            imageResource(Res.drawable.icons_sidebar_overlay_player_detail_blue_modified)
        }
    }

    fun getPlayerPortrait(player: PlayerId): ImageBitmap {
        return cachedPortraits[player]!!
    }

    @Composable
    fun getSidebarBackground(): ImageBitmap {
        return imageResource(Res.drawable.jervis_dogout)
    }

    fun getField(field: FieldDetails): ImageBitmap {
        return loadImageFromCache(field.resource)
    }

    @Composable
    fun getButton(): ImageBitmap {
        return imageResource(Res.drawable.icons_sidebar_box_button)
    }

    @Composable
    fun getLargeButton(): ImageBitmap {
        return imageResource(Res.drawable.icons_sidebar_turn_button)
    }

    @Composable
    fun getSidebarBannerTop(isHomeTeam: Boolean): ImageBitmap {
        return when (isHomeTeam) {
            true -> imageResource(Res.drawable.icons_sidebar_background_player_detail_red)
            false -> imageResource(Res.drawable.icons_sidebar_background_player_detail_blue)
        }
    }

    @Composable
    fun getSidebarBannerMiddle(isHomeTeam: Boolean): ImageBitmap {
        return when (isHomeTeam) {
            true -> imageResource(Res.drawable.icons_sidebar_background_turn_dice_status_red)
            false -> imageResource(Res.drawable.icons_sidebar_background_turn_dice_status_blue)
        }
    }

    @Composable
    fun getSidebarBannerBottom(isHomeTeam: Boolean): ImageBitmap {
        return when (isHomeTeam) {
            true -> imageResource(Res.drawable.icons_sidebar_background_resource_red)
            false -> imageResource(Res.drawable.icons_sidebar_background_resource_blue)
        }
    }

    @Composable
    fun getScorebar(): ImageBitmap {
        return imageResource(Res.drawable.icons_scorebar_background_scorebar)
    }

    @Composable
    fun getStunnedDecoration(): ImageBitmap {
        return imageResource(Res.drawable.icons_decorations_stunned)
    }

    @Composable
    fun getProneDecoration(): ImageBitmap {
        return imageResource(Res.drawable.icons_decorations_prone)
    }

    @Composable
    fun getBlockedDecoration(homeTeam: Boolean = true): DrawableResource {
        return when (homeTeam) {
            true -> Res.drawable.icons_decorations_block_home
            false -> Res.drawable.icons_decorations_block_away
        }
    }

    fun getDirection(direction: Direction, active: Boolean): DrawableResource {
        return when (direction) {
            UP_LEFT -> {
                if (active) Res.drawable.icons_game_pb_northwest_filled else Res.drawable.icons_game_pb_northwest
            }
            UP -> {
                if (active) Res.drawable.icons_game_pb_north_filled else Res.drawable.icons_game_pb_north
            }
            UP_RIGHT -> {
                if (active) Res.drawable.icons_game_pb_northeast_filled else Res.drawable.icons_game_pb_northeast
            }
            LEFT -> {
                if (active) Res.drawable.icons_game_pb_west_filled else Res.drawable.icons_game_pb_west
            }
            RIGHT -> {
                if (active) Res.drawable.icons_game_pb_east_filled else Res.drawable.icons_game_pb_east
            }
            BOTTOM_LEFT -> {
                if (active) Res.drawable.icons_game_pb_southwest_filled else Res.drawable.icons_game_pb_southwest
            }
            BOTTOM -> {
                if (active) Res.drawable.icons_game_pb_south_filled else Res.drawable.icons_game_pb_south
            }
            BOTTOM_RIGHT -> {
                if (active) Res.drawable.icons_game_pb_southeast_filled else Res.drawable.icons_game_pb_southeast
            }
            else -> error("Unsupported direction: $direction")
        }
    }

    fun getBlockDiceRolledIndicator(dice: Int): DrawableResource {
        return when (dice) {
            -3 -> Res.drawable.icons_decorations_block3dagainst
            -2 -> Res.drawable.icons_decorations_block2dagainst
            1 -> Res.drawable.icons_decorations_block1d
            2 -> Res.drawable.icons_decorations_block2d
            3 -> Res.drawable.icons_decorations_block3d
            else -> error("Unsupported number of dice: $dice")
        }
    }

    @Composable
    fun getTeamRerollIcon(size: Dp): ImageBitmap {
        val sizePx = with(LocalDensity.current) { size.toPx() }
        val res = painterResource(Res.drawable.jervis_icon_team_reroll)
        return res.toImageBitmap(Size(sizePx, sizePx), LocalDensity.current)
    }

    @Composable
    fun getLeaderRerollIcon(size: Dp): ImageBitmap {
        val sizePx = with(LocalDensity.current) { size.toPx() }
        val res = painterResource(Res.drawable.jervis_icon_leader_reroll)
        return res.toImageBitmap(Size(sizePx, sizePx), LocalDensity.current)
    }

    @Composable
    fun getKegIcon(size: Dp): ImageBitmap {
        val sizePx = with(LocalDensity.current) { size.toPx() }
        val res = painterResource(Res.drawable.jervis_inducement_keg)
        return res.toImageBitmap(Size(sizePx, sizePx), LocalDensity.current)
    }

    @Composable
    fun getApothecaryIcon(size: Dp): ImageBitmap {
        val sizePx = with(LocalDensity.current) { size.toPx() }
        val res = painterResource(Res.drawable.jervis_inducement_apothercary)
        return res.toImageBitmap(Size(sizePx, sizePx), LocalDensity.current)
    }

    fun hasLogo(id: TeamId, size: LogoSize): Boolean {
        return when (size) {
            LogoSize.LARGE -> cachedLargeLogos.contains(id)
            LogoSize.SMALL -> cachedSmallLogos.contains(id)
        }
    }

    suspend fun saveLogo(id: TeamId, logo: SpriteSource, size: LogoSize) {
        val image = when (logo.type) {
            SpriteLocation.EMBEDDED -> loadImageFromResources(logo.resource)
            SpriteLocation.URL -> loadImageFromNetwork(Url(logo.resource), false)
            SpriteLocation.FUMBBL_INI -> loadImageFromFumbblIni(logo.resource)
            SpriteLocation.GENERATED -> error("Generated logos are not supported yet")
        }
        when (size) {
            LogoSize.LARGE -> cachedLargeLogos[id] = image ?: error("Could not find: ${logo.resource}")
            LogoSize.SMALL -> cachedSmallLogos[id] = image ?: error("Could not find: ${logo.resource}")
        }
    }

    /**
     * Returns the logo for the given team and size or throws an exception if the logo is not found.
     */
    fun getLogo(id: TeamId, size: LogoSize): ImageBitmap {
        return when (size) {
            LogoSize.LARGE -> cachedLargeLogos[id] ?: error("Could not find: $id")
            LogoSize.SMALL -> cachedSmallLogos[id] ?: error("Could not find: $id")
        }
    }

    suspend fun loadPlayerSprite(player: Player, isOnHomeTeam: Boolean): PlayerSprite? {
        if (player.icon?.sprite == null) return null
        val playerSprite = player.icon?.sprite!!
        val image = when (playerSprite.type) {
            SpriteLocation.EMBEDDED -> loadImageFromResources(playerSprite.resource)
            SpriteLocation.URL -> loadImageFromNetwork(Url(playerSprite.resource), false) ?: error("Could not find: ${playerSprite.resource}") // TODO Fallback to something?
            SpriteLocation.FUMBBL_INI -> loadImageFromFumbblIni(playerSprite.resource) ?: error("Could not find: ${playerSprite.resource}") // TODO Fallback to something?
            SpriteLocation.GENERATED -> generatePlayerSprite(playerSprite.resource)
        }
        val sprite = when (val sprite = playerSprite) {
            is SingleSprite -> {
                PlayerSprite(image, image)
            }
            is SpriteSheet -> {
                extractSprites(image, sprite.variants, sprite.selectedIndex ?: 0, isOnHomeTeam)
            }
        }
        cachedPlayers[player] = sprite
        return sprite
    }

    /**
     * Load a logo for the given team and size based on the [RosterLogo] configuration.
     */
    suspend fun loadRosterIcon(team: TeamId, logo: RosterLogo, size: LogoSize): ImageBitmap {
        val sprite = when (size) {
            LogoSize.LARGE -> logo.large ?: SingleSprite.embedded("jervis/roster/logo/roster_logo_jervis_default_large.png")
            LogoSize.SMALL -> logo.small ?: SingleSprite.embedded("jervis/roster/logo/roster_logo_jervis_default_small.png")
        }
        saveLogo(team, sprite, size)
        return getLogo(team, size)
    }

    /**
     * Load a logo directly. Normally the overload with [RosterLogo] should be used instead.
     */
    suspend fun loadRosterIcon(team: TeamId, logo: SpriteSource?, size: LogoSize): ImageBitmap? {
        if (logo == null) return null
        saveLogo(team, logo, size)
        return getLogo(team, size)
    }
}
