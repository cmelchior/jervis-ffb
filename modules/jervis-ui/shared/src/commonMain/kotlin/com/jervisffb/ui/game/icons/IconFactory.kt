package com.jervisffb.ui.game.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
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
import com.jervisffb.engine.bb2020.inducements.InducementType2020
import com.jervisffb.engine.bb2025.inducements.InducementType2025
import com.jervisffb.engine.common.inducements.InducementTypeCommon
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
import com.jervisffb.ui.utils.getSubImage
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.toImageBitmap
import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource

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
class IconFactory internal constructor(
    private val httpClient: HttpClient,
    getCachedOnDiskImage: suspend (Url) -> ImageBitmap?,
    saveOnDiskCachedImage: suspend (Url, ImageBitmap) -> Unit,
) {

    constructor(httpClient: HttpClient) : this(
        httpClient = httpClient,
        getCachedOnDiskImage = CacheManager::getCachedImage,
        saveOnDiskCachedImage = CacheManager::saveImage,
    )

    private val DEFAULT_PORTRAIT = SingleSprite.embedded("jervis/portraits/default_portrait.png")

    private val initializationMutex = Mutex()
    private var initializedStaticResources: StaticResourcesCache? = null
    private val initializedTeamResources = TeamResourcesCache(
        httpClient = httpClient,
        getCachedOnDiskImage = getCachedOnDiskImage,
        saveOnDiskCachedImage = saveOnDiskCachedImage,
    )
    private val staticResources: StaticResourcesCache
        get() = checkNotNull(initializedStaticResources) { "Static icon assets have not been initialized" }
    private val teamResources: TeamResourcesCache
        get() = initializedTeamResources

    // Many of the assets are pixel-art, where we want to preserve as much or the
    // blockiness as possible. Use the scale factor to adjust the size of images
    // so they are close to the intended usage size. This will remove interpolation
    // artifacts for smaller adjustments to the size.
    val scaleFactor: Int
        get() = initializedStaticResources?.scaleFactor ?: 1

    /**
     * Loads the fumbbl ini file and prepare the mapping between local paths
     * and download URLs
     */
    suspend fun initializeFumbblMapping() {
        teamResources.initializeFumbblMapping()
    }

    // Load all static image resources used.
    // It looks like we cannot lazy-load them due to how Compose Resources work on WasmJS
    // `Res.readBytes` is suspendable and runBlocking doesn't work on wasmJs, which makes
    // loading images in the middle of a Composable function quite a nightmare.
    // Instead, we preload all dynamic resources up front. This will result in slightly
    // higher memory usage, but it will probably not be problematic.
    suspend fun initializeStaticAssets(density: Density) {
        initializationMutex.withLock {
            if (initializedStaticResources != null) return@withLock

            val scaleFactor = density.density.toInt()
            val newCache = StaticResourcesCache(scaleFactor, httpClient)
            PitchDetails.entries.forEach { pitch ->
                newCache.loadPitch(pitch)
            }
            initializeDiceMappings(newCache)
            initializeGameActionIcons(newCache)
            initializedStaticResources = newCache
        }
    }

    /**
     * Add assets for [homeTeam] and [awayTeam] to the shared team cache.
     *
     * Static assets must be initialized first. Existing team assets are
     * retained when adding new team assets. They do not take up that much
     * memory and make swapping between different teams faster (like when
     * switching between challenges).
     */
    suspend fun initializeTeamAssets(homeTeam: Team, awayTeam: Team) {
        initializationMutex.withLock {
            checkNotNull(initializedStaticResources) {
                "Static icon assets must be initialized before team assets"
            }
            val newCache = initializedTeamResources.copy()
            saveTeamPlayerImagesToCache(homeTeam, newCache)
            saveTeamPlayerImagesToCache(awayTeam, newCache)
            // It should be safe to always write the result into the cache.
            // The only downside is perhaps getting too many teams into the
            // cache when browsing many teams, but that should be fine for now.
            // currentCoroutineContext().ensureActive()
            newCache.copyInto(initializedTeamResources)
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

    private suspend fun loadPlayerSpriteImage(
        playerSprite: SpriteSource,
        size: PlayerSize,
        cache: TeamResourcesCache,
    ): ImageBitmap? {
        return cache.loadSprite(playerSprite)
            ?: playerSprite.takeIf { it.type == SpriteLocation.GENERATED }?.let {
                cache.getOrCreateGeneratedPlayerSprite("${it.resource}:$size") {
                    generatePlayerSprite(letters = it.resource, size)
                }
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

    private suspend fun createFallbackPlayerSprite(
        player: Player,
        isHomeTeam: Boolean,
        cache: TeamResourcesCache,
    ): PlayerSprite {
        val letters = player.position.shortHand.ifBlank { "?" }
        val size = player.position.size
        val spriteSheet = cache.getOrCreateGeneratedPlayerSprite("$letters:$size") {
            PlayerSpriteFallbackGenerator.generatePlayerSprite(letters, size)
        }
        return extractSprites(spriteSheet, variants = 1, selectedIndex = 0, onHomeTeam = isHomeTeam)
    }

    private suspend fun initializeDiceMappings(cache: StaticResourcesCache) {
        DiceColor.entries.forEach {
            cache.dice[it] = mutableMapOf()
        }

        // Block Dice
        DBlockResult.allOptions().forEach {
            val die = it.blockResult
            val typeAsFileName = die.name.lowercase().replace("_", "")
            val path = "jervis/dice/jervis_dblock_black_$typeAsFileName.png"
            cache.loadDice(DiceColor.DEFAULT, it, path, saveAsDefault = false)
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
                val resourcePath = "jervis/dice/jervis_d6_${color.name.lowercase()}_${it.value}.png"
                cache.loadDice(color, it, resourcePath, saveAsDefault = isDefault)
            }
        }

        d6sColors.forEach { (color, isDefault) ->
            D6Result.allOptions().forEach {
                val resourcePath = "jervis/dice/jervis_d6_${color.name.lowercase()}_${it.value}.png"
                cache.loadDice(color, it, resourcePath, saveAsDefault = isDefault)
            }
        }

        // D8
        D8Result.allOptions().forEach {
            val resourcePath = "jervis/dice/jervis_d8_purple_${it.value}.png"
            cache.loadDice(DiceColor.DEFAULT, it, resourcePath)
        }

        // D12
        D12Result.allOptions().forEach {
            // We do not have a proper D12 yet, so just reuse D20
            val resourcePath = "jervis/dice/jervis_d20_green_${it.value}.png"
            cache.loadDice(DiceColor.DEFAULT, it, resourcePath)
        }

        // D16
        D16Result.allOptions().forEach {
            // We do not have a proper D16 yet, so just reuse D20
            val resourcePath = "jervis/dice/jervis_d20_green_${it.value}.png"
            cache.loadDice(DiceColor.DEFAULT, it, resourcePath)
        }

        // D20
        D20Result.allOptions().forEach {
            val resourcePath = "jervis/dice/jervis_d20_green_${it.value}.png"
            cache.loadDice(DiceColor.DEFAULT, it, resourcePath)
        }

        // Coins
        Coin.entries.forEach {
            val resourcePath = "jervis/dice/jervis_coin_${it.name.lowercase()}.png"
            cache.loadCoin(it, resourcePath)
        }
    }

    private suspend fun initializeGameActionIcons(cache: StaticResourcesCache) {
        ActionIcon.entries.forEach { icon ->
            cache.loadActionIcon(icon)
        }
    }

    private suspend fun saveTeamPlayerImagesToCache(team: Team, cache: TeamResourcesCache) {
        team.forEach { player ->
            loadPlayerSprite(player, player.isOnHomeTeam(), cache)
            if (!cache.portraits.containsKey(player.id)) {
                val portrait = player.icon?.portrait ?: DEFAULT_PORTRAIT
                val portraitImage = cache.loadPortrait(portrait)
                    ?: checkNotNull(cache.loadPortrait(DEFAULT_PORTRAIT))
                cache.portraits[player.id] = portraitImage
            }
        }
    }

    fun getPlayerIcon(player: UiPitchPlayer): ImageBitmap {
        val isActive = player.isActive
        val playerSpriteKey = TeamResourcesCache.PlayerSpriteKey(player.id, player.isOnHomeTeam)
        if (teamResources.players.contains(playerSpriteKey)) {
            return if (isActive) {
                teamResources.players[playerSpriteKey]!!.active
            } else {
                teamResources.players[playerSpriteKey]!!.default
            }
        } else {
            error("Could not find player: ${player.id}")
        }
    }

    /**
     * Returns size of dice image for the current dice type in [androidx.compose.ui.unit.Dp].
     */
    fun getDiceSizeDp(die: DieResult): DpSize {
        val cache = staticResources
        val image = cache.dice[DiceColor.DEFAULT]?.get(die) ?: error("Could not find die: $die")
        return DpSize(
            (image.width / cache.scaleFactor).jdp * 1.25f,
            (image.height / cache.scaleFactor).jdp * 1.25f
        )
    }

    /**
     * Returns size of dice image for the current dice type in pixels
     */
    fun getDiceSizePx(die: DieResult): Size {
        val image = staticResources.dice[DiceColor.DEFAULT]?.get(die) ?: error("Could not find die: $die")
        return Size(image.width.toFloat(), image.height.toFloat())
    }

    @Composable
    fun getDiceIcon(die: DieResult, color: DiceColor = DiceColor.DEFAULT): ImageBitmap {
        return staticResources.dice[color]?.get(die) ?: error("Could not find die: $die [$color]")
    }

    @Composable
    fun getCoinIcon(coin: Coin): ImageBitmap {
        return staticResources.coins[coin] ?: error("Could not find coin: $coin")
    }

    fun getCoinSizeDp(coin: Coin): DpSize {
        val cache = staticResources
        val image = cache.coins[coin] ?: error("Could not find coin: $coin")
        return DpSize(
            (image.width / cache.scaleFactor).jdp * 1.25f,
            (image.height / cache.scaleFactor).jdp * 1.25f
        )
    }

    fun getActionIcon(action: ActionIcon): ImageBitmap {
        return staticResources.actionIcons[action] ?: error("Could not find action: $action")
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
        return teamResources.portraits[player] ?: error("Could not find player portrait: $player")
    }

    @Composable
    fun getSidebarBackground(): ImageBitmap {
        return imageResource(Res.drawable.jervis_dogout)
    }

    fun getPitch(pitch: PitchDetails): ImageBitmap {
        return staticResources.pitches[pitch] ?: error("Could not find pitch: $pitch")
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
            type is InducementTypeCommon -> {
                when (type) {
                    InducementTypeCommon.BIASED_REFEREE -> null
                    InducementTypeCommon.BRIBE -> null
                    InducementTypeCommon.DESPERATE_MEASURES -> null
                    InducementTypeCommon.EXTRA_TEAM_TRAINING -> null
                    InducementTypeCommon.HALFLING_MASTER_CHEF -> null
                    InducementTypeCommon.INFAMOUS_COACHING_STAFF -> null
                    InducementTypeCommon.MORTUARY_ASSISTANT -> null
                    InducementTypeCommon.PART_TIME_ASSISTANT_COACH -> null
                    InducementTypeCommon.PLAGUE_DOCTOR -> null
                    InducementTypeCommon.RIOTOUS_ROOKIE -> null
                    InducementTypeCommon.STANDARD_MERCENARY_PLAYERS -> null
                    InducementTypeCommon.STAR_PLAYERS -> null
                    InducementTypeCommon.TEMP_AGENCY_CHEERLEADER -> null
                    InducementTypeCommon.WANDERING_APOTHECARY -> Res.drawable.jervis_inducement_apothercary
                    InducementTypeCommon.WEATHER_MAGE -> null
                    InducementTypeCommon.WIZARD -> null
                }
            }
            type is InducementType2020 -> {
                when (type) {
                    InducementType2020.BLOODWEISER_KEG -> Res.drawable.jervis_inducement_keg
                    InducementType2020.SPECIAL_PLAY -> null
                    InducementType2020.WAAAGH_DRUMMER -> null
                    InducementType2020.CAVORTING_NURGLINGS -> null
                    InducementType2020.DWARFEN_RUNESMITH -> null
                    InducementType2020.HALFLING_HOTPOT -> null
                    InducementType2020.MASTER_OF_BALLISTICS -> null
                    InducementType2020.EXPANDED_MERCENARY_PLAYERS -> null
                    InducementType2020.GIANT -> null
                    InducementType2020.DESPERATE_MEASURES -> null
                    InducementType2020.BRETONNIAN_PASTRIES -> null
                    InducementType2020.BRETONNIAN_DAMSEL -> null
                    InducementType2020.CANOPIC_JAR -> null
                }
            }
            type is InducementType2025 -> {
                when (type) {
                    InducementType2025.BLITZERS_BEST_KEGS -> Res.drawable.jervis_inducement_keg
                    InducementType2025.PRAYERS_TO_NUFFLE -> null
                    InducementType2025.TEAM_MASCOT -> null
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
        return teamResources.loadSprite(sprite)
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

    suspend fun saveLogo(id: TeamId, logo: SpriteSource, size: LogoSize) {
        checkNotNull(teamResources.loadLogo(id, logo, size)) {
            "Could not find logo: ${logo.resource}"
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
            LogoSize.LARGE -> teamResources.largeLogos[id]
            LogoSize.SMALL -> teamResources.smallLogos[id]
        }
    }

    suspend fun loadPlayerSprite(player: Player, isOnHomeTeam: Boolean): PlayerSprite {
        return loadPlayerSprite(player, isOnHomeTeam, teamResources)
    }

    private suspend fun loadPlayerSprite(
        player: Player,
        isOnHomeTeam: Boolean,
        cache: TeamResourcesCache,
    ): PlayerSprite {
        val playerSpriteKey = TeamResourcesCache.PlayerSpriteKey(player.id, isOnHomeTeam)
        cache.players[playerSpriteKey]?.let { return it }
        val playerSprite = player.icon?.sprite
        val sprite = if (playerSprite == null) {
            createFallbackPlayerSprite(player, isOnHomeTeam, cache)
        } else {
            val image = loadPlayerSpriteImage(playerSprite, player.position.size, cache)
            if (image == null) {
                createFallbackPlayerSprite(player, isOnHomeTeam, cache)
            } else {
                createPlayerSprite(image, playerSprite, isOnHomeTeam)
            }
        }
        cache.players[playerSpriteKey] = sprite
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
            cachedLogo = checkNotNull(teamResources.loadLogo(team, sprite, size)) {
                "Could not find logo: ${sprite.resource}"
            }
        }
        return cachedLogo
    }

    /**
     * Load a logo directly. Normally the overload with [RosterLogo] should be used instead.
     */
    suspend fun loadRosterIcon(team: TeamId, logo: SpriteSource?, size: LogoSize): ImageBitmap? {
        if (logo == null) return null
        return teamResources.loadLogo(team, logo, size)
    }
}
