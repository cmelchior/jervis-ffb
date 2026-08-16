package com.jervisffb.ui.markings

import com.jervisffb.utils.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class PlayerMarkingsManager(private val settings: SettingsManager) {

    companion object {
        const val PLAYER_MARKINGS_KEY = "jervis.playerMarkings"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    init {
        initializePlayerMarkings()
    }

    fun getPlayerMarkings(): PlayerMarkingsSettings {
        return decodePlayerMarkings(settings.getStringOrNull(PLAYER_MARKINGS_KEY)) ?: PlayerMarkingsSettings()
    }

    fun observePlayerMarkings(): Flow<PlayerMarkingsSettings> {
        val defaultValue = json.encodeToString(PlayerMarkingsSettings())
        return settings.observeStringKey(PLAYER_MARKINGS_KEY, defaultValue).map { value ->
            decodePlayerMarkings(value) ?: PlayerMarkingsSettings()
        }
    }

    fun setPlayerMarkings(value: PlayerMarkingsSettings) {
        settings[PLAYER_MARKINGS_KEY] = json.encodeToString(value)
    }

    /** Add settings that are not part of the generated client-settings file. */
    fun initializePlayerMarkings() {
        if (!settings.hasKey(PLAYER_MARKINGS_KEY) || decodePlayerMarkings(settings.getStringOrNull(PLAYER_MARKINGS_KEY)) == null) {
            setPlayerMarkings(PlayerMarkingsSettings())
        }
    }

    private fun decodePlayerMarkings(value: String?): PlayerMarkingsSettings? {
        return value?.let { runCatching { json.decodeFromString<PlayerMarkingsSettings>(it) }.getOrNull() }
    }

}
