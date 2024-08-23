package dk.ilios.jervis.ui.images

import androidx.compose.ui.graphics.ImageBitmap
import dk.ilios.bloodbowl.ui.game_ui.generated.resources.Res
import dk.ilios.jervis.actions.BlockDice
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.isOnHomeTeam
import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.Roster
import dk.ilios.jervis.rules.roster.bb2020.ChaosDwarfTeam
import dk.ilios.jervis.rules.roster.bb2020.ElvenUnionTeam
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.rules.roster.bb2020.KhorneTeam
import dk.ilios.jervis.rules.roster.bb2020.SkavenTeam
import dk.ilios.jervis.ui.getSubImage
import dk.ilios.jervis.ui.loadImage
import dk.ilios.jervis.ui.model.UiPlayer


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
        val imageIndex = player.number.number % homeTeamIcons.size
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

    private fun loadImageFromResources(
        path: String,
        cache: Boolean = false,
    ): ImageBitmap {
        if (cache && cachedImages.containsKey(path)) {
            return cachedImages[path]!!
        } else {
            try {
                val image = Res.loadImage(path)
                cachedImages[path] = image
                return image
            } catch (ex: NullPointerException) {
                throw IllegalStateException("Could not find $path")
            }
        }
    }

    private fun getPositionSpriteSheet(position: Position): PositionImageFactory {
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

    fun getDiceIcon(die: BlockDice): ImageBitmap {
        val root = "icons/sidebar/dice"
        val path = when(die) {
            BlockDice.PLAYER_DOWN -> "new_skool_black_1.png"
            BlockDice.BOTH_DOWN -> "new_skool_black_2.png"
            BlockDice.PUSH_BACK -> "new_skool_black_3_4.png"
            BlockDice.STUMBLE -> "new_skool_black_5.png"
            BlockDice.POW -> "new_skool_black_6.png"
        }

        // Find platform path seperator somewhere
        return loadImageFromResources("$root/$path", cache = true)
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
            val variants = getPositionSpriteSheet(player.position)
            val playerImage = variants.getVariant(player.model)
            cachedPlayers[player.model] = playerImage
            return if (isActive) {
                playerImage.active
            } else {
                playerImage.default
            }
        }
    }

    fun getHeldBallOverlay(): ImageBitmap {
        return loadImageFromResources("icons/decorations/holdball.png", cache = true)
    }

    fun getBall(): ImageBitmap {
        return loadImageFromResources("icons/game/sball_30x30.png")
    }
}
