package com.jervisffb.ui.game.icons

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteLocation
import com.jervisffb.engine.sprites.SpriteSource
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.game.viewmodel.PitchDetails
import com.jervisffb.ui.loadFileAsImage
import com.jervisffb.ui.utils.scalePixels
import com.jervisffb.utils.loggerInstance
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import io.ktor.http.headers
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.internal.commonToUtf8String
import org.jetbrains.skia.Image

/**
 * Resource caches used by [IconFactory]. They should only be accessed through
 * that class.
 */

/**
 * Shared logic for static and team caches.
 * This is an in-memory cache, with a 2nd-level cache on disk.
 * It will keep all teams loaded into it for the duration of the application.
 * This trade-off is acceptable (for now), as player sprites are pretty small.
 */
internal abstract class AbstractResourcesCache(
    protected val httpClient: HttpClient,
    // Callbacks defining how to access and store files on-disk for the 2nd level cache
    protected val getCachedImage: suspend (Url) -> ImageBitmap? = CacheManager::getCachedImage,
    protected val saveCachedImage: suspend (Url, ImageBitmap) -> Unit = CacheManager::saveImage,
) {
    private data class ResourceKey(
        val location: SpriteLocation,
        val resource: String,
    )

    private val resources: MutableMap<ResourceKey, ImageBitmap> = mutableMapOf()

    // Track all in-flight network requests, so we can track the lifetime of them correctly.
    private val inFlightRequests: MutableMap<Url, CompletableDeferred<ImageBitmap?>> = mutableMapOf()
    private val inFlightRequestsMutex = Mutex()

    protected suspend fun loadResource(source: SpriteSource): ImageBitmap? {
        val key = ResourceKey(source.type, source.resource)
        resources[key]?.let { return it }

        val image = when (source.type) {
            SpriteLocation.EMBEDDED -> loadEmbeddedResource(source.resource)
            SpriteLocation.URL -> loadNetworkResource(Url(source.resource))
            SpriteLocation.FUMBBL_INI -> {
                val url = resolveFumbblUrl(source.resource) ?: return null
                loadNetworkResource(url)
            }
            SpriteLocation.GENERATED -> null
        }
        if (image != null) {
            resources[key] = image
        }
        return image
    }

    protected open fun resolveFumbblUrl(path: String): Url? = null

    private suspend fun loadEmbeddedResource(resourcePath: String): ImageBitmap {
        return try {
            Res.loadFileAsImage(resourcePath)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            throw IllegalStateException("Problems loading image resource: $resourcePath", ex)
        }
    }

    private suspend fun loadNetworkResource(url: Url): ImageBitmap? {
        getCachedImage(url)?.let { return it }
        val (deferred, ownsRequest) = inFlightRequestsMutex.withLock {
            val existing = inFlightRequests[url]
            when (existing) {
                null -> CompletableDeferred<ImageBitmap?>().also { inFlightRequests[url] = it } to true
                else -> existing to false
            }
        }
        if (!ownsRequest) return deferred.await()

        try {
            val callUrl = Url("$PROXY_BASE_URL/proxy.php?url=${url.toString().encodeURLParameter()}")
            val response = httpClient.get(callUrl) {
                headers {
                    accept(ContentType.Image.PNG)
                    accept(ContentType.Image.GIF)
                    accept(ContentType.Image.AVIF)
                }
            }
            val image = when (response.status.isSuccess()) {
                true -> Image.makeFromEncoded(response.readRawBytes()).toComposeImageBitmap()
                false -> null
            }
            if (image != null) {
                saveCachedImage(url, image)
            }
            deferred.complete(image)
            return image
        } catch (ex: CancellationException) {
            // Cancellation belongs to this caller. It should be published to other waiters.
            deferred.complete(null)
            throw ex
        } catch (ex: Throwable) {
            loggerInstance.w("Error loading image from network: $url", ex)
            deferred.complete(null)
            return null
        } finally {
            inFlightRequestsMutex.withLock {
                if (inFlightRequests[url] === deferred) {
                    inFlightRequests.remove(url)
                }
            }
        }
    }

    protected fun copyResourcesTo(target: AbstractResourcesCache) {
        target.resources.putAll(resources)
    }

    private companion object {
        const val PROXY_BASE_URL = "https://jervis.ilios.dk"
    }
}

/**
 * Cache for "static" resources, i.e., dice, pitches, and other rule icons that
 * are the same between different games.
 */
