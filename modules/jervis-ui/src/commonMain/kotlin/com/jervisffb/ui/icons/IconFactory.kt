package com.jervisffb.ui.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
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
import com.jervisffb.engine.model.isOnHomeTeam
import com.jervisffb.engine.rules.bb2020.roster.ChaosDwarfTeam
import com.jervisffb.engine.rules.bb2020.roster.ElvenUnionTeam
import com.jervisffb.engine.rules.bb2020.roster.HumanTeam
import com.jervisffb.engine.rules.bb2020.roster.KhorneTeam
import com.jervisffb.engine.rules.bb2020.roster.LizardmenTeam
import com.jervisffb.engine.rules.bb2020.roster.SkavenTeam
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.roster.Roster
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
import com.jervisffb.jervis_ui.generated.resources.icons_sidebar_overlay_player_detail_blue
import com.jervisffb.ui.getSubImage
import com.jervisffb.ui.loadFileAsImage
import com.jervisffb.ui.loadImage
import com.jervisffb.ui.model.UiPlayer
import com.jervisffb.ui.viewmodel.FieldDetails
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource


const val iconRootPath = "icons/cached/players/iconsets"
val playerIconSpriteSheets =
    mutableMapOf(
        HumanTeam.LINEMAN to "$iconRootPath/human_lineman.png",
        HumanTeam.THROWER to "$iconRootPath/human_thrower.png",
        HumanTeam.CATCHER to "$iconRootPath/human_catcher.png",
        HumanTeam.BLITZER to "$iconRootPath/human_blitzer.png",
        HumanTeam.HALFLING_HOPEFUL to "$iconRootPath/human_halflinghopeful.png",
        HumanTeam.OGRE to "$iconRootPath/human_ogre.png",
        ChaosDwarfTeam.HOBGOBLIN_LINEMEN to "$iconRootPath/chaosdwarf_hobgoblinlineman.png",
        ChaosDwarfTeam.CHAOS_DWARF_BLOCKERS to "$iconRootPath/chaosdwarf_chaosdwarfblocker.png",
        ChaosDwarfTeam.BULL_CENTAUR_BLITZERS to "$iconRootPath/chaosdwarf_bullcentaurblitzer.png",
        ChaosDwarfTeam.ENSLAVED_MINOTAUR to "$iconRootPath/chaosdwarf_enslavedminotaur.png",
        KhorneTeam.BLOODBORN_MARAUDER_LINEMEN to "$iconRootPath/khorne_bloodbornmarauderlineman.png",
        KhorneTeam.KHORNGORS to "$iconRootPath/khorne_khorngor.png",
        KhorneTeam.BLOODSEEKERS to "$iconRootPath/khorne_bloodseeker.png",
        KhorneTeam.BLOODSPAWN to "$iconRootPath/khorne_bloodspawn.png",
        ElvenUnionTeam.LINEMAN to "$iconRootPath/elvenunion_lineman.png",
        ElvenUnionTeam.CATCHER to "$iconRootPath/elvenunion_catcher.png",
        ElvenUnionTeam.THROWER to "$iconRootPath/elvenunion_thrower.png",
        ElvenUnionTeam.BLITZER to "$iconRootPath/elvenunion_blitzer.png",
        SkavenTeam.LINEMAN to "$iconRootPath/skaven_lineman.png",
        SkavenTeam.THROWER to "$iconRootPath/skaven_thrower.png",
        SkavenTeam.GUTTER_RUNNER to "$iconRootPath/skaven_gutterrunner.png",
        SkavenTeam.BLITZER to "$iconRootPath/skaven_blitzer.png",
        SkavenTeam.RAT_OGRE to "$iconRootPath/skaven_ratogre.png",
        LizardmenTeam.SKINK_RUNNER_LINEMEN to "$iconRootPath/lizardmen_skinkrunnerlineman.png",
        LizardmenTeam.CHAMELEON_SKINKS to "$iconRootPath/lizardmen_chameleonskink.png",
        LizardmenTeam.SAURUS_BLOCKERS to "$iconRootPath/lizardmen_saurusblocker.png",
        LizardmenTeam.KROXIGOR to "$iconRootPath/lizardmen_kroxigor.png",
    )

data class PositionImage(
    val default: ImageBitmap,
    val active: ImageBitmap,
)

class PositionImageFactory(spriteSheet: ImageBitmap) {
    private val homeTeamIcons: List<PositionImage>
    private val awayTeamIcons: List<PositionImage>

    init {
        val homeIcons = mutableListOf<PositionImage>()
        val awayIcons = mutableListOf<PositionImage>()
        extractSprites(spriteSheet).forEach {
            homeIcons.add(it.first)
            awayIcons.add(it.second)
        }
        homeTeamIcons = homeIcons
        awayTeamIcons = awayIcons
    }

