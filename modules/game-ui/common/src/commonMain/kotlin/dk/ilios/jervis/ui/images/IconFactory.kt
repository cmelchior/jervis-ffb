package dk.ilios.jervis.ui.images

import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.isOnHomeTeam
import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.Roster
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO

const val iconRootPath = "icons/cached/players/iconsets"
val playerIconSpriteSheets = mutableMapOf(
    HumanTeam.LINEMAN to "$iconRootPath/human_lineman.png",
    HumanTeam.THROWER to "$iconRootPath/human_thrower.png",
    HumanTeam.CATCHER to "$iconRootPath/human_catcher.png",
    HumanTeam.BLITZER to "$iconRootPath/human_blitzer.png",
    HumanTeam.OGRE to "$iconRootPath/human_ogre.png",
    HumanTeam.HALFLING_HOPEFUL to "$iconRootPath/human_haflinghopeful.png"
)

data class PositionImage(
    val default: BufferedImage,
    val active: BufferedImage
)

class PositionImageFactory(spriteSheet: BufferedImage) {
    private val spriteWidth = 28
    private val spriteHeight = 28
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

    private fun extractSprites(image: BufferedImage): List<Pair<PositionImage, PositionImage>> {
        val lines = image.height/spriteHeight
        return (0 until lines).map { line ->
            val homeDefaultX = 0
            val homeActiveX = spriteWidth
            val awayDefaultX = spriteWidth*2
            val awayActiveX = spriteWidth*3
            val homeDefault = image.getSubimage(homeDefaultX, line*spriteHeight, spriteWidth, spriteHeight)
            val homeActive = image.getSubimage(homeActiveX, line*spriteHeight, spriteWidth, spriteHeight)
            val awayDefault = image.getSubimage(awayDefaultX, line*spriteHeight, spriteWidth, spriteHeight)
            val awayActive = image.getSubimage(awayActiveX, line*spriteHeight, spriteWidth, spriteHeight)
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
    private var classLoader: ClassLoader
    private val cachedSpriteSheets: MutableMap<Position, BufferedImage> = mutableMapOf()
    private val cachedPositionVariants: MutableMap<Position, PositionImageFactory> = mutableMapOf()
    private val cachedImages: MutableMap<Player, PositionImage> = mutableMapOf()

    init {
        classLoader = Thread.currentThread().contextClassLoader
    }

    private fun loadImageFromResources(path: String): BufferedImage {
        val input: InputStream = classLoader.getResourceAsStream(path)
        return ImageIO.read(input)
    }

    private fun getPositionSpriteSheet(position: Position): PositionImageFactory {
        if (cachedPositionVariants.contains(position)) {
            return cachedPositionVariants[position]!!
        } else {
            val path: String = playerIconSpriteSheets[position] ?: throw IllegalStateException("Cannot find player icons configured for: $position")
            val spriteSheet: BufferedImage = loadImageFromResources(path)
            return PositionImageFactory(spriteSheet).also {
                cachedPositionVariants[position] = it
            }
        }
    }

    fun getImage(player: Player): BufferedImage {
        val isHomeTeam: Boolean = player.isOnHomeTeam()
        val roster: Roster = player.position.roster
        val playerType: Position = player.position
        val isActive = false // game.activePlayer

        if (cachedImages.contains(player)) {
            return cachedImages[player]!!.default
        } else {
            val variants = getPositionSpriteSheet(player.position)
            val playerImage = variants.getVariant(player)
            return playerImage.default
        }
    }
}