internal class StaticResourcesCache(
    val scaleFactor: Int,
    httpClient: HttpClient,
    getCachedImage: suspend (Url) -> ImageBitmap? = CacheManager::getCachedImage,
    saveCachedImage: suspend (Url, ImageBitmap) -> Unit = CacheManager::saveImage,
) : AbstractResourcesCache(httpClient, getCachedImage, saveCachedImage) {
    val pitches: MutableMap<PitchDetails, ImageBitmap> = mutableMapOf()
    val dice: MutableMap<DiceColor, MutableMap<DieResult, ImageBitmap>> = mutableMapOf()
    val coins: MutableMap<Coin, ImageBitmap> = mutableMapOf()
    val actionIcons: MutableMap<ActionIcon, ImageBitmap> = mutableMapOf()

    suspend fun loadPitch(pitch: PitchDetails): ImageBitmap {
        return checkNotNull(loadResource(SingleSprite.embedded(pitch.resourcePath))).also {
            pitches[pitch] = it
        }
    }

    suspend fun loadDice(
        color: DiceColor,
        die: DieResult,
        resourcePath: String,
        saveAsDefault: Boolean = false,
    ): ImageBitmap {
        return checkNotNull(loadResource(SingleSprite.embedded(resourcePath))).scalePixels(scaleFactor).also { bitmap ->
            dice.getValue(color)[die] = bitmap
            if (saveAsDefault) {
                dice.getValue(DiceColor.DEFAULT)[die] = bitmap
            }
        }
    }

    suspend fun loadCoin(coin: Coin, resourcePath: String): ImageBitmap {
        return checkNotNull(loadResource(SingleSprite.embedded(resourcePath))).scalePixels(scaleFactor).also { bitmap ->
            coins[coin] = bitmap
        }
    }

    suspend fun loadActionIcon(icon: ActionIcon): ImageBitmap {
        return checkNotNull(loadResource(SingleSprite.embedded(icon.path))).scalePixels(scaleFactor).also { bitmap ->
            actionIcons[icon] = bitmap
        }
    }
}

/**
 * Cache for "team" resources, i.e., resources like player sprites, portraits or
 * logos that are associated with a single team.
 */
internal class TeamResourcesCache(
    httpClient: HttpClient,
    fumbblCache: Map<String, Url> = emptyMap(),
    getCachedOnDiskImage: suspend (Url) -> ImageBitmap? = CacheManager::getCachedImage,
    saveOnDiskCachedImage: suspend (Url, ImageBitmap) -> Unit = CacheManager::saveImage,
) : AbstractResourcesCache(httpClient, getCachedOnDiskImage, saveOnDiskCachedImage) {

    // A long-lived app might have players from the same team act as both home and away players.
    // So the cache needs to be able to distinguish between them.
    data class PlayerSpriteKey(
        val id: PlayerId,
        val isOnHomeTeam: Boolean,
    )

    private val fumbblCache: MutableMap<String, Url> = fumbblCache.toMutableMap()
    val players: MutableMap<PlayerSpriteKey, PlayerSprite> = mutableMapOf()
    val portraits: MutableMap<PlayerId, ImageBitmap> = mutableMapOf()
    val largeLogos: MutableMap<TeamId, ImageBitmap> = mutableMapOf()
    val smallLogos: MutableMap<TeamId, ImageBitmap> = mutableMapOf()
    private val generatedPlayerSprites: MutableMap<String, ImageBitmap> = mutableMapOf()

    override fun resolveFumbblUrl(path: String): Url? = fumbblCache[path]

    suspend fun initializeFumbblMapping() {
        val newMappings = mutableMapOf<String, Url>()

        fun addMapping(line: String) {
            val parts = line.split("=")
            if (parts.size == 2) {
                val url = parts[0].replace("https\\", "https")
                newMappings[parts[1]] = Url(url)
            }
        }

        suspend fun loadMappingFile(path: String) {
            Res.readBytes(path).commonToUtf8String().lines().forEach(::addMapping)
        }

        loadMappingFile("files/fumbbl/icons.ini")
        loadMappingFile("files/fumbbl/icons-extra.ini")
        fumbblCache.putAll(newMappings)
    }

    suspend fun loadSprite(source: SpriteSource): ImageBitmap? = loadResource(source)

    suspend fun loadPortrait(source: SpriteSource): ImageBitmap? = loadResource(source)

    suspend fun loadLogo(id: TeamId, source: SpriteSource, size: LogoSize): ImageBitmap? {
        val image = loadResource(source) ?: return null
        when (size) {
            LogoSize.LARGE -> largeLogos[id] = image
            LogoSize.SMALL -> smallLogos[id] = image
        }
        return image
    }

    suspend fun getOrCreateGeneratedPlayerSprite(
        key: String,
        create: suspend () -> ImageBitmap,
    ): ImageBitmap {
        generatedPlayerSprites[key]?.let { return it }
        return create().also { generatedPlayerSprites[key] = it }
    }

    fun copy(): TeamResourcesCache {
        return TeamResourcesCache(httpClient, fumbblCache, getCachedImage, saveCachedImage).also { target ->
            copyInto(target)
        }
    }

    // By always copying into an existing `target` cache, we avoid race
    // conditions where other consumers have a a reference to an outdated
    // cache.
    fun copyInto(target: TeamResourcesCache) {
        copyResourcesTo(target)
        target.fumbblCache.putAll(fumbblCache)
        target.players.putAll(players)
        target.portraits.putAll(portraits)
        target.largeLogos.putAll(largeLogos)
        target.smallLogos.putAll(smallLogos)
        target.generatedPlayerSprites.putAll(generatedPlayerSprites)
    }
}