    private fun extractSprites(image: ImageBitmap): List<Pair<PositionImage, PositionImage>> {
        val spriteWidth = image.width / 4 // There are always 4 sprites pr line.
        val spriteHeight: Int = spriteWidth
        val lines = image.height / spriteHeight
        return (0 until lines).map { line: Int ->
            val homeDefaultX = 0
            val homeActiveX = spriteWidth
            val awayDefaultX = spriteWidth * 2
            val awayActiveX = spriteWidth * 3
            val homeDefault = image.getSubImage(homeDefaultX, line * spriteHeight, spriteWidth, spriteHeight)
            val homeActive = image.getSubImage(homeActiveX, line * spriteHeight, spriteWidth, spriteHeight)
            val awayDefault = image.getSubImage(awayDefaultX, line * spriteHeight, spriteWidth, spriteHeight)
            val awayActive = image.getSubImage(awayActiveX, line * spriteHeight, spriteWidth, spriteHeight)
            val homePlayer = PositionImage(homeDefault, homeActive)
            val awayPlayer = PositionImage(awayDefault, awayActive)
            Pair(homePlayer, awayPlayer)
        }
    }

    fun getVariant(player: Player): PositionImage {
        val imageIndex = player.number.value % homeTeamIcons.size
        return if (player.isOnHomeTeam()) {
            homeTeamIcons[imageIndex]
        } else {
            awayTeamIcons[imageIndex]
        }
    }
}

object IconFactory {
    private val iconHeight = 40
    private val iconWidth = 40
    // private var classLoader: ClassLoader

    private val cachedSpriteSheets: MutableMap<Position, ImageBitmap> = mutableMapOf()
    private val cachedPositionVariants: MutableMap<Position, PositionImageFactory> = mutableMapOf()
    private val cachedPlayers: MutableMap<Player, PositionImage> = mutableMapOf()
    private val cachedImages: MutableMap<String, ImageBitmap> = mutableMapOf()

    // Load all image resources used.
    // It looks like we cannot lazy load them due to how Compose Resources work on WasmJS
    // `Res.readBytes` is suspendable and runBlocking doesn't work on wasmJs, which makes
    // loading images in the middle of a Composable function quite a nightmare.
    // Instead we pre-load all dynamic resources up front. This will probably result in slightly
    // higher memory usage, but it will probably not be problematic.
    suspend fun initialize(homeTeam: Team, awayTeam: Team): Boolean {
        // TODO How to map player images?
        saveFileIntoCache("icons/cached/players/portraits/AnqiPanqi.png")

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
        cache: Boolean = false,
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

    private suspend fun getPositionSpriteSheet(position: Position): PositionImageFactory {
        if (cachedPositionVariants.contains(position)) {
            return cachedPositionVariants[position]!!
        } else {
            val path: String = playerIconSpriteSheets[position] ?: throw IllegalStateException("Cannot find player icons configured for: $position")
            val spriteSheet: ImageBitmap = loadImageFromResources(path)
            return PositionImageFactory(spriteSheet).also {
                cachedPositionVariants[position] = it
            }
        }
    }

    private suspend fun saveTeamPlayerImagesToCache(team: Team) {
        team.forEach { player ->
            val variants = getPositionSpriteSheet(player.position)
            val playerImage: PositionImage = variants.getVariant(player)
            cachedPlayers[player] = playerImage
        }
    }

    fun getImage(player: UiPlayer): ImageBitmap {
        val isHomeTeam: Boolean = player.isOnHomeTeam
        val roster: Roster = player.position.roster
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
    fun getPlayerDetailOverlay(): ImageBitmap {
        return imageResource(Res.drawable.icons_sidebar_overlay_player_detail_blue)
    }

    fun getPlayerImage(player: PlayerId): ImageBitmap {
        // TODO If we want the jervis-engine to not track
        //  player images. Where/how do we do the mapping?
        return loadImageFromCache("icons/cached/players/portraits/AnqiPanqi.png")
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
    fun getSidebarBannerTop(isHomeTeam: Boolean): ImageBitmap {
        return when(isHomeTeam) {
            true -> imageResource(Res.drawable.icons_sidebar_background_player_detail_red)
            false -> imageResource(Res.drawable.icons_sidebar_background_player_detail_blue)
        }
    }

    @Composable
    fun getSidebarBannerMiddle(isHomeTeam: Boolean): ImageBitmap {
        return when(isHomeTeam) {
            true -> imageResource(Res.drawable.icons_sidebar_background_turn_dice_status_red)
            false -> imageResource(Res.drawable.icons_sidebar_background_turn_dice_status_blue)
        }
    }

    @Composable
    fun getSidebarBannerBottom(isHomeTeam: Boolean): ImageBitmap {
        return when(isHomeTeam) {
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
}
