package com.jervisffb.ui.game.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.jervisffb.engine.actions.BlockDice
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
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_box
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_player_detail_blue
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_player_detail_red
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_resource_blue
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_resource_red
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_turn_dice_status_blue
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_background_turn_dice_status_red
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_box_button
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_dice_new_skool_black_1
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_dice_new_skool_black_2
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_dice_new_skool_black_3_4
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_dice_new_skool_black_5
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_dice_new_skool_black_6
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_overlay_player_detail_blue_modified
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_overlay_player_detail_red_modified
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_turn_button
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.game.model.UiPlayer
import com.jervisffb.ui.game.viewmodel.FieldDetails
import com.jervisffb.ui.getSubImage
import com.jervisffb.ui.loadFileAsImage
import com.jervisffb.ui.loadImage
import com.jervisffb.utils.getHttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.http.Url
import io.ktor.http.isSuccess
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.skia.Image

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
 * Main class responsible for handling all a logic around fetching and storing
 * graphic assets.
 *
 * A lot of the methods in here are `suspend` functions due to how WASM loads
 * resources.
 */
object IconFactory {
    private val iconHeight = 40
    private val iconWidth = 40
    private val cachedPlayers: MutableMap<Player, PlayerSprite> = mutableMapOf()
    // Map from resource "path" to loaded in-memory image
    private val cachedImages: MutableMap<String, ImageBitmap> = mutableMapOf()
    private val cachedPortraits: MutableMap<PlayerId, ImageBitmap> = mutableMapOf()
    private val cachedLargeLogos: MutableMap<TeamId, ImageBitmap> = mutableMapOf()
    private val cachedSmallLogos: MutableMap<TeamId, ImageBitmap> = mutableMapOf()

    private val httpClient = getHttpClient()

    // Load all image resources used.
    // It looks like we cannot lazy load them due to how Compose Resources work on WasmJS
    // `Res.readBytes` is suspendable and runBlocking doesn't work on wasmJs, which makes
    // loading images in the middle of a Composable function quite a nightmare.
    // Instead we pre-load all dynamic resources up front. This will probably result in slightly
    // higher memory usage, but it will probably not be problematic.
    suspend fun initialize(homeTeam: Team, awayTeam: Team): Boolean {
        FieldDetails.entries.forEach {
            saveFileIntoCache(it.resource)
        }
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
            } catch (ex: NullPointerException) {
                throw IllegalStateException("Could not find $path")
            }
        }
    }

    private suspend fun createPlayerSprite(player: Player, isHomeTeam: Boolean): PlayerSprite {
        val playerSprite = player.icon?.sprite ?: throw IllegalStateException("Cannot find sprite configured for: $player")
        val image = when (playerSprite.type) {
            SpriteLocation.EMBEDDED -> loadImageFromResources(playerSprite.resource)
            SpriteLocation.URL -> loadImageFromNetwork(Url(playerSprite.resource))!! // TODO
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

    private suspend fun loadImageFromNetwork(url: Url): ImageBitmap? {
        val cachedImage = CacheManager.getCachedImage(url)
        if (cachedImage != null) return cachedImage
        val result = httpClient.get(url)
        return if (result.status.isSuccess()) {
            val bytes = result.readBytes()
            val image = Image.makeFromEncoded(bytes).toComposeImageBitmap()
            CacheManager.saveImage(url, image)
            image
        } else {
            null
        }
    }

    private suspend fun saveTeamPlayerImagesToCache(team: Team) {
        team.forEach { player ->
            val playerSprite = createPlayerSprite(player, player.isOnHomeTeam())
            cachedPlayers[player] = playerSprite
            val portrait = player?.icon?.portrait ?: TODO()
            val portraitImage = when (portrait.type) {
                SpriteLocation.EMBEDDED -> loadImageFromResources(portrait.resource)
                SpriteLocation.URL -> loadImageFromNetwork(Url(portrait.resource))
                null -> TODO()
            }
            cachedPortraits[player.id] = portraitImage!! // TODO Fix null value
        }
    }

    fun getImage(player: UiPlayer): ImageBitmap {
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

    @Composable
    fun getDiceIcon(die: BlockDice): ImageBitmap {
        val res = when (die) {
            BlockDice.PLAYER_DOWN -> Res.drawable.icons_sidebar_dice_new_skool_black_1
            BlockDice.BOTH_DOWN -> Res.drawable.icons_sidebar_dice_new_skool_black_2
            BlockDice.PUSH_BACK -> Res.drawable.icons_sidebar_dice_new_skool_black_3_4
            BlockDice.STUMBLE -> Res.drawable.icons_sidebar_dice_new_skool_black_5
            BlockDice.POW -> Res.drawable.icons_sidebar_dice_new_skool_black_6
        }
        return imageResource(res)
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

    fun getPlayerImage(player: PlayerId): ImageBitmap {
        return cachedPortraits[player]!!
    }

    @Composable
    fun getSidebarBackground(): ImageBitmap {
        return imageResource(Res.drawable.icons_sidebar_background_box)
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

    fun hasLogo(id: TeamId, size: LogoSize): Boolean {
        return when (size) {
            LogoSize.LARGE -> cachedLargeLogos.contains(id)
            LogoSize.SMALL -> cachedSmallLogos.contains(id)
        }
    }

    suspend fun saveLogo(id: TeamId, logo: SpriteSource, size: LogoSize) {
        val image = when (logo.type) {
            SpriteLocation.EMBEDDED -> loadImageFromResources(logo.resource)
            SpriteLocation.URL -> loadImageFromNetwork(Url(logo.resource))
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
            SpriteLocation.URL -> loadImageFromNetwork(Url(playerSprite.resource))!! // TODO
        }
        val sprite = when (val sprite = playerSprite) {
            is SingleSprite -> {
                PlayerSprite(image, image)
            }
            is SpriteSheet -> {
                extractSprites(image, sprite.variants, sprite.selectedIndex ?: 0, isOnHomeTeam)
            }
            null -> TODO()
        }
        cachedPlayers[player] = sprite
        return sprite
    }

    /**
     * Load a logo for the given team and size based on the [RosterLogo] configuration.
     */
    suspend fun loadRosterIcon(team: TeamId, logo: RosterLogo, size: LogoSize): ImageBitmap {
        // TODO If no logo is defined we need to have a placeholder
        val sprite = when (size) {
            LogoSize.LARGE -> logo.large ?: error("Could not find large logo: $logo")
            LogoSize.SMALL -> logo.small ?: error("Could not find small logo: $logo")
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
