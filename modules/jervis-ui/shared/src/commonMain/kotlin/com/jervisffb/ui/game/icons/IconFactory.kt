package com.jervisffb.ui.game.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.bb2020.inducements.BB2020InducementType
import com.jervisffb.engine.bb2025.inducements.BB2025InducementType
import com.jervisffb.engine.common.inducements.CommonInducementType
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Direction.Companion.DOWN
import com.jervisffb.engine.model.Direction.Companion.DOWN_LEFT
import com.jervisffb.engine.model.Direction.Companion.DOWN_RIGHT
import com.jervisffb.engine.model.Direction.Companion.LEFT
import com.jervisffb.engine.model.Direction.Companion.RIGHT
import com.jervisffb.engine.model.Direction.Companion.UP
import com.jervisffb.engine.model.Direction.Companion.UP_LEFT
import com.jervisffb.engine.model.Direction.Companion.UP_RIGHT
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteLocation
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.engine.sprites.SpriteSource
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.icons_decorations_block1d
import com.jervisffb.shared.generated.resources.icons_decorations_block2d
import com.jervisffb.shared.generated.resources.icons_decorations_block2dagainst
import com.jervisffb.shared.generated.resources.icons_decorations_block3d
import com.jervisffb.shared.generated.resources.icons_decorations_block3dagainst
import com.jervisffb.shared.generated.resources.icons_decorations_block_away
import com.jervisffb.shared.generated.resources.icons_decorations_block_home
import com.jervisffb.shared.generated.resources.icons_decorations_holdball
import com.jervisffb.shared.generated.resources.icons_decorations_holdball_fumblerooski
import com.jervisffb.shared.generated.resources.icons_decorations_prone
import com.jervisffb.shared.generated.resources.icons_decorations_stunned
import com.jervisffb.shared.generated.resources.icons_game_pb_east
import com.jervisffb.shared.generated.resources.icons_game_pb_east_filled
import com.jervisffb.shared.generated.resources.icons_game_pb_north
import com.jervisffb.shared.generated.resources.icons_game_pb_north_filled
import com.jervisffb.shared.generated.resources.icons_game_pb_northeast
import com.jervisffb.shared.generated.resources.icons_game_pb_northeast_filled
import com.jervisffb.shared.generated.resources.icons_game_pb_northwest
import com.jervisffb.shared.generated.resources.icons_game_pb_northwest_filled
import com.jervisffb.shared.generated.resources.icons_game_pb_south
import com.jervisffb.shared.generated.resources.icons_game_pb_south_filled
import com.jervisffb.shared.generated.resources.icons_game_pb_southeast
import com.jervisffb.shared.generated.resources.icons_game_pb_southeast_filled
import com.jervisffb.shared.generated.resources.icons_game_pb_southwest
import com.jervisffb.shared.generated.resources.icons_game_pb_southwest_filled
import com.jervisffb.shared.generated.resources.icons_game_pb_west
import com.jervisffb.shared.generated.resources.icons_game_pb_west_filled
import com.jervisffb.shared.generated.resources.icons_game_sball_30x30
import com.jervisffb.shared.generated.resources.icons_scorebar_background_scorebar
import com.jervisffb.shared.generated.resources.icons_sidebar_box_button
import com.jervisffb.shared.generated.resources.icons_sidebar_turn_button
import com.jervisffb.shared.generated.resources.jervis_dogout
import com.jervisffb.shared.generated.resources.jervis_icon_leader_reroll
import com.jervisffb.shared.generated.resources.jervis_icon_team_reroll
import com.jervisffb.shared.generated.resources.jervis_inducement_apothercary
import com.jervisffb.shared.generated.resources.jervis_inducement_keg
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.game.icons.PlayerSpriteFallbackGenerator.generatePlayerSprite
import com.jervisffb.ui.game.model.UiPitchPlayer
import com.jervisffb.ui.game.viewmodel.PitchDetails
import com.jervisffb.ui.loadFileAsImage
import com.jervisffb.ui.loadImage
import com.jervisffb.ui.utils.getSubImage
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.scalePixels
import com.jervisffb.ui.utils.toImageBitmap
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.loggerInstance
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import io.ktor.http.headers
import io.ktor.http.isSuccess
import kotlinx.coroutines.CompletableDeferred
import okio.internal.commonToUtf8String
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skia.Image

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

    // Skills
    FUMBLEROOSKI_CANCEL("jervis/actions/jervis_action_cancel.png"),
    FUMBLEROOSKI_USE("jervis/actions/jervis_action_use_fumblerooski.png"),

    // Player Actions
    MOVE("jervis/actions/jervis_action_move.png"),
    BLOCK("jervis/actions/jervis_action_block.png"),
    BLITZ("jervis/actions/jervis_action_blitz.png"),
    FOUL("jervis/actions/jervis_action_foul.png"),
    PASS("jervis/actions/jervis_action_pass.png"),
    HANDOFF("jervis/actions/jervis_action_handoff.png"),
    THROW_TEAM_MATE("jervis/actions/jervis_action_throwteammate.png"),
    SECURE_THE_BALL("jervis/actions/jervis_action_secure_the_ball.png"),

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
    BREATHE_FIRE("jervis/actions/jervis_action_block.png"),
    CHAINSAW("jervis/actions/jervis_action_block.png"),
    CHOMP("jervis/actions/jervis_action_block.png"),
    HYPNOTIC_GAZE("jervis/actions/jervis_action_block.png"),
    KICK_TEAM_MATE("jervis/actions/jervis_action_pass.png"),
    MULTIPLE_BLOCK("jervis/actions/jervis_action_block.png"),
    PROJECTILE_VOMIT("jervis/actions/jervis_action_block.png"),
    STAB("jervis/actions/jervis_action_block.png"),
    PUNT("jervis/actions/jervis_action_pass.png"),
    HAIL_MARY_PASS("jervis/actions/jervis_action_pass.png"),
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

    private val BASE_URL = "https://jervis.ilios.dk"
    private val DEFAULT_PORTRAIT = SingleSprite.embedded("jervis/portraits/default_portrait.png")

    // Many of the assets are pixel-art, where we want to preserve as much or the
    // blockiness as possible. Use the scale factor to adjust the size of images
    // so they are close to the intended usage size. This will remove interpolation
    // artifacts for smaller adjustments to the size.
    val scaleFactor
        get() = density.density.toInt()
    private lateinit var density: Density

    private val cachedPlayers: MutableMap<PlayerId, PlayerSprite> = mutableMapOf()
    private var cachedReferee: ImageBitmap? = null
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
    private val inFlightRequests = mutableMapOf<Url, CompletableDeferred<ImageBitmap?>>()

    /**
     * Loads the fumbbl ini file and prepare the mapping between local paths
     * and download URLs
     */
    suspend fun initializeFumbblMapping() {
        fun addPropLineToCache(line: String) {
            val parts = line.split("=")
            if (parts.size == 2) {
                val url = parts[0].replace("https\\", "https")
                val path = parts[1]
                fumbblCache[path] = Url(url)
            }
        }
        suspend fun cacheFile(path: String) {
            val fileContent = Res.readBytes(path)
            val propsFile = fileContent.commonToUtf8String()
            propsFile.lines().forEach { line -> addPropLineToCache(line) }
        }
        cacheFile("files/fumbbl/icons.ini")
        cacheFile("files/fumbbl/icons-extra.ini")
    }

    // Load all image resources used.
    // It looks like we cannot lazy-load them due to how Compose Resources work on WasmJS
    // `Res.readBytes` is suspendable and runBlocking doesn't work on wasmJs, which makes
    // loading images in the middle of a Composable function quite a nightmare.
    // Instead, we preload all dynamic resources up front. This will probably result in slightly
    // higher memory usage, but it will probably not be problematic.
    suspend fun initialize(density: Density, homeTeam: Team, awayTeam: Team): Boolean {
        this.density = density
        PitchDetails.entries.forEach {
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
                throw IllegalStateException("Problems loading image resource: $path", ex)
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

    private suspend fun loadPlayerSpriteImage(playerSprite: SpriteSource, size: PlayerSize): ImageBitmap? {
        return when (playerSprite.type) {
            SpriteLocation.EMBEDDED -> loadImageFromResources(playerSprite.resource)
            SpriteLocation.URL -> runCatching { loadImageFromNetwork(Url(playerSprite.resource), false) }.getOrNull()
            SpriteLocation.FUMBBL_INI -> runCatching { loadImageFromFumbblIni(playerSprite.resource) }.getOrNull()
            SpriteLocation.GENERATED -> generatePlayerSprite(letters = playerSprite.resource, size)
        }
    }

    private fun createPlayerSprite(image: ImageBitmap, playerSprite: SpriteSource, isHomeTeam: Boolean): PlayerSprite {
        return when (val sprite = playerSprite) {
            is SingleSprite -> {
                PlayerSprite(image, image)
            }
            is SpriteSheet -> {
                extractSprites(image, sprite.variants, sprite.selectedIndex ?: 0, isHomeTeam)
            }
        }
    }

    private suspend fun createFallbackPlayerSprite(player: Player, isHomeTeam: Boolean): PlayerSprite {
        val letters = player.position.shortHand.ifBlank { "?" }
        val size  = player.position.size
        val spriteSheet = if (cachedGeneratedPlayers.contains(letters)) {
            cachedGeneratedPlayers[letters]!!
        } else {
            val spriteSheet = PlayerSpriteFallbackGenerator.generatePlayerSprite(letters, size)
            cachedGeneratedPlayers[letters] = spriteSheet
            spriteSheet
        }
        return extractSprites(spriteSheet, variants = 1, selectedIndex = 0, onHomeTeam = isHomeTeam)
    }

    private suspend fun loadImageFromNetwork(url: Url, useProxy: Boolean): ImageBitmap? {
        CacheManager.getCachedImage(url)?.let { return it }

        // If a fetch for this URL is already in-flight, wait for it instead of
        // firing a duplicate request. This avoids race conditions on WASM where coroutines
        // interleave at every suspension point. Which will cause fatal cash misses later.
        // See https://github.com/cmelchior/jervis-ffb/issues/48
        inFlightRequests[url]?.let { return it.await() }

        val deferred = CompletableDeferred<ImageBitmap?>()
        inFlightRequests[url] = deferred
        try {
            // Originally the proxy was only used to bypass CORS restrictions on the web client when
            // running on localhost. JVM and iOS did not need this.
            // However, TourPlay is using AVIF images, which Compose does not yet support. To work around
            // this, we also let the proxy convert any AVIF images to PNG.
            // Due to this, all targets must use the proxy for now.

            // Old check
            //    val callUrl = when (useProxy && !canBeHost()) {
            //        true -> Url("${BASE_URL}/proxy.php?url=${url.toString().encodeURLParameter()}")
            //        false -> url
            //    }

            // New check: Always use the proxy to work around both CORS and AVIF issues.
            val callUrl = Url("${BASE_URL}/proxy.php?url=${url.toString().encodeURLParameter()}")

            val result = httpClient.get(callUrl) {
                headers {
                    // In some cases, gifs are returned even though the path is a png. Problem?
                    accept(ContentType.Image.PNG)
                    accept(ContentType.Image.GIF)
                    accept(ContentType.Image.AVIF)
                }
            }
            val image = when (result.status.isSuccess()) {
                true -> {
                    val bytes = result.readRawBytes()
                    val image = Image.makeFromEncoded(bytes).toComposeImageBitmap()
                    CacheManager.saveImage(url, image)
                    image
                }
                false -> null
            }
            deferred.complete(image)
            return image
        } catch (e: Throwable) {
            loggerInstance.w("Error loading image from network: $url", e)
            deferred.completeExceptionally(e)
            return null
        } finally {
            inFlightRequests.remove(url)
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
            loadPlayerSprite(player, player.isOnHomeTeam())
            val portrait = player.icon?.portrait ?: DEFAULT_PORTRAIT
            val portraitImage = when (portrait.type) {
                SpriteLocation.EMBEDDED -> loadImageFromResources(portrait.resource)
                SpriteLocation.URL -> loadImageFromNetwork(Url(portrait.resource), useProxy = false)
                SpriteLocation.FUMBBL_INI -> loadImageFromFumbblIni(portrait.resource)
                SpriteLocation.GENERATED -> generatePlayerSprite(letters = portrait.resource, player.position.size)
            } ?: loadImageFromResources(DEFAULT_PORTRAIT.resource)
            cachedPortraits[player.id] = portraitImage
        }
    }

    fun getPlayerIcon(player: UiPitchPlayer): ImageBitmap {
        val isActive = player.isActive
        if (cachedPlayers.contains(player.id)) {
            return if (isActive) {
                cachedPlayers[player.id]!!.active
            } else {
                cachedPlayers[player.id]!!.default
            }
        } else {
            error("Could not find player: ${player.id}")
        }
    }

    /**
     * Returns size of dice image for the current dice type in [androidx.compose.ui.unit.Dp].
     */
    fun getDiceSizeDp(die: DieResult): DpSize {
        val image = cachedDice[DiceColor.DEFAULT]?.get(die) ?: error("Could not find die: $die")
        return DpSize(
            (image.width / scaleFactor).jdp * 1.25f,
            (image.height / scaleFactor).jdp * 1.25f
        )
    }

    /**
     * Returns size of dice image for the current dice type in pixels
     */
    fun getDiceSizePx(die: DieResult): Size {
        val image = cachedDice[DiceColor.DEFAULT]?.get(die) ?: error("Could not find die: $die")
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
            (image.width / scaleFactor).jdp * 1.25f,
            (image.height / scaleFactor).jdp * 1.25f
        )
    }

    fun getActionIcon(action: ActionIcon): ImageBitmap {
        return cachedActionIcons[action] ?: error("Could not find action: $action")
    }

    @Composable
    fun getHeldBallOverlay(useFumblerooski: Boolean): ImageBitmap {
        return when (useFumblerooski) {
            true -> imageResource(Res.drawable.icons_decorations_holdball_fumblerooski)
            false -> imageResource(Res.drawable.icons_decorations_holdball)
        }
    }

    @Composable
    fun getBall(): ImageBitmap {
        return imageResource(Res.drawable.icons_game_sball_30x30)
    }

    fun getPlayerPortrait(player: PlayerId): ImageBitmap {
        return cachedPortraits[player]!!
    }

    @Composable
    fun getSidebarBackground(): ImageBitmap {
        return imageResource(Res.drawable.jervis_dogout)
    }

    fun getPitch(pitch: PitchDetails): ImageBitmap {
        return loadImageFromCache(pitch.resource)
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
            DOWN_LEFT -> {
                if (active) Res.drawable.icons_game_pb_southwest_filled else Res.drawable.icons_game_pb_southwest
            }
            DOWN -> {
                if (active) Res.drawable.icons_game_pb_south_filled else Res.drawable.icons_game_pb_south
            }
            DOWN_RIGHT -> {
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

    fun getInducementIcon(type: InducementType): DrawableResource? {
        return when {
            type is CommonInducementType -> {
                when (type) {
                    CommonInducementType.BIASED_REFEREE -> null
                    CommonInducementType.BRIBE -> null
                    CommonInducementType.DESPERATE_MEASURES -> null
                    CommonInducementType.EXTRA_TEAM_TRAINING -> null
                    CommonInducementType.HALFLING_MASTER_CHEF -> null
                    CommonInducementType.INFAMOUS_COACHING_STAFF -> null
                    CommonInducementType.MORTUARY_ASSISTANT -> null
                    CommonInducementType.PART_TIME_ASSISTANT_COACH -> null
                    CommonInducementType.PLAGUE_DOCTOR -> null
                    CommonInducementType.RIOTOUS_ROOKIE -> null
                    CommonInducementType.STANDARD_MERCENARY_PLAYERS -> null
                    CommonInducementType.STAR_PLAYERS -> null
                    CommonInducementType.TEMP_AGENCY_CHEERLEADER -> null
                    CommonInducementType.WANDERING_APOTHECARY -> Res.drawable.jervis_inducement_apothercary
                    CommonInducementType.WEATHER_MAGE -> null
                    CommonInducementType.WIZARD -> null
                }
            }
            type is BB2020InducementType -> {
                when (type) {
                    BB2020InducementType.BLOODWEISER_KEG -> Res.drawable.jervis_inducement_keg
                    BB2020InducementType.SPECIAL_PLAY -> null
                    BB2020InducementType.WAAAGH_DRUMMER -> null
                    BB2020InducementType.CAVORTING_NURGLINGS -> null
                    BB2020InducementType.DWARFEN_RUNESMITH -> null
                    BB2020InducementType.HALFLING_HOTPOT -> null
                    BB2020InducementType.MASTER_OF_BALLISTICS -> null
                    BB2020InducementType.EXPANDED_MERCENARY_PLAYERS -> null
                    BB2020InducementType.GIANT -> null
                    BB2020InducementType.DESPERATE_MEASURES -> null
                    BB2020InducementType.BRETONNIAN_PASTRIES -> null
                    BB2020InducementType.BRETONNIAN_DAMSEL -> null
                    BB2020InducementType.CANOPIC_JAR -> null
                }
            }
            type is BB2025InducementType -> {
                when (type) {
                    BB2025InducementType.BLITZERS_BEST_KEGS -> Res.drawable.jervis_inducement_keg
                    BB2025InducementType.PRAYERS_TO_NUFFLE -> null
                    BB2025InducementType.TEAM_MASCOT -> null
                }
            }
            else -> error("Unknown inducement type: $type")
        }
    }

    /**
     * Load an arbitrary `SpriteSource` (embedded, URL, or FUMBBL ini) as a single
     * `ImageBitmap`. Returns `null` if the source cannot be resolved. Intended for
     * one-off UI needs such as the Buy Inducements dialog rendering star player
     * icons.
     */
    suspend fun loadSpriteImage(sprite: SpriteSource): ImageBitmap? {
        return when (sprite.type) {
            SpriteLocation.EMBEDDED -> runCatching { loadImageFromResources(sprite.resource) }.getOrNull()
            SpriteLocation.URL -> runCatching { loadImageFromNetwork(Url(sprite.resource), useProxy = false) }.getOrNull()
            SpriteLocation.FUMBBL_INI -> runCatching { loadImageFromFumbblIni(sprite.resource) }.getOrNull()
            SpriteLocation.GENERATED -> null // No size context here; caller should fall back.
        }
    }

    /**
     * Load a player icon sprite, extracting the default frame for the given team side
     * when the sprite is a `SpriteSheet`. Returns `null` if the sprite cannot be loaded.
     */
    suspend fun loadPlayerIcon(sprite: SpriteSource, isHomeTeam: Boolean): ImageBitmap? {
        val raw = loadSpriteImage(sprite) ?: return null
        return when (sprite) {
            is SpriteSheet -> extractSprites(raw, sprite.variants, sprite.selectedIndex ?: 0, isHomeTeam).default
            else -> raw
        }
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
            SpriteLocation.URL -> loadImageFromNetwork(Url(logo.resource), true)
            SpriteLocation.FUMBBL_INI -> loadImageFromFumbblIni(logo.resource)
            SpriteLocation.GENERATED -> error("Generated logos are not supported yet")
        }
        when (size) {
            LogoSize.LARGE -> cachedLargeLogos[id] = image ?: error("Could not find logo: ${logo.resource}")
            LogoSize.SMALL -> cachedSmallLogos[id] = image ?: error("Could not find logo: ${logo.resource}")
        }
    }

    /**
     * Returns the logo for the given team and size or throws an exception if the logo is not found.
     */
    fun getLogo(id: TeamId, size: LogoSize): ImageBitmap {
        return getLogoOrNull(id, size) ?: error("Could not find logo: $id")
    }

    fun getLogoOrNull(id: TeamId, size: LogoSize): ImageBitmap? {
        return when (size) {
            LogoSize.LARGE -> cachedLargeLogos[id]
            LogoSize.SMALL -> cachedSmallLogos[id]
        }
    }

    suspend fun loadPlayerSprite(player: Player, isOnHomeTeam: Boolean): PlayerSprite {
        cachedPlayers[player.id]?.let { return it }
        val playerSprite = player.icon?.sprite
        val sprite = if (playerSprite == null) {
            createFallbackPlayerSprite(player, isOnHomeTeam)
        } else {
            val image = loadPlayerSpriteImage(playerSprite, player.position.size)
            if (image == null) {
                createFallbackPlayerSprite(player, isOnHomeTeam)
            } else {
                createPlayerSprite(image, playerSprite, isOnHomeTeam)
            }
        }
        cachedPlayers[player.id] = sprite
        return sprite
    }

    /**
     * Load a logo for the given team and size based on the [RosterLogo] configuration.
     */
    suspend fun loadRosterIcon(team: TeamId, logo: RosterLogo, size: LogoSize): ImageBitmap {
        val sprite = when (size) {
            LogoSize.LARGE -> logo.large ?: SingleSprite.embedded("jervis/roster/logo_default_large.png")
            LogoSize.SMALL -> logo.small ?: SingleSprite.embedded("jervis/roster/logo_default_small.png")
        }
        var cachedLogo = getLogoOrNull(team, size)
        if (cachedLogo == null) {
            saveLogo(team, sprite, size)
            cachedLogo = getLogo(team, size)
        }
        return cachedLogo
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
